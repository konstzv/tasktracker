package com.tasktracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tasktracker.ui.theme.GlassColors
import com.tasktracker.ui.theme.GlassPanel
import com.tasktracker.ui.theme.GlassSizes

@Composable
fun AddTaskDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAddTask: (title: String, description: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            AnimatedVisibility(
                visible = showDialog,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(250)
                ),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(200)
                )
            ) {
                GlassPanel(
                    modifier = Modifier
                        .width(500.dp)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Title
                        Text(
                            text = "Add New Task",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Title field
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                showError = false
                            },
                            label = { Text("Title") },
                            placeholder = { Text("Enter task title") },
                            isError = showError && title.isBlank(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlassColors.AccentPrimary,
                                unfocusedBorderColor = GlassColors.GlassBorder,
                                errorBorderColor = GlassColors.DeleteRed,
                                focusedLabelColor = GlassColors.AccentPrimary,
                                unfocusedLabelColor = GlassColors.TextSecondary
                            )
                        )

                        if (showError && title.isBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Title is required",
                                fontSize = 12.sp,
                                color = GlassColors.DeleteRed
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description field
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (optional)") },
                            placeholder = { Text("Enter task description") },
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlassColors.AccentPrimary,
                                unfocusedBorderColor = GlassColors.GlassBorder,
                                focusedLabelColor = GlassColors.AccentPrimary,
                                unfocusedLabelColor = GlassColors.TextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    title = ""
                                    description = ""
                                    showError = false
                                    onDismiss()
                                }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = GlassColors.TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (title.isBlank()) {
                                        showError = true
                                    } else {
                                        onAddTask(title, description)
                                        title = ""
                                        description = ""
                                        showError = false
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GlassColors.AccentPrimary,
                                    contentColor = GlassColors.TextOnAccent
                                ),
                                shape = RoundedCornerShape(GlassSizes.SmallCornerRadius)
                            ) {
                                Text("Add Task")
                            }
                        }
                    }
                }
            }
        }
    }
}
