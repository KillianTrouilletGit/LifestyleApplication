package com.example.personallevelingsystem.ui.compose.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.personallevelingsystem.ui.compose.theme.SpaceBlack

// Force Dark Theme for Future Neon aesthetic
private val SciFiColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    secondary = SecondaryAccent,
    tertiary = TelemetryGreen,
    background = Color.Transparent, // Transparent so AmbientBackground shows through
    surface = Color.Transparent, // Transparent surface for components to handle their own backgrounds (e.g. Glass)
    onPrimary = SpaceBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PersonalLevelingSystemTheme(
    // Ignore system setting, force "Sci-Fi" dark mode
    darkTheme: Boolean = true, 
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce branding
    content: @Composable () -> Unit
) {
    val colorScheme = SciFiColorScheme

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
