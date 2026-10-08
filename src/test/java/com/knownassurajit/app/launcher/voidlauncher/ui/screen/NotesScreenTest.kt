package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import com.knownassurajit.app.launcher.voidlauncher.ui.components.dismissDirection
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesScreenTest {

    /** The physical way a note row is swiped to delete it: `-1` is left, `+1` is right. */
    private fun deleteSign(edge: PanelEdge, rtl: Boolean): Int {
        val fromStartToEnd = noteDeleteFromStartToEnd(edge, rtl)
        // Start to end is a swipe right in LTR and a swipe left in RTL.
        return if (fromStartToEnd != rtl) 1 else -1
    }

    @Test
    fun notesFromTheLeft_deleteWithASwipeRightInLtr() {
        assertTrue(noteDeleteFromStartToEnd(PanelEdge.Left, rtl = false))
    }

    @Test
    fun notesFromTheRightOrWithoutALeftEdge_deleteWithASwipeLeftInLtr() {
        listOf(PanelEdge.Right, PanelEdge.Top, PanelEdge.Bottom, PanelEdge.None).forEach { edge ->
            assertFalse("$edge", noteDeleteFromStartToEnd(edge, rtl = false))
        }
    }

    @Test
    fun startAndEndFlipInRtlSoTheSwipeStaysOnTheSamePhysicalSide() {
        PanelEdge.entries.forEach { edge ->
            assertEquals("$edge", !noteDeleteFromStartToEnd(edge, rtl = false), noteDeleteFromStartToEnd(edge, rtl = true))
            assertEquals("$edge", deleteSign(edge, rtl = false), deleteSign(edge, rtl = true))
        }
    }

    @Test
    fun deletingANote_isNeverTheSwipeThatClosesNotes() {
        // Regression: a note row consumes the drag first, so closing Notes with a finger over a row
        // used to delete that note when both were a swipe left.
        listOf(false, true).forEach { rtl ->
            PanelEdge.entries.forEach { edge ->
                val closing = dismissDirection(edge)
                val closesHorizontally = closing.axis == SwipeAxis.Horizontal
                assertFalse("$edge rtl=$rtl", closesHorizontally && closing.sign == deleteSign(edge, rtl))
            }
        }
    }
}
