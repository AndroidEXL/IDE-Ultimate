package com.androidexl.ideultimate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Constant Base Palette
val DeepNavyDark = Color(0xFF050816)
val DeepNavyLight = Color(0xFFF1F5F9)

val MidnightDark = Color(0xFF0A0F24)
val MidnightLight = Color(0xFFFFFFFF)

val IndigoDeepDark = Color(0xFF161B4D)
val IndigoDeepLight = Color(0xFFE2E8F0)

val Violet = Color(0xFF7C3AED)
val VioletSoft = Color(0xFFA78BFA)
val Lilac = Color(0xFFC4B5FD)
val Fuchsia = Color(0xFFF0ABFC)
val Cyan = Color(0xFF0284C7)
val Teal = Color(0xFF0D9488)

val InkDark = Color(0xFFEDEFFF)
val InkLight = Color(0xFF0F172A)

val MutedDark = Color(0xFF9AA3C7)
val MutedLight = Color(0xFF64748B)

val GlassStrokeTopDark = Color(0x40FFFFFF)
val GlassStrokeTopLight = Color(0x20000000)

val GlassStrokeBottomDark = Color(0x0DFFFFFF)
val GlassStrokeBottomLight = Color(0x0A000000)

// Dynamic Adaptive Properties
val DeepNavy: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) DeepNavyDark else DeepNavyLight

val Midnight: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) MidnightDark else MidnightLight

val IndigoDeep: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) IndigoDeepDark else IndigoDeepLight

val Ink: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) InkDark else InkLight

val Muted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) MutedDark else MutedLight

val GlassStrokeTop: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) GlassStrokeTopDark else GlassStrokeTopLight

val GlassStrokeBottom: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) GlassStrokeBottomDark else GlassStrokeBottomLight
