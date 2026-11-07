package com.example.pausa

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Pulsación estilo iOS: sin ondas, el elemento se atenúa mientras lo tocas. */
@Composable
fun Modifier.pressable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    return this
        .graphicsLayer { alpha = if (pressed) 0.55f else 1f }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

@Composable
fun LargeTitle(text: String) {
    Text(
        text, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Ios.c.label,
        modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp)
    )
}

/** Grupo con esquinas redondeadas, como las listas agrupadas de Ajustes en iOS. */
@Composable
fun Section(
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        if (header != null) {
            Text(
                header.uppercase(), fontSize = 13.sp, color = Ios.c.secondary,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Ios.c.card),
            content = content
        )
        if (footer != null) {
            Text(
                footer, fontSize = 13.sp, color = Ios.c.secondary,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun Hairline(start: Dp = 16.dp) {
    Box(Modifier.fillMaxWidth().padding(start = start).height(0.5.dp).background(Ios.c.separator))
}

@Composable
fun IconTile(icon: ImageVector, color: Color) {
    Box(Modifier.size(30.dp).clip(RoundedCornerShape(7.dp)).background(color), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun Chevron() {
    Icon(Icons.Filled.ChevronRight, null, tint = Ios.c.secondary.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
}

@Composable
fun ListRow(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    titleColor: Color = Ios.c.label
) {
    val base = Modifier.fillMaxWidth()
    val m = if (onClick != null) base.pressable(onClick = onClick) else base
    Row(
        m.padding(horizontal = 16.dp, vertical = 11.dp).heightIn(min = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, color = titleColor)
            if (subtitle != null) Text(subtitle, fontSize = 13.sp, color = Ios.c.secondary)
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun ValueText(text: String) {
    Text(text, fontSize = 17.sp, color = Ios.c.secondary)
}

@Composable
fun IosSwitch(checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    val knobX by animateDpAsState(if (checked) 22.dp else 2.dp, label = "knob")
    val track by animateColorAsState(if (checked) Ios.c.green else Ios.c.fill, label = "track")
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .size(width = 51.dp, height = 31.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .clip(RoundedCornerShape(50))
            .background(track)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChange(!checked)
            }
    ) {
        Box(
            Modifier.offset(x = knobX, y = 2.dp).size(27.dp)
                .shadow(2.dp, CircleShape).background(Color.White, CircleShape)
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ios.c.blue,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier.fillMaxWidth().height(52.dp)
            .pressable(enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(14.dp)).background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
fun TextAction(text: String, onClick: () -> Unit) {
    Text(
        text, fontSize = 17.sp, color = Ios.c.blue,
        modifier = Modifier.pressable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

/** Barra de pestañas inferior estilo iOS. */
@Composable
fun IosTabBar(items: List<Pair<ImageVector, String>>, selected: Int, onSelect: (Int) -> Unit) {
    val c = Ios.c
    val haptic = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().background(c.card.copy(alpha = 0.95f)).navigationBarsPadding()) {
        Hairline(0.dp)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp)) {
            items.forEachIndexed { i, (icon, label) ->
                val tint = if (i == selected) c.blue else c.secondary
                Column(
                    Modifier.weight(1f).pressable {
                        if (i != selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(i)
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
                    Text(label, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
