package com.example.pausa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.ui.text.style.TextAlign
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
fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        content()
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun Root(prefs: Prefs) {
    val ctx = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var showShop by remember { mutableStateOf(false) }
    var a11y by remember { mutableStateOf(isServiceEnabled(ctx)) }
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { a11y = isServiceEnabled(ctx); tick++ }

    val tabs = remember {
        listOf(
            Icons.Filled.Home to "Inicio",
            Icons.Filled.Lock to "Apps",
            Icons.Filled.EmojiEvents to "Liga",
            Icons.Filled.BarChart to "Progreso",
            Icons.Filled.Settings to "Ajustes"
        )
    }

    if (showShop) {
        BackHandler { showShop = false; tick++ }
        ShopScreen(prefs) { showShop = false; tick++ }
    } else {
        Scaffold(
            containerColor = Ios.c.bg,
            contentWindowInsets = WindowInsets.statusBars,
            bottomBar = { IosTabBar(tabs, tab) { tab = it } }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (tab) {
                    0 -> HomeScreen(prefs, a11y, tick) { showShop = true }
                    1 -> AppsScreen(prefs)
                    2 -> LeagueScreen(prefs, tick)
                    3 -> ProgressScreen(prefs, tick)
                    else -> SettingsScreen(prefs, a11y, tick) { tick++ }
                }
            }
        }
    }
}

private fun openUsageAccess(ctx: Context) {
    ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

// ---------------------------------------------------------------- Inicio
@Composable
private fun HomeScreen(prefs: Prefs, a11y: Boolean, tick: Int, openShop: () -> Unit) {
    val ctx = LocalContext.current
    val c = Ios.c
    var enabled by remember { mutableStateOf(prefs.enabled) }
    val blockedCount = prefs.blocked.size
    val canEnable = blockedCount > 0

    val usage = rememberUsage(tick)
    val access = remember(tick) { Usage.hasAccess(ctx) }
    val goal = prefs.dailyGoal
    val minutes = remember(tick) { prefs.minutesToday }
    val mood = moodFor(minutes, goal)
    val head = remember(tick) { prefs.equipped(Slot.HEAD) }
    val face = remember(tick) { prefs.equipped(Slot.FACE) }
    val neck = remember(tick) { prefs.equipped(Slot.NECK) }
    val streak = remember(tick) { prefs.streak() }
    val gems = remember(tick) { prefs.gems }

    val moodText = when (mood) {
        Mood.HAPPY -> "¡Brote está feliz! Vas muy bien hoy."
        Mood.OK -> "Brote está tranquilo. Cuida tus minutos."
        Mood.WORRIED -> "Brote está preocupado: casi llegas a tu meta."
        Mood.SAD -> "Brote está triste: superaste tu meta de hoy."
    }

    ScreenColumn {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pausa", Modifier.weight(1f), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.label)
            Pill(Icons.Filled.Whatshot, "$streak", c.orange)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.pressable(onClick = openShop)) { Pill(Icons.Filled.Diamond, "$gems", c.purple) }
        }

        Column(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)).background(c.card).padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Mascot(mood, head, face, neck, 190.dp)
            Spacer(Modifier.height(6.dp))
            Text(
                moodText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = c.label,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text("Hoy: $minutes de $goal min desbloqueados", fontSize = 13.sp, color = c.secondary)
        }

        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat(usage?.let { fmtMs(it.todayMs) } ?: "—", "en pantalla hoy", Modifier.weight(1f))
            MiniStat(usage?.pickups?.toString() ?: "—", "veces que lo tomaste", Modifier.weight(1f))
            MiniStat("${prefs.unlocksToday}", "desbloqueos", Modifier.weight(1f))
        }

        if (!access) {
            Section(
                header = "Estadísticas",
                footer = "Para ver el tiempo en pantalla y las veces que tomas el móvil, Android pide que concedas el acceso a datos de uso. Se queda en tu teléfono."
            ) {
                ListRow(
                    title = "Activar estadísticas de uso",
                    leading = { IconTile(Icons.Filled.BarChart, c.indigo) },
                    trailing = { Chevron() },
                    onClick = { openUsageAccess(ctx) }
                )
            }
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
                subtitle = if (blockedCount == 1) "1 app protegida" else "$blockedCount apps protegidas",
                leading = { IconTile(if (enabled) Icons.Filled.Lock else Icons.Filled.LockOpen, c.blue) },
                trailing = {
                    IosSwitch(checked = enabled && canEnable, enabled = canEnable) {
                        enabled = it; prefs.enabled = it
                    }
                }
            )
        }

        Section {
            ListRow(
                title = "Tienda de Brote",
                subtitle = "Gasta tus gemas en accesorios",
                leading = { IconTile(Icons.Filled.Diamond, c.purple) },
                trailing = { Chevron() },
                onClick = openShop
            )
            Hairline(start = 58.dp)
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
private fun MiniStat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Ios.c.card).padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ios.c.label, textAlign = TextAlign.Center)
        Text(label, fontSize = 11.sp, color = Ios.c.secondary, textAlign = TextAlign.Center)
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
private fun BarChart(data: List<Pair<String, Long>>, fmt: (Long) -> String) {
    val c = Ios.c
    val max = (data.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(1L)
    Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        data.forEachIndexed { i, (day, v) ->
            val today = i == data.lastIndex
            Column(
                Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(fmt(v), fontSize = 11.sp, color = c.secondary)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.size(width = 30.dp, height = (110.dp * (v.toFloat() / max.toFloat())).coerceAtLeast(6.dp))
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

@Composable
private fun ProgressScreen(prefs: Prefs, tick: Int) {
    val ctx = LocalContext.current
    val c = Ios.c
    val usage = rememberUsage(tick)
    val access = remember(tick) { Usage.hasAccess(ctx) }
    val week = remember(tick) { prefs.lastWeek() }
    val total = week.sumOf { it.second }

    ScreenColumn {
        LargeTitle("Progreso")

        if (!access) {
            Section(
                header = "Tiempo en pantalla",
                footer = "Concede el acceso a datos de uso para ver cuánto usas el móvil y qué apps te quitan más tiempo."
            ) {
                ListRow(
                    title = "Activar estadísticas de uso",
                    leading = { IconTile(Icons.Filled.BarChart, c.indigo) },
                    trailing = { Chevron() },
                    onClick = { openUsageAccess(ctx) }
                )
            }
        } else {
            usage?.let { u ->
                val avg = u.week.map { it.second }.average().toLong()
                Section(header = "Tiempo en pantalla · 7 días") {
                    Column(Modifier.padding(16.dp)) {
                        Text(fmtMs(u.todayMs), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.label)
                        Text("hoy · promedio ${fmtMs(avg)} al día", fontSize = 15.sp, color = c.secondary)
                        Spacer(Modifier.height(18.dp))
                        BarChart(u.week.map { it.first to it.second / 60_000L }) { v ->
                            if (v >= 60) "${v / 60}h" else "${v}m"
                        }
                    }
                }
                if (u.top.isNotEmpty()) {
                    Section(header = "Lo que más usas hoy") {
                        u.top.forEachIndexed { i, app ->
                            ListRow(
                                title = app.label,
                                leading = {
                                    if (app.icon != null) {
                                        Image(app.icon, null, Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
                                    } else Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(c.fill))
                                },
                                trailing = { ValueText(fmtMs(app.ms)) }
                            )
                            if (i < u.top.lastIndex) Hairline(start = 64.dp)
                        }
                    }
                }
            }
        }

        Section(
            header = "Minutos desbloqueados · 7 días",
            footer = "Menos minutos desbloqueados significa más tiempo recuperado."
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("$total min", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.label)
                Text("${prefs.weekUnlocks()} desbloqueos esta semana", fontSize = 15.sp, color = c.secondary)
                Spacer(Modifier.height(18.dp))
                BarChart(week.map { it.first to it.second.toLong() }) { v -> "$v" }
            }
        }
    }
}

// ---------------------------------------------------------------- Ajustes
@Composable
private fun SettingsScreen(prefs: Prefs, a11y: Boolean, tick: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val c = Ios.c
    var games by remember { mutableStateOf(prefs.games) }
    var goal by remember { mutableIntStateOf(prefs.dailyGoal) }
    val access = remember(tick) { Usage.hasAccess(ctx) }

    val meta = mapOf(
        GameKind.WORD to (Icons.Filled.Edit to c.green),
        GameKind.MATH to (Icons.Filled.Calculate to c.blue),
        GameKind.SEQUENCE to (Icons.Filled.GridView to c.purple),
        GameKind.REACTION to (Icons.Filled.Bolt to c.orange)
    )

    fun setGoal(v: Int) {
        goal = v.coerceIn(5, 240)
        prefs.dailyGoal = goal
        onChanged()
    }

    ScreenColumn {
        LargeTitle("Ajustes")

        Section(
            header = "Meta diaria",
            footer = "Brote está contento mientras desbloquees menos que tu meta, y tu racha sube cada día que la cumples."
        ) {
            ListRow(
                title = "Minutos máximos al día",
                trailing = {
                    Text(
                        "−", fontSize = 30.sp, color = c.blue,
                        modifier = Modifier.pressable { setGoal(goal - 5) }.padding(horizontal = 12.dp)
                    )
                    Text("$goal", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = c.label)
                    Text(
                        "+", fontSize = 30.sp, color = c.blue,
                        modifier = Modifier.pressable { setGoal(goal + 5) }.padding(horizontal = 12.dp)
                    )
                }
            )
        }

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
            header = "Permisos y privacidad",
            footer = "Todo se queda en tu teléfono. La accesibilidad solo sirve para saber qué app se abre; los datos de uso, para mostrar tus estadísticas."
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
            Hairline()
            ListRow(
                title = "Acceso a datos de uso",
                trailing = {
                    ValueText(if (access) "Activado" else "Desactivado")
                    Spacer(Modifier.width(4.dp))
                    Chevron()
                },
                onClick = { openUsageAccess(ctx) }
            )
        }

        Section(header = "Acerca de") {
            ListRow(title = "Versión", trailing = { ValueText("1.1") })
        }
    }
}
