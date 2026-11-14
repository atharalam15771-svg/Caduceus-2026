package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CaduceusDarkColorScheme = darkColorScheme(
    primary = PrimaryCrimson,
    onPrimary = TextWhite,
    primaryContainer = DarkBloodRed,
    onPrimaryContainer = TextWhite,
    secondary = AntiqueGold,
    onSecondary = VoidBlack,
    secondaryContainer = CrimsonDeep,
    onSecondaryContainer = SolarGold,
    tertiary = SolarGold,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = TextWhite,
    surface = DarkObsidian,
    onSurface = TextWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextLightGrey,
    outline = AntiqueGold.copy(alpha = 0.5f),
    outlineVariant = BorderDark,
    error = PrimaryCrimson,
    onError = TextWhite
)

@Composable
fun CaduceusTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CaduceusDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VoidBlack.toArgb()
            window.navigationBarColor = VoidBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CaduceusTypography,
        content = content
    )
}
