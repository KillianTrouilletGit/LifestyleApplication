package com.example.personallevelingsystem.ui.compose.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Visual Design System: Future Neon / High-Precision
val SpaceBlack = Color(0xFF000000) // True Depth Black
val NeonMagenta = Color(0xFFFF00FF) // Hot Magenta
val NeonCyan = Color(0xFF00FFFF)   // Electric Cyan
val NeonViolet = Color(0xFF9D00FF) // Electric Violet
val AlertOrange = Color(0xFFFF5722)
val GlassSurfaceStart = Color(0x33FFFFFF)
val GlassSurfaceEnd = Color(0x0DFFFFFF)
val GlassSurface = Color(0x1AFFFFFF)
val HologramText = Color(0xCCFFFFFF)
val TelemetryGreen = Color(0xFF00E676)

// Functional Mappings
val PrimaryBackground = SpaceBlack
val PrimaryAccent = NeonCyan // Default primary color for text/icons
val SecondaryAccent = NeonMagenta

val TextPrimary = HologramText
val TextSecondary = NeonCyan.copy(alpha = 0.6f)

// Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(NeonMagenta, NeonCyan)
)

val GlassGradient = Brush.verticalGradient(
    colors = listOf(GlassSurfaceStart, GlassSurfaceEnd)
)

// Legacy but redirected
val BorderGradient = PrimaryGradient
