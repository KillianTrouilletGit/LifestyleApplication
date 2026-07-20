package com.example.personallevelingsystem.ui.compose.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Sober premium dark scheme — red for actions, deep violet for structure
private val OperatorColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = Color.White,
    secondary = SecondaryAccent,
    onSecondary = Color.White,
    tertiary = TelemetryGreen,
    background = Color.Transparent, // Transparent so AmbientBackground shows through
    surface = Color.Transparent, // Components handle their own surfaces
    surfaceVariant = SurfaceElevated,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = CrimsonRed
)

@Composable
fun PersonalLevelingSystemTheme(
    // Ignore system setting, the app is dark by design
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce branding
    content: @Composable () -> Unit
) {
    val colorScheme = OperatorColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SpaceBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SciFiTypography,
        content = content
    )
}
