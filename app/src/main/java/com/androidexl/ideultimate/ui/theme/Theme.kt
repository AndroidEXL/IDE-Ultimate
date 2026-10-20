package com.androidexl.ideultimate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { true }

private val DarkAppColorScheme = darkColorScheme(
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

private val LightAppColorScheme = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = Color(0xFFC026D3),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0x29000000),
    outlineVariant = Color(0x14000000),
)

@Composable
fun IDEUltimateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkAppColorScheme else LightAppColorScheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

