package com.example.personallevelingsystem.ui.compose.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

object DesignSystem {
    val Background = PrimaryBackground
    val Padding = 16.dp
    val CornerRadius = 20.dp

    object Space {
        val xs = 4.dp
        val s = 8.dp
        val m = 16.dp
        val l = 24.dp
        val xl = 32.dp
    }

    object Radius {
        val chip = 12.dp
        val card = 20.dp
        val sheet = 28.dp
    }
}

// Premium-subtle by default (~250 ms, springs with no overshoot). Playful springs are
// reserved for reward moments: mission complete, streak, level up.
object Motion {
    const val Quick = 150
    const val Standard = 250
    const val Emphasized = 400

    val Press = spring<Float>(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
    val Gentle = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    val Playful = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    fun <T> standard(): TweenSpec<T> = tween(Standard, easing = FastOutSlowInEasing)
    fun <T> quick(): TweenSpec<T> = tween(Quick, easing = FastOutSlowInEasing)
}

val BouncySpring = Motion.Playful

val PlacementSpring = spring<IntOffset>(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessMediumLow
)
