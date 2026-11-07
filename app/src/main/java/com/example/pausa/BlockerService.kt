package com.example.pausa

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

/**
 * Cuando una app bloqueada pasa a primer plano (y no hay minutos ganados),
 * abre BlockActivity encima. Los servicios de accesibilidad pueden iniciar
 * actividades desde segundo plano.
 */
class BlockerService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var recheck: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        blockIfNeeded(pkg)
    }

    private fun blockIfNeeded(pkg: String) {
        val prefs = Prefs(this)
        if (!prefs.enabled || pkg !in prefs.blocked) return
        if (System.currentTimeMillis() < prefs.unlockUntil) return
        startActivity(
            Intent(this, BlockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(BlockActivity.EXTRA_PACKAGE, pkg)
        )
    }

    /** Al acabarse los minutos, vuelve a bloquear si la app sigue abierta. */
    fun scheduleRecheck(delayMs: Long) {
        recheck?.let { handler.removeCallbacks(it) }
        val r = Runnable {
            val pkg = rootInActiveWindow?.packageName?.toString() ?: return@Runnable
            if (pkg != packageName) blockIfNeeded(pkg)
        }
        recheck = r
        handler.postDelayed(r, delayMs + 300)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        var instance: BlockerService? = null
            private set
    }
}
