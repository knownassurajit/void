package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsCategoryTest {

    @Test
    fun allExpectedCategoriesArePresent() {
        val categories = SettingsCategory.entries.map { it.name }
        val expected = listOf(
            "APPEARANCE",
            "HOME_SCREEN",
            "APP_DRAWER",
            "GESTURES",
            "PANELS",
            "PERMISSIONS",
            "ABOUT"
        )
        assertEquals(expected, categories)
    }

    @Test
    fun categoryCountIsSeven() {
        assertEquals(7, SettingsCategory.entries.size)
    }
}
