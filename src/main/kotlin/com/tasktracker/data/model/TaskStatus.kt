package com.tasktracker.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    PENDING,
    COMPLETED
}
