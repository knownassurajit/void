package com.knownassurajit.app.launcher.voidlauncher.helper

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureAvailabilityTest {

    @Test
    fun privateSpaceProperty_matchesApiFloor() {
        val expected = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
        @Suppress("DEPRECATION")
        assertEquals(expected, FeatureAvailability.isPrivateSpaceAvailable)
    }

    @Test
    fun legacyGates_defaultTrueForOssSingleVariant() {
        @Suppress("DEPRECATION")
        assertTrue(FeatureAvailability.isWidgetsAvailable)
        @Suppress("DEPRECATION")
        assertTrue(FeatureAvailability.isNotificationsAvailable)
        @Suppress("DEPRECATION")
        assertTrue(FeatureAvailability.isNotificationSummaryAvailable)
    }

    @Test
    fun privateSpaceApiFloor_isVanillaIceCream() {
        // Document the hard floor used by helpers.
        assertFalse(Build.VERSION_CODES.VANILLA_ICE_CREAM < 35)
        assertTrue(Build.VERSION_CODES.VANILLA_ICE_CREAM >= 35)
    }
}
