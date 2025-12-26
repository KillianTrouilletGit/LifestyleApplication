package com.example.personallevelingsystem.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.personallevelingsystem.ui.compose.theme.NeonCyan
import com.example.personallevelingsystem.ui.compose.theme.NeonMagenta
import com.example.personallevelingsystem.ui.compose.theme.NeonViolet
import com.example.personallevelingsystem.ui.compose.theme.SpaceBlack
import kotlin.random.Random

@Composable
fun AmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_animation")

    // Define multiple orbs with slightly different animation phases
    val orb1 = rememberOrbState(infiniteTransition, NeonMagenta, 10000)
    val orb2 = rememberOrbState(infiniteTransition, NeonCyan, 13000)
    val orb3 = rememberOrbState(infiniteTransition, NeonViolet, 11000)
    val orb4 = rememberOrbState(infiniteTransition, NeonCyan.copy(alpha=0.5f), 15000)

    Canvas(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        drawOrb(orb1)
        drawOrb(orb2)
        drawOrb(orb3)
        drawOrb(orb4)
    }
}

data class OrbState(
    val color: Color,
    val offsetX: State<Float>,
    val offsetY: State<Float>,
    val scale: State<Float>,
    val alpha: State<Float>
)

@Composable
fun rememberOrbState(
    transition: InfiniteTransition,
    baseColor: Color,
    durationMillis: Int
): OrbState {
    val random = remember { Random(System.currentTimeMillis() + durationMillis) }
    
    // Animate Position (Drift)
    // We'll simplistic drift: specific range 0.2 to 0.8 of screen
    val offsetX = transition.animateFloat(
        initialValue = 0.2f + random.nextFloat() * 0.6f,
        targetValue = 0.2f + random.nextFloat() * 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val offsetY = transition.animateFloat(
        initialValue = 0.2f + random.nextFloat() * 0.6f,
        targetValue = 0.2f + random.nextFloat() * 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis + 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // Pulse size
    val scale = transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Pulse Alpha
    val alpha = transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    return OrbState(baseColor, offsetX, offsetY, scale, alpha)
}

fun DrawScope.drawOrb(orb: OrbState) {
    val centerX = size.width * orb.offsetX.value
    val centerY = size.height * orb.offsetY.value
    val radius = (size.minDimension / 2) * orb.scale.value
    
    val brush = Brush.radialGradient(
        colors = listOf(
            orb.color.copy(alpha = orb.alpha.value), // Center glow
            orb.color.copy(alpha = 0f)  // Fade out
        ),
        center = Offset(centerX, centerY),
        radius = radius
    )

    drawCircle(
        brush = brush,
        radius = radius,
        center = Offset(centerX, centerY)
    )
}
