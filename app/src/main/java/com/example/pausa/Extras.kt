package com.example.pausa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Etiqueta redondeada con icono, para racha y gemas. */
@Composable
fun Pill(icon: ImageVector, text: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Ios.c.fill).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ios.c.label)
    }
}

private fun fmtMinutes(m: Int) = if (m >= 60) "${m / 60} h ${m % 60} min" else "$m min"

// ------------------------------------------------------------------ Liga
private data class Player(val name: String, val minutes: Int, val color: Color, val me: Boolean = false)

@Composable
fun LeagueScreen(prefs: Prefs, tick: Int) {
    val c = Ios.c
    val mine = remember(tick) { prefs.weekMinutes() }
    val ranked = remember(mine, c) {
        listOf(
            Player("Camila", 95, c.purple),
            Player("Mateo", 140, c.orange),
            Player("Sofía", 185, c.green),
            Player("Daniel", 240, c.indigo),
            Player("Valentina", 310, c.red),
            Player("Tú", mine, c.blue, me = true)
        ).sortedWith(compareBy({ it.minutes }, { if (it.me) 1 else 0 }))
    }
    val rank = ranked.indexOfFirst { it.me } + 1

    ScreenColumn {
        LargeTitle("Liga")

        Column(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)).background(c.card).padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.EmojiEvents, null, tint = c.orange, modifier = Modifier.size(44.dp))
            Text("#$rank", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = c.label)
            Text("de ${ranked.size} esta semana", fontSize = 15.sp, color = c.secondary)
            Spacer(Modifier.size(6.dp))
            Text("Gana quien desbloquea menos minutos", fontSize = 13.sp, color = c.secondary)
        }

        Section(
            header = "Clasificación semanal",
            footer = "Ranking de ejemplo: las demás posiciones son datos de muestra. La liga en línea con amigos necesita un servidor y todavía no está conectada."
        ) {
            ranked.forEachIndexed { i, p ->
                ListRow(
                    title = if (p.me) "Tú" else p.name,
                    titleColor = if (p.me) c.blue else c.label,
                    leading = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}", fontSize = 15.sp, color = c.secondary, modifier = Modifier.width(22.dp))
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(p.color),
                                contentAlignment = Alignment.Center
                            ) { Text(p.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    },
                    trailing = { ValueText(fmtMinutes(p.minutes)) }
                )
                if (i < ranked.lastIndex) Hairline(start = 72.dp)
            }
        }
    }
}

// ------------------------------------------------------------------ Tienda
@Composable
fun ShopScreen(prefs: Prefs, onClose: () -> Unit) {
    val c = Ios.c
    var gems by remember { mutableIntStateOf(prefs.gems) }
    var owned by remember { mutableStateOf(prefs.owned) }
    var head by remember { mutableStateOf(prefs.equipped(Slot.HEAD)) }
    var face by remember { mutableStateOf(prefs.equipped(Slot.FACE)) }
    var neck by remember { mutableStateOf(prefs.equipped(Slot.NECK)) }
    var msg by remember { mutableStateOf<String?>(null) }

    fun current(slot: Slot) = when (slot) { Slot.HEAD -> head; Slot.FACE -> face; Slot.NECK -> neck }
    fun set(slot: Slot, id: String) {
        prefs.equip(slot, id)
        when (slot) { Slot.HEAD -> head = id; Slot.FACE -> face = id; Slot.NECK -> neck = id }
    }

    Box(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextAction("Cerrar", onClose)
                Box(Modifier.padding(end = 16.dp)) { Pill(Icons.Filled.Diamond, "$gems", c.purple) }
            }
            LargeTitle("Tienda de Brote")

            Box(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.card).padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) { Mascot(Mood.HAPPY, head, face, neck, 180.dp) }

            val m = msg
            if (m != null) Text(m, fontSize = 15.sp, color = c.red, modifier = Modifier.padding(horizontal = 20.dp))

            Slot.values().forEach { slot ->
                Section(
                    header = slot.label,
                    footer = if (slot == Slot.HEAD) "Ganas 10 gemas cada vez que desbloqueas una app resolviendo un minijuego." else null
                ) {
                    ListRow(
                        title = "Ninguno",
                        trailing = { if (current(slot).isEmpty()) Icon(Icons.Filled.Check, null, tint = c.blue, modifier = Modifier.size(22.dp)) },
                        onClick = { set(slot, ""); msg = null }
                    )
                    SHOP.filter { it.slot == slot }.forEach { item ->
                        Hairline()
                        val isOwned = item.id in owned
                        val isOn = current(slot) == item.id
                        ListRow(
                            title = item.name,
                            subtitle = if (isOwned) "Comprado" else null,
                            trailing = {
                                when {
                                    isOn -> Icon(Icons.Filled.Check, null, tint = c.blue, modifier = Modifier.size(22.dp))
                                    isOwned -> Text("Equipar", fontSize = 17.sp, color = c.blue)
                                    else -> Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Diamond, null, tint = c.purple, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${item.price}", fontSize = 17.sp, color = c.secondary)
                                    }
                                }
                            },
                            onClick = {
                                if (isOwned) {
                                    set(slot, if (isOn) "" else item.id); msg = null
                                } else if (gems >= item.price) {
                                    gems -= item.price; prefs.gems = gems
                                    owned = owned + item.id; prefs.owned = owned
                                    set(slot, item.id); msg = null
                                } else {
                                    msg = "Te faltan ${item.price - gems} gemas para ${item.name.lowercase()}."
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.size(12.dp))
        }
    }
}
