package com.example.pausa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/** Pantalla de pausa: minijuego -> ruleta -> minutos ganados. */
class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = Prefs(this)
        val practice = intent.getStringExtra(EXTRA_PACKAGE) == null

        setContent {
            PausaTheme {
                BackHandler { goHome() }
                GameFlow(
                    prefs = prefs,
                    practice = practice,
                    onClose = { if (practice) finish() else goHome() },
                    onUnlocked = { minutes ->
                        if (!practice) {
                            prefs.unlockUntil = System.currentTimeMillis() + minutes * 60_000L
                            prefs.recordUnlock(minutes)
                            prefs.gems += 10   // recompensa para la tienda de Brote
                            BlockerService.instance?.scheduleRecheck(minutes * 60_000L)
                        }
                        finish() // vuelve a la app que querías abrir
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE = "pkg"
    }
}

@Composable
private fun GameFlow(prefs: Prefs, practice: Boolean, onClose: () -> Unit, onUnlocked: (Int) -> Unit) {
    val game = remember { prefs.games.ifEmpty { GameKind.values().toSet() }.random() }
    var won by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize().background(Ios.c.bg).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth()) {
                TextAction(if (practice) "Cerrar" else "Mejor no", onClose)
            }
            if (won) {
                SpinScreen(onFinish = onUnlocked)
            } else {
                key(round) {
                    val lose: () -> Unit = { round++ }
                    when (game) {
                        GameKind.WORD -> WordGame({ won = true }, lose)
                        GameKind.MATH -> MathGame({ won = true }, lose)
                        GameKind.SEQUENCE -> SequenceGame({ won = true }, lose)
                        GameKind.REACTION -> ReactionGame({ won = true }, lose)
                    }
                }
            }
        }
    }
}
