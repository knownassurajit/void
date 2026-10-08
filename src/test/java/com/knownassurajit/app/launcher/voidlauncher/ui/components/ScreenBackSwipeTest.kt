package com.knownassurajit.app.launcher.voidlauncher.ui.components

import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeMath
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenBackSwipeTest {

    // 8 dp touch slop and a 2x display.
    private val slop = 16f
    private val density = 2f

    /**
     * Whether a swipe that moved the finger [dx], [dy] px from where it started (in one event), and
     * was moving at [vx], [vy] px/s when it ended, closes a panel from [edge].
     */
    private fun closes(edge: PanelEdge, dx: Float, dy: Float, vx: Float = 0f, vy: Float = 0f): Boolean {
        val recognizer = BackSwipeRecognizer(dismissDirection(edge), slop, density)
        recognizer.onMove(dx, dy, consumed = false)
        return recognizer.shouldClose(vx, vy)
    }

    @Test
    fun dismissDirection_isTowardTheEdgeThePanelCameFrom() {
        assertEquals(DismissDirection(SwipeAxis.Horizontal, -1), dismissDirection(PanelEdge.Left))
        assertEquals(DismissDirection(SwipeAxis.Horizontal, 1), dismissDirection(PanelEdge.Right))
        assertEquals(DismissDirection(SwipeAxis.Vertical, -1), dismissDirection(PanelEdge.Top))
        assertEquals(DismissDirection(SwipeAxis.Vertical, 1), dismissDirection(PanelEdge.Bottom))
    }

    @Test
    fun panelWithoutAnEdge_closesWithASwipeDownLikeTheDrawer() {
        assertEquals(dismissDirection(PanelEdge.Bottom), dismissDirection(PanelEdge.None))
    }

    @Test
    fun along_isPositiveWhenTheFingerMovesTheWayThatCloses() {
        assertEquals(30f, dismissDirection(PanelEdge.Left).along(dx = -30f, dy = 99f), 0f)
        assertEquals(-30f, dismissDirection(PanelEdge.Left).along(dx = 30f, dy = 99f), 0f)
        assertEquals(30f, dismissDirection(PanelEdge.Right).along(dx = 30f, dy = 99f), 0f)
        assertEquals(30f, dismissDirection(PanelEdge.Top).along(dx = 99f, dy = -30f), 0f)
        assertEquals(30f, dismissDirection(PanelEdge.Bottom).along(dx = 99f, dy = 30f), 0f)
    }

    @Test
    fun leftPanel_closesWhenTheFingerMovesLeft() {
        assertTrue(closes(PanelEdge.Left, dx = -200f, dy = 10f))
        assertFalse(closes(PanelEdge.Left, dx = 200f, dy = 10f))
    }

    @Test
    fun rightPanel_closesWhenTheFingerMovesRight() {
        assertTrue(closes(PanelEdge.Right, dx = 200f, dy = -10f))
        assertFalse(closes(PanelEdge.Right, dx = -200f, dy = -10f))
    }

    @Test
    fun topPanel_closesWhenTheFingerMovesUp() {
        assertTrue(closes(PanelEdge.Top, dx = 5f, dy = -200f))
        assertFalse(closes(PanelEdge.Top, dx = 5f, dy = 200f))
    }

    @Test
    fun bottomPanelAndPanelWithoutAnEdge_closeWhenTheFingerMovesDown() {
        listOf(PanelEdge.Bottom, PanelEdge.None).forEach { edge ->
            assertTrue("$edge", closes(edge, dx = 5f, dy = 200f))
            assertFalse("$edge", closes(edge, dx = 5f, dy = -200f))
        }
    }

    @Test
    fun swipeShorterThan56dpAndSlow_doesNotClose() {
        // 55 dp at 2x.
        PanelEdge.entries.forEach { edge ->
            assertFalse("$edge", closes(edge, dx = -110f, dy = 0f))
            assertFalse("$edge", closes(edge, dx = 110f, dy = 0f))
            assertFalse("$edge", closes(edge, dx = 0f, dy = -110f))
            assertFalse("$edge", closes(edge, dx = 0f, dy = 110f))
        }
    }

    @Test
    fun shortFlickTheRightWay_closes_theWrongWayDoesNot() {
        assertTrue(closes(PanelEdge.Left, dx = -40f, dy = 0f, vx = -600f))
        assertFalse(closes(PanelEdge.Left, dx = -40f, dy = 0f, vx = 600f))
        assertTrue(closes(PanelEdge.Bottom, dx = 0f, dy = 40f, vy = 600f))
        assertFalse(closes(PanelEdge.Bottom, dx = 0f, dy = 40f, vy = -600f))
    }

    // ---- BackSwipeRecognizer ----

    private fun recognizer(edge: PanelEdge) = BackSwipeRecognizer(dismissDirection(edge), slop, density)

    @Test
    fun recognizer_shortDragIsUndecidedAndNeverCloses() {
        val recognizer = recognizer(PanelEdge.Left)
        assertEquals(BackSwipeStep.Undecided, recognizer.onMove(-10f, 2f, consumed = false))
        assertFalse(recognizer.shouldClose(velocityX = -5_000f, velocityY = 0f))
    }

    @Test
    fun recognizer_dragAlongThePanelsAxisTracksAndClosesOnRelease() {
        val recognizer = recognizer(PanelEdge.Left)
        assertEquals(BackSwipeStep.Undecided, recognizer.onMove(-20f, 0f, consumed = false))
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(-10f, 4f, consumed = false))
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(-120f, 4f, consumed = false))
        assertEquals(-150f, recognizer.travelX, 0f)
        assertEquals(8f, recognizer.travelY, 0f)
        assertTrue(recognizer.shouldClose(velocityX = 0f, velocityY = 0f))
    }

    @Test
    fun recognizer_countsTheTravelBeforeTheSlop() {
        // The first events are too short to lock the axis; their distance still counts once it does.
        val recognizer = recognizer(PanelEdge.Bottom)
        assertEquals(BackSwipeStep.Undecided, recognizer.onMove(0f, 4f, consumed = false))
        assertEquals(BackSwipeStep.Undecided, recognizer.onMove(0f, 8f, consumed = false))
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(0f, 100f, consumed = false))
        // 4 + 8 + 100 = 112 px = 56 dp at 2x: exactly the commit distance.
        assertTrue(recognizer.shouldClose(velocityX = 0f, velocityY = 0f))
    }

    @Test
    fun recognizer_dragAlongTheOtherAxisGivesUp() {
        assertEquals(BackSwipeStep.GiveUp, recognizer(PanelEdge.Left).onMove(3f, -60f, consumed = false))
        assertEquals(BackSwipeStep.GiveUp, recognizer(PanelEdge.Bottom).onMove(60f, 3f, consumed = false))
    }

    @Test
    fun recognizer_dragAlongTheOtherAxisNeverClosesWhateverTheVelocity() {
        val recognizer = recognizer(PanelEdge.Left)
        recognizer.onMove(3f, -200f, consumed = false)
        assertFalse(recognizer.shouldClose(velocityX = -5_000f, velocityY = -5_000f))
    }

    @Test
    fun recognizer_aChildThatConsumedThePointerWins() {
        val recognizer = recognizer(PanelEdge.Bottom)
        assertEquals(BackSwipeStep.GiveUp, recognizer.onMove(0f, 5f, consumed = true))
        assertEquals(BackSwipeStep.GiveUp, recognizer.onMove(0f, 195f, consumed = true))
    }

    @Test
    fun recognizer_aCancelledPointerAfterTheLockGivesUp() {
        val recognizer = recognizer(PanelEdge.Left)
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(-200f, 0f, consumed = false))
        // The system takes the pointer (its own back gesture): the cancel arrives consumed.
        assertEquals(BackSwipeStep.GiveUp, recognizer.onMove(0f, 0f, consumed = true))
    }

    @Test
    fun recognizer_theAxisStaysLockedWhenTheFingerSwings() {
        val recognizer = recognizer(PanelEdge.Left)
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(-40f, 0f, consumed = false))
        // The finger swings down: still this panel's swipe, but there is no travel toward the edge to close it.
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(30f, 300f, consumed = false))
        assertFalse(recognizer.shouldClose(velocityX = 0f, velocityY = 900f))
    }

    @Test
    fun recognizer_reversingBeforeReleaseDoesNotClose() {
        val recognizer = recognizer(PanelEdge.Left)
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(-160f, 0f, consumed = false))
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(140f, 0f, consumed = false))
        // Only the position at release counts, not how far the finger went.
        assertFalse(recognizer.shouldClose(velocityX = 600f, velocityY = 0f))
    }

    @Test
    fun recognizer_aFlickBackAfterALongDragDoesNotClose() {
        // Regression: 100 dp toward the edge and then a quick reversal at lift-off closed the panel.
        val recognizer = recognizer(PanelEdge.Left)
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(-200f, 0f, consumed = false))
        assertFalse(recognizer.shouldClose(velocityX = 300f, velocityY = 0f))
    }

    @Test
    fun recognizer_aFlickTheRightWayClosesEvenWhenShort() {
        val recognizer = recognizer(PanelEdge.Top)
        assertEquals(BackSwipeStep.Tracking, recognizer.onMove(2f, -40f, consumed = false))
        assertTrue(recognizer.shouldClose(velocityX = 0f, velocityY = -600f))
    }

    @Test
    fun recognizer_aPanelSlidingUnderAStillFingerIsNotASwipe() {
        // Regression: a panel that is still sliding in moves under a resting finger. A Left panel
        // slides toward the right, so the position the panel sees drifts left, the way that closes
        // it. Each event's movement is position minus previous position, both read through the
        // panel's transform of that moment, so it is zero however far the panel has moved.
        val recognizer = recognizer(PanelEdge.Left)
        val fingerOnScreenX = 300f
        val touchDownLocalX = fingerOnScreenX
        (1..30).forEach { event ->
            val localX = fingerOnScreenX - 4f * event
            // The previous screen position of the finger is read through the same transform: it is the same point.
            val previousLocalX = localX
            assertEquals("$event", BackSwipeStep.Undecided, recognizer.onMove(localX - previousLocalX, 0f, consumed = false))
        }
        assertEquals(0f, recognizer.travelX, 0f)
        assertFalse(recognizer.shouldClose(velocityX = 0f, velocityY = 0f))
        // A difference from the touch-down position would have read 120 px (60 dp) toward the edge.
        val drift = (fingerOnScreenX - 4f * 30) - touchDownLocalX
        assertEquals(-120f, drift, 0f)
        assertTrue(SwipeMath.shouldCommit(dismissDirection(PanelEdge.Left).along(drift, 0f), 0f, slop, density))
    }
}
