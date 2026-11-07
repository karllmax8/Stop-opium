package com.example.pausa

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Detecta qué app pasa a primer plano. Si está bloqueada y no hay minutos ganados,
 * dibuja un aviso encima (ventana de accesibilidad, no necesita permisos extra) con
 * el botón "Jugar para desbloquear".
 *
 * Se usa un aviso superpuesto y no startActivity() directo porque muchos fabricantes
 * (Xiaomi/HyperOS, Oppo, Vivo…) bloquean abrir pantallas desde segundo plano.
 * El juego solo se abre cuando el usuario toca el botón (ventana visible = permitido).
 */
class BlockerService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { Prefs(this) }
    private var recheck: Runnable? = null
    private var overlay: View? = null
    private var overlayPkg: String? = null
    private var currentPkg: String? = null
    private var lastSeen = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || isNoise(pkg)) return

        currentPkg = pkg
        if (pkg != lastSeen) {
            lastSeen = pkg
            prefs.lastSeen = pkg   // diagnóstico: se muestra en Ajustes
        }
        // Si el usuario se fue a otra app (o al inicio), quitamos el aviso.
        if (overlay != null && pkg != overlayPkg) hideOverlay()
        blockIfNeeded(pkg)
    }

    private fun isNoise(pkg: String) =
        pkg == "com.android.systemui" || pkg == "android" || pkg.contains("inputmethod")

    private fun blockIfNeeded(pkg: String) {
        if (!prefs.enabled || pkg !in prefs.blocked) return
        if (System.currentTimeMillis() < prefs.unlockUntil) return
        if (overlay != null) return
        showOverlay(pkg)
    }

    private fun showOverlay(pkg: String) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val d = resources.displayMetrics.density
        fun px(v: Int) = (v * d).toInt()
        fun rounded(color: Int) = GradientDrawable().apply {
            setColor(color); cornerRadius = 14 * d
        }

        val appName = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        }.getOrDefault("Esta app")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F2101420"))
            setPadding(px(28), px(28), px(28), px(28))
        }
        root.addView(TextView(this).apply {
            text = "Haz una pausa"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "$appName está bloqueada.\nResuelve un minijuego para ganar minutos."
            textSize = 16f
            setTextColor(Color.parseColor("#B3FFFFFF"))
            gravity = Gravity.CENTER
            setPadding(0, px(12), 0, px(8))
        })
        root.addView(Button(this).apply {
            text = "Jugar para desbloquear"
            isAllCaps = false
            textSize = 17f
            setTextColor(Color.WHITE)
            background = rounded(Color.parseColor("#0A84FF"))
            setOnClickListener {
                hideOverlay()
                startActivity(
                    Intent(this@BlockerService, BlockActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra(BlockActivity.EXTRA_PACKAGE, pkg)
                )
            }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px(54)).apply { topMargin = px(20) })
        root.addView(Button(this).apply {
            text = "Mejor no"
            isAllCaps = false
            textSize = 17f
            setTextColor(Color.WHITE)
            background = rounded(Color.parseColor("#33FFFFFF"))
            setOnClickListener {
                hideOverlay()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px(54)).apply { topMargin = px(12) })

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        try {
            wm.addView(root, lp)
            overlay = root
            overlayPkg = pkg
        } catch (e: Exception) {
            // Último recurso: intentar abrir el juego directamente.
            startActivity(
                Intent(this, BlockActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(BlockActivity.EXTRA_PACKAGE, pkg)
            )
        }
    }

    private fun hideOverlay() {
        overlay?.let { v ->
            runCatching { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(v) }
        }
        overlay = null
        overlayPkg = null
    }

    /** Al acabarse los minutos, vuelve a bloquear si la app sigue abierta. */
    fun scheduleRecheck(delayMs: Long) {
        recheck?.let { handler.removeCallbacks(it) }
        val r = Runnable {
            val pkg = rootInActiveWindow?.packageName?.toString() ?: currentPkg ?: return@Runnable
            if (pkg != packageName && !isNoise(pkg)) blockIfNeeded(pkg)
        }
        recheck = r
        handler.postDelayed(r, delayMs + 300)
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        hideOverlay()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        hideOverlay()
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        var instance: BlockerService? = null
            private set
    }
}
