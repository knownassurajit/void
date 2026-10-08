package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import androidx.compose.animation.core.spring
import androidx.compose.runtime.MonotonicFrameClock
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidMotion
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidMotionScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.resume

class HomeSwipeStateTest {

    /** A frame clock that plays a frame only when the test says so, so a spring runs step by step. */
    private class ManualFrameClock : MonotonicFrameClock {
        private var nanos = 0L
        private var pending: ((Long) -> Unit)? = null

        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R =
            suspendCancellableCoroutine { continuation ->
                pending = { time -> continuation.resume(onFrame(time)) }
                continuation.invokeOnCancellation { pending = null }
            }

        fun tick(millis: Long = 16L) {
            nanos += millis * 1_000_000L
            val frame = pending ?: return
            pending = null
            frame(nanos)
        }
    }

    private val clock = ManualFrameClock()

    // Unconfined, so the spring starts and resumes right where the test calls it.
    private val scope = CoroutineScope(Dispatchers.Unconfined + clock)
    private val state = HomeSwipeState()
    private val spring = spring<Float>(dampingRatio = 0.9f, stiffness = 700f, visibilityThreshold = 0.5f)

    private fun playUntilAtRest(maxFrames: Int = 500) {
        var frames = 0
        while (state.isSettling && frames++ < maxFrames) clock.tick()
    }

    // ---- following ----

    @Test
    fun atFirst_homeIsAtRest() {
        assertEquals(0f, state.offsetPx, 0f)
        assertFalse(state.isSettling)
    }

    @Test
    fun following_putsHomeOffRestOnTheAxisOfTheSwipe() {
        state.follow(SwipeAxis.Vertical, 12f)
        assertEquals(12f, state.offsetPx, 0f)
        assertEquals(SwipeAxis.Vertical, state.axis)
        state.follow(SwipeAxis.Vertical, -7f)
        assertEquals(-7f, state.offsetPx, 0f)
    }

    // ---- settling ----

    @Test
    fun settling_springsHomeBackToExactlyRest() {
        state.follow(SwipeAxis.Horizontal, -40f)
        state.settle(scope, spring, velocity = 0f)
        assertTrue(state.isSettling)
        clock.tick()
        clock.tick()
        assertTrue("on its way", state.offsetPx < 0f && state.offsetPx > -40f)
        playUntilAtRest()
        assertFalse(state.isSettling)
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(SwipeAxis.Horizontal, state.axis)
    }

    @Test
    fun settlingFromRest_startsNothing() {
        state.settle(scope, spring, velocity = 500f)
        assertFalse(state.isSettling)
        assertEquals(0f, state.offsetPx, 0f)
    }

    @Test
    fun settling_startsFromWhereHomeIsWithNoJump() {
        state.follow(SwipeAxis.Horizontal, 25f)
        state.settle(scope, spring, velocity = 0f)
        // The first frame only sets the start time: Home has not moved yet.
        clock.tick()
        assertEquals(25f, state.offsetPx, 0.001f)
    }

    // ---- a new touch while Home springs back ----

    @Test
    fun aNewTouchOnTheSameAxis_picksTheSpringUpWhereItIs() {
        state.follow(SwipeAxis.Horizontal, -40f)
        state.settle(scope, spring, velocity = 0f)
        repeat(6) { clock.tick() }
        val left = state.offsetPx
        assertTrue("still on its way back", left < -1f && left > -40f)

        // The new swipe locks its axis: a pull of 0 must not snap what is left of the spring.
        state.follow(SwipeAxis.Horizontal, 0f)
        assertEquals(left, state.offsetPx, 0f)
        assertTrue("the spring goes on under the finger", state.isSettling)
        clock.tick()
        assertTrue(state.offsetPx > left)
        assertTrue(state.offsetPx < 0f)

        // The finger then pulls: its pull adds to what is left of the spring.
        val springOnly = state.offsetPx
        state.follow(SwipeAxis.Horizontal, -10f)
        assertEquals(springOnly - 10f, state.offsetPx, 0f)
    }

    @Test
    fun releasingAfterANewTouch_settlesFromTheSumOfThePullAndWhatIsLeft() {
        state.follow(SwipeAxis.Horizontal, -40f)
        state.settle(scope, spring, velocity = 0f)
        repeat(6) { clock.tick() }
        state.follow(SwipeAxis.Horizontal, 0f)
        state.follow(SwipeAxis.Horizontal, -10f)
        val total = state.offsetPx

        state.settle(scope, spring, velocity = 0f)
        assertEquals(total, state.offsetPx, 0f)
        playUntilAtRest()
        assertEquals(0f, state.offsetPx, 0f)
    }

    @Test
    fun aNewTouchOnTheOtherAxis_dropsWhatIsLeftOfTheSpringOfTheFirst() {
        state.follow(SwipeAxis.Horizontal, -40f)
        state.settle(scope, spring, velocity = 0f)
        repeat(6) { clock.tick() }

        state.follow(SwipeAxis.Vertical, 0f)
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(SwipeAxis.Vertical, state.axis)
        assertFalse(state.isSettling)
        // The spring that was cancelled must not write the old axis' value into the new one.
        clock.tick()
        assertEquals(0f, state.offsetPx, 0f)
    }

    // ---- the spring a committed swipe settles on ----

    private val standard = VoidMotionScheme(Prefs.AnimationSpeed.STANDARD)

    @Test
    fun aPanelCommit_settlesOnTheSpringTheNavHostMovesHomeOn() {
        val panel = commitSettleSpec(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Left), standard)
        assertEquals(VoidMotion.spatialSlowStiffness, panel.stiffness, 0f)
        assertEquals(VoidMotion.spatialSlowDamping, panel.dampingRatio, 0f)
        val notifications = commitSettleSpec(SwipeTarget.Notifications, standard)
        assertEquals(VoidMotion.spatialSlowStiffness, notifications.stiffness, 0f)
    }

    @Test
    fun anAppCommit_settlesFast_becauseNoComposeTransitionMovesHome() {
        val app = commitSettleSpec(SwipeTarget.App("an.app", PanelEdge.Left), standard)
        assertEquals(VoidMotion.spatialFastStiffness, app.stiffness, 0f)
        assertEquals(VoidMotion.spatialFastDamping, app.dampingRatio, 0f)
    }

    @Test
    fun theCommitSpring_followsTheAnimationSpeedSetting() {
        val slow = VoidMotionScheme(Prefs.AnimationSpeed.SLOW)
        val panel = commitSettleSpec(SwipeTarget.Panel(NavRouteKind.Widgets, PanelEdge.Right), slow)
        assertEquals(VoidMotion.stiffnessFor(Prefs.AnimationSpeed.SLOW, VoidMotion.spatialSlowStiffness), panel.stiffness, 0.001f)
    }
}
