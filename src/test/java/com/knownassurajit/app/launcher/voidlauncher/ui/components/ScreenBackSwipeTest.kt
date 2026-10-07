package com.knownassurajit.app.launcher.voidlauncher.ui.components

import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenBackSwipeTest {

    private val threshold = 120f

    @Test
    fun leftPanel_closesWhenTheFingerMovesLeft() {
        assertTrue(isBackSwipe(PanelEdge.Left, dx = -200f, dy = 10f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.Left, dx = 200f, dy = 10f, threshold = threshold))
    }

    @Test
    fun rightPanel_closesWhenTheFingerMovesRight() {
        assertTrue(isBackSwipe(PanelEdge.Right, dx = 200f, dy = -10f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.Right, dx = -200f, dy = -10f, threshold = threshold))
    }

    @Test
    fun topPanel_closesWhenTheFingerMovesUp() {
        assertTrue(isBackSwipe(PanelEdge.Top, dx = 5f, dy = -200f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.Top, dx = 5f, dy = 200f, threshold = threshold))
    }

    @Test
    fun bottomPanel_closesWhenTheFingerMovesDown() {
        assertTrue(isBackSwipe(PanelEdge.Bottom, dx = 5f, dy = 200f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.Bottom, dx = 5f, dy = -200f, threshold = threshold))
    }

    @Test
    fun panelWithoutAnEdge_closesWhenTheFingerMovesDown() {
        assertTrue(isBackSwipe(PanelEdge.None, dx = 5f, dy = 200f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.None, dx = 5f, dy = -200f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.None, dx = 200f, dy = 5f, threshold = threshold))
    }

    @Test
    fun swipeShorterThanTheThreshold_doesNotClose() {
        PanelEdge.entries.forEach { edge ->
            assertFalse("$edge", isBackSwipe(edge, dx = -100f, dy = 0f, threshold = threshold))
            assertFalse("$edge", isBackSwipe(edge, dx = 100f, dy = 0f, threshold = threshold))
            assertFalse("$edge", isBackSwipe(edge, dx = 0f, dy = -100f, threshold = threshold))
            assertFalse("$edge", isBackSwipe(edge, dx = 0f, dy = 100f, threshold = threshold))
        }
    }

    @Test
    fun swipeAlongTheWrongAxis_doesNotClose() {
        assertFalse(isBackSwipe(PanelEdge.Left, dx = -150f, dy = 200f, threshold = threshold))
        assertFalse(isBackSwipe(PanelEdge.Bottom, dx = 200f, dy = 150f, threshold = threshold))
    }

    @Test
    fun conflictSafe_movesALeftPanelToASwipeUp() {
        assertEquals(PanelEdge.Top, backSwipeEdge(PanelEdge.Left, conflictSafe = true))
    }

    @Test
    fun conflictSafe_leavesOtherEdgesAlone() {
        PanelEdge.entries.filter { it != PanelEdge.Left }.forEach { edge ->
            assertEquals(edge, backSwipeEdge(edge, conflictSafe = true))
        }
        PanelEdge.entries.forEach { edge ->
            assertEquals(edge, backSwipeEdge(edge, conflictSafe = false))
        }
    }
}
