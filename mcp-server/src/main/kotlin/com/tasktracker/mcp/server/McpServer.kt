package com.tasktracker.mcp.server

import com.tasktracker.data.repository.JsonTaskStorage
import com.tasktracker.mcp.protocol.JsonRpcRequest
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class McpServer {
    private val logger = KotlinLogging.logger {}
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    private val storage = JsonTaskStorage()
    private val requestHandler = McpRequestHandler(storage)

    fun start() {
        logger.info { "TaskTracker MCP Server starting..." }
        logger.info { "Reading from stdin, writing to stdout" }

        val reader = BufferedReader(InputStreamReader(System.`in`))
        val writer = OutputStreamWriter(System.out)

        try {
            while (true) {
                val line = reader.readLine() ?: break

                if (line.isBlank()) continue

                logger.debug { "Received: $line" }

                try {
                    val request = json.decodeFromString<JsonRpcRequest>(line)

                    // Skip notifications (requests without id) - no response needed
                    if (request.id == null) {
                        logger.debug { "Received notification (no response needed): ${request.method}" }
                        continue
                    }

                    val response = requestHandler.handleRequest(request)
                    val responseJson = json.encodeToString(
                        kotlinx.serialization.serializer(),
                        response
                    )

                    writer.write(responseJson)
                    writer.write("\n")
                    writer.flush()

                    logger.debug { "Sent: $responseJson" }
                } catch (e: Exception) {
                    logger.error(e) { "Error processing request: $line" }
                }
            }
        } finally {
            logger.info { "Server shutting down" }
        }
    }
}
