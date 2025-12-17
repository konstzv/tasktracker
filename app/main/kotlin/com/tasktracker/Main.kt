package com.tasktracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.tasktracker.data.repository.JsonTaskStorage
import com.tasktracker.data.repository.TaskRepository
import com.tasktracker.ui.menubar.MenuBarManager
import com.tasktracker.ui.window.MainWindow
import com.tasktracker.viewmodel.TaskViewModel

fun main() = application {
    var isWindowVisible by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val storage = remember { JsonTaskStorage() }
    val repository = remember { TaskRepository(storage) }
    val viewModel = remember { TaskViewModel(repository, scope) }

    // Initialize menu bar manager
    val menuBarManager = remember {
        MenuBarManager(
            viewModel = viewModel,
            scope = scope,
            onShowMainWindow = { isWindowVisible = true },
            onQuit = { exitApplication() }
        )
    }

    LaunchedEffect(Unit) {
        menuBarManager.initialize()
    }

    DisposableEffect(Unit) {
        onDispose {
            menuBarManager.remove()
        }
    }

    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                // Hide instead of closing when user clicks X
                isWindowVisible = false
            },
            title = "Task Tracker",
            state = WindowState(size = DpSize(600.dp, 800.dp))
        ) {
            MaterialTheme {
                MainWindow(viewModel)
            }
        }
    }
}
