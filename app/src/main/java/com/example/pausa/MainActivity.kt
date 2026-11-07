package com.example.pausa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PausaTheme { Root(Prefs(this)) } }
    }
}

private fun isServiceEnabled(ctx: Context): Boolean {
    val me = ComponentName(ctx, BlockerService::class.java).flattenToString()
    val list = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        ?: return false
    return list.split(':').any { it.equals(me, ignoreCase = true) }
}

@Composable
private fun Root(prefs: Prefs) {
    val ctx = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var a11y by remember { mutableStateOf(isServiceEnabled(ctx)) }
    var tick by remember { mutableIntStateOf(0) }   // fuerza refrescar estadísticas
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { a11y = isServiceEnabled(ctx); tick++ }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Bg) {
                listOf(
                    Icons.Filled.Home to "Inicio",
                    Icons.Filled.Lock to "Apps",
                    Icons.Filled.BarChart to "Progreso",
                    Icons.Filled.Settings to "Ajustes"
                ).forEachIndexed { i, (icon, label) ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Icon(icon, label) }, label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).statusBarsPadding().fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(prefs, a11y, tick)
                1 -> AppsScreen(prefs)
                2 -> ProgressScreen(prefs, tick)
                else -> SettingsScreen(prefs)
            }
        }
    }
}

@Composable
private fun cardModifier() = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Card)

@Composable
private fun HomeScreen(prefs: Prefs, a11y: Boolean, tick: Int) {
    val ctx = LocalContext.current
    var enabled by remember { mutableStateOf(prefs.enabled) }
    val canEnable = prefs.blocked.isNotEmpty()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            cardModifier().background(Indigo.copy(alpha = 0.15f)).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Lock, null, Modifier.size(54.dp), tint = if (enabled) Indigo else Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(if (enabled) "Bloqueo activo" else "Bloqueo apagado", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("${prefs.blocked.size} apps bloqueadas", color = Color.White.copy(alpha = 0.6f))
        }

        if (!a11y) {
            Column(cardModifier().background(Color(0x33FF9800)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Falta un permiso", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Activa el servicio de accesibilidad “Pausa” para poder detectar las apps bloqueadas. No lee tu pantalla ni guarda datos.", color = Color.White.copy(alpha = 0.8f))
                Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Abrir ajustes de accesibilidad") }
            }
        }

        Row(cardModifier().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Activar bloqueo", Modifier.weight(1f), color = Color.White, fontSize = 16.sp)
            Switch(checked = enabled, enabled = canEnable, onCheckedChange = { enabled = it; prefs.enabled = it })
        }
        if (!canEnable) Text("Primero elige apps en la pestaña Apps.", color = Color.White.copy(alpha = 0.6f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("${prefs.unlocksToday}", "desbloqueos hoy", Modifier.weight(1f))
            StatCard("${prefs.minutesToday}", "min ganados hoy", Modifier.weight(1f))
        }

        Button(
            onClick = { ctx.startActivity(Intent(ctx, BlockActivity::class.java)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo)
        ) { Text("Probar un minijuego", Modifier.padding(6.dp)) }
    }
}

@Composable
private fun StatCard(big: String, small: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Card).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(big, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(small, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

private data class AppItem(val pkg: String, val label: String, val icon: ImageBitmap?)

@Composable
private fun AppsScreen(prefs: Prefs) {
    val ctx = LocalContext.current
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    val selected = remember { prefs.blocked.toMutableStateList() }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) {
            val pm = ctx.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
                .distinctBy { it.first }
                .filter { it.first != ctx.packageName }
                .sortedBy { it.second.lowercase() }
                .map { (pkg, label) ->
                    val d: Drawable? = runCatching { pm.getApplicationIcon(pkg) }.getOrNull()
                    AppItem(pkg, label, d?.toBitmap(96, 96)?.asImageBitmap())
                }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Apps bloqueadas", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 16.dp))
        Text("${selected.size} seleccionadas", color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn {
            items(apps, key = { it.pkg }) { app ->
                val checked = app.pkg in selected
                Row(
                    Modifier.fillMaxWidth().clickable {
                        if (checked) selected.remove(app.pkg) else selected.add(app.pkg)
                        prefs.blocked = selected.toSet()
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    app.icon?.let { Image(it, null, Modifier.size(40.dp)) }
                    Text(app.label, Modifier.weight(1f).padding(horizontal = 12.dp), color = Color.White)
                    Checkbox(checked = checked, onCheckedChange = null)
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen(prefs: Prefs, tick: Int) {
    val week = remember(tick) { prefs.lastWeek() }
    val max = (week.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Progreso", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Minutos desbloqueados en los últimos 7 días", color = Color.White.copy(alpha = 0.6f))
        Row(
            cardModifier().height(240.dp).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            week.forEach { (day, minutes) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Text("$minutes", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    Box(
                        Modifier.size(width = 28.dp, height = (150.dp * minutes / max).coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(6.dp)).background(Indigo)
                    )
                    Text(day, fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Text("Menos minutos = más tiempo recuperado.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
private fun SettingsScreen(prefs: Prefs) {
    var games by remember { mutableStateOf(prefs.games) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ajustes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Minijuegos que pueden desbloquear", color = Color.White.copy(alpha = 0.6f))
        GameKind.values().forEach { g ->
            val on = g in games
            Row(
                cardModifier().clickable {
                    val new = if (on) games - g else games + g
                    if (new.isNotEmpty()) { games = new; prefs.games = new }
                }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(g.title, Modifier.weight(1f), color = Color.White)
                Checkbox(checked = on, onCheckedChange = null)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Pausa usa un servicio de accesibilidad solo para saber qué app se abre. No lee tu pantalla ni envía datos fuera del teléfono.",
            fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f)
        )
    }
}
