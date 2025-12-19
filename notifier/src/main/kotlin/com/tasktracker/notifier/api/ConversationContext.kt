package com.tasktracker.notifier.api

import com.tasktracker.notifier.api.models.Message
import mu.KotlinLogging

/**
 * Manages conversation context for iterative agentic workflows
 * Handles message storage, token estimation, and context pruning
 */
class ConversationContext(
    private val maxMessages: Int = 20
) {
    private val logger = KotlinLogging.logger {}
    private val messages = mutableListOf<Message>()

    /**
     * Add a system message (typically tool definitions and instructions)
     */
    fun addSystemMessage(content: String) {
        messages.add(Message(role = "system", content = content))
        logger.debug { "Added system message (${estimateTokens(content)} tokens)" }
    }

    /**
     * Add a user message
     */
    fun addUserMessage(content: String) {
        messages.add(Message(role = "user", content = content))
        logger.debug { "Added user message (${estimateTokens(content)} tokens)" }
        pruneIfNecessary()
    }

    /**
     * Add an assistant message
     */
    fun addAssistantMessage(content: String) {
        messages.add(Message(role = "assistant", content = content))
        logger.debug { "Added assistant message (${estimateTokens(content)} tokens)" }
        pruneIfNecessary()
    }

    /**
     * Add a tool result as a user message
     * Format: [TOOL_RESULT] toolName: result
     */
    fun addToolResult(toolName: String, result: String) {
        val truncated = if (result.length > 500) {
            result.take(500) + "... (truncated)"
        } else {
            result
        }

        val content = "[TOOL_RESULT] $toolName:\n$truncated"
        addUserMessage(content)
    }

    /**
     * Get all messages in the conversation
     */
    fun getMessages(): List<Message> = messages.toList()

    /**
     * Get the total number of messages
     */
    fun getMessageCount(): Int = messages.size

    /**
     * Estimate total tokens in the conversation
     * Rough estimation: 4 characters ≈ 1 token
     */
    fun estimateTotalTokens(): Int {
        return messages.sumOf { estimateTokens(it.content) }
    }

    /**
     * Prune old messages if we exceed the maximum
     * Keeps system message + most recent exchanges
     */
    private fun pruneIfNecessary() {
        if (messages.size <= maxMessages) {
            return
        }

        val systemMessages = messages.filter { it.role == "system" }
        val otherMessages = messages.filter { it.role != "system" }

        // Keep system messages + last (maxMessages - systemMessages.size) messages
        val keepCount = maxMessages - systemMessages.size
        val recentMessages = otherMessages.takeLast(keepCount)

        val before = messages.size
        messages.clear()
        messages.addAll(systemMessages)
        messages.addAll(recentMessages)

        logger.info { "Pruned conversation: $before → ${messages.size} messages" }
    }

    /**
     * Estimate tokens for a string
     * Simple heuristic: 4 characters ≈ 1 token
     */
    private fun estimateTokens(text: String): Int {
        return (text.length / 4.0).toInt()
    }

    /**
     * Clear all messages
     */
    fun clear() {
        messages.clear()
        logger.debug { "Cleared conversation context" }
    }
}
