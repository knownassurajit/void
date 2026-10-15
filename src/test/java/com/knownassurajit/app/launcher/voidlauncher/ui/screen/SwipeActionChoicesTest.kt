package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeActionChoicesTest {

    @Test
    fun theAccessibilityActionIsNotOffered_nothingRunsIt() {
        val choices = swipeActionChoices(SwipeAction.NONE, enableSummary = true, enableWidgets = true, enableNotes = true)
        assertFalse(SwipeAction.ACCESSIBILITY in choices)
    }

    @Test
    fun disabledPanelsAndTheOtherSidesPanelAreNotOffered() {
        val choices = swipeActionChoices(SwipeAction.WIDGETS, enableSummary = false, enableWidgets = true, enableNotes = true)
        assertEquals(listOf(SwipeAction.NOTES, SwipeAction.NOTIFICATIONS, SwipeAction.APP, SwipeAction.NONE), choices)
    }

    @Test
    fun sharedActionsStayOnBothSides() {
        val choices = swipeActionChoices(SwipeAction.APP, enableSummary = true, enableWidgets = true, enableNotes = true)
        assertTrue(SwipeAction.APP in choices)
        assertTrue(SwipeAction.NOTIFICATIONS in choices)
    }
}
