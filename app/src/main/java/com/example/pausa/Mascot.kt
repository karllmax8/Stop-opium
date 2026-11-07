package com.example.pausa

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class Mood { HAPPY, OK, WORRIED, SAD }

enum class Slot(val label: String) { HEAD("Cabeza"), FACE("Cara"), NECK("Cuello") }

data class ShopItem(val id: String, val name: String, val slot: Slot, val price: Int)

val SHOP = listOf(
    ShopItem("hat_party", "Gorro de fiesta", Slot.HEAD, 60),
    ShopItem("crown", "Corona", Slot.HEAD, 150),
    ShopItem("shades", "Gafas oscuras", Slot.FACE, 120),
    ShopItem("mustache", "Bigote", Slot.FACE, 90),
    ShopItem("bowtie", "Pajarita", Slot.NECK, 60),
    ShopItem("scarf", "Bufanda", Slot.NECK, 100)
)

/** Ánimo de Brote según los minutos desbloqueados hoy frente a tu meta. */
fun moodFor(minutesToday: Int, goal: Int): Mood = when {
    minutesToday <= goal / 2 -> Mood.HAPPY
    minutesToday <= goal -> Mood.OK
    minutesToday <= goal * 3 / 2 -> Mood.WORRIED
    else -> Mood.SAD
}

/** Brote: mascota original dibujada con formas básicas. */
@Composable
fun Mascot(mood: Mood, head: String, face: String, neck: String, size: Dp = 170.dp) {
    Canvas(Modifier.size(size)) { drawMascot(mood, head, face, neck) }
}

private fun DrawScope.drawMascot(mood: Mood, head: String, face: String, neck: String) {
    val w = size.width
    fun p(x: Float, y: Float) = Offset(x * w, y * w)
    fun s(width: Float, height: Float) = Size(width * w, height * w)

    val body = Color(0xFF5AD8A6)
    val leaf = Color(0xFF2FB36F)
    val ink = Color(0xFF1F2A44)

    // Sombra
    drawOval(Color.Black.copy(alpha = 0.12f), p(0.18f, 0.90f), s(0.64f, 0.07f))

    // Brote (hojita) detrás del cuerpo
    drawLine(leaf, p(0.72f, 0.26f), p(0.76f, 0.13f), strokeWidth = 0.022f * w, cap = StrokeCap.Round)
    rotate(-30f, pivot = p(0.76f, 0.13f)) { drawOval(leaf, p(0.76f, 0.07f), s(0.13f, 0.065f)) }
    rotate(30f, pivot = p(0.76f, 0.13f)) { drawOval(leaf, p(0.64f, 0.07f), s(0.13f, 0.065f)) }

    // Cuerpo y barriga
    drawOval(body, p(0.10f, 0.20f), s(0.80f, 0.70f))
    drawOval(Color.White.copy(alpha = 0.18f), p(0.28f, 0.60f), s(0.44f, 0.26f))

    // Mejillas
    val blush = Color(0xFFFF8FA3).copy(alpha = 0.55f)
    drawOval(blush, p(0.19f, 0.58f), s(0.10f, 0.06f))
    drawOval(blush, p(0.71f, 0.58f), s(0.10f, 0.06f))

    // Ojos
    val eyeY = if (mood == Mood.SAD) 0.52f else 0.48f
    val look = if (mood == Mood.SAD || mood == Mood.WORRIED) 0.015f else 0f
    for (cx in listOf(0.36f, 0.64f)) {
        drawCircle(Color.White, 0.085f * w, p(cx, eyeY))
        drawCircle(ink, 0.047f * w, p(cx, eyeY + look))
        drawCircle(Color.White, 0.016f * w, p(cx + 0.015f, eyeY + look - 0.015f))
    }

    // Cejas preocupadas
    if (mood == Mood.WORRIED || mood == Mood.SAD) {
        drawLine(ink, p(0.27f, 0.39f), p(0.42f, 0.34f), strokeWidth = 0.02f * w, cap = StrokeCap.Round)
        drawLine(ink, p(0.58f, 0.34f), p(0.73f, 0.39f), strokeWidth = 0.02f * w, cap = StrokeCap.Round)
    }

    // Boca
    val stroke = Stroke(width = 0.022f * w, cap = StrokeCap.Round)
    when (mood) {
        Mood.HAPPY -> drawArc(ink, 0f, 180f, false, p(0.40f, 0.58f), s(0.20f, 0.14f), style = stroke)
        Mood.OK -> drawArc(ink, 20f, 140f, false, p(0.42f, 0.62f), s(0.16f, 0.08f), style = stroke)
        Mood.WORRIED -> drawLine(ink, p(0.43f, 0.68f), p(0.57f, 0.68f), strokeWidth = 0.022f * w, cap = StrokeCap.Round)
        Mood.SAD -> {
            drawArc(ink, 200f, 140f, false, p(0.41f, 0.67f), s(0.18f, 0.10f), style = stroke)
            drawOval(Color(0xFF6EC1FF), p(0.30f, 0.58f), s(0.03f, 0.05f))
        }
    }

    // Cara
    when (face) {
        "shades" -> {
            drawRoundRect(ink, p(0.24f, 0.41f), s(0.21f, 0.13f), CornerRadius(0.04f * w))
            drawRoundRect(ink, p(0.55f, 0.41f), s(0.21f, 0.13f), CornerRadius(0.04f * w))
            drawLine(ink, p(0.44f, 0.45f), p(0.56f, 0.45f), strokeWidth = 0.02f * w)
            drawLine(Color.White.copy(alpha = 0.5f), p(0.27f, 0.43f), p(0.31f, 0.43f), strokeWidth = 0.015f * w, cap = StrokeCap.Round)
        }
        "mustache" -> {
            val brown = Color(0xFF6B4226)
            drawOval(brown, p(0.37f, 0.565f), s(0.13f, 0.05f))
            drawOval(brown, p(0.50f, 0.565f), s(0.13f, 0.05f))
        }
    }

    // Cuello
    when (neck) {
        "bowtie" -> {
            val red = Color(0xFFFF4D6D)
            val left = Path().apply { moveTo(0.5f * w, 0.85f * w); lineTo(0.37f * w, 0.79f * w); lineTo(0.37f * w, 0.91f * w); close() }
            val right = Path().apply { moveTo(0.5f * w, 0.85f * w); lineTo(0.63f * w, 0.79f * w); lineTo(0.63f * w, 0.91f * w); close() }
            drawPath(left, red); drawPath(right, red)
            drawCircle(Color(0xFFD63A57), 0.03f * w, p(0.5f, 0.85f))
        }
        "scarf" -> {
            val orange = Color(0xFFFF9F43)
            drawRoundRect(orange, p(0.22f, 0.79f), s(0.56f, 0.08f), CornerRadius(0.04f * w))
            drawRoundRect(Color(0xFFE88A2E), p(0.58f, 0.84f), s(0.09f, 0.12f), CornerRadius(0.03f * w))
        }
    }

    // Cabeza
    when (head) {
        "hat_party" -> rotate(-12f, pivot = p(0.5f, 0.24f)) {
            val hat = Path().apply { moveTo(0.5f * w, 0.0f); lineTo(0.64f * w, 0.24f * w); lineTo(0.36f * w, 0.24f * w); close() }
            drawPath(hat, Color(0xFFFF6FB5))
            drawCircle(Color.White.copy(alpha = 0.7f), 0.012f * w, p(0.47f, 0.16f))
            drawCircle(Color.White.copy(alpha = 0.7f), 0.012f * w, p(0.53f, 0.12f))
            drawCircle(Color(0xFFFFD54F), 0.03f * w, p(0.5f, 0.01f))
        }
        "crown" -> {
            val crown = Path().apply {
                moveTo(0.34f * w, 0.24f * w); lineTo(0.34f * w, 0.10f * w); lineTo(0.42f * w, 0.17f * w)
                lineTo(0.50f * w, 0.06f * w); lineTo(0.58f * w, 0.17f * w); lineTo(0.66f * w, 0.10f * w)
                lineTo(0.66f * w, 0.24f * w); close()
            }
            drawPath(crown, Color(0xFFFFC83D))
            drawCircle(Color(0xFFFF4D6D), 0.014f * w, p(0.5f, 0.19f))
            drawCircle(Color(0xFF4DA3FF), 0.011f * w, p(0.40f, 0.21f))
            drawCircle(Color(0xFF4DA3FF), 0.011f * w, p(0.60f, 0.21f))
        }
    }
}
