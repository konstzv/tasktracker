package com.tasktracker.notifier.mcp

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.*

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: JsonElement? = null,
    val method: String,
    val params: JsonObject? = null
)

class McpClient(private val mcpServerJarPath: String) {
    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    suspend fun start() = withContext(Dispatchers.IO) {
        val processBuilder = ProcessBuilder(
            "java",
            "-jar",
            mcpServerJarPath
        )

        // Redirect stderr for debugging (optional)
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

        process = processBuilder.start()

        writer = BufferedWriter(OutputStreamWriter(process!!.outputStream))
        reader = BufferedReader(InputStreamReader(process!!.inputStream))

        // Initialize the MCP protocol
        initialize()
    }

    private suspend fun initialize() = withContext(Dispatchers.IO) {
        val initRequest = JsonRpcRequest(
            id = JsonPrimitive(1),
            method = "initialize",
            params = buildJsonObject {
                put("protocolVersion", "2024-11-05")
                put("clientInfo", buildJsonObject {
                    put("name", "TaskTracker Notifier")
                    put("version", "1.0.0")
                })
            }
        )

        sendRequest(initRequest)
    }

    suspend fun callTool(toolName: String, arguments: JsonObject = buildJsonObject {}): String =
        withContext(Dispatchers.IO) {
            val request = JsonRpcRequest(
                id = JsonPrimitive(System.currentTimeMillis()),
                method = "tools/call",
                params = buildJsonObject {
                    put("name", toolName)
                    put("arguments", arguments)
                }
            )

            val response = sendRequest(request)
            val content = response["result"]?.jsonObject
                ?.get("content")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content

            content ?: "No response"
        }

    private suspend fun sendRequest(request: JsonRpcRequest): JsonObject =
        withContext(Dispatchers.IO) {
            val requestJson = json.encodeToString(JsonRpcRequest.serializer(), request)

            writer?.write(requestJson)
            writer?.write("\n")
            writer?.flush()

            val responseLine = reader?.readLine()
                ?: throw IOException("No response from MCP server")

            json.parseToJsonElement(responseLine).jsonObject
        }

    fun close() {
        try {
            writer?.close()
            reader?.close()
            process?.destroy()
        } catch (e: Exception) {
            println("Error closing MCP client: ${e.message}")
        }
    }
}
