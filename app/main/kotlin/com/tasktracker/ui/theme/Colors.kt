package com.tasktracker.ui.theme

import androidx.compose.ui.graphics.Color

object GlassColors {
    // Glassmorphic backgrounds
    val GlassBackground = Color(0xF0FFFFFF)      // 94% white with slight transparency
    val GlassBorder = Color(0x4DFFFFFF)          // 30% white for subtle borders
    val GlassShadow = Color(0x1A000000)          // 10% black for soft shadows
    val GlassOverlay = Color(0x80000000)         // 50% black for modal overlays

    // Accent colors
    val AccentPrimary = Color(0xFF6B4EFF)        // Purple accent
    val AccentSecondary = Color(0xFF9D7FFF)      // Light purple

    // Text colors
    val TextPrimary = Color(0xFF1A1A1A)          // Near black
    val TextSecondary = Color(0xFF666666)        // Medium gray
    val TextTertiary = Color(0xFF999999)         // Light gray
    val TextOnAccent = Color(0xFFFFFFFF)         // White for accent backgrounds

    // Task status colors
    val CompletedGreen = Color(0xFF34C759)       // Green for completed tasks
    val PendingBlue = Color(0xFF007AFF)          // Blue for pending tasks
    val DeleteRed = Color(0xFFFF3B30)            // Red for delete action

    // Subtle gradients
    val GradientStart = Color(0xF5FFFFFF)
    val GradientEnd = Color(0xE5F5F5F5)
}
