package com.example.personallevelingsystem.ui.compose.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Visual Design System: Premium Dark Glass with Ruby Accents
val SpaceBlack = Color(0xFF0A0A0A) // Very deep gray/black
val CharcoalGrey = Color(0xFF141414)
val CrimsonRed = Color(0xFFE50914) // Bold Red
val RubyRed = Color(0xFF9E0B0F)
val DarkBlood = Color(0xFF4A0404)
val AlertOrange = Color(0xFFFF5722)
val TelemetryGreen = Color(0xFF00E676)

// Glassmorphic colors
val GlassSurfaceStart = Color(0x22FFFFFF)
val GlassSurfaceEnd = Color(0x05FFFFFF)
val GlassSurface = Color(0x15FFFFFF)
val HologramText = Color(0xE6FFFFFF)

// Functional Mappings
val PrimaryBackground = SpaceBlack
val PrimaryAccent = CrimsonRed
val SecondaryAccent = RubyRed

val TextPrimary = HologramText
val TextSecondary = Color(0x99FFFFFF)
val AccentRed = CrimsonRed
val CyberCyan = CrimsonRed // Retained for compatibility

// Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF3B5C), CrimsonRed, RubyRed)
)

val GlassGradient = Brush.verticalGradient(
    colors = listOf(GlassSurfaceStart, GlassSurfaceEnd)
)

val BorderGradient = Brush.linearGradient(
    colors = listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF))
)
