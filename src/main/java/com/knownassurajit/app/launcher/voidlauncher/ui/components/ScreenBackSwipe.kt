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
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalPanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import kotlin.math.abs

/**
 * The finger moves toward the edge the panel came from: left for a panel from the left edge, up for
 * one from the top, and so on. A panel with no edge ([PanelEdge.None]) closes with a swipe down.
 */
internal fun isBackSwipe(edge: PanelEdge, dx: Float, dy: Float, threshold: Float): Boolean {
    val absX = abs(dx)
    val absY = abs(dy)
    return when (edge) {
        PanelEdge.Left -> dx < -threshold && absX > absY
        PanelEdge.Right -> dx > threshold && absX > absY
        PanelEdge.Top -> dy < -threshold && absY > absX
        PanelEdge.Bottom, PanelEdge.None -> dy > threshold && absY > absX
    }
}

/**
 * A panel from the left edge competes with horizontal row swipes; [conflictSafe] closes it with a
 * swipe up instead.
 */
internal fun backSwipeEdge(edge: PanelEdge, conflictSafe: Boolean): PanelEdge =
    if (conflictSafe && edge == PanelEdge.Left) PanelEdge.Top else edge

@Composable
fun Modifier.screenBackSwipe(
    onBack: () -> Unit,
    enabled: Boolean = LocalContentSwipeToBack.current,
    edge: PanelEdge = LocalPanelEdge.current,
    conflictSafe: Boolean = false
): Modifier {
    val density = LocalDensity.current
    val effective = backSwipeEdge(edge, conflictSafe)
    return this.pointerInput(enabled, effective, onBack) {
        if (!enabled) return@pointerInput
        val systemEdgePx = with(density) { 24.dp.toPx() }
        val threshold = 120f
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val atSystemEdge = down.position.x < systemEdgePx || down.position.x > size.width - systemEdgePx
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
            if (isBackSwipe(effective, total.x, total.y, threshold)) onBack()
        }
    }
}

@Composable
fun ChildScreenBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
