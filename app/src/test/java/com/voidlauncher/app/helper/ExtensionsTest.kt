package com.voidlauncher.app.helper

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExtensionsTest {

    @Test
    fun `convertEpochToMidnight should return epoch for start of same day`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.OCTOBER, 15, 14, 30, 45)
        calendar.set(Calendar.MILLISECOND, 500)
        val originalEpoch = calendar.timeInMillis

        // Expected midight
        val expectedCalendar = Calendar.getInstance()
        expectedCalendar.set(2023, Calendar.OCTOBER, 15, 0, 0, 0)
        expectedCalendar.set(Calendar.MILLISECOND, 0)
        val expectedEpoch = expectedCalendar.timeInMillis

        // Act
        val midnightEpoch = originalEpoch.convertEpochToMidnight()

        // Assert
        assertEquals(expectedEpoch, midnightEpoch)
    }

    @Test
    fun `convertEpochToMidnight on exact midnight should return same epoch`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.OCTOBER, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val originalEpoch = calendar.timeInMillis

        // Act
        val midnightEpoch = originalEpoch.convertEpochToMidnight()

        // Assert
        assertEquals(originalEpoch, midnightEpoch)
    }

    @Test
    fun `convertEpochToMidnight on end of day should return start of same day`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.OCTOBER, 15, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val originalEpoch = calendar.timeInMillis

        // Expected midnight
        val expectedCalendar = Calendar.getInstance()
        expectedCalendar.set(2023, Calendar.OCTOBER, 15, 0, 0, 0)
        expectedCalendar.set(Calendar.MILLISECOND, 0)
        val expectedEpoch = expectedCalendar.timeInMillis

        // Act
        val midnightEpoch = originalEpoch.convertEpochToMidnight()

        // Assert
        assertEquals(expectedEpoch, midnightEpoch)
    }
}
