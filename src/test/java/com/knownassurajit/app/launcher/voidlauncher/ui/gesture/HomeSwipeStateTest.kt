package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import androidx.compose.animation.core.spring
import androidx.compose.runtime.MonotonicFrameClock
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
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

    // ---- resetting: whatever the swipe left behind is undone at once ----

    @Test
    fun resettingWhileTheFingerIsDown_putsHomeBackAtRestWithoutAnimation() {
        // Cancel, navigation in the middle of a drag, resume: the finger's pull is dropped.
        state.follow(SwipeAxis.Horizontal, -40f)
        assertTrue("it was out of place", state.reset())
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(1f, state.alpha, 0f)
        assertFalse(state.isSettling)
        assertFalse(state.isDeparted)
    }

    @Test
    fun resettingHomeThatIsAtRest_saysNothingWasOutOfPlace() {
        assertFalse(state.reset())
        state.follow(SwipeAxis.Vertical, 10f)
        state.settle(scope, spring, velocity = 0f)
        playUntilAtRest()
        assertFalse("a spring that has finished leaves nothing behind", state.reset())
    }

    @Test
    fun resettingWhileASpringRuns_stopsItForGood() {
        state.follow(SwipeAxis.Horizontal, -40f)
        state.settle(scope, spring, velocity = 0f)
        repeat(3) { clock.tick() }
        assertTrue(state.isSettling)

        assertTrue(state.reset())
        assertFalse(state.isSettling)
        assertEquals(0f, state.offsetPx, 0f)
        // The cancelled spring must not write another value into the state.
        clock.tick()
        clock.tick()
        assertEquals(0f, state.offsetPx, 0f)
    }

    @Test
    fun aSwipeAfterAReset_startsFromRestWithNothingLeftOfTheOneBefore() {
        state.follow(SwipeAxis.Horizontal, -40f)
        state.settle(scope, spring, velocity = 0f)
        repeat(3) { clock.tick() }
        state.reset()

        state.follow(SwipeAxis.Horizontal, -10f)
        assertEquals(-10f, state.offsetPx, 0f)
        state.settle(scope, spring, velocity = 0f)
        playUntilAtRest()
        assertEquals(0f, state.offsetPx, 0f)
    }

    @Test
    fun anInterruptedLock_resetsAndTheNextSwipeLocksTheOtherAxisFromZero() {
        // The axis locked and Home followed, then the swipe was cut off (the screen changed under it).
        state.follow(SwipeAxis.Horizontal, 22f)
        state.reset()
        state.follow(SwipeAxis.Vertical, 8f)
        assertEquals(SwipeAxis.Vertical, state.axis)
        assertEquals(8f, state.offsetPx, 0f)
    }

    @Test
    fun navigationInTheMiddleOfADrag_endsWithHomeAlreadyResetAndNothingLeftToSettle() {
        // Navigation in the middle of a drag: the pointer loop stops and settles Home, but the
        // screen has been resumed (reset) in between, so there is nothing left to bring back.
        state.follow(SwipeAxis.Horizontal, -30f)
        state.reset()
        state.settle(scope, spring, velocity = 300f)
        assertFalse(state.isSettling)
        assertEquals(0f, state.offsetPx, 0f)
    }

    @Test
    fun aCancelledSwipe_springsBackToExactlyRestAndLeavesNoDeparture() {
        state.follow(SwipeAxis.Vertical, 30f)
        state.settle(scope, spring, velocity = -200f)
        playUntilAtRest()
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(1f, state.alpha, 0f)
        assertFalse(state.isDeparted)
    }

    // ---- departing for an app that is launching ----

    private val parked = 324f

    @Test
    fun departing_carriesHomeOnToTheParkedPlaceAndDimsItWithoutAJump() {
        state.follow(SwipeAxis.Horizontal, 40f)
        state.depart(scope, spring, toPx = parked, velocity = 0f)
        assertTrue(state.isDeparted)
        assertTrue(state.isSettling)
        // Not moved yet, so neither the offset nor the opacity jumps when the finger lets go.
        assertEquals(40f, state.offsetPx, 0f)
        assertEquals(1f, state.alpha, 0f)

        clock.tick()
        clock.tick()
        clock.tick()
        assertTrue("moving on, in the direction of the swipe", state.offsetPx > 40f && state.offsetPx < parked)
        assertTrue("dimming on the way", state.alpha < 1f && state.alpha > VoidAlpha.coveredByPanel)

        playUntilAtRest()
        assertEquals(parked, state.offsetPx, 0f)
        assertEquals(VoidAlpha.coveredByPanel, state.alpha, 0.0001f)
        assertTrue("it stays departed until it is reset", state.isDeparted)
    }

    @Test
    fun departingThePullOfALeftEdgeSwipeAndOfARightEdgeSwipe_goTheirOwnWay() {
        state.follow(SwipeAxis.Horizontal, -25f)
        state.depart(scope, spring, toPx = -parked, velocity = -500f)
        playUntilAtRest()
        assertEquals(-parked, state.offsetPx, 0f)
        assertEquals(VoidAlpha.coveredByPanel, state.alpha, 0.0001f)
    }

    @Test
    fun resettingADepartedHome_putsItBackAtRestOpaqueAndWithoutAnimation() {
        // VOID is resumed after the app: Home must be in its place before the first frame.
        state.follow(SwipeAxis.Horizontal, 40f)
        state.depart(scope, spring, toPx = parked, velocity = 0f)
        playUntilAtRest()

        assertTrue(state.reset())
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(1f, state.alpha, 0f)
        assertFalse(state.isDeparted)
        assertFalse(state.isSettling)
    }

    @Test
    fun resettingWhileHomeIsStillDeparting_cancelsTheSpring() {
        state.follow(SwipeAxis.Horizontal, 40f)
        state.depart(scope, spring, toPx = parked, velocity = 0f)
        repeat(3) { clock.tick() }
        state.reset()
        repeat(3) { clock.tick() }
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(1f, state.alpha, 0f)
        assertFalse(state.isDeparted)
    }

    @Test
    fun settlingADepartedHome_bringsItBackOpaqueAndNoLongerDeparted() {
        // The launch did not take VOID off the screen: Home springs back by itself.
        state.follow(SwipeAxis.Horizontal, 40f)
        state.depart(scope, spring, toPx = parked, velocity = 0f)
        playUntilAtRest()

        state.settle(scope, spring, velocity = 0f)
        assertTrue("still departed while it comes back", state.isDeparted)
        repeat(3) { clock.tick() }
        assertTrue(state.offsetPx < parked && state.offsetPx > 0f)
        assertTrue("lighter again on the way back", state.alpha > VoidAlpha.coveredByPanel)

        playUntilAtRest()
        assertEquals(0f, state.offsetPx, 0f)
        assertEquals(1f, state.alpha, 0f)
        assertFalse(state.isDeparted)
    }

    @Test
    fun aDepartureThatHasNowhereToGo_isStillDeparted_andResetClearsIt() {
        state.depart(scope, spring, toPx = 0f, velocity = 0f)
        assertTrue(state.isDeparted)
        assertTrue(state.reset())
        assertFalse(state.isDeparted)
    }

    // ---- what Home moves on after a commit ----

    private val standard = VoidMotionScheme(Prefs.AnimationSpeed.STANDARD)

    @Test
    fun aPanelOrTheNotificationList_handsOverOnTheSpringTheNavHostMovesHomeOn() {
        val handOff = afterCommitSpec(HomeAfterCommit.HandOff, standard)
        assertEquals(VoidMotion.spatialSlowStiffness, handOff.stiffness, 0f)
        assertEquals(VoidMotion.spatialSlowDamping, handOff.dampingRatio, 0f)
    }

    @Test
    fun anAppMovedByTheSystemWindowAnimation_settlesFast_becauseNoComposeTransitionMovesHome() {
        val quick = afterCommitSpec(HomeAfterCommit.SettleQuickly, standard)
        assertEquals(VoidMotion.spatialFastStiffness, quick.stiffness, 0f)
        assertEquals(VoidMotion.spatialFastDamping, quick.dampingRatio, 0f)
    }

    @Test
    fun anAppDeparture_andASpringBackAfterAFailure_useSpatialDefault() {
        listOf(HomeAfterCommit.Depart(PanelEdge.Left), HomeAfterCommit.SpringBack).forEach { exit ->
            val spec = afterCommitSpec(exit, standard)
            assertEquals("$exit", VoidMotion.spatialDefaultStiffness, spec.stiffness, 0f)
            assertEquals("$exit", VoidMotion.spatialDefaultDamping, spec.dampingRatio, 0f)
        }
    }

    @Test
    fun theCommitSpring_followsTheAnimationSpeedSetting() {
        val slow = VoidMotionScheme(Prefs.AnimationSpeed.SLOW)
        val handOff = afterCommitSpec(HomeAfterCommit.HandOff, slow)
        assertEquals(VoidMotion.stiffnessFor(Prefs.AnimationSpeed.SLOW, VoidMotion.spatialSlowStiffness), handOff.stiffness, 0.001f)
        val depart = afterCommitSpec(HomeAfterCommit.Depart(PanelEdge.Right), slow)
        assertEquals(VoidMotion.stiffnessFor(Prefs.AnimationSpeed.SLOW, VoidMotion.spatialDefaultStiffness), depart.stiffness, 0.001f)
    }
}
