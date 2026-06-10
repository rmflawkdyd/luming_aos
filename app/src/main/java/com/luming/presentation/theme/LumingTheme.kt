package com.luming.presentation.theme

import androidx.compose.material3.MaterialTheme
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

@Composable
fun LumingTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = LumingTypography,
        shapes = LumingShapes,
        content = content,
    )
}
