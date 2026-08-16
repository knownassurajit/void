package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutHelperTest {

    @Test
    fun bothEnabled_usesRequestedSplit() {
        val weights = HomeLayoutHelper.sectionWeights(
            clockEnabled = true,
            appsEnabled = true,
            requestedClockWeight = 0.35f
        )
        assertEquals(0.35f, weights.clock, 0.001f)
        assertEquals(0.65f, weights.apps, 0.001f)
    }

    @Test
    fun requestedWeight_isClamped() {
        val low = HomeLayoutHelper.sectionWeights(true, true, 0.01f)
        val high = HomeLayoutHelper.sectionWeights(true, true, 0.95f)
        assertEquals(HomeLayoutHelper.MIN_CLOCK_WEIGHT, low.clock, 0.001f)
        assertEquals(HomeLayoutHelper.MAX_CLOCK_WEIGHT, high.clock, 0.001f)
        assertEquals(1f, low.clock + low.apps, 0.001f)
        assertEquals(1f, high.clock + high.apps, 0.001f)
    }

    @Test
    fun singleSection_takesFullWeight() {
        val clockOnly = HomeLayoutHelper.sectionWeights(true, false, 0.35f)
        val appsOnly = HomeLayoutHelper.sectionWeights(false, true, 0.35f)
        assertEquals(1f, clockOnly.clock, 0.001f)
        assertEquals(0f, clockOnly.apps, 0.001f)
        assertEquals(0f, appsOnly.clock, 0.001f)
        assertEquals(1f, appsOnly.apps, 0.001f)
    }
}
