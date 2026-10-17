package com.androidexl.ideultimate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = VioletSoft,
    onPrimary = Color(0xFF14092E),
    primaryContainer = IndigoDeep,
    onPrimaryContainer = Ink,
    secondary = Cyan,
    onSecondary = DeepNavy,
    tertiary = Fuchsia,
    onTertiary = DeepNavy,
    background = DeepNavy,
    onBackground = Ink,
    surface = Midnight,
    onSurface = Ink,
    surfaceVariant = IndigoDeep,
    onSurfaceVariant = Muted,
    outline = Color(0x33FFFFFF),
    outlineVariant = Color(0x1AFFFFFF),
)

@Composable
fun IDEUltimateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
