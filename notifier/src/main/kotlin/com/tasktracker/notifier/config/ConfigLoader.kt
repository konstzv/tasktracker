package com.tasktracker.notifier.config

import com.typesafe.config.ConfigFactory
import java.io.File

data class NotifierConfig(
    val perplexityApiKey: String,
    val mcpServerJarPath: String,
    val intervalMinutes: Int,
    val notificationsEnabled: Boolean
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
            intervalMinutes = notifierConfig.getInt("schedule.intervalMinutes"),
            notificationsEnabled = notifierConfig.getBoolean("notifications.enabled")
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
                  serverJarPath = "$userHome/StudioProjects/tasktracker/mcp-server/build/libs/mcp-server-1.0.0.jar"
                }

                schedule {
                  # Check tasks every N minutes (used for documentation, actual interval set by cron)
                  intervalMinutes = 30
                }

                notifications {
                  enabled = true
                }
              }
            }
        """.trimIndent()

        configFile.writeText(defaultConfig)
    }
}
