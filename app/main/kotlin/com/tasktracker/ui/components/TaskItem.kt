package com.tasktracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.data.model.Task
import com.tasktracker.data.model.TaskStatus
import com.tasktracker.ui.theme.GlassCard
import com.tasktracker.ui.theme.GlassColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TaskItem(
    task: Task,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Animation for completion
    val scale by animateFloatAsState(
        targetValue = if (task.status == TaskStatus.COMPLETED) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val alpha by animateFloatAsState(
        targetValue = if (task.status == TaskStatus.COMPLETED) 0.7f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = onToggleStatus,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (task.status == TaskStatus.COMPLETED) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.Circle
                    },
                    contentDescription = if (task.status == TaskStatus.COMPLETED) {
                        "Mark as incomplete"
                    } else {
                        "Mark as complete"
                    },
                    tint = if (task.status == TaskStatus.COMPLETED) {
                        GlassColors.CompletedGreen
                    } else {
                        GlassColors.TextSecondary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    }
                )

                // Description
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = GlassColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dates
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Created: ${formatDate(task.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()))}",
                        fontSize = 12.sp,
                        color = GlassColors.TextTertiary
                    )

                    task.stateChangedAt?.let { stateChangedAt ->
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = GlassColors.TextTertiary
                        )
                        Text(
                            text = "Updated: ${formatDate(stateChangedAt.toLocalDateTime(TimeZone.currentSystemDefault()))}",
                            fontSize = 12.sp,
                            color = GlassColors.TextTertiary
                        )
                    }
                }
            }

            // Delete button (shows on hover)
            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = GlassColors.DeleteRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(dateTime: kotlinx.datetime.LocalDateTime): String {
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$month/$day ${hour}:${minute}"
}
