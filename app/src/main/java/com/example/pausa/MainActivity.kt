package com.example.pausa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { a11y = isServiceEnabled(ctx); tick++ }

    val tabs = remember {
        listOf(
            Icons.Filled.Home to "Inicio",
            Icons.Filled.Lock to "Apps",
            Icons.Filled.BarChart to "Progreso",
            Icons.Filled.Settings to "Ajustes"
        )
    }

    Scaffold(
        containerColor = Ios.c.bg,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = { IosTabBar(tabs, tab) { tab = it } }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(prefs, a11y, tick)
                1 -> AppsScreen(prefs)
                2 -> ProgressScreen(prefs, tick)
                else -> SettingsScreen(prefs, a11y)
            }
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        content()
        Spacer(Modifier.height(12.dp))
    }
}

// ---------------------------------------------------------------- Inicio
@Composable
private fun HomeScreen(prefs: Prefs, a11y: Boolean, tick: Int) {
    val ctx = LocalContext.current
    val c = Ios.c
    var enabled by remember { mutableStateOf(prefs.enabled) }
    val blockedCount = prefs.blocked.size
    val canEnable = blockedCount > 0

    ScreenColumn {
        LargeTitle("Pausa")

        // Tarjeta de estado
        Column(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)).background(c.card).padding(vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(84.dp).clip(CircleShape).background(if (enabled) c.blue else c.fill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (enabled) Icons.Filled.Lock else Icons.Filled.LockOpen, null,
                    tint = if (enabled) Color.White else c.secondary, modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (enabled) "Bloqueo activado" else "Bloqueo desactivado",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.label
            )
            Text(
                if (blockedCount == 1) "1 app protegida" else "$blockedCount apps protegidas",
                fontSize = 15.sp, color = c.secondary
            )
        }

        if (!a11y) {
            Section(
                header = "Permiso necesario",
                footer = "Pausa usa accesibilidad solo para saber qué app se abre. No lee tu pantalla ni envía datos."
            ) {
                ListRow(
                    title = "Activar accesibilidad",
                    leading = { IconTile(Icons.Filled.Warning, c.orange) },
                    trailing = { Chevron() },
                    onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                )
            }
        }

        Section(
            footer = if (canEnable) "Al abrir una app bloqueada tendrás que resolver un minijuego."
            else "Primero elige las apps a bloquear en la pestaña Apps."
        ) {
            ListRow(
                title = "Bloqueo de apps",
                leading = { IconTile(Icons.Filled.Lock, c.blue) },
                trailing = {
                    IosSwitch(checked = enabled && canEnable, enabled = canEnable) {
                        enabled = it; prefs.enabled = it
                    }
                }
            )
        }

        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("${prefs.unlocksToday}", "desbloqueos hoy", Modifier.weight(1f))
            StatTile("${prefs.minutesToday}", "min ganados hoy", Modifier.weight(1f))
        }

        Section {
            ListRow(
                title = "Probar un minijuego",
                leading = { IconTile(Icons.Filled.SportsEsports, c.green) },
                trailing = { Chevron() },
                onClick = { ctx.startActivity(Intent(ctx, BlockActivity::class.java)) }
            )
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(Ios.c.card).padding(16.dp)) {
        Text(value, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Ios.c.label)
        Text(label, fontSize = 13.sp, color = Ios.c.secondary)
    }
}

// ---------------------------------------------------------------- Apps
private data class AppItem(val pkg: String, val label: String, val icon: ImageBitmap?)

@Composable
private fun AppsScreen(prefs: Prefs) {
    val ctx = LocalContext.current
    val c = Ios.c
    var apps by remember { mutableStateOf<List<AppItem>?>(null) }
    var query by remember { mutableStateOf("") }
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
                    val icon = runCatching { pm.getApplicationIcon(pkg).toBitmap(96, 96).asImageBitmap() }.getOrNull()
                    AppItem(pkg, label, icon)
                }
        }
    }

    ScreenColumn {
        Column {
            LargeTitle("Apps")
            Text(
                when (selected.size) { 0 -> "Ninguna seleccionada"; 1 -> "1 seleccionada"; else -> "${selected.size} seleccionadas" },
                fontSize = 15.sp, color = c.secondary, modifier = Modifier.padding(start = 20.dp)
            )
        }

        // Campo de búsqueda
        Row(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(c.fill).padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, null, tint = c.secondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) Text("Buscar", fontSize = 17.sp, color = c.secondary)
                BasicTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    textStyle = TextStyle(color = c.label, fontSize = 17.sp),
                    cursorBrush = SolidColor(c.blue), modifier = Modifier.fillMaxWidth()
                )
            }
        }

        val list = apps
        if (list == null) {
            Text("Cargando apps…", color = c.secondary, modifier = Modifier.padding(horizontal = 20.dp))
        } else {
            val shown = list.filter { it.label.contains(query.trim(), ignoreCase = true) }
            Section {
                shown.forEachIndexed { i, app ->
                    val checked = app.pkg in selected
                    ListRow(
                        title = app.label,
                        leading = {
                            if (app.icon != null) {
                                Image(app.icon, null, Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
                            } else Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(c.fill))
                        },
                        trailing = {
                            Icon(
                                if (checked) Icons.Filled.CheckCircle else Icons.Outlined.Circle, null,
                                tint = if (checked) c.blue else c.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        onClick = {
                            if (checked) selected.remove(app.pkg) else selected.add(app.pkg)
                            prefs.blocked = selected.toSet()
                        }
                    )
                    if (i < shown.lastIndex) Hairline(start = 64.dp)
                }
                if (shown.isEmpty()) ListRow(title = "Sin resultados", titleColor = c.secondary)
            }
        }
    }
}

// ---------------------------------------------------------------- Progreso
@Composable
private fun ProgressScreen(prefs: Prefs, tick: Int) {
    val c = Ios.c
    val week = remember(tick) { prefs.lastWeek() }
    val total = week.sumOf { it.second }
    val max = (week.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)

    ScreenColumn {
        LargeTitle("Progreso")

        Section(
            header = "Esta semana",
            footer = "Menos minutos desbloqueados significa más tiempo recuperado."
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("$total min", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.label)
                Text("desbloqueados · promedio ${total / 7} min al día", fontSize = 15.sp, color = c.secondary)
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth().height(170.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEachIndexed { i, (day, minutes) ->
                        val today = i == week.lastIndex
                        Column(
                            Modifier.fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text("$minutes", fontSize = 11.sp, color = c.secondary)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.size(width = 30.dp, height = (110.dp * minutes / max).coerceAtLeast(6.dp))
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (today) c.blue else c.blue.copy(alpha = 0.35f))
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                day, fontSize = 12.sp,
                                color = if (today) c.label else c.secondary,
                                fontWeight = if (today) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Section(header = "Hoy") {
            ListRow(title = "Desbloqueos", trailing = { ValueText("${prefs.unlocksToday}") })
            Hairline()
            ListRow(title = "Minutos ganados", trailing = { ValueText("${prefs.minutesToday}") })
        }
    }
}

// ---------------------------------------------------------------- Ajustes
@Composable
private fun SettingsScreen(prefs: Prefs, a11y: Boolean) {
    val ctx = LocalContext.current
    val c = Ios.c
    var games by remember { mutableStateOf(prefs.games) }

    val meta = mapOf(
        GameKind.MATH to (Icons.Filled.Calculate to c.blue),
        GameKind.SEQUENCE to (Icons.Filled.GridView to c.purple),
        GameKind.REACTION to (Icons.Filled.Bolt to c.orange)
    )

    ScreenColumn {
        LargeTitle("Ajustes")

        Section(
            header = "Minijuegos para desbloquear",
            footer = "Se elige uno al azar entre los activados. Debe haber al menos uno."
        ) {
            GameKind.values().forEachIndexed { i, g ->
                val on = g in games
                val (icon, color) = meta.getValue(g)
                ListRow(
                    title = g.title,
                    leading = { IconTile(icon, color) },
                    trailing = { if (on) Icon(Icons.Filled.Check, null, tint = c.blue, modifier = Modifier.size(22.dp)) },
                    onClick = {
                        val new = if (on) games - g else games + g
                        if (new.isNotEmpty()) { games = new; prefs.games = new }
                    }
                )
                if (i < GameKind.values().lastIndex) Hairline(start = 58.dp)
            }
        }

        Section(
            header = "Privacidad",
            footer = "Pausa solo detecta qué app se abre. No lee tu pantalla ni envía datos fuera del teléfono."
        ) {
            ListRow(
                title = "Servicio de accesibilidad",
                trailing = {
                    ValueText(if (a11y) "Activado" else "Desactivado")
                    Spacer(Modifier.width(4.dp))
                    Chevron()
                },
                onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            )
        }

        Section(header = "Acerca de") {
            ListRow(title = "Versión", trailing = { ValueText("1.0") })
        }
    }
}
