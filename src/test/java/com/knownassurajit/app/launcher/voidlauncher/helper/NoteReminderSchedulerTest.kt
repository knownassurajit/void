package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteReminderSchedulerTest {

    @Test
    fun requestCodes_keepIdsThatDifferOnlyInTheHighBitsApart() {
        // Regression: `id.toInt()` mapped both of these to the same PendingIntent, so one reminder replaced the other.
        val low = 1_700_000_000_000L
        val high = low + (1L shl 32)
        assertEquals(low.toInt(), high.toInt())
        assertNotEquals(NoteReminderScheduler.requestCodeFor(low), NoteReminderScheduler.requestCodeFor(high))
    }

    @Test
    fun requestCodes_areStableForTheSameNote() {
        assertEquals(NoteReminderScheduler.requestCodeFor(42L), NoteReminderScheduler.requestCodeFor(42L))
    }

    @Test
    fun exactAlarms_areUsedBelowAndroid12OrWhenGranted() {
        assertTrue(NoteReminderScheduler.usesExactAlarm(sdkInt = 30, canScheduleExactAlarms = false))
        assertTrue(NoteReminderScheduler.usesExactAlarm(sdkInt = 34, canScheduleExactAlarms = true))
        // Regression: without the special access the alarm call threw and the reminder never fired.
        assertFalse(NoteReminderScheduler.usesExactAlarm(sdkInt = 34, canScheduleExactAlarms = false))
    }
}
