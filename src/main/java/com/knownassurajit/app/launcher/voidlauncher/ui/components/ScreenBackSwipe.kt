package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.AxisLock
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeMath
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalPanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge

/**
 * The way the finger moves to close a panel: along [axis], toward [sign] (`-1` is up or left,
 * `+1` is down or right). It is the physical direction and is not mirrored for right-to-left layouts.
 */
internal data class DismissDirection(val axis: SwipeAxis, val sign: Int) {
    /** [dx] or [dy], whichever lies on [axis], positive when it points the way that closes the panel. */
    fun along(dx: Float, dy: Float): Float = sign * if (axis == SwipeAxis.Horizontal) dx else dy
}

/**
 * The finger moves toward the edge the panel came from: left for a panel from the left edge, up for
 * one from the top, and so on. A panel with no edge ([PanelEdge.None], Settings) closes with a
 * swipe down, like the drawer.
 */
internal fun dismissDirection(edge: PanelEdge): DismissDirection = when (edge) {
    PanelEdge.Left -> DismissDirection(SwipeAxis.Horizontal, -1)
    PanelEdge.Right -> DismissDirection(SwipeAxis.Horizontal, 1)
    PanelEdge.Top -> DismissDirection(SwipeAxis.Vertical, -1)
    PanelEdge.Bottom, PanelEdge.None -> DismissDirection(SwipeAxis.Vertical, 1)
}

/** What [BackSwipeRecognizer] makes of the pointer so far. */
internal enum class BackSwipeStep {
    /** Not this panel's swipe (another axis, or a child or the system took the pointer): stop watching. */
    GiveUp,

    /** Too short to tell the axis yet. */
    Undecided,

    /** The axis is locked to the panel's own: this is the swipe. */
    Tracking
}

/**
 * The decisions of a back swipe, away from the pointer events so that they can be unit tested. It
 * is fed what the finger moved since the previous event ([onMove]) and adds it up from the touch-down
 * point, so the travel before the touch slop counts. On release it is given the velocity
 * ([shouldClose]).
 *
 * The movement must be the finger's own: the difference between two positions of the same event,
 * both read through the panel's current transform. The difference between a position now and the
 * position at touch-down would also contain the panel's own motion (a panel that is still sliding
 * in moves under a resting finger), and a tap on it would read as a swipe.
 */
internal class BackSwipeRecognizer(
    private val direction: DismissDirection,
    private val slopPx: Float,
    private val density: Float
) {
    private val axisLock = AxisLock(slopPx)

    /** How far the finger has moved from the touch-down point, horizontally and vertically, in pixels. */
    var travelX: Float = 0f
        private set
    var travelY: Float = 0f
        private set

    /**
     * [movementX] and [movementY] are what the finger moved since the previous event. [consumed] is
     * true when a child took the pointer, or when the system cancelled it (a cancel arrives as a
     * consumed release). Either way the swipe is not this panel's.
     */
    fun onMove(movementX: Float, movementY: Float, consumed: Boolean): BackSwipeStep {
        travelX += movementX
        travelY += movementY
        if (consumed) return BackSwipeStep.GiveUp
        val axis = axisLock.update(travelX, travelY) ?: return BackSwipeStep.Undecided
        return if (axis == direction.axis) BackSwipeStep.Tracking else BackSwipeStep.GiveUp
    }

    /** Whether releasing now, with the finger moving at [velocityX], [velocityY] px/s, closes the panel. */
    fun shouldClose(velocityX: Float, velocityY: Float): Boolean =
        axisLock.axis == direction.axis &&
            SwipeMath.shouldCommit(
                direction.along(travelX, travelY),
                direction.along(velocityX, velocityY),
                slopPx,
                density
            )
}

/**
 * Gives screen readers a Dismiss action, the way to leave a panel without a gesture (TalkBack lists
 * it among the actions of the focused element).
 */
fun Modifier.dismissAction(onDismiss: () -> Unit): Modifier = semantics {
    dismiss {
        onDismiss()
        true
    }
}

/**
 * Closes a panel with a swipe toward the edge it came from, and gives screen readers a Dismiss
 * action that does the same ([dismissAction]). Apply it to the root of the panel.
 *
 * The swipe is the one of Material 3 `SwipeToDismissBox`, evaluated on release: it commits after
 * 56 dp of travel or a flick of 125 dp/s (see [SwipeMath.shouldCommit]); anything shorter does
 * nothing, and a flick back at that speed cancels. The axis is locked as soon as the drag is long enough to tell (see [AxisLock]) and a
 * drag along the other axis is left alone. A child that consumes the drag first wins, so a list
 * scrolls and a swipe-to-dismiss row slides without closing the panel; only the regions that
 * consume nothing start the swipe. Android's own back gesture, the Back key and TalkBack's
 * back do not depend on this modifier.
 *
 * [enabled] is the "Swipe to go back" setting.
 */
@Composable
fun Modifier.screenBackSwipe(
    onBack: () -> Unit,
    enabled: Boolean = LocalContentSwipeToBack.current,
    edge: PanelEdge = LocalPanelEdge.current
): Modifier {
    val currentOnBack by rememberUpdatedState(onBack)
    val swipe = if (enabled) {
        Modifier.pointerInput(edge) { detectBackSwipe(dismissDirection(edge)) { currentOnBack() } }
    } else {
        Modifier
    }
    return dismissAction(onBack).then(swipe)
}

/**
 * Watches one pointer from touch-down to release and calls [onBack] when it was a swipe that closes
 * the panel. The distance is measured from the touch-down point, so the travel before the touch
 * slop counts, but it is the sum of what the finger moved in each event (see
 * [BackSwipeRecognizer]), not a difference of positions, so a panel that moves under the finger
 * (while it slides in) adds nothing. `positionChange()` is not used because it is zero for a
 * consumed change. Gives up without any effect when a child consumes the pointer or the system
 * cancels it, which is what happens to a swipe that starts in the strip Android claims for its own
 * back gesture; that gesture then plays the predictive back animation.
 */
private suspend fun PointerInputScope.detectBackSwipe(direction: DismissDirection, onBack: () -> Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val recognizer = BackSwipeRecognizer(direction, viewConfiguration.touchSlop, density)
        // The velocity is that of the travel, not of the position, so the panel's own motion is not in it.
        val velocityTracker = VelocityTracker()
        velocityTracker.addPosition(down.uptimeMillis, Offset.Zero)
        while (true) {
            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
            val step = recognizer.onMove(
                movementX = change.position.x - change.previousPosition.x,
                movementY = change.position.y - change.previousPosition.y,
                consumed = change.isConsumed
            )
            velocityTracker.addPosition(change.uptimeMillis, Offset(recognizer.travelX, recognizer.travelY))
            when (step) {
                BackSwipeStep.GiveUp -> return@awaitEachGesture
                BackSwipeStep.Tracking -> change.consume()
                BackSwipeStep.Undecided -> Unit
            }
            if (!change.pressed) {
                val velocity = velocityTracker.calculateVelocity()
                if (recognizer.shouldClose(velocity.x, velocity.y)) onBack()
                return@awaitEachGesture
            }
        }
    }
}
