package com.example.pausa

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class GameKind(val title: String) {
    WORD("Palabra"),
    MATH("Cálculo rápido"),
    SEQUENCE("Secuencia"),
    REACTION("Reflejos"),
    NUMBER("Memoria de números"),
    CHIMP("Test del chimpancé"),
    COLOR("Color y palabra"),
    SUDOKU("Mini Sudoku"),
    CROWNS("Coronas")
}

/** Persistencia simple compartida por la app y el servicio. */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("pausa", Context.MODE_PRIVATE)

    init {
        if (!sp.contains("firstDay")) sp.edit().putString("firstDay", dayKey()).apply()
    }

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

    /** Última app detectada por el servicio (diagnóstico). */
    var lastSeen: String
        get() = sp.getString("lastSeen", "") ?: ""
        set(v) = sp.edit().putString("lastSeen", v).apply()

    /** Meta diaria: minutos máximos que quieres desbloquear al día. */
    var dailyGoal: Int
        get() = sp.getInt("goal", 30)
        set(v) = sp.edit().putInt("goal", v).apply()

    var gems: Int
        get() = sp.getInt("gems", 0)
        set(v) = sp.edit().putInt("gems", v).apply()

    var owned: Set<String>
        get() = sp.getStringSet("owned", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("owned", v).apply()

    fun equipped(slot: Slot): String = sp.getString("eq_${slot.name}", "") ?: ""
    fun equip(slot: Slot, id: String) = sp.edit().putString("eq_${slot.name}", id).apply()

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

    /** Últimos 7 días: etiqueta del día y minutos desbloqueados (el último es hoy). */
    fun lastWeek(): List<Pair<String, Int>> {
        val label = SimpleDateFormat("E", Locale.getDefault())
        return (6 downTo 0).map { back ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -back) }
            label.format(cal.time) to sp.getInt("min_${dayKey(cal.time)}", 0)
        }
    }

    fun weekMinutes(): Int = lastWeek().sumOf { it.second }

    fun weekUnlocks(): Int = (0..6).sumOf { back ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -back) }
        sp.getInt("unl_${dayKey(cal.time)}", 0)
    }

    /** Días seguidos (hasta hoy) en que no superaste tu meta diaria. */
    fun streak(): Int {
        val first = sp.getString("firstDay", dayKey()) ?: dayKey()
        val goal = dailyGoal
        val cal = Calendar.getInstance()
        var n = 0
        for (i in 0 until 365) {
            val k = dayKey(cal.time)
            if (k < first) break
            if (sp.getInt("min_$k", 0) > goal) break
            n++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return n
    }
}
