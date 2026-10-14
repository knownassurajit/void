package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.center
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The pointer loop of [screenBackSwipe], on the real Compose input pipeline: the distance and flick
 * rules, the axis, a child that consumes the drag, a cancelled pointer, a panel that moves under a
 * resting finger and the Dismiss action. The decisions themselves are covered by the JVM
 * `ScreenBackSwipeTest` and `SwipeMathTest`.
 *
 * Run it on an emulator (`./gradlew connectedDebugAndroidTest`), not on the physical device. The
 * touch slop is 8 dp, so the axis locks at 12 dp; a swipe commits at 56 dp or 125 dp/s.
 */
@RunWith(AndroidJUnit4::class)
class ScreenBackSwipeInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private var backs = 0

    /** A 300 dp panel from [edge], pushed [offset] to the right by a parent, the way a transition moves it. */
    private fun setPanel(
        edge: PanelEdge = PanelEdge.Left,
        enabled: Boolean = true,
        offset: () -> Dp = { 0.dp },
        content: @Composable BoxScope.() -> Unit = {}
    ) {
        backs = 0
        rule.setContent {
            Box(Modifier.offset(x = offset())) {
                Box(
                    modifier = Modifier
                        .testTag(PANEL)
                        .size(300.dp)
                        .screenBackSwipe(onBack = { backs++ }, enabled = enabled, edge = edge),
                    content = content
                )
            }
        }
    }

    /** A swipe from the middle of the panel by [dx], [dy] that lasts [durationMillis]. */
    private fun swipeBy(dx: Dp, dy: Dp, durationMillis: Long) {
        rule.onNodeWithTag(PANEL).performTouchInput {
            swipe(start = center, end = center + Offset(dx.toPx(), dy.toPx()), durationMillis = durationMillis)
        }
    }

    @Test
    fun aSlowDragPast56dpTowardTheEdge_closes() {
        setPanel(PanelEdge.Left)
        // 80 dp in 2.5 s is 32 dp/s: the distance alone decides.
        swipeBy(dx = (-80).dp, dy = 0.dp, durationMillis = 2_500)
        assertEquals(1, backs)
    }

    @Test
    fun aShortSlowDrag_doesNothing() {
        setPanel(PanelEdge.Left)
        swipeBy(dx = (-30).dp, dy = 0.dp, durationMillis = 1_500)
        assertEquals(0, backs)
    }

    @Test
    fun aShortFastFlick_closes() {
        setPanel(PanelEdge.Left)
        // 40 dp in 100 ms is 400 dp/s, past the 125 dp/s flick limit and the 12 dp lock distance.
        swipeBy(dx = (-40).dp, dy = 0.dp, durationMillis = 100)
        assertEquals(1, backs)
    }

    @Test
    fun aDragTheWrongWay_doesNothing() {
        setPanel(PanelEdge.Left)
        swipeBy(dx = 100.dp, dy = 0.dp, durationMillis = 300)
        assertEquals(0, backs)
    }

    @Test
    fun aVerticalDragOnAHorizontalPanel_doesNothing() {
        setPanel(PanelEdge.Left)
        swipeBy(dx = 0.dp, dy = (-100).dp, durationMillis = 300)
        assertEquals(0, backs)
    }

    @Test
    fun aHorizontalDragOnAVerticalPanel_doesNothing() {
        setPanel(PanelEdge.Bottom)
        swipeBy(dx = (-100).dp, dy = 0.dp, durationMillis = 300)
        assertEquals(0, backs)
    }

    @Test
    fun aPanelWithoutAnEdge_closesWithASwipeDown() {
        setPanel(PanelEdge.None)
        swipeBy(dx = 0.dp, dy = 100.dp, durationMillis = 300)
        assertEquals(1, backs)
    }

    @Test
    fun aChildThatConsumesTheDragFirst_winsAndTheSwipeDoesNothing() {
        setPanel(PanelEdge.Left) {
            Box(Modifier.fillMaxSize().draggable(rememberDraggableState { }, Orientation.Horizontal))
        }
        swipeBy(dx = (-100).dp, dy = 0.dp, durationMillis = 300)
        assertEquals(0, backs)
    }

    @Test
    fun aPointerTheSystemCancelsAfterTheDragHasLocked_doesNothing() {
        setPanel(PanelEdge.Left)
        rule.onNodeWithTag(PANEL).performTouchInput {
            down(center)
            moveBy(Offset((-100).dp.toPx(), 0f))
            cancel()
        }
        assertEquals(0, backs)
    }

    @Test
    fun aTap_doesNothing() {
        setPanel(PanelEdge.Left)
        rule.onNodeWithTag(PANEL).performTouchInput { click() }
        assertEquals(0, backs)
    }

    @Test
    fun aPanelSlidingUnderARestingFinger_isNotASwipe() {
        // Regression: a panel that is still sliding in moves under a finger that rests on it; reading the
        // position against the touch-down position counted that motion as a swipe toward its edge.
        var offset by mutableStateOf(0.dp)
        setPanel(PanelEdge.Left, offset = { offset })
        rule.onNodeWithTag(PANEL).performTouchInput { down(center) }
        rule.runOnIdle { offset = 100.dp }
        rule.waitForIdle()
        rule.onNodeWithTag(PANEL).performTouchInput { up() }
        assertEquals(0, backs)
    }

    @Test
    fun withTheSettingOff_theSwipeDoesNothingButDismissStillCloses() {
        setPanel(PanelEdge.Left, enabled = false)
        swipeBy(dx = (-100).dp, dy = 0.dp, durationMillis = 300)
        assertEquals(0, backs)
        rule.onNodeWithTag(PANEL).performSemanticsAction(SemanticsActions.Dismiss)
        assertEquals(1, backs)
    }

    @Test
    fun theDismissAction_closesThePanel() {
        setPanel(PanelEdge.Right)
        rule.onNodeWithTag(PANEL).performSemanticsAction(SemanticsActions.Dismiss)
        assertEquals(1, backs)
    }

    private companion object {
        const val PANEL = "panel"
    }
}
