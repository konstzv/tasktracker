package com.tasktracker.notifier.config

import com.typesafe.config.ConfigFactory
import java.io.File

data class NotifierConfig(
    val perplexityApiKey: String,
    val mcpServerJarPath: String,
    val notificationMcpServerPath: String,
    val intervalMinutes: Int,
    val notificationsEnabled: Boolean,
    // Agentic workflow configuration
    val agenticMode: Boolean = true,
    val maxIterations: Int = 10,
    val agenticTimeout: Long = 60000,  // 60 seconds
    val maxConversationMessages: Int = 20
)

object ConfigLoader {
    private val userHome = System.getProperty("user.home")
    private val configDir = File(userHome, ".tasktracker")
    private val configFile = File(configDir, "notifier.conf")

    fun load(): NotifierConfig {
        // Ensure config directory exists
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        // Create default config if missing
        if (!configFile.exists()) {
            createDefaultConfig()
        }

        val config = ConfigFactory.parseFile(configFile)
            .withFallback(ConfigFactory.load())
            .resolve()

        val notifierConfig = config.getConfig("tasktracker.notifier")

        // Read API key from environment variable
        val apiKey = System.getenv("PERPLEXITY_API_KEY")
            ?: throw IllegalStateException(
                "PERPLEXITY_API_KEY environment variable not set.\n" +
                "Please set it with: export PERPLEXITY_API_KEY='your-api-key-here'"
            )

        return NotifierConfig(
            perplexityApiKey = apiKey,
            mcpServerJarPath = notifierConfig.getString("mcp.serverJarPath"),
            notificationMcpServerPath = notifierConfig.getString("mcp.notificationServerPath"),
            intervalMinutes = notifierConfig.getInt("schedule.intervalMinutes"),
            notificationsEnabled = notifierConfig.getBoolean("notifications.enabled"),
            // Agentic configuration with defaults
            agenticMode = if (notifierConfig.hasPath("agentic.enabled"))
                notifierConfig.getBoolean("agentic.enabled") else true,
            maxIterations = if (notifierConfig.hasPath("agentic.maxIterations"))
                notifierConfig.getInt("agentic.maxIterations") else 10,
            agenticTimeout = if (notifierConfig.hasPath("agentic.timeoutSeconds"))
                notifierConfig.getLong("agentic.timeoutSeconds") * 1000 else 60000L,
            maxConversationMessages = if (notifierConfig.hasPath("agentic.maxConversationMessages"))
                notifierConfig.getInt("agentic.maxConversationMessages") else 20
        )
    }

    private fun createDefaultConfig() {
        val defaultConfig = """
            tasktracker {
              notifier {
                # API key is read from environment variable PERPLEXITY_API_KEY
                # Get your API key from https://www.perplexity.ai/settings/api
                # Set it with: export PERPLEXITY_API_KEY='your-api-key-here'

                mcp {
                  serverJarPath = "$userHome/IdeaProjects/tasktracker/mcp-server/build/libs/mcp-server-1.0.0.jar"
                  notificationServerPath = "$userHome/IdeaProjects/mcp-notifications/build/index.js"
                }

                schedule {
                  # Check tasks every N minutes (used for documentation, actual interval set by cron)
                  intervalMinutes = 30
                }

                notifications {
                  enabled = true
                }

                # Agentic workflow configuration
                agentic {
                  # Enable AI-driven tool calling (set to false for simple mode)
                  enabled = true

                  # Maximum conversation turns before forcing termination
                  maxIterations = 10

                  # Total workflow timeout in seconds
                  timeoutSeconds = 60

                  # Maximum messages to keep in conversation context
                  maxConversationMessages = 20
                }
              }
            }
        """.trimIndent()

        configFile.writeText(defaultConfig)
    }
}
