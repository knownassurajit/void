package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.center
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavTravel
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.rememberVoidMotionScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The pointer loop of [homeSwipe] and the follow-the-finger offset ([homeSwipeOffset]), on the real
 * Compose input pipeline: each direction opens the target of its edge, a short slow swipe and a
 * direction without a target do nothing, a quick flick commits, Home follows the finger while it is
 * down and springs back to rest when the swipe is cancelled, committed, rejected or switched off in
 * the middle, reversing under the distance cancels, and a drag that Home owns is not a click on the
 * child under the finger. The decisions themselves are covered by the JVM `HomeSwipeRecognizerTest`
 * and `HomeSwipeDecisionTest`, the state of the offset by `HomeSwipeStateTest`.
 *
 * Run it on an emulator (`./gradlew connectedDebugAndroidTest`), not on the physical device. The
 * touch slop is 8 dp, so the axis locks at 12 dp; a swipe commits at 56 dp or 125 dp/s. A press
 * that waits 400 ms is a long press, so the first move of a held swipe comes sooner than that.
 */
@RunWith(AndroidJUnit4::class)
class HomeSwipeInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private val state = HomeSwipeState()
    private val opened = mutableListOf<SwipeTarget>()
    private val enabled = mutableStateOf(true)
    private var commitResult = true
    private var clicks = 0

    private fun everyEdgeOpensANote(edge: PanelEdge): SwipeTarget = SwipeTarget.Panel(NavRouteKind.Notes, edge)

    private fun setHome(targetFor: (PanelEdge) -> SwipeTarget = ::everyEdgeOpensANote) {
        rule.setContent {
            val motion = rememberVoidMotionScheme(Prefs.AnimationSpeed.STANDARD)
            Box(
                modifier = Modifier
                    .testTag(HOME)
                    .size(300.dp)
                    .homeSwipe(
                        state = state,
                        gate = if (enabled.value) HomeSwipeGate.Open else HomeSwipeGate.Busy,
                        motion = motion,
                        isReordering = { false },
                        targetFor = targetFor,
                        onCommit = { target ->
                            opened += target
                            commitResult
                        }
                    )
            ) {
                // The tag comes after the offset, so its bounds include the translation of the layer.
                // The click is what a home app row has: a drag that Home owns must not reach it.
                Box(
                    Modifier
                        .fillMaxSize()
                        .homeSwipeOffset(state)
                        .testTag(CONTENT)
                        .clickable { clicks++ }
                )
            }
        }
    }

    private fun swipeBy(dx: Float, dy: Float, durationMillis: Long) {
        rule.onNodeWithTag(HOME).performTouchInput {
            swipe(
                start = center,
                end = center + Offset(dx.dp.toPx(), dy.dp.toPx()),
                durationMillis = durationMillis
            )
        }
    }

    /** Puts the finger down and drags it left by [firstDp] (which locks the axis) and then by [secondDp], slowly. */
    private fun holdLeft(firstDp: Float, secondDp: Float) {
        rule.onNodeWithTag(HOME).performTouchInput {
            down(center)
            moveBy(Offset(-firstDp.dp.toPx(), 0f), delayMillis = 100)
            moveBy(Offset(-secondDp.dp.toPx(), 0f), delayMillis = 300)
        }
        rule.waitForIdle()
    }

    /** Lets go after the finger has rested a moment, so the release carries no flick speed. */
    private fun lift() {
        rule.onNodeWithTag(HOME).performTouchInput {
            advanceEventTime(200)
            up()
        }
        rule.waitForIdle()
    }

    /** Where the content is, in pixels. */
    private fun contentLeft(): Float = rule.onNodeWithTag(CONTENT).fetchSemanticsNode().boundsInRoot.left

    private fun contentTop(): Float = rule.onNodeWithTag(CONTENT).fetchSemanticsNode().boundsInRoot.top

    private fun dpToPx(dp: Float): Float = with(rule.density) { dp.dp.toPx() }

    @Test
    fun aSlowSwipeLeftPast56dp_opensTheRightEdgeTarget() {
        setHome()
        swipeBy(dx = -80f, dy = 0f, durationMillis = 2_500)
        assertEquals(listOf<SwipeTarget>(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Right)), opened)
    }

    @Test
    fun aSlowSwipeRightPast56dp_opensTheLeftEdgeTarget() {
        setHome()
        swipeBy(dx = 80f, dy = 0f, durationMillis = 2_500)
        assertEquals(listOf<SwipeTarget>(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Left)), opened)
    }

    @Test
    fun aSlowSwipeUpPast56dp_opensTheBottomEdgeTarget() {
        setHome()
        swipeBy(dx = 0f, dy = -80f, durationMillis = 2_500)
        assertEquals(listOf<SwipeTarget>(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Bottom)), opened)
    }

    @Test
    fun aSlowSwipeDownPast56dp_opensTheTopEdgeTarget() {
        setHome()
        swipeBy(dx = 0f, dy = 80f, durationMillis = 2_500)
        assertEquals(listOf<SwipeTarget>(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Top)), opened)
    }

    @Test
    fun aShortFastFlick_commits() {
        setHome()
        // 40 dp in 100 ms is 400 dp/s: past the 125 dp/s flick limit and the 12 dp lock distance.
        swipeBy(dx = -40f, dy = 0f, durationMillis = 100)
        assertEquals(1, opened.size)
    }

    @Test
    fun homeFollowsTheFingerWhileItIsDown_andNothingOpensUntilItLetsGo() {
        setHome()
        val restLeft = contentLeft()
        val restTop = contentTop()
        holdLeft(firstDp = 14f, secondDp = 10f)
        // 10 dp past the 14 dp where the axis locked, on the rubber band (limit 64 dp).
        val expected = SwipeMath.rubberBand(dpToPx(10f), dpToPx(VoidDimens.swipePeekLimit.value))
        assertEquals("Home follows the finger to the left", restLeft - expected, contentLeft(), 1.5f)
        assertTrue(contentLeft() < restLeft - 1f)
        assertEquals("Home does not leave the axis", restTop, contentTop(), 0.5f)
        assertTrue(opened.isEmpty())
        lift()
    }

    @Test
    fun aShortSlowSwipe_doesNothingAndHomeSpringsBackToRest() {
        setHome()
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 14f)
        assertTrue("Home is out of place while the finger is down", contentLeft() < restLeft - 1f)
        lift()
        assertTrue(opened.isEmpty())
        assertEquals(restLeft, contentLeft(), 0.5f)
    }

    @Test
    fun reversingTheFingerBackUnderTheDistance_cancelsTheSwipeAndHomeSpringsBack() {
        setHome()
        val restLeft = contentLeft()
        rule.onNodeWithTag(HOME).performTouchInput {
            down(center)
            moveBy(Offset(-14.dp.toPx(), 0f), delayMillis = 100)
            moveBy(Offset(-86.dp.toPx(), 0f), delayMillis = 400)
            moveBy(Offset(60.dp.toPx(), 0f), delayMillis = 400)
        }
        rule.waitForIdle()
        lift()
        assertTrue(opened.isEmpty())
        assertEquals(restLeft, contentLeft(), 0.5f)
    }

    @Test
    fun aCommittedSwipe_runsTheTargetOnReleaseAndHomeSpringsBackWhenNothingElseMovesIt() {
        setHome()
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 50f)
        assertTrue(opened.isEmpty())
        assertTrue(contentLeft() < restLeft - 1f)
        lift()
        assertEquals(listOf<SwipeTarget>(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Right)), opened)
        assertEquals(restLeft, contentLeft(), 0.5f)
    }

    @Test
    fun aCommittedSwipeWhoseActionCannotRun_stillSpringsHomeBack() {
        setHome()
        commitResult = false
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 50f)
        lift()
        assertEquals(1, opened.size)
        assertEquals(restLeft, contentLeft(), 0.5f)
    }

    @Test
    fun aDirectionWithoutATarget_doesNothingAndHomeDoesNotMove() {
        setHome(targetFor = { edge -> if (edge == PanelEdge.Left) everyEdgeOpensANote(edge) else SwipeTarget.None })
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 46f)
        assertEquals(restLeft, contentLeft(), 0.5f)
        lift()
        assertTrue(opened.isEmpty())
    }

    @Test
    fun aDragTowardADirectionWithoutATarget_isNotAClickOnTheChildUnderTheFinger() {
        // Regression: the drag used to be dropped unconsumed, so lifting the finger inside a home
        // app row clicked the app.
        setHome(targetFor = { edge -> if (edge == PanelEdge.Left) everyEdgeOpensANote(edge) else SwipeTarget.None })
        swipeBy(dx = -60f, dy = 0f, durationMillis = 300)
        assertEquals(0, clicks)
        assertTrue(opened.isEmpty())
    }

    @Test
    fun aSwipeThatDoesNotCommit_isNotAClickOnTheChildUnderTheFinger() {
        setHome()
        // 50 dp/s, and past the touch slop well before the 400 ms long-press timeout.
        swipeBy(dx = -30f, dy = 0f, durationMillis = 600)
        assertEquals(0, clicks)
        assertTrue(opened.isEmpty())
    }

    @Test
    fun aPlainTap_stillClicksTheChild() {
        setHome()
        rule.onNodeWithTag(CONTENT).performClick()
        assertEquals(1, clicks)
        assertTrue(opened.isEmpty())
    }

    @Test
    fun withTheSwipeDisabled_nothingHappensAndHomeDoesNotMove() {
        enabled.value = false
        setHome()
        val restLeft = contentLeft()
        swipeBy(dx = -80f, dy = 0f, durationMillis = 300)
        assertTrue(opened.isEmpty())
        assertEquals(restLeft, contentLeft(), 0.5f)
    }

    @Test
    fun withTheSwipeDisabled_aDragIsNotAClickOnTheChildUnderTheFinger() {
        enabled.value = false
        setHome()
        swipeBy(dx = -80f, dy = 0f, durationMillis = 300)
        assertEquals(0, clicks)
    }

    // ---- an app that a swipe launches: Home carries on in the swipe direction (Android 12 and later) ----

    private fun everyEdgeLaunchesAnApp(edge: PanelEdge): SwipeTarget = SwipeTarget.App("an.app", edge)

    private fun homeWidthPx(): Float = rule.onNodeWithTag(HOME).fetchSemanticsNode().size.width.toFloat()

    @Test
    fun anAppCommit_carriesHomeOnInTheSwipeDirectionToTheParkedPlaceAndKeepsItThere() {
        // The system's own animation moves the Home window up to Android 11, so Home just settles there.
        assumeTrue(!LaunchAnimation.playsOn(Build.VERSION.SDK_INT))
        setHome(targetFor = ::everyEdgeLaunchesAnApp)
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 50f)
        lift()
        assertEquals(1, opened.size)
        // The finger moved left, so the right swipe action ran and Home goes left by 30% of the width.
        assertEquals(restLeft - homeWidthPx() * NavTravel.horizontalParallax, contentLeft(), 2f)
        assertTrue(state.isDeparted)
        assertEquals(VoidAlpha.coveredByPanel, state.alpha, 0.01f)
    }

    @Test
    fun aDepartedHome_isBackAtRestAtOnceWhenReset_asWhenVoidIsResumed() {
        assumeTrue(!LaunchAnimation.playsOn(Build.VERSION.SDK_INT))
        setHome(targetFor = ::everyEdgeLaunchesAnApp)
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 50f)
        lift()
        assertTrue(state.isDeparted)
        rule.runOnIdle { state.reset() }
        rule.waitForIdle()
        assertEquals(restLeft, contentLeft(), 0.5f)
        assertEquals(1f, state.alpha, 0f)
        assertFalse(state.isDeparted)
    }

    @Test
    fun aSwipeWhileHomeIsDeparted_isSwallowedAndLaunchesNothingElse() {
        assumeTrue(!LaunchAnimation.playsOn(Build.VERSION.SDK_INT))
        setHome(targetFor = ::everyEdgeLaunchesAnApp)
        holdLeft(firstDp = 14f, secondDp = 50f)
        lift()
        assertEquals(1, opened.size)
        swipeBy(dx = 80f, dy = 0f, durationMillis = 300)
        assertEquals(1, opened.size)
        assertEquals(0, clicks)
    }

    @Test
    fun anAppThatCouldNotBeLaunched_springsHomeBackInsteadOfDeparting() {
        assumeTrue(!LaunchAnimation.playsOn(Build.VERSION.SDK_INT))
        setHome(targetFor = ::everyEdgeLaunchesAnApp)
        commitResult = false
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 50f)
        lift()
        assertEquals(1, opened.size)
        assertFalse(state.isDeparted)
        assertEquals(restLeft, contentLeft(), 0.5f)
    }

    @Test
    fun whenTheSwipeIsSwitchedOffInTheMiddle_homeSpringsBackAndNothingOpens() {
        setHome()
        val restLeft = contentLeft()
        holdLeft(firstDp = 14f, secondDp = 30f)
        assertTrue(contentLeft() < restLeft - 1f)
        // A navigation transition starts: Home is no longer the screen to swipe on.
        rule.runOnIdle { enabled.value = false }
        rule.onNodeWithTag(HOME).performTouchInput { moveBy(Offset(-30.dp.toPx(), 0f), delayMillis = 300) }
        rule.waitForIdle()
        assertEquals(restLeft, contentLeft(), 0.5f)
        lift()
        assertTrue(opened.isEmpty())
    }

    private companion object {
        const val HOME = "home"
        const val CONTENT = "content"
    }
}
