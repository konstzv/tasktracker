package com.tasktracker.viewmodel

import com.tasktracker.data.model.Task
import com.tasktracker.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val scope: CoroutineScope
) {
    val tasks: StateFlow<List<Task>> = repository.tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.loadTasks()
            } catch (e: Exception) {
                _error.value = "Failed to load tasks: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(title: String, description: String) {
        if (title.isBlank()) {
            _error.value = "Task title cannot be empty"
            return
        }

        scope.launch {
            try {
                _error.value = null
                val task = Task(
                    title = title.trim(),
                    description = description.trim(),
                    createdAt = kotlinx.datetime.Clock.System.now()
                )
                repository.addTask(task)
            } catch (e: Exception) {
                _error.value = "Failed to add task: ${e.message}"
            }
        }
    }

    fun deleteTask(taskId: String) {
        scope.launch {
            try {
                _error.value = null
                repository.deleteTask(taskId)
            } catch (e: Exception) {
                _error.value = "Failed to delete task: ${e.message}"
            }
        }
    }

    fun toggleTaskStatus(taskId: String) {
        scope.launch {
            try {
                _error.value = null
                repository.toggleTaskStatus(taskId)
            } catch (e: Exception) {
                _error.value = "Failed to update task: ${e.message}"
            }
        }
    }

    fun updateTask(task: Task) {
        scope.launch {
            try {
                _error.value = null
                repository.updateTask(task)
            } catch (e: Exception) {
                _error.value = "Failed to update task: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
