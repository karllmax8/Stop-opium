package com.example.pausa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

// =================================================================== Memoria de números
@Composable
fun NumberMemoryGame(onWin: () -> Unit, onLose: () -> Unit) {
    val c = Ios.c
    var level by remember { mutableIntStateOf(0) }
    var number by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var showing by remember { mutableStateOf(true) }
    var run by remember { mutableIntStateOf(0) }
    val digits = 4 + level

    LaunchedEffect(run) {
        number = (1..digits).joinToString("") { Random.nextInt(10).toString() }
        input = ""
        showing = true
        delay(1300L + 450L * digits)
        showing = false
    }

    fun check() {
        if (input == number) {
            if (level >= 2) onWin() else { level++; run++ }
        } else onLose()
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GameHeader("Memoria de números", if (showing) "Memoriza el número…" else "Escríbelo")
        ProgressDots(level, 3)
        Box(Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(18.dp)).background(c.card), contentAlignment = Alignment.Center) {
            val shown = if (showing) number else input.padEnd(digits, '•')
            Text(
                shown, fontSize = 44.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                color = if (showing) c.blue else c.label, letterSpacing = 6.sp
            )
        }
        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("⌫", "0", "OK"))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { k ->
                        val special = k == "⌫" || k == "OK"
                        PadKey(
                            label = k,
                            bg = if (k == "OK") c.blue else if (special) c.fill else c.card,
                            fg = if (k == "OK") Color.White else c.label,
                            enabled = !showing
                        ) {
                            when (k) {
                                "⌫" -> input = input.dropLast(1)
                                "OK" -> if (input.length == digits) check()
                                else -> if (input.length < digits) input += k
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PadKey(label: String, bg: Color, fg: Color, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.weight(1f).height(60.dp)
            .pressable(enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(14.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) { Text(label, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = fg) }
}

// =================================================================== Test del chimpancé
@Composable
fun ChimpGame(onWin: () -> Unit, onLose: () -> Unit) {
    val c = Ios.c
    var round by remember { mutableIntStateOf(0) }
    val count = if (round == 0) 5 else 7
    var positions by remember { mutableStateOf(listOf<Int>()) }
    var next by remember { mutableIntStateOf(1) }

    LaunchedEffect(round) {
        positions = (0 until 20).shuffled().take(count)
        next = 1
    }

    fun tap(cell: Int) {
        val n = positions.indexOf(cell) + 1
        if (n == 0 || n < next) return
        if (n != next) { onLose(); return }
        next++
        if (next > count) { if (round >= 1) onWin() else round++ }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GameHeader("Test del chimpancé", "Toca los números en orden. Al tocar el 1 se ocultan")
        ProgressDots(round, 2)
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (r in 0 until 5) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until 4) {
                        val idx = r * 4 + col
                        val n = positions.indexOf(idx) + 1
                        val visible = n != 0 && n >= next
                        val m = Modifier.size(64.dp)
                        if (visible) {
                            Box(
                                m.pressable { tap(idx) }.clip(RoundedCornerShape(12.dp)).background(c.blue),
                                contentAlignment = Alignment.Center
                            ) {
                                if (next == 1) Text("$n", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Box(m)
                        }
                    }
                }
            }
        }
    }
}

// =================================================================== Color y palabra
@Composable
fun ColorMatchGame(onWin: () -> Unit, onLose: () -> Unit) {
    val c = Ios.c
    val palette = remember(c) {
        listOf("Rojo" to c.red, "Azul" to c.blue, "Verde" to c.green, "Naranja" to c.orange, "Morado" to c.purple)
    }
    var solved by remember { mutableIntStateOf(0) }
    var word by remember { mutableIntStateOf(0) }
    var ink by remember { mutableIntStateOf(1) }
    var options by remember { mutableStateOf(listOf<Int>()) }

    fun newRound() {
        val i = Random.nextInt(palette.size)
        val others = palette.indices.filter { it != i }
        ink = i
        word = others.random()
        options = (listOf(i) + others.shuffled().take(3)).shuffled()
    }
    LaunchedEffect(Unit) { newRound() }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        GameHeader("Color y palabra", "¿De qué color está pintada la palabra?")
        ProgressDots(solved, 6)
        Box(
            Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(20.dp)).background(c.card),
            contentAlignment = Alignment.Center
        ) {
            Text(palette[word].first.uppercase(), fontSize = 52.sp, fontWeight = FontWeight.Bold, color = palette[ink].second)
        }
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { o ->
                    Box(
                        Modifier.weight(1f).height(72.dp)
                            .pressable {
                                if (o == ink) {
                                    solved++
                                    if (solved >= 6) onWin() else newRound()
                                } else onLose()
                            }
                            .clip(RoundedCornerShape(16.dp)).background(c.card),
                        contentAlignment = Alignment.Center
                    ) { Text(palette[o].first, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = c.label) }
                }
            }
        }
    }
}

// =================================================================== Mini Sudoku 4x4
private fun canPlace(g: IntArray, i: Int, v: Int): Boolean {
    val r = i / 4
    val c = i % 4
    for (k in 0 until 4) if (g[r * 4 + k] == v || g[k * 4 + c] == v) return false
    val br = r / 2 * 2
    val bc = c / 2 * 2
    for (dr in 0..1) for (dc in 0..1) if (g[(br + dr) * 4 + bc + dc] == v) return false
    return true
}

private fun countSolutions(g: IntArray): Int {
    val i = g.indexOfFirst { it == 0 }
    if (i == -1) return 1
    var count = 0
    for (v in 1..4) {
        if (canPlace(g, i, v)) {
            g[i] = v
            count += countSolutions(g)
            g[i] = 0
            if (count >= 2) break
        }
    }
    return count
}

/** Devuelve (tablero con huecos, solución). Siempre tiene una única solución. */
private fun makeSudoku(): Pair<IntArray, IntArray> {
    val digits = (1..4).shuffled()
    val rows = listOf(0, 1).shuffled().flatMap { b -> listOf(0, 1).shuffled().map { b * 2 + it } }
    val cols = listOf(0, 1).shuffled().flatMap { b -> listOf(0, 1).shuffled().map { b * 2 + it } }
    val sol = IntArray(16)
    for (r in 0 until 4) for (c in 0 until 4) {
        val base = (2 * (rows[r] % 2) + rows[r] / 2 + cols[c]) % 4
        sol[r * 4 + c] = digits[base]
    }
    val puzzle = sol.copyOf()
    var blanks = 0
    for (idx in (0 until 16).shuffled()) {
        val keep = puzzle[idx]
        puzzle[idx] = 0
        if (countSolutions(puzzle.copyOf()) == 1) blanks++ else puzzle[idx] = keep
        if (blanks >= 9) break
    }
    return puzzle to sol
}

@Composable
fun SudokuGame(onWin: () -> Unit, onLose: () -> Unit) {
    val c = Ios.c
    val data = remember { makeSudoku() }
    val puzzle = data.first
    val sol = data.second
    val board = remember { mutableStateListOf<Int>().apply { addAll(puzzle.toList()) } }
    var selected by remember { mutableIntStateOf(-1) }
    val solved = board.indices.all { board[it] == sol[it] }

    LaunchedEffect(solved) { if (solved) { delay(800); onWin() } }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GameHeader("Mini Sudoku", "Cada fila, columna y cuadro 2×2 lleva 1, 2, 3 y 4")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until 4) {
                if (r == 2) Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (col in 0 until 4) {
                        if (col == 2) Spacer(Modifier.size(6.dp))
                        val i = r * 4 + col
                        val given = puzzle[i] != 0
                        val v = board[i]
                        val wrong = !given && v != 0 && v != sol[i]
                        val shape = RoundedCornerShape(10.dp)
                        val base = Modifier.size(66.dp).clip(shape)
                            .background(if (given) c.fill else c.card)
                            .border(if (i == selected) 3.dp else 0.dp, if (i == selected) c.blue else Color.Transparent, shape)
                        Box(
                            if (given) base else base.pressable { selected = i },
                            contentAlignment = Alignment.Center
                        ) {
                            if (v != 0) {
                                Text(
                                    "$v", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                                    color = when {
                                        given -> c.label
                                        wrong -> c.red
                                        else -> c.blue
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (n in 1..4) {
                PadKey("$n", c.card, c.label, true) {
                    if (selected >= 0 && puzzle[selected] == 0) board[selected] = n
                }
            }
            PadKey("⌫", c.fill, c.label, true) {
                if (selected >= 0 && puzzle[selected] == 0) board[selected] = 0
            }
        }
        Text(
            if (solved) "¡Resuelto!" else "Toca una casilla vacía y elige un número. En rojo: número incorrecto.",
            fontSize = 14.sp, color = if (solved) c.green else c.secondary, textAlign = TextAlign.Center
        )
    }
}

// =================================================================== Coronas
private val CROWN_MAPS = listOf(
    "BBBAABBBACEBDDCEDDEEEEEEE", "AAAAAAAAABDCADBDDDDBEEEDD", "BBAAABBBAADDDACDDDACEDDAC",
    "BBAAABAAAABDDCEDDDEEDDDEE", "AAAACAABBCDDDBCDDDCCDDDEE", "AAAAABAAACBCCCCECCCDEEEED",
    "BBBBABBBBBBBBCBDDEEBEEEEB", "BBAAABBCCABBCCCEECCDEECCC", "BBBBABBBBBCBBBBEBBDDEEDDD",
    "CCABBCCBBBCCBBBECDDDECDDD"
)

@Composable
fun CrownsGame(onWin: () -> Unit, onLose: () -> Unit) {
    val c = Ios.c
    val map = remember { CROWN_MAPS.random() }
    val crowns = remember { mutableStateListOf<Int>() }
    val colors = remember(c) { listOf(c.blue, c.green, c.orange, c.purple, c.red) }

    fun region(i: Int) = map[i] - 'A'
    fun conflicts(i: Int): Boolean = crowns.any { j ->
        j != i && (
            j / 5 == i / 5 || j % 5 == i % 5 || region(j) == region(i) ||
                (abs(j / 5 - i / 5) <= 1 && abs(j % 5 - i % 5) <= 1)
            )
    }
    val solved = crowns.size == 5 && crowns.none { conflicts(it) }

    LaunchedEffect(solved) { if (solved) { delay(800); onWin() } }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GameHeader("Coronas", "Una corona por fila, columna y color")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until 5) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (col in 0 until 5) {
                        val i = r * 5 + col
                        val has = i in crowns
                        val bad = has && conflicts(i)
                        Box(
                            Modifier.size(58.dp)
                                .pressable { if (has) crowns.remove(i) else crowns.add(i) }
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors[region(i)].copy(alpha = 0.55f))
                                .border(if (bad) 3.dp else 0.dp, if (bad) c.red else Color.Transparent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (has) Text("👑", fontSize = 28.sp)
                        }
                    }
                }
            }
        }
        Text(
            if (solved) "¡Resuelto!" else "Las coronas no pueden tocarse, ni en diagonal. Toca una casilla para poner o quitar una corona.",
            fontSize = 14.sp, color = if (solved) c.green else c.secondary, textAlign = TextAlign.Center
        )
    }
}
