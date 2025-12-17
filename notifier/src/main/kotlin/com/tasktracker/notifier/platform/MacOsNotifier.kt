package com.tasktracker.notifier.platform

import java.io.IOException

class MacOsNotifier {
    fun showNotification(title: String, message: String, subtitle: String = "") {
        try {
            val escapedMessage = message.replace("\"", "\\\"").replace("\n", " ")
            val escapedTitle = title.replace("\"", "\\\"")
            val escapedSubtitle = subtitle.replace("\"", "\\\"")

            val script = buildString {
                append("display notification \"")
                append(escapedMessage)
                append("\" with title \"")
                append(escapedTitle)
                append("\"")
                if (subtitle.isNotEmpty()) {
                    append(" subtitle \"")
                    append(escapedSubtitle)
                    append("\"")
                }
            }

            ProcessBuilder("osascript", "-e", script)
                .redirectErrorStream(true)
                .start()
                .waitFor()

        } catch (e: IOException) {
            println("Failed to show notification: ${e.message}")
        }
    }
}
