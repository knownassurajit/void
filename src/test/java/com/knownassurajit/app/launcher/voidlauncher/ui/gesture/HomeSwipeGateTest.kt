package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSwipeGateTest {

    @Test
    fun homeOnTopAndResumed_letsASwipeStart() {
        val gate = homeSwipeGate(isOnTop = true, isResumed = true)
        assertEquals(HomeSwipeGate.Open, gate)
        assertTrue(gate.isOpen)
        assertTrue(gate.isOnTop)
    }

    @Test
    fun homeOnTopButNotResumed_isBusy_aPanelIsStillLeavingOrTheActivityIsPaused() {
        // Regression: a swipe that started while a panel was still leaving stacked a second panel.
        val gate = homeSwipeGate(isOnTop = true, isResumed = false)
        assertEquals(HomeSwipeGate.Busy, gate)
        assertFalse(gate.isOpen)
        // Home is still the destination on top, so it keeps the back press.
        assertTrue(gate.isOnTop)
    }

    @Test
    fun anotherScreenOnTop_blocksTheSwipeAndFreesTheBackPress() {
        listOf(true, false).forEach { resumed ->
            val gate = homeSwipeGate(isOnTop = false, isResumed = resumed)
            assertEquals("resumed=$resumed", HomeSwipeGate.NotOnTop, gate)
            assertFalse(gate.isOpen)
            assertFalse(gate.isOnTop)
        }
    }

    @Test
    fun onlyOneCombinationOpensTheGate() {
        val open = listOf(true, false).flatMap { top -> listOf(true, false).map { resumed -> top to resumed } }
            .filter { (top, resumed) -> homeSwipeGate(top, resumed).isOpen }
        assertEquals(listOf(true to true), open)
    }

    @Test
    fun everyGateHasItsOwnLogReason_withoutAnyIdentifier() {
        val reasons = HomeSwipeGate.entries.map { it.logReason }
        assertEquals(reasons.size, reasons.toSet().size)
        assertEquals(listOf("open", "not_on_top", "busy"), reasons)
    }
}
