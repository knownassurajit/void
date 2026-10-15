package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigationevent.NavigationEvent
import kotlin.math.roundToInt

/**
 * The numbers of the Android predictive-back animation for a screen that goes back to another
 * screen of the same app, from the predictive-back design guidance on developer.android.com:
 *
 * - the leaving screen shrinks from 100% to 90% and moves sideways by at most `W / 20 - 8 dp`, the
 *   way the finger moves, and is gone after 35% of the gesture;
 * - the screen under it grows from 110% to 100% and fades in from 35% of the gesture on;
 * - everything eases with the cubic Bezier (0.1, 0.1, 0, 1).
 *
 * `NavHost` seeks these transitions by time, not by value (the gesture progress becomes a fraction
 * of the total duration), so every part must share one duration and be a tween, not a spring.
 * Committing the gesture continues from the current fraction.
 */
object PredictiveBackMath {
    /** Scale of the leaving screen at the end of the gesture. */
    const val exitScale = 0.9f

    /** Scale of the screen under it at the start of the gesture. */
    const val enterScale = 1.1f

    /** Share of the gesture in which the leaving screen fades out and after which the other one fades in. */
    const val fadeThroughFraction = 0.35f

    /** Duration at the standard animation speed, in milliseconds. */
    const val baseDurationMillis = 300

    /**
     * Half of the width the leaving screen gives up by shrinking to [exitScale], as a fraction of the
     * screen width (`W / 20`): the shift that would put its edge against the screen edge.
     */
    const val shiftWidthFraction = 0.05f

    val easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

    /**
     * Which way the leaving screen moves for a swipe that started at [swipeEdge] (a
     * `NavigationEvent` edge): `+1` (right) for the left edge, `-1` (left) for the right edge, `0`
     * when there is no edge. Do not count on `0` for the Back key: in the Android 14 and 16
     * platform sources a Back key press starts the same predictive back as a swipe and reports the
     * left edge (Android 16 sends `EDGE_NONE` only where its `predictiveBackSwipeEdgeNone` flag is
     * on), so the leaving screen shifts right whatever edge it came from. It is the physical
     * direction and is not mirrored for right-to-left layouts.
     */
    fun shiftSign(swipeEdge: Int): Int = when (swipeEdge) {
        NavigationEvent.EDGE_LEFT -> 1
        NavigationEvent.EDGE_RIGHT -> -1
        else -> 0
    }

    /** The largest shift in pixels: `W / 20` minus the edge margin, never negative on a very narrow screen. */
    fun maxShiftPx(widthPx: Int, edgeMarginPx: Float): Int =
        (widthPx * shiftWidthFraction - edgeMarginPx).coerceAtLeast(0f).roundToInt()

    /** The shift of the leaving screen at the end of the gesture, in pixels, positive to the right. */
    fun shiftPx(swipeEdge: Int, widthPx: Int, edgeMarginPx: Float): Int =
        shiftSign(swipeEdge) * maxShiftPx(widthPx, edgeMarginPx)

    /** [shiftPx] as the offset of a screen of [fullSize]: sideways only, never up or down. */
    fun shiftOffset(swipeEdge: Int, fullSize: IntSize, edgeMarginPx: Float): IntOffset =
        IntOffset(shiftPx(swipeEdge, fullSize.width, edgeMarginPx), 0)

    /** Duration at the animation-speed [speedFactor] (see `VoidMotion.speedFactor`), in milliseconds. */
    fun durationMillis(speedFactor: Float): Int = (baseDurationMillis * speedFactor).roundToInt()

    /** How long the leaving screen takes to fade out: the first [fadeThroughFraction] of [durationMillis]. */
    fun fadeOutMillis(durationMillis: Int): Int = (durationMillis * fadeThroughFraction).roundToInt()

    /** When the screen under it starts to fade in: where the fade-out ends. */
    fun fadeInDelayMillis(durationMillis: Int): Int = fadeOutMillis(durationMillis)

    /** How long the screen under it takes to fade in: the rest of [durationMillis] after [fadeInDelayMillis]. */
    fun fadeInMillis(durationMillis: Int): Int = durationMillis - fadeInDelayMillis(durationMillis)
}
