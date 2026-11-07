package com.example.pausa

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Paleta inspirada en los colores del sistema de iOS (claro y oscuro). */
@Immutable
class IosColors(
    val bg: Color,
    val card: Color,
    val label: Color,
    val secondary: Color,
    val separator: Color,
    val fill: Color,
    val blue: Color,
    val green: Color,
    val red: Color,
    val orange: Color,
    val indigo: Color,
    val purple: Color,
    val isDark: Boolean
)

private val LightIos = IosColors(
    bg = Color(0xFFF2F2F7), card = Color.White, label = Color.Black,
    secondary = Color(0x993C3C43), separator = Color(0x4A3C3C43), fill = Color(0x33787880),
    blue = Color(0xFF007AFF), green = Color(0xFF34C759), red = Color(0xFFFF3B30),
    orange = Color(0xFFFF9500), indigo = Color(0xFF5856D6), purple = Color(0xFFAF52DE),
    isDark = false
)

private val DarkIos = IosColors(
    bg = Color.Black, card = Color(0xFF1C1C1E), label = Color.White,
    secondary = Color(0x99EBEBF5), separator = Color(0xA6545458), fill = Color(0x5C787880),
    blue = Color(0xFF0A84FF), green = Color(0xFF30D158), red = Color(0xFFFF453A),
    orange = Color(0xFFFF9F0A), indigo = Color(0xFF5E5CE6), purple = Color(0xFFBF5AF2),
    isDark = true
)

val LocalIos = staticCompositionLocalOf { LightIos }

object Ios {
    val c: IosColors
        @Composable @ReadOnlyComposable get() = LocalIos.current
}

@Composable
fun PausaTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val c = if (dark) DarkIos else LightIos
    val scheme = if (dark) {
        darkColorScheme(primary = c.blue, background = c.bg, surface = c.bg, onBackground = c.label, onSurface = c.label)
    } else {
        lightColorScheme(primary = c.blue, background = c.bg, surface = c.bg, onBackground = c.label, onSurface = c.label)
    }
    CompositionLocalProvider(LocalIos provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
