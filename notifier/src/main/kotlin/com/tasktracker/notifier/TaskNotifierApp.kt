package com.tasktracker.notifier

import com.tasktracker.notifier.api.PerplexityClient
import com.tasktracker.notifier.config.ConfigLoader
import com.tasktracker.notifier.mcp.McpClient
import com.tasktracker.notifier.platform.MacOsNotifier
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

class TaskNotifierApp {
    private val logger = KotlinLogging.logger {}

    fun run() = runBlocking {
        val notifier = MacOsNotifier()

        try {
            logger.info { "TaskTracker Notifier starting..." }

            // Load configuration
            val config = try {
                ConfigLoader.load()
            } catch (e: Exception) {
                logger.error(e) { "Configuration error" }
                notifier.showNotification(
                    title = "TaskTracker Config Error",
                    message = e.message ?: "Failed to load configuration",
                    subtitle = "❌ Configuration Error"
                )
                return@runBlocking
            }

            if (!config.notificationsEnabled) {
                logger.info { "Notifications disabled in config" }
                return@runBlocking
            }

            // Initialize clients
            val mcpClient = McpClient(config.mcpServerJarPath)
            val perplexityClient = PerplexityClient(config.perplexityApiKey)

            try {
                // Start MCP client and get tasks
                try {
                    mcpClient.start()
                    logger.info { "MCP client connected" }
                } catch (e: Exception) {
                    logger.error(e) { "MCP connection error" }
                    notifier.showNotification(
                        title = "TaskTracker MCP Error",
                        message = "Failed to start MCP server: ${e.message}",
                        subtitle = "❌ MCP Connection Error"
                    )
                    return@runBlocking
                }

                val taskData = try {
                    mcpClient.callTool("list_tasks")
                } catch (e: Exception) {
                    logger.error(e) { "Failed to retrieve tasks" }
                    notifier.showNotification(
                        title = "TaskTracker Task Error",
                        message = "Failed to get tasks: ${e.message}",
                        subtitle = "❌ Task Retrieval Error"
                    )
                    return@runBlocking
                }

                logger.info { "Retrieved tasks:\n$taskData" }

                // Skip if no tasks
                if (taskData.contains("No tasks found")) {
                    logger.info { "No tasks to analyze" }
                    notifier.showNotification(
                        title = "TaskTracker",
                        message = "No tasks found to analyze",
                        subtitle = "ℹ️ No Tasks"
                    )
                    return@runBlocking
                }

                // Get AI insights
                logger.info { "Requesting Perplexity analysis..." }
                val insights = try {
                    perplexityClient.analyzeTaskInsights(taskData)
                } catch (e: Exception) {
                    logger.error(e) { "Perplexity API error" }
                    notifier.showNotification(
                        title = "TaskTracker Perplexity Error",
                        message = "API Error: ${e.message}",
                        subtitle = "❌ Perplexity API Error"
                    )
                    return@runBlocking
                }

                logger.info { "Received insights:\n$insights" }

                // Show notification
                notifier.showNotification(
                    title = "Task Insights",
                    message = insights,
                    subtitle = "✨ TaskTracker Analysis"
                )

                logger.info { "Notification sent successfully" }

            } finally {
                mcpClient.close()
            }

        } catch (e: Exception) {
            // Catch-all for any unexpected errors
            logger.error(e) { "Unexpected error in notifier app" }
            notifier.showNotification(
                title = "TaskTracker Unexpected Error",
                message = "Unexpected error: ${e.message}",
                subtitle = "❌ Error"
            )
        }
    }
}
