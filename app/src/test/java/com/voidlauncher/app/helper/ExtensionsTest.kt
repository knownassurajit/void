package com.voidlauncher.app.helper

import com.voidlauncher.app.data.Constants
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    @Test
    fun testHasBeenDays() {
        val now = System.currentTimeMillis()

        // Exact boundary (1 day)
        val exactlyOneDayAgo = now - Constants.ONE_DAY_IN_MILLIS
        assertTrue(exactlyOneDayAgo.hasBeenDays(1))

        // Less than 1 day
        val lessThanOneDayAgo = now - Constants.ONE_DAY_IN_MILLIS + 1000L
        assertFalse(lessThanOneDayAgo.hasBeenDays(1))

        // More than 1 day
        val moreThanOneDayAgo = now - Constants.ONE_DAY_IN_MILLIS - 1000L
        assertTrue(moreThanOneDayAgo.hasBeenDays(1))

        // Exactly 0 days
        val exactlyNow = now
        assertTrue(exactlyNow.hasBeenDays(0))

        // 5 days
        val fiveDaysAgo = now - (Constants.ONE_DAY_IN_MILLIS * 5)
        assertTrue(fiveDaysAgo.hasBeenDays(5))
        assertFalse(fiveDaysAgo.hasBeenDays(6))
    }

    @Test
    fun testHasBeenHours() {
        val now = System.currentTimeMillis()

        // Exact boundary (1 hour)
        val exactlyOneHourAgo = now - Constants.ONE_HOUR_IN_MILLIS
        assertTrue(exactlyOneHourAgo.hasBeenHours(1))

        // Less than 1 hour
        val lessThanOneHourAgo = now - Constants.ONE_HOUR_IN_MILLIS + 1000L
        assertFalse(lessThanOneHourAgo.hasBeenHours(1))

        // More than 1 hour
        val moreThanOneHourAgo = now - Constants.ONE_HOUR_IN_MILLIS - 1000L
        assertTrue(moreThanOneHourAgo.hasBeenHours(1))

        // Exactly 0 hours
        val exactlyNow = now
        assertTrue(exactlyNow.hasBeenHours(0))

        // 5 hours
        val fiveHoursAgo = now - (Constants.ONE_HOUR_IN_MILLIS * 5)
        assertTrue(fiveHoursAgo.hasBeenHours(5))
        assertFalse(fiveHoursAgo.hasBeenHours(6))
    }

    @Test
    fun testHasBeenMinutes() {
        val now = System.currentTimeMillis()

        // Exact boundary (1 minute)
        val exactlyOneMinuteAgo = now - Constants.ONE_MINUTE_IN_MILLIS
        assertTrue(exactlyOneMinuteAgo.hasBeenMinutes(1))

        // Less than 1 minute
        val lessThanOneMinuteAgo = now - Constants.ONE_MINUTE_IN_MILLIS + 1000L
        assertFalse(lessThanOneMinuteAgo.hasBeenMinutes(1))

        // More than 1 minute
        val moreThanOneMinuteAgo = now - Constants.ONE_MINUTE_IN_MILLIS - 1000L
        assertTrue(moreThanOneMinuteAgo.hasBeenMinutes(1))

        // Exactly 0 minutes
        val exactlyNow = now
        assertTrue(exactlyNow.hasBeenMinutes(0))

        // 5 minutes
        val fiveMinutesAgo = now - (Constants.ONE_MINUTE_IN_MILLIS * 5)
        assertTrue(fiveMinutesAgo.hasBeenMinutes(5))
        assertFalse(fiveMinutesAgo.hasBeenMinutes(6))
    }
}
