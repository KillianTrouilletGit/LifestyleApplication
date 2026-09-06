package com.example.personallevelingsystem.ui.compose.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ARC palette on near-black. Red = action, violet = structure, cyan = data/telemetry.

// Surfaces
val SpaceBlack = Color(0xFF0B0A10)
val CharcoalGrey = Color(0xFF15131C)
val SurfaceElevated = Color(0xFF16141D)
val SurfaceHigh = Color(0xFF1E1B28)
val SurfaceCard = Color(0xF214121B)

// Accents
val CrimsonRed = Color(0xFFE5484D)
val RubyRed = Color(0xFF8C3A46)
val DarkBlood = Color(0xFF3B2E58)
val DeepViolet = Color(0xFF6E56CF)
val AccentViolet = Color(0xFF9E8CFC)
val SignalCyan = Color(0xFF22D3EE)
val CyanDeep = Color(0xFF0E7490)
val AlertOrange = Color(0xFFF5A524)
val TelemetryGreen = Color(0xFF30A46C)
val CalmBlue = Color(0xFF52A9FF)

// Text
val HologramText = Color(0xFFEDEAF4)
val TextPrimary = HologramText
val TextSecondary = Color(0xFFA9A3B8)
val TextTertiary = Color(0xFF6F6A7E)

// Glass & borders
val GlassSurfaceStart = Color(0xF215121D)
val GlassSurfaceEnd = Color(0xF20F0D14)
val GlassSurface = Color(0xEE14121A)
val GlassFill = Color(0x14FFFFFF)
val BorderSubtle = Color(0x1AFFFFFF)
val BorderStrong = Color(0x33FFFFFF)

// Functional
val PrimaryBackground = SpaceBlack
val PrimaryAccent = CrimsonRed
val SecondaryAccent = DeepViolet
val TertiaryAccent = SignalCyan
val AccentRed = CrimsonRed
val CyberCyan = SignalCyan

// Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(CrimsonRed, Color(0xFFAA4F8E), DeepViolet)
)

val TelemetryGradient = Brush.linearGradient(
    colors = listOf(DeepViolet, SignalCyan)
)

val GlassGradient = Brush.verticalGradient(
    colors = listOf(GlassSurfaceStart, GlassSurfaceEnd)
)

val CardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xF41A1724), Color(0xF4120F1A))
)

val BorderGradient = Brush.linearGradient(
    colors = listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))
)

val SheenGradient = Brush.verticalGradient(
    colors = listOf(Color(0x2EFFFFFF), Color(0x00FFFFFF))
)
