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
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeMath
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalNavEnterAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
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

data class DismissDirection(val axis: SwipeAxis, val sign: Int) {
    fun along(dx: Float, dy: Float): Float = if (axis == SwipeAxis.Horizontal) dx * sign else dy * sign
}

fun dismissDirection(edge: PanelEdge): DismissDirection = when (edge) {
    PanelEdge.Left -> DismissDirection(SwipeAxis.Horizontal, -1)
    PanelEdge.Right -> DismissDirection(SwipeAxis.Horizontal, 1)
    PanelEdge.Top -> DismissDirection(SwipeAxis.Vertical, -1)
    PanelEdge.Bottom, PanelEdge.None -> DismissDirection(SwipeAxis.Vertical, 1)
}

enum class BackSwipeStep {
    Undecided,
    Tracking,
    GiveUp
}

class BackSwipeRecognizer(
    val direction: DismissDirection,
    val slop: Float,
    val density: Float
) {
    var travelX: Float = 0f
        private set
    var travelY: Float = 0f
        private set

    private var state: BackSwipeStep = BackSwipeStep.Undecided

    fun onMove(dx: Float, dy: Float, consumed: Boolean): BackSwipeStep {
        if (consumed || state == BackSwipeStep.GiveUp) {
            state = BackSwipeStep.GiveUp
            return state
        }
        travelX += dx
        travelY += dy
        val absX = abs(travelX)
        val absY = abs(travelY)
        val lockThresholdPx = slop * 1.5f
        if (state == BackSwipeStep.Undecided) {
            if (absX < lockThresholdPx && absY < lockThresholdPx) {
                return BackSwipeStep.Undecided
            }
            val alongAxis = if (direction.axis == SwipeAxis.Horizontal) absX > absY else absY > absX
            state = if (alongAxis) BackSwipeStep.Tracking else BackSwipeStep.GiveUp
        }
        return state
    }

    fun shouldClose(velocityX: Float, velocityY: Float): Boolean {
        if (state != BackSwipeStep.Tracking) return false
        val travel = direction.along(travelX, travelY)
        val velocity = direction.along(velocityX, velocityY)
        return SwipeMath.shouldCommit(travel, velocity, slop, density)
    }
}
