package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.personallevelingsystem.ui.compose.theme.CrimsonRed
import com.example.personallevelingsystem.ui.compose.theme.DarkBlood
import com.example.personallevelingsystem.ui.compose.theme.DeepViolet
import com.example.personallevelingsystem.ui.compose.theme.SpaceBlack

/**
 * Sober ambient backdrop: fixed corner washes of deep violet and red over
 * near-black, with one very slow breathing cycle. No drifting, no pulsing orbs.
 */
@Composable
fun AmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_breath")

    val breath by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        // Deep violet wash, top-right
        drawWash(DeepViolet, Offset(size.width * 0.95f, size.height * 0.05f), 0.95f, 0.11f * breath)
        // Plum wash, mid-left
        drawWash(DarkBlood, Offset(size.width * 0.05f, size.height * 0.45f), 0.85f, 0.14f * breath)
        // Faint red wash, bottom-right
        drawWash(CrimsonRed, Offset(size.width * 0.85f, size.height * 0.95f), 0.75f, 0.06f * breath)
    }
}

private fun DrawScope.drawWash(
    color: Color,
    center: Offset,
    radiusFactor: Float,
    alpha: Float
) {
    val radius = size.minDimension * radiusFactor
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
