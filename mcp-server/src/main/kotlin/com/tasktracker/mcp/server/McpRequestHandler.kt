package com.tasktracker.mcp.server

import com.tasktracker.data.repository.JsonTaskStorage
import com.tasktracker.mcp.protocol.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import mu.KotlinLogging

class McpRequestHandler(
    private val storage: JsonTaskStorage
) {
    private val logger = KotlinLogging.logger {}
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    fun handleRequest(request: JsonRpcRequest): JsonRpcResponse {
        logger.debug { "Handling request: ${request.method}" }

        return try {
            when (request.method) {
                "initialize" -> handleInitialize(request)
                "tools/list" -> handleToolsList(request)
                "tools/call" -> handleToolCall(request)
                else -> createErrorResponse(
                    request.id,
                    JsonRpcErrorCode.METHOD_NOT_FOUND,
                    "Method not found: ${request.method}"
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling request: ${request.method}" }
            createErrorResponse(
                request.id,
                JsonRpcErrorCode.INTERNAL_ERROR,
                e.message ?: "Internal error"
            )
        }
    }

    private fun handleInitialize(request: JsonRpcRequest): JsonRpcResponse {
        val result = buildJsonObject {
            put("protocolVersion", "2024-11-05")
            put("serverInfo", buildJsonObject {
                put("name", "TaskTracker MCP Server")
                put("version", "1.0.0")
            })
            put("capabilities", buildJsonObject {
                put("tools", buildJsonObject {})
            })
        }

        return JsonRpcResponse(id = request.id, result = result)
    }

    private fun handleToolsList(request: JsonRpcRequest): JsonRpcResponse {
        val tools = McpToolDefinitions.getAllTools()
        val result = buildJsonObject {
            put("tools", JsonArray(
                tools.map { tool ->
                    buildJsonObject {
                        put("name", tool.name)
                        put("description", tool.description)
                        put("inputSchema", tool.inputSchema)
                    }
                }
            ))
        }

        return JsonRpcResponse(id = request.id, result = result)
    }

    private fun handleToolCall(request: JsonRpcRequest): JsonRpcResponse {
        val params = request.params ?: return createErrorResponse(
            request.id,
            JsonRpcErrorCode.INVALID_PARAMS,
            "Missing params"
        )

        val toolName = params["name"]?.jsonPrimitive?.content ?: return createErrorResponse(
            request.id,
            JsonRpcErrorCode.INVALID_PARAMS,
            "Missing tool name"
        )

        val arguments = params["arguments"]?.jsonObject

        val toolResult = when (toolName) {
            "list_tasks" -> executeListTasks()
            "get_task" -> executeGetTask(arguments)
            else -> return createErrorResponse(
                request.id,
                JsonRpcErrorCode.METHOD_NOT_FOUND,
                "Unknown tool: $toolName"
            )
        }

        val result = buildJsonObject {
            put("content", JsonArray(listOf(
                buildJsonObject {
                    put("type", "text")
                    put("text", toolResult)
                }
            )))
        }

        return JsonRpcResponse(id = request.id, result = result)
    }

    private fun executeListTasks(): String = runBlocking {
        val tasks = storage.loadTasks()

        if (tasks.isEmpty()) {
            return@runBlocking "No tasks found."
        }

        buildString {
            appendLine("Found ${tasks.size} task(s):\n")
            tasks.forEachIndexed { index, task ->
                appendLine("${index + 1}. ${task.title}")
                appendLine("   ID: ${task.id}")
                appendLine("   Status: ${task.status}")
                appendLine("   Created: ${task.createdAt}")
                if (task.description.isNotBlank()) {
                    appendLine("   Description: ${task.description}")
                }
                if (task.stateChangedAt != null) {
                    appendLine("   Status changed: ${task.stateChangedAt}")
                }
                appendLine()
            }
        }
    }

    private fun executeGetTask(arguments: JsonObject?): String = runBlocking {
        val taskId = arguments?.get("task_id")?.jsonPrimitive?.content
            ?: return@runBlocking "Error: task_id parameter is required"

        val tasks = storage.loadTasks()
        val task = tasks.find { it.id == taskId }
            ?: return@runBlocking "Task not found with ID: $taskId"

        buildString {
            appendLine("Task Details:")
            appendLine("Title: ${task.title}")
            appendLine("ID: ${task.id}")
            appendLine("Status: ${task.status}")
            appendLine("Created: ${task.createdAt}")
            if (task.description.isNotBlank()) {
                appendLine("Description: ${task.description}")
            }
            if (task.stateChangedAt != null) {
                appendLine("Status last changed: ${task.stateChangedAt}")
            }
        }
    }

    private fun createErrorResponse(
        id: JsonElement?,
        code: Int,
        message: String
    ): JsonRpcResponse {
        return JsonRpcResponse(
            id = id,
            error = JsonRpcError(code = code, message = message)
        )
    }
}
