package com.example.pausa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
    Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ios.c.label)
        Text(subtitle, fontSize = 15.sp, color = Ios.c.secondary)
    }
}

@Composable
private fun ProgressDots(done: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { i ->
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (i < done) Ios.c.blue else Ios.c.fill))
        }
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
        GameHeader("Cálculo rápido", "Resuelve 5 seguidas")
        ProgressDots(solved, 5)
        Spacer(Modifier.height(8.dp))
        Text(q.text, fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Ios.c.label)
        q.options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { v ->
                    Box(
                        Modifier.weight(1f).height(76.dp)
                            .pressable {
                                if (v == q.answer) {
                                    solved++
                                    if (solved >= 5) onWin() else q = newQuestion()
                                } else onLose()
                            }
                            .clip(RoundedCornerShape(16.dp)).background(Ios.c.card),
                        contentAlignment = Alignment.Center
                    ) { Text("$v", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Ios.c.label) }
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
        GameHeader("Secuencia", if (showing) "Memoriza el orden…" else "Ahora repítelo")
        Spacer(Modifier.height(12.dp))
        (0 until 9).chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { i ->
                    Box(
                        Modifier.weight(1f).height(92.dp)
                            .pressable(enabled = !showing) { tap(i) }
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (lit == i) Ios.c.blue else Ios.c.card)
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
        GameHeader("Reflejos", "Toca cuando se ponga verde")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { ProgressDots(hits, 3) }
        Box(
            Modifier.fillMaxSize()
                .pressable {
                    if (waiting) onLose()
                    else if (System.currentTimeMillis() - readyAt < 600) {
                        hits++
                        if (hits >= 3) onWin() else run++
                    } else onLose()
                }
                .clip(RoundedCornerShape(28.dp))
                .background(if (waiting) Ios.c.red else Ios.c.green),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (waiting) "Espera…" else "¡Ahora!",
                fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center
            )
        }
    }
}
