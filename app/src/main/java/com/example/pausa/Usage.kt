package com.example.pausa

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TopApp(val pkg: String, val label: String, val icon: ImageBitmap?, val ms: Long)

data class UsageSummary(
    val todayMs: Long,
    val pickups: Int,
    /** Últimos 7 días: etiqueta y milisegundos en pantalla (el último es hoy). */
    val week: List<Pair<String, Long>>,
    val top: List<TopApp>
)

fun fmtMs(ms: Long): String {
    val m = ms / 60_000
    return if (m >= 60) "${m / 60} h ${m % 60} min" else "$m min"
}

/** Estadísticas reales de uso del teléfono (requiere el permiso "Acceso a datos de uso"). */
object Usage {

    fun hasAccess(ctx: Context): Boolean {
        val ops = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun midnight(daysBack: Int): Long =
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun load(ctx: Context): UsageSummary? {
        if (!hasAccess(ctx)) return null
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = ctx.packageManager

        val launchable = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), PackageManager.MATCH_ALL
        ).map { it.activityInfo.packageName }.toSet()
        val home = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName
        val ignore = listOfNotNull(ctx.packageName, home)

        val dayLabel = SimpleDateFormat("E", Locale.getDefault())
        val now = System.currentTimeMillis()
        val week = ArrayList<Pair<String, Long>>()
        var todayPerApp: Map<String, Long> = emptyMap()

        for (back in 6 downTo 0) {
            val start = midnight(back)
            val end = if (back == 0) now else start + 24L * 3_600_000L
            val perApp = usm.queryAndAggregateUsageStats(start, end)
                .filter { (pkg, _) -> pkg in launchable && pkg !in ignore }
                .mapValues { it.value.totalTimeInForeground }
            week.add(dayLabel.format(Date(start)) to perApp.values.sum())
            if (back == 0) todayPerApp = perApp
        }

        // Veces que se encendió la pantalla hoy (evento SCREEN_INTERACTIVE = 15).
        var pickups = 0
        val events = usm.queryEvents(midnight(0), now)
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == 15) pickups++
        }

        val top = todayPerApp.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .take(5)
            .map { (pkg, ms) ->
                val label = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }
                    .getOrDefault(pkg)
                val icon = runCatching { pm.getApplicationIcon(pkg).toBitmap(96, 96).asImageBitmap() }.getOrNull()
                TopApp(pkg, label, icon, ms)
            }

        return UsageSummary(week.last().second, pickups, week, top)
    }
}

/** Carga las estadísticas fuera del hilo principal; se recarga cuando cambia [tick]. */
@Composable
fun rememberUsage(tick: Int): UsageSummary? {
    val ctx = LocalContext.current
    val state = produceState<UsageSummary?>(null, tick) {
        value = withContext(Dispatchers.Default) { runCatching { Usage.load(ctx) }.getOrNull() }
    }
    return state.value
}
