package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigationevent.NavigationEvent
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictiveBackMathTest {

    // 8 dp on a 2.625x display.
    private val margin = 21f

    @Test
    fun shiftSign_followsTheFingerFromTheEdgeTheSwipeStartedAt() {
        assertEquals(1, PredictiveBackMath.shiftSign(NavigationEvent.EDGE_LEFT))
        assertEquals(-1, PredictiveBackMath.shiftSign(NavigationEvent.EDGE_RIGHT))
    }

    @Test
    fun shiftSign_isZeroWithoutAnEdge() {
        assertEquals(0, PredictiveBackMath.shiftSign(NavigationEvent.EDGE_NONE))
        assertEquals(0, PredictiveBackMath.shiftSign(-1))
        assertEquals(0, PredictiveBackMath.shiftSign(99))
    }

    @Test
    fun edgeConstants_areTheValuesTheSystemSends() {
        // The edge arrives as a plain Int from NavHost; these are the values of the three constants.
        assertEquals(0, NavigationEvent.EDGE_LEFT)
        assertEquals(1, NavigationEvent.EDGE_RIGHT)
        assertEquals(2, NavigationEvent.EDGE_NONE)
    }

    @Test
    fun maxShift_isAScreenWidthOver20MinusTheEdgeMargin() {
        // 1080 px / 20 = 54 px, minus 21 px.
        assertEquals(33, PredictiveBackMath.maxShiftPx(1080, margin))
        assertEquals(1000 / 20 - 8, PredictiveBackMath.maxShiftPx(1000, 8f))
    }

    @Test
    fun maxShift_isNeverNegative() {
        // A 300 px wide window: 15 px of room, a 21 px margin.
        assertEquals(0, PredictiveBackMath.maxShiftPx(300, margin))
        assertEquals(0, PredictiveBackMath.maxShiftPx(0, margin))
    }

    @Test
    fun maxShift_reachesExactlyTheMarginWhenTheScreenShrinksToNinetyPercent() {
        // The leaving screen is 0.9 W wide, so it has 0.05 W of room on each side.
        val width = 1080
        val room = (width - width * PredictiveBackMath.exitScale) / 2f
        assertEquals(room, width * PredictiveBackMath.shiftWidthFraction, 0.01f)
    }

    @Test
    fun shiftPx_carriesTheSignOfTheEdge() {
        assertEquals(33, PredictiveBackMath.shiftPx(NavigationEvent.EDGE_LEFT, 1080, margin))
        assertEquals(-33, PredictiveBackMath.shiftPx(NavigationEvent.EDGE_RIGHT, 1080, margin))
        assertEquals(0, PredictiveBackMath.shiftPx(NavigationEvent.EDGE_NONE, 1080, margin))
    }

    @Test
    fun shiftOffset_isSidewaysOnlyOnTheScreenSize() {
        val size = IntSize(1080, 2400)
        assertEquals(IntOffset(33, 0), PredictiveBackMath.shiftOffset(NavigationEvent.EDGE_LEFT, size, margin))
        assertEquals(IntOffset(-33, 0), PredictiveBackMath.shiftOffset(NavigationEvent.EDGE_RIGHT, size, margin))
        assertEquals(IntOffset(0, 0), PredictiveBackMath.shiftOffset(NavigationEvent.EDGE_NONE, size, margin))
    }

    @Test
    fun shiftOffset_usesTheWidthNotTheHeight() {
        // 2400 px of height would give 99 px; the width of 1080 px gives 33.
        assertEquals(33, PredictiveBackMath.shiftOffset(NavigationEvent.EDGE_LEFT, IntSize(1080, 2400), margin).x)
        assertEquals(0, PredictiveBackMath.shiftOffset(NavigationEvent.EDGE_LEFT, IntSize(1080, 2400), margin).y)
        // A wide window: 2400 px of width.
        assertEquals(99, PredictiveBackMath.shiftOffset(NavigationEvent.EDGE_LEFT, IntSize(2400, 1080), margin).x)
    }

    @Test
    fun scales_areTheGuidanceNumbers() {
        assertEquals(0.9f, PredictiveBackMath.exitScale, 0f)
        assertEquals(1.1f, PredictiveBackMath.enterScale, 0f)
    }

    @Test
    fun duration_isThreeHundredMillisecondsTimesTheSpeedFactor() {
        assertEquals(180, PredictiveBackMath.durationMillis(VoidMotion.speedFactor(Prefs.AnimationSpeed.FAST)))
        assertEquals(300, PredictiveBackMath.durationMillis(VoidMotion.speedFactor(Prefs.AnimationSpeed.STANDARD)))
        assertEquals(480, PredictiveBackMath.durationMillis(VoidMotion.speedFactor(Prefs.AnimationSpeed.SLOW)))
    }

    @Test
    fun fade_leavingScreenIsGoneAtThirtyFivePercent() {
        assertEquals(105, PredictiveBackMath.fadeOutMillis(300))
        assertEquals(63, PredictiveBackMath.fadeOutMillis(180))
        assertEquals(168, PredictiveBackMath.fadeOutMillis(480))
    }

    @Test
    fun fade_screenUnderItFadesInFromWhereTheOtherOneEnds() {
        listOf(180, 300, 480).forEach { duration ->
            val delay = PredictiveBackMath.fadeInDelayMillis(duration)
            assertEquals("$duration", PredictiveBackMath.fadeOutMillis(duration), delay)
            // The delay and the fade-in together fill the whole duration, so every part of the
            // transition ends together and a seek by fraction lines them all up.
            assertEquals("$duration", duration, delay + PredictiveBackMath.fadeInMillis(duration))
        }
    }

    @Test
    fun easing_isTheGuidanceCurve() {
        // CubicBezierEasing compares by its control points.
        assertEquals(CubicBezierEasing(0.1f, 0.1f, 0f, 1f), PredictiveBackMath.easing)
        // Values of the curve (0.1, 0.1, 0, 1), to pin it beyond its end points.
        assertEquals(0.3717f, PredictiveBackMath.easing.transform(0.1f), 0.001f)
        assertEquals(0.8949f, PredictiveBackMath.easing.transform(0.5f), 0.001f)
    }

    @Test
    fun easing_startsAtZeroEndsAtOneAndLeavesQuickly() {
        val easing = PredictiveBackMath.easing
        assertEquals(0f, easing.transform(0f), 0.0001f)
        assertEquals(1f, easing.transform(1f), 0.0001f)
        // The curve (0.1, 0.1, 0, 1) is steep at first: a quarter of the time covers more than a quarter of the way.
        assertTrue(easing.transform(0.25f) > 0.25f)
    }
}
