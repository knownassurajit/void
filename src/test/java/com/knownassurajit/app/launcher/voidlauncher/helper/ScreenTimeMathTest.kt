package com.knownassurajit.app.launcher.voidlauncher.helper

import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTimeMathTest {

    private val minute = 60_000L
    private val hour = 60 * minute

    @Test
    fun aggregate_addsTheBucketsOfOnePackage() {
        val result = ScreenTimeMath.aggregate(
            listOf("mail" to 5 * minute, "maps" to minute, "mail" to 7 * minute)
        )
        assertEquals(mapOf("mail" to 12 * minute, "maps" to minute), result)
    }

    @Test
    fun aggregate_dropsExcludedPackagesAndEmptyEntries() {
        val result = ScreenTimeMath.aggregate(
            listOf("void" to hour, "mail" to 0L, "maps" to -5L, "chat" to minute),
            exclude = setOf("void")
        )
        assertEquals(mapOf("chat" to minute), result)
    }

    @Test
    fun aggregate_ofNothingIsEmpty() {
        assertTrue(ScreenTimeMath.aggregate(emptyList()).isEmpty())
    }

    @Test
    fun hoursMinutes_roundsDownToWholeMinutes() {
        assertEquals("0m", ScreenTimeMath.hoursMinutes(0L))
        assertEquals("0m", ScreenTimeMath.hoursMinutes(59_999L))
        assertEquals("12m", ScreenTimeMath.hoursMinutes(12 * minute + 59_000L))
        assertEquals("1h 0m", ScreenTimeMath.hoursMinutes(hour))
        assertEquals("2h 5m", ScreenTimeMath.hoursMinutes(2 * hour + 5 * minute + 30_000L))
        assertEquals("0m", ScreenTimeMath.hoursMinutes(-1L))
    }

    @Test
    fun appLabel_showsNothingForNoUsageAndASmallMarkForSeconds() {
        assertNull(ScreenTimeMath.appLabel(0L))
        assertNull(ScreenTimeMath.appLabel(-3L))
        assertEquals("<1m", ScreenTimeMath.appLabel(1L))
        assertEquals("<1m", ScreenTimeMath.appLabel(minute - 1))
        assertEquals("1m", ScreenTimeMath.appLabel(minute))
        assertEquals("1h 5m", ScreenTimeMath.appLabel(hour + 5 * minute))
    }

    @Test
    fun startOfDay_isMidnightInTheGivenZone() {
        val utc = TimeZone.getTimeZone("UTC")
        val noonOnDayTwo = 2 * 24 * hour + 12 * hour + 34 * minute + 56_789L
        assertEquals(2 * 24 * hour, ScreenTimeMath.startOfDay(noonOnDayTwo, utc))
        // Exactly midnight stays put.
        assertEquals(2 * 24 * hour, ScreenTimeMath.startOfDay(2 * 24 * hour, utc))
        // 23:30 UTC is already the next day at UTC+2.
        val plusTwo = TimeZone.getTimeZone("GMT+2")
        val lateEvening = 2 * 24 * hour + 23 * hour + 30 * minute
        assertEquals(3 * 24 * hour - 2 * hour, ScreenTimeMath.startOfDay(lateEvening, plusTwo))
    }

    @Test
    fun usageTotal_isTheSumOfItsApps() {
        val usage = ScreenTimeResult.Usage(mapOf("mail" to 5 * minute, "maps" to 7 * minute))
        assertEquals(12 * minute, usage.totalMillis)
        assertEquals(0L, ScreenTimeResult.Usage(emptyMap()).totalMillis)
    }
}
