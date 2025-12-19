package com.tasktracker.notifier.api

import com.tasktracker.notifier.api.models.ToolCallRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging

/**
 * Parser for extracting tool calls and final answers from AI text responses
 */
object ToolCallParser {
    private val logger = KotlinLogging.logger {}

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Primary format: [TOOL_CALL] {"toolName": "...", "arguments": {...}}
    private val TOOL_CALL_REGEX_1 =
        """\[TOOL_CALL\]\s*(\{[^}]*"toolName"[^}]*\})""".toRegex(RegexOption.DOT_MATCHES_ALL)

    // Fallback format: ```json\n{"toolName": "...", "arguments": {...}}\n```
    private val TOOL_CALL_REGEX_2 =
        """```json\s*(\{[^}]*"toolName"[^}]*\})\s*```""".toRegex(RegexOption.DOT_MATCHES_ALL)

    // Final answer format: FINAL_ANSWER: <text>
    private val FINAL_ANSWER_REGEX =
        """FINAL_ANSWER:\s*(.+)""".toRegex(RegexOption.DOT_MATCHES_ALL)

    /**
     * Extract tool call requests from AI response text
     * Supports multiple formats for robustness
     */
    fun extractToolCalls(text: String): List<ToolCallRequest> {
        val calls = mutableListOf<ToolCallRequest>()

        // Try primary format: [TOOL_CALL] {...}
        TOOL_CALL_REGEX_1.findAll(text).forEach { match ->
            val jsonStr = match.groupValues[1]
            parseToolCall(jsonStr)?.let { calls.add(it) }
        }

        // If no calls found, try fallback format: ```json ... ```
        if (calls.isEmpty()) {
            TOOL_CALL_REGEX_2.findAll(text).forEach { match ->
                val jsonStr = match.groupValues[1]
                parseToolCall(jsonStr)?.let { calls.add(it) }
            }
        }

        if (calls.isNotEmpty()) {
            logger.info { "Extracted ${calls.size} tool call(s) from response" }
        }

        return calls
    }

    /**
     * Parse a JSON string into a ToolCallRequest
     */
    private fun parseToolCall(jsonStr: String): ToolCallRequest? {
        return try {
            val jsonElement = json.parseToJsonElement(jsonStr).jsonObject
            val toolName = jsonElement["toolName"]?.jsonPrimitive?.content
            val arguments = jsonElement["arguments"]?.jsonObject

            if (toolName != null) {
                ToolCallRequest(
                    toolName = toolName,
                    arguments = arguments ?: JsonObject(emptyMap())
                )
            } else {
                logger.warn { "Tool call JSON missing 'toolName' field: $jsonStr" }
                null
            }
        } catch (e: Exception) {
            logger.warn { "Failed to parse tool call JSON: $jsonStr - ${e.message}" }
            null
        }
    }

    /**
     * Extract final answer from AI response if present
     */
    fun extractFinalAnswer(text: String): String? {
        return FINAL_ANSWER_REGEX.find(text)?.groupValues?.get(1)?.trim()
    }

    /**
     * Check if response contains a final answer marker
     */
    fun hasFinalAnswer(text: String): Boolean {
        return FINAL_ANSWER_REGEX.containsMatchIn(text)
    }
}
