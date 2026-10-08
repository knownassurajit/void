package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidMotionScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "HomeSwipe"

/** The spring that moves Home stops once it is closer to rest than this, in pixels: half a pixel is not visible. */
private const val SETTLE_VISIBILITY_PX = 0.5f

/**
 * How far Home is out of its place because of a swipe, along [axis] (negative is left or up). It
 * is the pull of the finger that is opening something plus what is left of the spring that is
 * bringing Home back from the swipe before, so a new touch takes the motion up where it is instead
 * of snapping it to rest. Only the pointer loop of [homeSwipe] and that spring write it;
 * [homeSwipeOffset] reads it in the draw phase, so following the finger costs no recomposition.
 */
@Stable
internal class HomeSwipeState {
    private var pullPx: Float by mutableFloatStateOf(0f)
    private var springPx: Float by mutableFloatStateOf(0f)
    private var settling: Job? = null

    var axis: SwipeAxis by mutableStateOf(SwipeAxis.Horizontal)
        private set

    val offsetPx: Float get() = pullPx + springPx

    /** Whether a spring is still bringing Home back to rest. */
    val isSettling: Boolean get() = settling?.isActive == true

    /**
     * Home follows the finger: it is pulled [pullPx] on [axis] from where it is, so a spring that is
     * still running goes on under the finger. A spring on the other axis cannot be carried over to
     * this one: what is left of it is dropped.
     */
    fun follow(axis: SwipeAxis, pullPx: Float) {
        if (axis != this.axis) {
            stopSettling()
            springPx = 0f
            this.axis = axis
        }
        this.pullPx = pullPx
    }

    /**
     * Springs Home back to rest with [spec] starting at [velocity] px/s along the axis, from
     * wherever it is. Runs in [scope], which outlives the pointer loop, so the spring plays on
     * after the finger is up.
     */
    fun settle(scope: CoroutineScope, spec: AnimationSpec<Float>, velocity: Float) {
        stopSettling()
        val from = offsetPx
        pullPx = 0f
        springPx = from
        if (from == 0f) return
        settling = scope.launch {
            animate(initialValue = from, targetValue = 0f, initialVelocity = velocity, animationSpec = spec) { value, _ ->
                springPx = value
            }
        }
    }

    private fun stopSettling() {
        settling?.cancel()
        settling = null
    }
}

/**
 * Moves the content by what [state] says, so Home follows the finger. Put it on the content, not
 * on the box that watches the pointer ([homeSwipe]): the pointer positions must not move under the
 * finger. A navigation transition moves the screen from outside, so the two offsets add up.
 */
internal fun Modifier.homeSwipeOffset(state: HomeSwipeState): Modifier = graphicsLayer {
    if (state.axis == SwipeAxis.Horizontal) translationX = state.offsetPx else translationY = state.offsetPx
}

/**
 * The spring that brings Home back to rest after a swipe that opened [target]. A panel or the
 * notification list is brought in by the `NavHost` transition, which moves Home on spatial slow
 * in the same direction: the peek decays on that spring too, so the two add up to one smooth
 * movement (on a faster spring the peek would pull Home back against the finger for a moment). An
 * app has no Compose transition, so Home just settles quickly.
 */
internal fun commitSettleSpec(target: SwipeTarget, motion: VoidMotionScheme): SpringSpec<Float> =
    if (target is SwipeTarget.App) {
        motion.fastSpatialSpec(SETTLE_VISIBILITY_PX)
    } else {
        motion.slowSpatialSpec(SETTLE_VISIBILITY_PX)
    }

/**
 * Opens what a swipe on Home is bound to, with Home following the finger.
 *
 * The finger moves right to bring something in from the left, left for the right, up for the
 * bottom and down for the top. [targetFor] says what each direction opens. A drag toward a target
 * locks its axis early (see [HomeSwipeRecognizer]), consumes the pointer and pulls Home along with
 * a rubber band ([homeSwipeOffset]). A drag toward a direction without a target moves nothing and
 * opens nothing, but it is still consumed: the drag is Home's, so the home apps under the finger
 * see neither a tap when it lifts nor a long press when it waits. The swipe commits on release,
 * after [SwipeMath.shouldCommit]'s 56 dp of travel or a flick of 125 dp/s: [onCommit] runs the
 * target and says whether it could, a haptic cue confirms or rejects, and Home springs back to rest
 * (the panel transition that [onCommit] starts moves Home on top of that). Reversing the finger
 * under the distance, or letting go too early, cancels it and Home springs back with the speed of
 * the finger.
 *
 * Long presses belong to the children (reordering the apps, the app picker): a finger that stays
 * put for the long-press timeout is not a swipe, and neither is anything while [isReordering].
 * While [enabled] is false (Home is not the current destination, or a navigation transition is
 * still moving it) no swipe starts: a drag is swallowed like one toward a direction without a
 * target, so it does not click a home app either. A swipe that is following the finger when
 * [enabled] turns false stops and Home springs back.
 *
 * The pointer loop is keyed on [state] alone and reads everything else through the latest values,
 * so a screen change in the middle of a swipe never restarts it.
 */
@Composable
internal fun Modifier.homeSwipe(
    state: HomeSwipeState,
    enabled: Boolean,
    motion: VoidMotionScheme,
    isReordering: () -> Boolean,
    targetFor: (PanelEdge) -> SwipeTarget,
    onCommit: (SwipeTarget) -> Boolean
): Modifier {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val currentEnabled by rememberUpdatedState(enabled)
    val currentMotion by rememberUpdatedState(motion)
    val currentIsReordering by rememberUpdatedState(isReordering)
    val currentTargetFor by rememberUpdatedState(targetFor)
    val currentOnCommit by rememberUpdatedState(onCommit)
    val currentHaptic by rememberUpdatedState(haptic)
    return pointerInput(state) {
        val maxVelocity = Velocity(viewConfiguration.maximumFlingVelocity, viewConfiguration.maximumFlingVelocity)
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // A reorder drag is a child's: leave it alone.
            if (currentIsReordering()) return@awaitEachGesture
            val recognizer = HomeSwipeRecognizer(
                slopPx = viewConfiguration.touchSlop,
                density = density,
                longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis,
                targetFor = { edge -> if (currentEnabled) currentTargetFor(edge) else SwipeTarget.None }
            )
            // The velocity is that of the travel from the touch-down point, like the recognizer's distances.
            val velocityTracker = VelocityTracker()
            velocityTracker.addPosition(down.uptimeMillis, Offset.Zero)
            var following = false
            var released = false
            try {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (currentIsReordering() || (following && !currentEnabled)) break
                    val step = recognizer.onMove(
                        movementX = change.position.x - change.previousPosition.x,
                        movementY = change.position.y - change.previousPosition.y,
                        consumed = change.isConsumed,
                        elapsedMillis = change.uptimeMillis - down.uptimeMillis
                    )
                    velocityTracker.addPosition(change.uptimeMillis, Offset(recognizer.travelX, recognizer.travelY))
                    if (step.consumesPointer) change.consume()
                    when (step) {
                        HomeSwipeStep.GiveUp -> break
                        HomeSwipeStep.Undecided, HomeSwipeStep.Ignored -> Unit
                        is HomeSwipeStep.Tracking -> {
                            following = true
                            recognizer.axis?.let { state.follow(it, step.offsetPx) }
                            if (step.thresholdCrossed) {
                                currentHaptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            }
                        }
                    }
                    if (!change.pressed) {
                        if (following) {
                            released = true
                            val velocity = velocityTracker.calculateVelocity(maxVelocity)
                            if (recognizer.shouldCommit(velocity.x, velocity.y)) {
                                val ran = currentOnCommit(recognizer.target)
                                if (ran) {
                                    Log.d(TAG, "swipe_commit edge=${recognizer.edge}")
                                } else {
                                    Log.w(TAG, "swipe_action_failed edge=${recognizer.edge}")
                                }
                                currentHaptic.performHapticFeedback(
                                    if (ran) HapticFeedbackType.GestureEnd else HapticFeedbackType.Reject
                                )
                                state.settle(scope, commitSettleSpec(recognizer.target, currentMotion), velocity = 0f)
                            } else {
                                state.settle(
                                    scope,
                                    currentMotion.defaultSpatialSpec(SETTLE_VISIBILITY_PX),
                                    velocity = recognizer.releaseVelocity(velocity.x, velocity.y)
                                )
                            }
                        }
                        break
                    }
                }
            } finally {
                // Gave up, was cancelled by the system or by a child, or the composition went away
                // after Home had started to follow the finger: it must not stay out of place.
                if (following && !released) {
                    state.settle(scope, currentMotion.defaultSpatialSpec(SETTLE_VISIBILITY_PX), velocity = 0f)
                }
            }
        }
    }
}

/**
 * Two full-height strips along the left and right screen edges that ask the system to leave the
 * edge to the app, so a swipe that starts right at the edge reaches [homeSwipe] instead of being
 * taken by the system back gesture. A strip is only drawn for an edge a swipe from which opens
 * something ([left], [right]): the system back gesture on Home does nothing anyway, and the rest
 * of the time (a panel is open) the strips are gone, so back works as usual.
 *
 * It is a request, not a guarantee: the platform only allows 200 dp of exclusion height per edge,
 * except for the home activity, and an OEM may ignore it. A swipe that the system still takes
 * arrives cancelled and does nothing; swipes that start further in are unaffected. The width is
 * [GestureEdgeStrip.exclusionWidthPx].
 */
@Composable
internal fun BoxScope.HomeEdgeExclusionStrips(left: Boolean, right: Boolean) {
    if (!left && !right) return
    val density = LocalDensity.current
    val layoutDirection: LayoutDirection = LocalLayoutDirection.current
    val gestures = WindowInsets.systemGestures
    if (left) {
        val width = GestureEdgeStrip.exclusionWidthPx(gestures.getLeft(density, layoutDirection).toFloat(), density.density)
        Box(
            Modifier
                .align(AbsoluteAlignment.CenterLeft)
                .fillMaxHeight()
                .width(with(density) { width.toDp() })
                .systemGestureExclusion()
        )
    }
    if (right) {
        val width = GestureEdgeStrip.exclusionWidthPx(gestures.getRight(density, layoutDirection).toFloat(), density.density)
        Box(
            Modifier
                .align(AbsoluteAlignment.CenterRight)
                .fillMaxHeight()
                .width(with(density) { width.toDp() })
                .systemGestureExclusion()
        )
    }
}
