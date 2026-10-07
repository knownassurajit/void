package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateSpaceUiTest {

    @Test
    fun outcome_lockRequestThatWentThroughIsLocked() {
        assertEquals(
            ToggleOutcome.LOCKED,
            PrivateSpaceUi.outcomeFor(requestedQuiet = true, requestReturned = true)
        )
    }

    @Test
    fun outcome_unlockRequestThatWentThroughIsUnlocked() {
        assertEquals(
            ToggleOutcome.UNLOCKED,
            PrivateSpaceUi.outcomeFor(requestedQuiet = false, requestReturned = true)
        )
    }

    @Test
    fun outcome_unlockRequestThatReturnsFalseNeedsUserActionNotUnlocked() {
        // Regression: the system answers false while it shows the credential prompt (or the
        // screen-lock setup). The UI used to flip to "unlocked" anyway and stayed wrong on cancel.
        val outcome = PrivateSpaceUi.outcomeFor(requestedQuiet = false, requestReturned = false)

        assertEquals(ToggleOutcome.NEEDS_USER_ACTION, outcome)
        assertNotEquals(ToggleOutcome.UNLOCKED, outcome)
    }

    @Test
    fun outcome_lockRequestThatReturnsFalseFailed() {
        assertEquals(
            ToggleOutcome.FAILED,
            PrivateSpaceUi.outcomeFor(requestedQuiet = true, requestReturned = false)
        )
    }

    @Test
    fun settingsAction_hiddenWhenLockedEvenIfEntryExists() {
        // Regression: cold-opening the drawer while locked used to show the gear next to the lock.
        assertFalse(PrivateSpaceUi.settingsActionVisible(isLocked = true, settingsAvailable = true))
    }

    @Test
    fun settingsAction_shownWhenUnlockedAndEntryExists() {
        assertTrue(PrivateSpaceUi.settingsActionVisible(isLocked = false, settingsAvailable = true))
    }

    @Test
    fun settingsAction_hiddenWhenUnlockedButNoEntry() {
        assertFalse(PrivateSpaceUi.settingsActionVisible(isLocked = false, settingsAvailable = false))
    }

    @Test
    fun settingsAction_hiddenWhenLockedAndNoEntry() {
        assertFalse(PrivateSpaceUi.settingsActionVisible(isLocked = true, settingsAvailable = false))
    }

    @Test
    fun addEntry_matchesAddLabels() {
        assertTrue(PrivateSpaceUi.isAddEntryLabel("Add", null))
        assertTrue(PrivateSpaceUi.isAddEntryLabel(" add apps ", null))
        assertTrue(PrivateSpaceUi.isAddEntryLabel("Add app", null))
    }

    @Test
    fun addEntry_matchesPrivateSpaceAddActivityClass() {
        assertTrue(
            PrivateSpaceUi.isAddEntryLabel(
                "Anything",
                "com.android.settings.privatespace.PrivateSpaceAddAppsActivity"
            )
        )
    }

    @Test
    fun addEntry_rejectsLookalikeLabels() {
        assertFalse(PrivateSpaceUi.isAddEntryLabel("Address Book", null))
        assertFalse(PrivateSpaceUi.isAddEntryLabel("Adobe", null))
        assertFalse(PrivateSpaceUi.isAddEntryLabel("Settings", null))
    }

    @Test
    fun addEntry_rejectsPrivateSpaceSettingsClassAndNullClass() {
        assertFalse(
            PrivateSpaceUi.isAddEntryLabel(
                "Settings",
                "com.android.settings.privatespace.PrivateSpaceSettingsActivity"
            )
        )
        assertFalse(PrivateSpaceUi.isAddEntryLabel("Notes", null))
    }
}
