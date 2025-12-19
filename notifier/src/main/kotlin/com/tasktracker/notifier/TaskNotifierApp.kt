package com.tasktracker.notifier

import com.tasktracker.mcp.protocol.McpToolDefinitions
import com.tasktracker.notifier.api.AgenticPerplexityClient
import com.tasktracker.notifier.api.PerplexityClient
import com.tasktracker.notifier.config.ConfigLoader
import com.tasktracker.notifier.mcp.McpClient
import com.tasktracker.notifier.notification.NotificationClient
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

class TaskNotifierApp {
    private val logger = KotlinLogging.logger {}

    fun run() = runBlocking {
        try {
            logger.info { "TaskTracker Notifier starting..." }

            // Load configuration
            val config = try {
                ConfigLoader.load()
            } catch (e: Exception) {
                logger.error(e) { "Configuration error" }
                // Cannot send notification if config failed to load
                return@runBlocking
            }

            if (!config.notificationsEnabled) {
                logger.info { "Notifications disabled in config" }
                return@runBlocking
            }

            // Initialize clients
            val tasksMcpClient = McpClient(config.mcpServerJarPath, McpClient.ServerType.JAR)
            val notificationMcpClient = McpClient(config.notificationMcpServerPath, McpClient.ServerType.NODE)
            val perplexityClient = PerplexityClient(config.perplexityApiKey)
            val agenticClient = AgenticPerplexityClient(config.perplexityApiKey)

            try {
                // Start MCP clients
                try {
                    println("[DEBUG] Starting notification MCP client...")
                    notificationMcpClient.start()
                    println("[DEBUG] Notification MCP client start() completed")
                    logger.info { "Notification MCP client connected" }
                } catch (e: Exception) {
                    logger.error(e) { "Notification MCP connection error" }
                    return@runBlocking
                }

                println("[DEBUG] Creating NotificationClient...")
                val notificationClient = NotificationClient(notificationMcpClient)
                println("[DEBUG] NotificationClient created")

                // Send test notification to confirm MCP is working
                println("[DEBUG] About to send test notification...")
                logger.info { "Sending test notification..." }
                notificationClient.showNotification(
                    title = "TaskTracker Started",
                    content = "Notification system initialized successfully",
                    icon = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/ToolbarInfo.icns",
                    sound = NotificationClient.Sound.PING
                )
                logger.info { "Test notification sent" }

                try {
                    tasksMcpClient.start()
                    logger.info { "Tasks MCP client connected" }
                } catch (e: Exception) {
                    logger.error(e) { "Tasks MCP connection error" }
                    notificationClient.showNotification(
                        title = "TaskTracker MCP Error",
                        content = "Failed to start tasks MCP server: ${e.message}",
                        icon = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/AlertStopIcon.icns",
                        sound = NotificationClient.Sound.BASSO
                    )
                    notificationMcpClient.close()
                    return@runBlocking
                }

                // Get AI insights using agentic or simple workflow
                logger.info { "Using ${if (config.agenticMode) "agentic" else "simple"} workflow" }

                val insights = if (config.agenticMode) {
                    // NEW: Agentic workflow with autonomous tool calling
                    try {
                        val tools = McpToolDefinitions.getAllTools()

                        logger.info { "Starting agentic workflow with ${tools.size} tools" }

                        val result = agenticClient.runAgenticWorkflow(
                            initialPrompt = "Analyze my tasks and provide actionable insights",
                            availableTools = tools,
                            mcpClient = tasksMcpClient,
                            maxIterations = config.maxIterations
                        )

                        logger.info {
                            "Agentic workflow completed: ${result.iterations} iterations, " +
                            "${result.toolCallsExecuted.size} tools called, " +
                            "reason: ${result.terminationReason}"
                        }

                        result.finalResponse

                    } catch (e: Exception) {
                        logger.error(e) { "Agentic workflow failed, falling back to simple mode" }
                        // Fallback to simple mode
                        try {
                            val taskData = tasksMcpClient.callTool("list_tasks")
                            if (taskData.contains("No tasks found")) {
                                "No tasks found to analyze"
                            } else {
                                perplexityClient.analyzeTaskInsights(taskData)
                            }
                        } catch (fallbackError: Exception) {
                            logger.error(fallbackError) { "Fallback also failed" }
                            "Error: Unable to analyze tasks"
                        }
                    }
                } else {
                    // EXISTING: Simple workflow (backward compatibility)
                    logger.info { "Using simple workflow" }
                    val taskData = try {
                        tasksMcpClient.callTool("list_tasks")
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to retrieve tasks" }
                        notificationClient.showNotification(
                            title = "TaskTracker Task Error",
                            content = "Failed to get tasks: ${e.message}",
                            icon = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/AlertStopIcon.icns",
                            sound = NotificationClient.Sound.BASSO
                        )
                        tasksMcpClient.close()
                        notificationMcpClient.close()
                        return@runBlocking
                    }

                    logger.info { "Retrieved tasks:\n$taskData" }

                    // Skip if no tasks
                    if (taskData.contains("No tasks found")) {
                        logger.info { "No tasks to analyze" }
                        notificationClient.showNotification(
                            title = "TaskTracker",
                            content = "No tasks found to analyze",
                            icon = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/ToolbarInfo.icns",
                            sound = NotificationClient.Sound.TINK
                        )
                        tasksMcpClient.close()
                        notificationMcpClient.close()
                        return@runBlocking
                    }

                    // Get AI insights
                    logger.info { "Requesting Perplexity analysis..." }
                    try {
                        perplexityClient.analyzeTaskInsights(taskData)
                    } catch (e: Exception) {
                        logger.error(e) { "Perplexity API error" }
                        notificationClient.showNotification(
                            title = "TaskTracker Perplexity Error",
                            content = "API Error: ${e.message}",
                            icon = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/AlertCautionIcon.icns",
                            sound = NotificationClient.Sound.BASSO
                        )
                        tasksMcpClient.close()
                        notificationMcpClient.close()
                        return@runBlocking
                    }
                }

                logger.info { "Received insights:\n$insights" }

                // Show notification
                notificationClient.showNotification(
                    title = "Task Insights",
                    content = insights,
                    icon = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/BookmarkIcon.icns",
                    sound = NotificationClient.Sound.GLASS
                )

                logger.info { "Notification sent successfully" }

            } finally {
                tasksMcpClient.close()
                notificationMcpClient.close()
            }

        } catch (e: Exception) {
            // Catch-all for any unexpected errors
            logger.error(e) { "Unexpected error in notifier app" }
            // Note: Cannot send notification here if MCP client failed to initialize
        }
    }
}
