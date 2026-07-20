package com.example.personallevelingsystem.ui.compose.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Visual Design System: sober premium dark with red / deep-violet accents.
// Red = action & emphasis. Violet = structure & labels. Everything else stays neutral.

// Base surfaces — near-black with a faint violet tint
val SpaceBlack = Color(0xFF0C0B10)
val CharcoalGrey = Color(0xFF16141D)
val SurfaceElevated = Color(0xFF16141D)
val SurfaceHigh = Color(0xFF1D1A26)

// Accents
val CrimsonRed = Color(0xFFE5484D)     // primary action / emphasis
val RubyRed = Color(0xFF8C3A46)        // deep wine — subtle borders, secondary red
val DarkBlood = Color(0xFF3B2E58)      // deep plum — ambient washes
val DeepViolet = Color(0xFF6E56CF)     // secondary accent
val AccentViolet = Color(0xFF9E8CFC)   // violet for overlines / section labels
val AlertOrange = Color(0xFFF5A524)    // sober amber — warnings, streaks
val TelemetryGreen = Color(0xFF30A46C) // sober success
val CalmBlue = Color(0xFF52A9FF)       // recovery / hydration telemetry

// Text
val HologramText = Color(0xFFEDEAF4)
val TextPrimary = HologramText
val TextSecondary = Color(0xFFA9A3B8)

// Card surfaces — almost opaque so content stays readable over the ambient wash
val GlassSurfaceStart = Color(0xF215121D)
val GlassSurfaceEnd = Color(0xF20F0D14)
val GlassSurface = Color(0xEE14121A)
val BorderSubtle = Color(0x1AFFFFFF)

// Functional Mappings
val PrimaryBackground = SpaceBlack
val PrimaryAccent = CrimsonRed
val SecondaryAccent = DeepViolet

val AccentRed = CrimsonRed
val CyberCyan = CrimsonRed // Retained for compatibility

// Gradients — the signature red→violet ramp, reserved for progress & identity moments
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(CrimsonRed, Color(0xFFAA4F8E), DeepViolet)
)

val GlassGradient = Brush.verticalGradient(
    colors = listOf(GlassSurfaceStart, GlassSurfaceEnd)
)

val BorderGradient = Brush.linearGradient(
    colors = listOf(Color(0x26FFFFFF), Color(0x0DFFFFFF))
)
