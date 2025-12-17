package com.tasktracker.data.repository

import com.tasktracker.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class TaskData(
    val tasks: List<Task> = emptyList(),
    val version: String = "1.0"
)

class JsonTaskStorage {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val storageDir: File by lazy {
        val homeDir = System.getProperty("user.home")
        val appSupportDir = File(homeDir, "Library/Application Support/TaskTracker")
        appSupportDir.apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val tasksFile: File by lazy {
        File(storageDir, "tasks.json")
    }

    suspend fun loadTasks(): List<Task> = withContext(Dispatchers.IO) {
        try {
            if (!tasksFile.exists()) {
                return@withContext emptyList()
            }

            val jsonContent = tasksFile.readText()
            if (jsonContent.isBlank()) {
                return@withContext emptyList()
            }

            val taskData = json.decodeFromString<TaskData>(jsonContent)
            taskData.tasks
        } catch (e: Exception) {
            println("Error loading tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveTasks(tasks: List<Task>) = withContext(Dispatchers.IO) {
        try {
            val taskData = TaskData(tasks = tasks)
            val jsonContent = json.encodeToString(taskData)

            // Atomic write: write to temp file, then rename
            val tempFile = File(storageDir, "tasks.json.tmp")
            tempFile.writeText(jsonContent)
            tempFile.renameTo(tasksFile)
        } catch (e: Exception) {
            println("Error saving tasks: ${e.message}")
            throw e
        }
    }
}
