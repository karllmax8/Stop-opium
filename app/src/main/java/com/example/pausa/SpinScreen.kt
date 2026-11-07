package com.example.pausa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val Gold = Color(0xFFFFC107)

/** Cabina de premios: un rodillo que gira y se frena en 5, 10, 15 o, con suerte, 30 minutos. */
@Composable
fun SpinScreen(onFinish: (Int) -> Unit) {
    val c = Ios.c
    val symbols = remember { listOf(5, 10, 15, 30) }
    var idx by remember { mutableIntStateOf(0) }
    var spinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val n = symbols.size

    // Probabilidades: 5 min 40 %, 10 min 35 %, 15 min 20 %, 30 min (jackpot) 5 %.
    fun roll(): Int {
        val r = Random.nextInt(100)
        return when {
            r < 40 -> 5
            r < 75 -> 10
            r < 95 -> 15
            else -> 30
        }
    }

    fun spin() {
        spinning = true
        val target = roll()
        scope.launch {
            var wait = 45L
            while (wait < 380L) {
                idx = (idx + 1) % n
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(wait)
                wait += 18
            }
            idx = symbols.indexOf(target)
            result = target
            delay(1600)
            onFinish(target)
        }
    }

    val jackpot = result == 30
    val frame = if (jackpot) Gold else c.blue

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically)
    ) {
        Text("¡Lo lograste!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = c.label)
        Text("Gira la cabina para ganar minutos", fontSize = 15.sp, color = c.secondary)

        Column(
            Modifier.size(width = 230.dp, height = 270.dp)
                .clip(RoundedCornerShape(28.dp)).background(c.card)
                .border(4.dp, frame, RoundedCornerShape(28.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("${symbols[(idx + n - 1) % n]}", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.label.copy(alpha = 0.18f))
            Text(
                "${symbols[idx]} min", fontSize = 58.sp, fontWeight = FontWeight.Bold,
                color = if (jackpot) Gold else c.label
            )
            Text("${symbols[(idx + 1) % n]}", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.label.copy(alpha = 0.18f))
        }

        val r = result
        if (r != null) {
            Text(
                if (jackpot) "¡JACKPOT! +$r min" else "+$r min ganados",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (jackpot) Gold else c.green
            )
        } else {
            PrimaryButton(
                text = if (spinning) "Girando…" else "Girar",
                enabled = !spinning,
                modifier = Modifier.padding(horizontal = 24.dp),
                onClick = { spin() }
            )
        }
        if (r == null) Text("Premio gordo: 30 min (5 %)", fontSize = 13.sp, color = c.secondary, modifier = Modifier.height(18.dp))
    }
}
