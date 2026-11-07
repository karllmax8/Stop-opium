package com.example.pausa

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class GameKind(val title: String) {
    MATH("Cálculo rápido"),
    SEQUENCE("Secuencia"),
    REACTION("Reflejos")
}

/** Persistencia simple compartida por la app y el servicio. */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("pausa", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(v) = sp.edit().putBoolean("enabled", v).apply()

    var blocked: Set<String>
        get() = sp.getStringSet("blocked", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("blocked", v).apply()

    /** Momento (ms) hasta el que las apps bloqueadas están desbloqueadas. */
    var unlockUntil: Long
        get() = sp.getLong("unlockUntil", 0L)
        set(v) = sp.edit().putLong("unlockUntil", v).apply()

    var games: Set<GameKind>
        get() = sp.getStringSet("games", GameKind.values().map { it.name }.toSet())!!
            .mapNotNull { runCatching { GameKind.valueOf(it) }.getOrNull() }.toSet()
        set(v) = sp.edit().putStringSet("games", v.map { it.name }.toSet()).apply()

    private fun dayKey(date: Date = Date()) = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    fun recordUnlock(minutes: Int) {
        val k = dayKey()
        sp.edit()
            .putInt("min_$k", sp.getInt("min_$k", 0) + minutes)
            .putInt("unl_$k", sp.getInt("unl_$k", 0) + 1)
            .apply()
    }

    val unlocksToday: Int get() = sp.getInt("unl_${dayKey()}", 0)
    val minutesToday: Int get() = sp.getInt("min_${dayKey()}", 0)

    /** Últimos 7 días: etiqueta del día y minutos desbloqueados. */
    fun lastWeek(): List<Pair<String, Int>> {
        val label = SimpleDateFormat("E", Locale.getDefault())
        return (6 downTo 0).map { back ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -back) }
            label.format(cal.time) to sp.getInt("min_${dayKey(cal.time)}", 0)
        }
    }
}
