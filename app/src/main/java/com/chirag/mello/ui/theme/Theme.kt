package com.chirag.mello.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val MelloDarkColorScheme = darkColorScheme(
    primary = Lavender,
    secondary = Mint,
    tertiary = Peach,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onPrimary = Background
)

@Composable
fun MelloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MelloDarkColorScheme,
        typography = MelloTypography,
        content = content
    )
}
