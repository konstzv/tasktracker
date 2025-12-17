package com.tasktracker.mcp.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

object McpToolDefinitions {
    val LIST_TASKS = McpTool(
        name = "list_tasks",
        description = "List all tasks in the task tracker. Returns tasks with their ID, title, description, status (PENDING/COMPLETED), creation time, and state change time.",
        inputSchema = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {})
            put("required", JsonArray(emptyList()))
        }
    )

    val GET_TASK = McpTool(
        name = "get_task",
        description = "Get detailed information about a specific task by its ID. Returns the task's title, description, status, creation time, and when the status was last changed.",
        inputSchema = buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                put("task_id", buildJsonObject {
                    put("type", "string")
                    put("description", "The unique identifier of the task to retrieve")
                })
            })
            put("required", JsonArray(listOf(JsonPrimitive("task_id"))))
        }
    )

    fun getAllTools(): List<McpTool> = listOf(LIST_TASKS, GET_TASK)
}
