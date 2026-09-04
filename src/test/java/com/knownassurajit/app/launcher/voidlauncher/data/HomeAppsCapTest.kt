package com.knownassurajit.app.launcher.voidlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAppsCapTest {

    @Test
    fun missingMaxKey_withSixFilledSlots_resolvesCapAtLeastSix() {
        val cap = HomeAppsCap.resolve(
            hasMaxKey = false,
            maxValue = 0,
            hasLegacyKey = false,
            legacyValue = 4,
            filledSlots = 6
        )
        assertTrue(cap >= 6)
        assertEquals(10, cap)
    }

    @Test
    fun clockToggleEmit_doesNotCollapseSixFilledSlots() {
        val slots = listOf("a", "b", "c", "d", "e", "f")
        val filled = HomeAppsCap.countFilledSlots { index ->
            slots.getOrNull(index - 1).orEmpty()
        }
        assertEquals(6, filled)
        val capBefore = HomeAppsCap.resolve(false, 0, false, 4, filled)
        val capAfter = HomeAppsCap.resolve(false, 0, false, 4, filled)
        assertEquals(capBefore, capAfter)
        assertTrue(capAfter >= 6)
        assertFalse(HomeAppsCap.shouldReloadHomeApps(capBefore, capAfter))
        assertEquals(6, minOf(filled, capAfter))
    }

    @Test
    fun clockToggleCollect_withTenFilledSlots_doesNotReloadOrWipe() {
        val filled = 10
        val cap = HomeAppsCap.resolve(
            hasMaxKey = false,
            maxValue = 0,
            hasLegacyKey = false,
            legacyValue = 4,
            filledSlots = filled
        )
        assertEquals(10, cap)
        var lastMaxApps = -1
        val firstReload = HomeAppsCap.shouldReloadHomeApps(lastMaxApps, cap)
        lastMaxApps = cap
        val afterClockToggle = HomeAppsCap.shouldReloadHomeApps(lastMaxApps, cap)
        assertTrue(firstReload)
        assertFalse(afterClockToggle)
        assertEquals(filled, minOf(filled, cap))
    }

    @Test
    fun staleLegacyNum_doesNotHideFilledSlots() {
        val cap = HomeAppsCap.resolve(false, 0, true, 4, 6)
        assertEquals(6, cap)
    }

    @Test
    fun existingMaxKey_isHonored() {
        assertEquals(8, HomeAppsCap.resolve(true, 8, true, 4, 6))
    }
}
