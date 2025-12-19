package com.tasktracker.notifier.notification

import com.tasktracker.notifier.mcp.McpClient
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Client for sending macOS notifications via MCP notification server.
 * Supports titles, messages, icons, and system sounds.
 */
class NotificationClient(private val mcpClient: McpClient) {

    /**
     * Available macOS system sounds
     */
    enum class Sound(val soundName: String) {
        PING("Ping"),
        BASSO("Basso"),
        BLOW("Blow"),
        BOTTLE("Bottle"),
        FROG("Frog"),
        FUNK("Funk"),
        GLASS("Glass"),
        HERO("Hero"),
        MORSE("Morse"),
        POP("Pop"),
        PURR("Purr"),
        SOSUMI("Sosumi"),
        SUBMARINE("Submarine"),
        TINK("Tink")
    }

    /**
     * Shows a notification with optional icon and sound
     *
     * @param title Notification title (required)
     * @param content Notification message content (required)
     * @param icon Path to icon file (optional)
     * @param sound System sound to play (optional)
     */
    suspend fun showNotification(
        title: String,
        content: String,
        icon: String? = null,
        sound: Sound? = null
    ): String {
        val arguments = buildJsonObject {
            put("title", title)
            put("content", content)
            icon?.let { put("icon", it) }
            sound?.let { put("sound", it.soundName) }
        }

        return mcpClient.callTool("show_notification", arguments)
    }

    /**
     * Shows a simple notification without icon or sound
     */
    suspend fun showSimple(title: String, message: String): String {
        return showNotification(title = title, content = message)
    }

    /**
     * Shows a notification with sound
     */
    suspend fun showWithSound(title: String, message: String, sound: Sound): String {
        return showNotification(title = title, content = message, sound = sound)
    }

    /**
     * Shows a notification with icon
     */
    suspend fun showWithIcon(title: String, message: String, icon: String): String {
        return showNotification(title = title, content = message, icon = icon)
    }
}
