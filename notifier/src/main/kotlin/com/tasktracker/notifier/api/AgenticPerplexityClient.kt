package com.tasktracker.notifier.api

import com.tasktracker.mcp.protocol.McpTool
import com.tasktracker.notifier.api.models.*
import com.tasktracker.notifier.mcp.McpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Agentic Perplexity client with tool-aware iterative workflow
 */
class AgenticPerplexityClient(private val apiKey: String) {
    private val logger = KotlinLogging.logger {}

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    private val mediaType = "application/json".toMediaType()

    /**
     * Build system prompt with tool definitions
     */
    private fun buildSystemPromptWithTools(tools: List<McpTool>): String {
        val toolDescriptions = tools.joinToString("\n\n") { tool ->
            val params = tool.inputSchema.toString()
                .replace("{", "")
                .replace("}", "")
                .replace("\"", "")

            """
            Tool: ${tool.name}
            Description: ${tool.description}
            Parameters: ${if (params.isBlank()) "(none)" else params}
            """.trimIndent()
        }

        return """
You are a productivity assistant with access to task management tools.

AVAILABLE TOOLS:
$toolDescriptions

INSTRUCTIONS:
1. Analyze the user's request carefully
2. Use the available tools to gather necessary information
3. To call a tool, respond with EXACTLY this format on its own line:
   [TOOL_CALL] {"toolName": "tool_name", "arguments": {...}}
4. You can call multiple tools sequentially to build understanding
5. When you have all the information needed, provide your final answer with:
   FINAL_ANSWER: Your concise, actionable insight here

RULES:
- ALWAYS use tools to get current data, never make assumptions about task content
- Be concise and actionable in your final answer (1-2 sentences max)
- If a tool call fails, try to work with available information
- Focus on the most important insights or actions the user should take

EXAMPLES:
User: Analyze my tasks
Assistant: [TOOL_CALL] {"toolName": "list_tasks", "arguments": {}}

User: [TOOL_RESULT] list_tasks:
Found 2 tasks...
Assistant: FINAL_ANSWER: You have 2 pending tasks. Focus on completing "Buy groceries" first.
        """.trimIndent()
    }

    /**
     * Execute a single tool call via MCP client
     */
    private suspend fun executeToolCall(
        toolCall: ToolCallRequest,
        mcpClient: McpClient
    ): ToolCallResult = withContext(Dispatchers.IO) {
        return@withContext try {
            logger.info { "Executing tool: ${toolCall.toolName} with args: ${toolCall.arguments}" }

            val result = withTimeout(10000) { // 10 second timeout per tool
                mcpClient.callTool(
                    toolName = toolCall.toolName,
                    arguments = toolCall.arguments
                )
            }

            logger.info { "Tool ${toolCall.toolName} succeeded (${result.length} chars)" }

            ToolCallResult(
                toolName = toolCall.toolName,
                success = true,
                result = result
            )
        } catch (e: Exception) {
            logger.error(e) { "Tool ${toolCall.toolName} failed" }

            ToolCallResult(
                toolName = toolCall.toolName,
                success = false,
                result = "",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Call Perplexity API with conversation context
     */
    private suspend fun callPerplexity(messages: List<Message>): String = withContext(Dispatchers.IO) {
        val requestBody = PerplexityRequest(messages = messages)
        val requestJson = json.encodeToString(PerplexityRequest.serializer(), requestBody)

        logger.debug { "Calling Perplexity with ${messages.size} messages" }

        val request = Request.Builder()
            .url("https://api.perplexity.ai/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestJson.toRequestBody(mediaType))
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body?.string()
            ?: throw IOException("Empty response from Perplexity API")

        if (!response.isSuccessful) {
            throw IOException("Perplexity API error ${response.code}: $responseBody")
        }

        val perplexityResponse = json.decodeFromString<PerplexityResponse>(responseBody)

        perplexityResponse.choices.firstOrNull()?.message?.content
            ?: throw IOException("No content in Perplexity response")
    }

    /**
     * Run the agentic workflow with iterative tool calling
     */
    suspend fun runAgenticWorkflow(
        initialPrompt: String,
        availableTools: List<McpTool>,
        mcpClient: McpClient,
        maxIterations: Int = 10
    ): AgentResult = withContext(Dispatchers.IO) {
        val context = ConversationContext(maxMessages = 20)
        val toolCallsExecuted = mutableListOf<ToolCallResult>()
        var consecutiveNoToolCalls = 0
        var consecutiveFailedToolCalls = 0

        // Initialize conversation with system prompt and user request
        context.addSystemMessage(buildSystemPromptWithTools(availableTools))
        context.addUserMessage(initialPrompt)

        logger.info { "Starting agentic workflow: \"$initialPrompt\"" }

        for (iteration in 1..maxIterations) {
            logger.info { "Iteration $iteration/$maxIterations" }

            // Call Perplexity with current context
            val response = try {
                callPerplexity(context.getMessages())
            } catch (e: Exception) {
                logger.error(e) { "Perplexity API call failed" }
                return@withContext AgentResult(
                    finalResponse = "Error: ${e.message}",
                    iterations = iteration,
                    toolCallsExecuted = toolCallsExecuted,
                    terminationReason = TerminationReason.ERROR
                )
            }

            context.addAssistantMessage(response)
            logger.debug { "Perplexity response: $response" }

            // Check for final answer
            val finalAnswer = ToolCallParser.extractFinalAnswer(response)
            if (finalAnswer != null) {
                logger.info { "Received FINAL_ANSWER, completing workflow" }
                return@withContext AgentResult(
                    finalResponse = finalAnswer,
                    iterations = iteration,
                    toolCallsExecuted = toolCallsExecuted,
                    terminationReason = TerminationReason.COMPLETED
                )
            }

            // Extract and execute tool calls
            val toolCalls = ToolCallParser.extractToolCalls(response)

            if (toolCalls.isEmpty()) {
                consecutiveNoToolCalls++
                logger.warn { "No tool calls found ($consecutiveNoToolCalls consecutive)" }

                if (consecutiveNoToolCalls >= 2) {
                    logger.info { "No tool calls for 2 iterations, treating as completion" }
                    return@withContext AgentResult(
                        finalResponse = response,
                        iterations = iteration,
                        toolCallsExecuted = toolCallsExecuted,
                        terminationReason = TerminationReason.NO_TOOL_CALLS
                    )
                }

                // Prompt for final answer
                context.addUserMessage("Please provide your FINAL_ANSWER based on available information.")
                continue
            }

            consecutiveNoToolCalls = 0

            // Execute all tool calls
            var allFailed = true
            for (toolCall in toolCalls) {
                val result = executeToolCall(toolCall, mcpClient)
                toolCallsExecuted.add(result)

                if (result.success) {
                    allFailed = false
                    context.addToolResult(result.toolName, result.result)
                } else {
                    context.addToolResult(result.toolName, "ERROR: ${result.error}")
                }
            }

            if (allFailed) {
                consecutiveFailedToolCalls++
                logger.warn { "All tool calls failed ($consecutiveFailedToolCalls consecutive)" }

                if (consecutiveFailedToolCalls >= 3) {
                    logger.error { "3 consecutive failed tool calls, terminating" }
                    return@withContext AgentResult(
                        finalResponse = "Error: Unable to execute tools after multiple attempts",
                        iterations = iteration,
                        toolCallsExecuted = toolCallsExecuted,
                        terminationReason = TerminationReason.ERROR
                    )
                }
            } else {
                consecutiveFailedToolCalls = 0
            }
        }

        // Max iterations reached
        logger.warn { "Max iterations ($maxIterations) reached" }

        // Try to get a final answer
        context.addUserMessage("Please provide your FINAL_ANSWER now based on the information gathered.")
        val lastResponse = try {
            callPerplexity(context.getMessages())
        } catch (e: Exception) {
            "Unable to complete analysis after $maxIterations iterations"
        }

        val finalAnswer = ToolCallParser.extractFinalAnswer(lastResponse) ?: lastResponse

        return@withContext AgentResult(
            finalResponse = finalAnswer,
            iterations = maxIterations,
            toolCallsExecuted = toolCallsExecuted,
            terminationReason = TerminationReason.MAX_ITERATIONS
        )
    }
}
