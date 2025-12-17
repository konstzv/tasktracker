package com.tasktracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassSizes {
    val CornerRadius = 16.dp
    val SmallCornerRadius = 8.dp
    val BorderWidth = 1.dp
    val ShadowElevation = 8.dp
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassSizes.CornerRadius,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = GlassSizes.ShadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = GlassColors.GlassShadow,
                spotColor = GlassColors.GlassShadow
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassColors.GradientStart,
                        GlassColors.GradientEnd
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = GlassSizes.BorderWidth,
                color = GlassColors.GlassBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.wrapContentSize()
    } else {
        modifier
    }

    Box(
        modifier = cardModifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(GlassSizes.SmallCornerRadius),
                ambientColor = GlassColors.GlassShadow,
                spotColor = GlassColors.GlassShadow
            )
            .background(
                color = GlassColors.GlassBackground,
                shape = RoundedCornerShape(GlassSizes.SmallCornerRadius)
            )
            .border(
                width = GlassSizes.BorderWidth,
                color = GlassColors.GlassBorder,
                shape = RoundedCornerShape(GlassSizes.SmallCornerRadius)
            )
    ) {
        content()
    }
}

fun Modifier.glassBackground(
    color: Color = GlassColors.GlassBackground,
    cornerRadius: Dp = GlassSizes.CornerRadius
): Modifier = this
    .shadow(
        elevation = GlassSizes.ShadowElevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = GlassColors.GlassShadow,
        spotColor = GlassColors.GlassShadow
    )
    .background(
        color = color,
        shape = RoundedCornerShape(cornerRadius)
    )
    .border(
        width = GlassSizes.BorderWidth,
        color = GlassColors.GlassBorder,
        shape = RoundedCornerShape(cornerRadius)
    )
