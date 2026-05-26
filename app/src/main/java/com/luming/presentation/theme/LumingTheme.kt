package com.luming.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SageGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = SageGreenLight,
    secondary = SoftBlue,
    tertiary = WarmBrown,
    background = BackgroundWarm,
    surface = SurfaceWhite,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = DividerLight,
)

private val DarkColors = darkColorScheme(
    primary = SageGreenLight,
    onPrimary = SageGreenDark,
    secondary = SoftBlue,
    tertiary = WarmBrown,
    background = OnSurface,
    surface = OnSurface,
    onBackground = BackgroundWarm,
    onSurface = BackgroundWarm,
)

@Composable
fun LumingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LumingTypography,
        shapes = LumingShapes,
        content = content,
    )
}
