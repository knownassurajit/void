package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageRefreshPolicyTest {

    private val interval = UsageRefreshPolicy.MIN_REFRESH_INTERVAL_MS

    @Test
    fun theFirstRequestIsNeverSkipped() {
        assertFalse(UsageRefreshPolicy.shouldSkip(enabled = true, lastEnabled = null, lastRefreshAt = UsageRefreshPolicy.NEVER, now = 0L))
        // Even a clock that reads below zero after boot must not look like a recent request.
        assertFalse(UsageRefreshPolicy.shouldSkip(enabled = true, lastEnabled = true, lastRefreshAt = UsageRefreshPolicy.NEVER, now = 0L))
    }

    @Test
    fun aRepeatRightAfterTheLastRequestIsSkipped() {
        assertTrue(UsageRefreshPolicy.shouldSkip(enabled = true, lastEnabled = true, lastRefreshAt = 1_000L, now = 1_000L + interval - 1))
        assertTrue(UsageRefreshPolicy.shouldSkip(enabled = false, lastEnabled = false, lastRefreshAt = 1_000L, now = 1_500L))
    }

    @Test
    fun aRequestAfterTheIntervalIsRun() {
        assertFalse(UsageRefreshPolicy.shouldSkip(enabled = true, lastEnabled = true, lastRefreshAt = 1_000L, now = 1_000L + interval))
    }

    @Test
    fun aChangeOfTheSettingIsRunAtOnce() {
        // The user switched the figures on or off: the repeat guard must not hold that back.
        assertFalse(UsageRefreshPolicy.shouldSkip(enabled = false, lastEnabled = true, lastRefreshAt = 1_000L, now = 1_001L))
        assertFalse(UsageRefreshPolicy.shouldSkip(enabled = true, lastEnabled = false, lastRefreshAt = 1_000L, now = 1_001L))
    }
}
