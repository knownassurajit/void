package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import com.knownassurajit.app.launcher.voidlauncher.data.NoteItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.dismissDirection
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

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

    @Test
    fun clearReminder_isOfferedOnlyForANoteWithAReminder() {
        val plain = NoteItem(id = 1L, text = "milk")
        assertFalse(NoteAction.ClearReminder in noteActionsFor(plain))
        assertTrue(NoteAction.ClearReminder in noteActionsFor(plain.copy(reminderTime = 1_000L)))
        assertEquals(NoteAction.Delete, noteActionsFor(plain).last())
    }

    @Test
    fun reminderTime_keepsThePickedDayWestOfUtc() {
        // Regression: the date picker returns midnight UTC; read in Toronto time that is the previous evening,
        // so a reminder picked for 15 Oct used to be set for 14 Oct.
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Toronto"))
            val utcMidnight15Oct = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(2026, Calendar.OCTOBER, 15)
            }.timeInMillis
            val trigger = Calendar.getInstance().apply { timeInMillis = reminderTriggerTime(utcMidnight15Oct, 9, 30) }
            assertEquals(15, trigger.get(Calendar.DAY_OF_MONTH))
            assertEquals(9, trigger.get(Calendar.HOUR_OF_DAY))
            assertEquals(30, trigger.get(Calendar.MINUTE))
        } finally {
            TimeZone.setDefault(previous)
        }
    }
}
