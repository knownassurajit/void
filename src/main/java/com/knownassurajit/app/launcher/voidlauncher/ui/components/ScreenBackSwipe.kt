package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalNavEnterAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavAxis
import kotlin.math.abs

@Composable
fun Modifier.screenBackSwipe(
    onBack: () -> Unit,
    enabled: Boolean = LocalContentSwipeToBack.current,
    axis: NavAxis = LocalNavEnterAxis.current,
    conflictSafe: Boolean = false
): Modifier {
    val density = LocalDensity.current
    val effective = if (conflictSafe && axis == NavAxis.Start) NavAxis.Down else axis
    return this.pointerInput(enabled, effective, onBack) {
        if (!enabled) return@pointerInput
        val edge = with(density) { 24.dp.toPx() }
        val threshold = 120f
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val atSystemEdge = down.position.x < edge || down.position.x > size.width - edge
            val slop = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                change.consume()
            } ?: return@awaitEachGesture
            var total = slop.positionChange()
            drag(slop.id) { change ->
                total += change.positionChange()
                change.consume()
            }
            val absX = abs(total.x)
            val absY = abs(total.y)
            if (atSystemEdge && absX > absY) return@awaitEachGesture
            val matched = when (effective) {
                NavAxis.Up -> total.y > threshold && absY > absX
                NavAxis.Down -> total.y < -threshold && absY > absX
                NavAxis.Start -> total.x < -threshold && absX > absY
                NavAxis.End -> total.x > threshold && absX > absY
                NavAxis.Fade -> total.y > threshold && absY > absX
            }
            if (matched) onBack()
        }
    }
}

@Composable
fun ChildScreenBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
