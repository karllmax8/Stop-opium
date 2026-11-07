package com.example.pausa

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF121729)
val Indigo = Color(0xFF5C6BFF)
val Purple = Color(0xFF8E4DFF)
val Card = Color.White.copy(alpha = 0.07f)

@Composable
fun PausaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Indigo,
            background = Bg,
            surface = Bg,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
