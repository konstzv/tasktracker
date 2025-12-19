package com.tasktracker.notifier.api.models

import kotlinx.serialization.Serializable

/**
 * Represents a message in a conversation with an AI
 */
@Serializable
data class Message(
    val role: String,  // "system", "user", or "assistant"
    val content: String
)
