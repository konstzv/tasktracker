package com.tasktracker.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    val status: TaskStatus = TaskStatus.PENDING,
    @Serializable(with = InstantSerializer::class)
    val stateChangedAt: Instant? = null
) {
    fun toggleStatus(): Task {
        val newStatus = if (status == TaskStatus.PENDING) TaskStatus.COMPLETED else TaskStatus.PENDING
        return copy(
            status = newStatus,
            stateChangedAt = kotlinx.datetime.Clock.System.now()
        )
    }
}
