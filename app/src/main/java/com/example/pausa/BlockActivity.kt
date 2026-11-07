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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                    onClose = { goHome() },
                    onUnlocked = { minutes ->
                        if (!practice) {
                            prefs.unlockUntil = System.currentTimeMillis() + minutes * 60_000L
                            prefs.recordUnlock(minutes)
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
private fun GameFlow(prefs: Prefs, onClose: () -> Unit, onUnlocked: (Int) -> Unit) {
    val game = remember { prefs.games.ifEmpty { GameKind.values().toSet() }.random() }
    var won by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text("Mejor no") }
            }
            if (won) {
                SpinScreen(onFinish = onUnlocked)
            } else {
                key(round) {
                    val lose: () -> Unit = { round++ }
                    when (game) {
                        GameKind.MATH -> MathGame({ won = true }, lose)
                        GameKind.SEQUENCE -> SequenceGame({ won = true }, lose)
                        GameKind.REACTION -> ReactionGame({ won = true }, lose)
                    }
                }
            }
        }
    }
}
