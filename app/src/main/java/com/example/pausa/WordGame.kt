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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

object Words {
    val list = listOf(
        "ABEJA", "ABRIR", "ACTOR", "AGUAS", "AGUJA", "AMIGO", "ANTES", "ARBOL", "ARENA", "AVION",
        "AYUDA", "BAILE", "BANCO", "BARCO", "BESAR", "BOTAS", "BRAZO", "BUENO", "BURRO", "CABLE",
        "CALOR", "CAMPO", "CARNE", "CARTA", "CASAS", "CERCA", "CIELO", "CINCO", "CLARO", "COCHE",
        "CORTE", "CUERO", "DEDOS", "DOLOR", "DULCE", "ENERO", "FELIZ", "FIRMA", "FRESA", "FRUTA",
        "FUEGO", "FUERA", "GATOS", "GENTE", "GRADO", "GRUPO", "HABLA", "HIELO", "HORAS", "HOTEL",
        "HUEVO", "HUMOR", "JOVEN", "JUEGO", "LAPIZ", "LEJOS", "LIBRO", "LIMON", "LLAVE", "LUCES",
        "LUNES", "MADRE", "MANGO", "MANOS", "MESAS", "METAL", "MISMO", "MONTE", "MORIR", "MUNDO",
        "NARIZ", "NEGRO", "NIEVE", "NOCHE", "NUBES", "NUEVO", "PADRE", "PAPEL", "PAUSA", "PERAS",
        "PERRO", "PIANO", "PIZZA", "PLATO", "PLAYA", "PLUMA", "POLLO", "QUESO", "RADIO", "RELOJ",
        "RISAS", "ROBOT", "ROSAS", "RUEDA", "SALIR", "SALSA", "SALTO", "SILLA", "SOLAR", "SUELO",
        "TARDE", "TECLA", "TENIS", "TIGRE", "TORRE", "TORTA", "TRIGO", "VASOS", "VELAS", "VERDE",
        "VIAJE", "VIDEO", "VIVIR", "VOLAR", "ZORRO"
    )
}

/** 2 = letra en su sitio, 1 = está en otro sitio, 0 = no está. */
private fun evaluate(guess: String, target: String): IntArray {
    val res = IntArray(5)
    val left = HashMap<Char, Int>()
    for (i in 0 until 5) {
        if (guess[i] == target[i]) res[i] = 2
        else left[target[i]] = (left[target[i]] ?: 0) + 1
    }
    for (i in 0 until 5) {
        if (res[i] != 2) {
            val n = left[guess[i]] ?: 0
            if (n > 0) { res[i] = 1; left[guess[i]] = n - 1 }
        }
    }
    return res
}

/** Adivina la palabra de 5 letras en 6 intentos. */
@Composable
fun WordGame(onWin: () -> Unit, onLose: () -> Unit) {
    val c = Ios.c
    val target = remember { Words.list.random() }
    val guesses = remember { mutableStateListOf<String>() }
    var current by remember { mutableStateOf("") }
    var done by remember { mutableStateOf<Boolean?>(null) }
    var hint by remember { mutableStateOf("") }

    LaunchedEffect(done) {
        when (done) {
            true -> { delay(1000); onWin() }
            false -> { delay(2400); onLose() }
            null -> {}
        }
    }

    fun submit() {
        if (done != null) return
        if (current.length < 5) { hint = "Faltan letras"; return }
        guesses.add(current)
        current = ""
        hint = ""
        if (guesses.last() == target) done = true
        else if (guesses.size >= 6) done = false
    }
    fun type(ch: Char) { if (done == null && current.length < 5) { current += ch; hint = "" } }
    fun back() { if (done == null && current.isNotEmpty()) current = current.dropLast(1) }

    val keyState = HashMap<Char, Int>()
    guesses.forEach { g ->
        val ev = evaluate(g, target)
        g.forEachIndexed { i, ch -> keyState[ch] = maxOf(keyState[ch] ?: -1, ev[i]) }
    }

    fun stateColor(s: Int?): Color = when (s) {
        2 -> c.green
        1 -> c.orange
        0 -> c.secondary.copy(alpha = 0.55f)
        else -> c.fill
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GameHeader("Palabra", "Adivina la palabra de 5 letras")

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in 0 until 6) {
                val word = when {
                    row < guesses.size -> guesses[row]
                    row == guesses.size -> current
                    else -> ""
                }
                val ev = if (row < guesses.size) evaluate(guesses[row], target) else null
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (col in 0 until 5) {
                        val ch = word.getOrNull(col)
                        val shape = RoundedCornerShape(8.dp)
                        val base = Modifier.size(48.dp).clip(shape)
                        val m = if (ev != null) {
                            base.background(stateColor(ev[col]))
                        } else {
                            base.background(c.card).border(2.dp, if (ch != null) c.secondary else c.separator, shape)
                        }
                        Box(m, contentAlignment = Alignment.Center) {
                            if (ch != null) {
                                Text(
                                    ch.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                    color = if (ev != null) Color.White else c.label
                                )
                            }
                        }
                    }
                }
            }
        }

        val msg = when {
            done == false -> "Era: $target"
            done == true -> "¡Correcto!"
            else -> hint
        }
        Text(
            msg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            color = if (done == true) c.green else if (done == false) c.red else c.secondary,
            modifier = Modifier.height(20.dp)
        )

        val rows = listOf("QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEachIndexed { idx, r ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (idx == 2) Key("ENTER", 1.7f, c.blue, Color.White, 13) { submit() }
                    r.forEach { ch ->
                        val s = keyState[ch]
                        val bg = if (s == null) c.fill else stateColor(s)
                        val fg = if (s == null) c.label else Color.White
                        Key(ch.toString(), 1f, bg, fg, 18) { type(ch) }
                    }
                    if (idx == 2) Key("⌫", 1.7f, c.fill, c.label, 18) { back() }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RowScope.Key(label: String, weight: Float, bg: Color, fg: Color, size: Int, onClick: () -> Unit) {
    Box(
        Modifier.weight(weight).height(46.dp)
            .pressable(onClick = onClick)
            .clip(RoundedCornerShape(7.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = size.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}
