package com.tasktracker.notifier.mcp

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.*

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String,
    val id: JsonElement,
    val method: String,
    val params: JsonObject? = null
)

class McpClient(private val mcpServerPath: String, private val serverType: ServerType = ServerType.JAR) {
    enum class ServerType {
        JAR,    // Java JAR file
        NODE    // Node.js script
    }

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    suspend fun start() = withContext(Dispatchers.IO) {
        val processBuilder = when (serverType) {
            ServerType.JAR -> ProcessBuilder(
                "java",
                "-jar",
                mcpServerPath
            )
            ServerType.NODE -> ProcessBuilder(
                "node",
                mcpServerPath
            )
        }

        // Don't redirect stderr - we need to read it to know when server is ready
        // processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

        process = processBuilder.start()

        writer = BufferedWriter(OutputStreamWriter(process!!.outputStream, Charsets.UTF_8))
        reader = BufferedReader(InputStreamReader(process!!.inputStream, Charsets.UTF_8))
        val errorReader = BufferedReader(InputStreamReader(process!!.errorStream, Charsets.UTF_8))

        // Different startup strategies for different server types
        when (serverType) {
            ServerType.NODE -> {
                // Node.js servers output startup message to stderr
                var serverReady = false
                val readyJob = GlobalScope.launch(Dispatchers.IO) {
                    try {
                        while (!serverReady) {
                            val line = errorReader.readLine() ?: break
                            System.err.println(line)
                            if (line.contains("running on stdio") || line.contains("Server started")) {
                                serverReady = true
                                println("[MCP Client] Server is ready!")
                            }
                        }
                        // Continue reading stderr in background
                        errorReader.lineSequence().forEach { line ->
                            System.err.println(line)
                        }
                    } catch (e: Exception) {
                        // Stream closed
                    }
                }

                // Wait for server ready signal (with timeout)
                var waited = 0
                while (!serverReady && waited < 5000) {
                    delay(100)
                    waited += 100
                }

                if (!serverReady) {
                    throw IOException("Server did not start within 5 seconds")
                }
            }
            ServerType.JAR -> {
                // Java servers may not output to stderr, just wait a bit and read stderr in background
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        errorReader.lineSequence().forEach { line ->
                            System.err.println(line)
                        }
                    } catch (e: Exception) {
                        // Stream closed
                    }
                }

                // Give Java server time to start
                delay(1500)
                println("[MCP Client] JAR server startup wait completed")
            }
        }

        // Initialize the MCP protocol
        println("[MCP Client] About to call initialize()...")
        initialize()
        println("[MCP Client] start() completed")
    }

    private suspend fun initialize() = withContext(Dispatchers.IO) {
        val initRequest = JsonRpcRequest(
            jsonrpc = "2.0",
            id = JsonPrimitive(1),
            method = "initialize",
            params = buildJsonObject {
                put("protocolVersion", "2024-11-05")
                put("capabilities", buildJsonObject {
                    // Client capabilities (empty for now)
                })
                put("clientInfo", buildJsonObject {
                    put("name", "TaskTracker Notifier")
                    put("version", "1.0.0")
                })
            },
        )

        val response = sendRequest(initRequest)
        println("[MCP Client] Initialize response: $response")

        // Send initialized notification (required by MCP protocol)
        sendNotification("initialized")
        println("[MCP Client] initialize() completed")
    }

    private suspend fun sendNotification(method: String, params: JsonObject? = null) = withContext(Dispatchers.IO) {
        val notification = buildJsonObject {
            put("jsonrpc", "2.0")
            put("method", method)
            if (params != null) {
                put("params", params)
            }
        }

        println("[MCP Client] Sending notification: $notification")
        writer?.write(notification.toString())
        writer?.write("\n")
        writer?.flush()
        println("[MCP Client] Notification sent")
    }

    suspend fun callTool(toolName: String, arguments: JsonObject = buildJsonObject {}): String =
        withContext(Dispatchers.IO) {
            val request = JsonRpcRequest(
                jsonrpc = "2.0",
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

            println("[MCP Client] Sending: $requestJson")
            println("[MCP Client] Sending bytes: ${requestJson.toByteArray(Charsets.UTF_8).size}")

            writer?.write(requestJson)
            writer?.write("\n")
            writer?.flush()

            println("[MCP Client] Data flushed, waiting for response...")

            // Check if reader is ready
            println("[MCP Client] Reader ready: ${reader?.ready()}")

            val responseLine = reader?.readLine()
                ?: throw IOException("No response from MCP server")

            println("[MCP Client] Received: $responseLine")
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
