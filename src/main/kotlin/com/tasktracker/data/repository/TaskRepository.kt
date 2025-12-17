package com.tasktracker.data.repository

import com.tasktracker.data.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskRepository(private val storage: JsonTaskStorage) {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    suspend fun loadTasks() {
        val loadedTasks = storage.loadTasks()
        _tasks.value = loadedTasks
    }

    suspend fun addTask(task: Task) {
        val updatedTasks = _tasks.value + task
        _tasks.value = updatedTasks
        storage.saveTasks(updatedTasks)
    }

    suspend fun updateTask(task: Task) {
        val updatedTasks = _tasks.value.map { if (it.id == task.id) task else it }
        _tasks.value = updatedTasks
        storage.saveTasks(updatedTasks)
    }

    suspend fun deleteTask(taskId: String) {
        val updatedTasks = _tasks.value.filterNot { it.id == taskId }
        _tasks.value = updatedTasks
        storage.saveTasks(updatedTasks)
    }

    suspend fun toggleTaskStatus(taskId: String) {
        val updatedTasks = _tasks.value.map { task ->
            if (task.id == taskId) task.toggleStatus() else task
        }
        _tasks.value = updatedTasks
        storage.saveTasks(updatedTasks)
    }
}
