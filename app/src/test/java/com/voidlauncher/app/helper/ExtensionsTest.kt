package com.voidlauncher.app.helper

import com.voidlauncher.app.data.Constants
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ExtensionsTest {

    @Test
    fun isDaySince_today_returnsZero() {
        val now = System.currentTimeMillis()
        assertEquals(0, now.isDaySince())
    }

    @Test
    fun isDaySince_yesterday_returnsOne() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        assertEquals(1, yesterday.isDaySince())
    }

    @Test
    fun isDaySince_fiveDaysAgo_returnsFive() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -5)
        val fiveDaysAgo = calendar.timeInMillis

        assertEquals(5, fiveDaysAgo.isDaySince())
    }

    @Test
    fun isDaySince_tomorrow_returnsMinusOne() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.timeInMillis

        assertEquals(-1, tomorrow.isDaySince())
    }

    @Test
    fun isDaySince_thirtyDaysAgo_returnsThirty() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val thirtyDaysAgo = calendar.timeInMillis

        assertEquals(30, thirtyDaysAgo.isDaySince())
    }

    @Test
    fun isDaySince_crossingYearBoundary_returnsCorrectDays() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -366) // About a year and a day ago
        val pastDate = calendar.timeInMillis

        // Since leap years can affect exact days, we calculate expected based on the exact same logic
        val expected = ((System.currentTimeMillis().convertEpochToMidnight() - pastDate.convertEpochToMidnight()) / Constants.ONE_DAY_IN_MILLIS).toInt()
        assertEquals(expected, pastDate.isDaySince())
    }
}
