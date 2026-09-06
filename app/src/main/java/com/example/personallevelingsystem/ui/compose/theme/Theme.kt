package com.example.personallevelingsystem.ui.compose.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ArcColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    primaryContainer = RubyRed,
    onPrimaryContainer = HologramText,
    secondary = DeepViolet,
    onSecondary = Color.White,
    secondaryContainer = DarkBlood,
    onSecondaryContainer = HologramText,
    tertiary = SignalCyan,
    onTertiary = SpaceBlack,
    tertiaryContainer = CyanDeep,
    onTertiaryContainer = HologramText,
    // Transparent so AmbientBackground shows through; components paint their own surfaces.
    background = Color.Transparent,
    onBackground = TextPrimary,
    surface = Color.Transparent,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = SpaceBlack,
    surfaceContainerLow = CharcoalGrey,
    surfaceContainer = SurfaceElevated,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHigh,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = CrimsonRed,
    onError = Color.White,
    scrim = Color.Black
)

val ArcShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PersonalLevelingSystemTheme(
    // The app is dark by design; system setting and dynamic color are ignored on purpose.
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ArcColorScheme,
        typography = ArcTypography,
        shapes = ArcShapes,
        content = content
    )
}
