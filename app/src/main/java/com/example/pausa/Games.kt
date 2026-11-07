package com.example.pausa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Question(val text: String, val answer: Int, val options: List<Int>)

private fun newQuestion(): Question {
    val a = Random.nextInt(6, 20)
    val b = Random.nextInt(3, 13)
    val op = listOf("+", "−", "×").random()
    val ans = when (op) { "+" -> a + b; "−" -> a - b; else -> a * b }
    val set = mutableSetOf(ans)
    while (set.size < 4) set.add(ans + Random.nextInt(-9, 10))
    return Question("$a $op $b", ans, set.shuffled())
}

@Composable
private fun GameHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(subtitle, color = Color.White.copy(alpha = 0.6f))
    }
}

// ---------- Cálculo rápido: 5 operaciones seguidas ----------
@Composable
fun MathGame(onWin: () -> Unit, onLose: () -> Unit) {
    var solved by remember { mutableIntStateOf(0) }
    var q by remember { mutableStateOf(newQuestion()) }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GameHeader("Cálculo rápido", "$solved/5")
        Text(q.text, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        q.options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { v ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                if (v == q.answer) {
                                    solved++
                                    if (solved >= 5) onWin() else q = newQuestion()
                                } else onLose()
                            }.padding(vertical = 22.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("$v", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        }
    }
}

// ---------- Secuencia: memoriza el patrón de casillas ----------
@Composable
fun SequenceGame(onWin: () -> Unit, onLose: () -> Unit) {
    var level by remember { mutableIntStateOf(3) }
    var pattern by remember { mutableStateOf(listOf<Int>()) }
    var lit by remember { mutableStateOf<Int?>(null) }
    var input by remember { mutableStateOf(listOf<Int>()) }
    var showing by remember { mutableStateOf(true) }
    var run by remember { mutableIntStateOf(0) }

    LaunchedEffect(run) {
        val p = List(level) { Random.nextInt(9) }
        pattern = p; input = emptyList(); showing = true
        delay(600)
        for (x in p) { lit = x; delay(550); lit = null; delay(250) }
        showing = false
    }

    fun tap(i: Int) {
        if (showing) return
        val n = input + i
        input = n
        if (n.last() != pattern[n.size - 1]) { onLose(); return }
        if (n.size == pattern.size) {
            if (level >= 5) onWin() else { level++; run++ }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameHeader("Secuencia", if (showing) "Memoriza…" else "Repite el orden")
        Box(Modifier.height(8.dp))
        (0 until 9).chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { i ->
                    Box(
                        Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (lit == i) Indigo else Color.White.copy(alpha = 0.1f))
                            .clickable { tap(i) }
                    )
                }
            }
        }
    }
}

// ---------- Reflejos: toca al ponerse verde, 3 veces en menos de 600 ms ----------
@Composable
fun ReactionGame(onWin: () -> Unit, onLose: () -> Unit) {
    var waiting by remember { mutableStateOf(true) }
    var readyAt by remember { mutableLongStateOf(0L) }
    var hits by remember { mutableIntStateOf(0) }
    var run by remember { mutableIntStateOf(0) }

    LaunchedEffect(run) {
        waiting = true
        delay(Random.nextLong(1200, 3000))
        readyAt = System.currentTimeMillis()
        waiting = false
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        GameHeader("Reflejos", "Toca cuando se ponga verde · $hits/3")
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))
                .background(if (waiting) Color(0xCCE53935) else Color(0xFF43A047))
                .clickable {
                    if (waiting) onLose()
                    else if (System.currentTimeMillis() - readyAt < 600) {
                        hits++
                        if (hits >= 3) onWin() else run++
                    } else onLose()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (waiting) "Espera…" else "¡Ahora!",
                fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center
            )
        }
    }
}
