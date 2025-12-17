package com.tasktracker.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.ui.components.AddTaskDialog
import com.tasktracker.ui.components.TaskList
import com.tasktracker.ui.theme.GlassColors
import com.tasktracker.ui.theme.GlassPanel
import com.tasktracker.ui.theme.GlassSizes
import com.tasktracker.viewmodel.TaskViewModel

@Composable
fun MainWindow(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8E8F8),  // Light purple tint
                        Color(0xFFF5F5FA)   // Very light gray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = GlassSizes.CornerRadius
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Task Tracker",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tasks.size} task${if (tasks.size != 1) "s" else ""}",
                            fontSize = 14.sp,
                            color = GlassColors.TextSecondary
                        )
                    }
                }
            }

            // Error message
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GlassColors.DeleteRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(GlassSizes.SmallCornerRadius)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = GlassColors.DeleteRed,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss", color = GlassColors.DeleteRed)
                        }
                    }
                }
            }

            // Task list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GlassColors.AccentPrimary
                    )
                } else {
                    TaskList(
                        tasks = tasks,
                        onToggleStatus = { taskId -> viewModel.toggleTaskStatus(taskId) },
                        onDelete = { taskId -> viewModel.deleteTask(taskId) }
                    )
                }
            }
        }

        // Floating Action Button (FAB)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = GlassColors.AccentPrimary,
                    spotColor = GlassColors.AccentPrimary
                ),
            containerColor = GlassColors.AccentPrimary,
            contentColor = GlassColors.TextOnAccent,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add task",
                modifier = Modifier.size(32.dp)
            )
        }

        // Add task dialog
        AddTaskDialog(
            showDialog = showAddDialog,
            onDismiss = { showAddDialog = false },
            onAddTask = { title, description ->
                viewModel.addTask(title, description)
            }
        )
    }
}
