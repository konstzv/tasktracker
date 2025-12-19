package com.tasktracker.notifier.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a request to call a tool extracted from AI's text response
 */
@Serializable
data class ToolCallRequest(
    val toolName: String,
    val arguments: JsonObject
)

/**
 * Result of executing a tool call
 */
@Serializable
data class ToolCallResult(
    val toolName: String,
    val success: Boolean,
    val result: String,
    val error: String? = null
)

/**
 * Final result of the agentic workflow
 */
@Serializable
data class AgentResult(
    val finalResponse: String,
    val iterations: Int,
    val toolCallsExecuted: List<ToolCallResult>,
    val terminationReason: TerminationReason
)

/**
 * Reason why the agentic workflow terminated
 */
enum class TerminationReason {
    /** Agent provided FINAL_ANSWER */
    COMPLETED,

    /** Reached maximum iteration limit */
    MAX_ITERATIONS,

    /** Unrecoverable error occurred */
    ERROR,

    /** Response without tool calls (natural completion) */
    NO_TOOL_CALLS
}
