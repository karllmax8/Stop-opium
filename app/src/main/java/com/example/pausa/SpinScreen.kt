package com.example.pausa

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Ruleta que decide cuántos minutos ganas. */
@Composable
fun SpinScreen(onFinish: (Int) -> Unit) {
    val c = Ios.c
    val values = remember { listOf(5, 10, 15, 5, 10, 15) }
    val rotation = remember { Animatable(0f) }
    var result by remember { mutableStateOf<Int?>(null) }
    var spinning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun spin() {
        spinning = true
        val index = Random.nextInt(values.size)
        val slice = 360f / values.size
        // El puntero está arriba: llevamos el centro de la porción elegida a ese punto.
        val target = 360f * 5 + (360f - (index + 0.5f) * slice)
        scope.launch {
            rotation.animateTo(target, tween(3200, easing = FastOutSlowInEasing))
            result = values[index]
            delay(1200)
            onFinish(values[index])
        }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text("¡Lo lograste!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = c.label)
        Text("Gira la ruleta para ganar minutos", fontSize = 15.sp, color = c.secondary)

        Box(contentAlignment = Alignment.TopCenter) {
            Canvas(Modifier.size(290.dp).rotate(rotation.value)) {
                val slice = 360f / values.size
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 26.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                values.forEachIndexed { i, v ->
                    drawArc(
                        color = if (i % 2 == 0) c.blue else c.indigo,
                        startAngle = -90f + i * slice,
                        sweepAngle = slice,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height)
                    )
                    drawIntoCanvas { cv ->
                        cv.nativeCanvas.save()
                        cv.nativeCanvas.rotate((i + 0.5f) * slice, center.x, center.y)
                        cv.nativeCanvas.drawText("$v", center.x, center.y - size.width * 0.34f, paint)
                        cv.nativeCanvas.restore()
                    }
                }
            }
            // Puntero
            Canvas(Modifier.size(26.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2, size.height); close()
                }
                drawPath(path, c.label)
            }
        }

        val r = result
        if (r != null) {
            Text("+$r min", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = c.green)
        } else {
            PrimaryButton(
                text = "Girar",
                enabled = !spinning,
                modifier = Modifier.padding(horizontal = 24.dp),
                onClick = { spin() }
            )
        }
    }
}
