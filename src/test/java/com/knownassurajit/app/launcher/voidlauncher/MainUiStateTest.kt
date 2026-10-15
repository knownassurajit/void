package com.knownassurajit.app.launcher.voidlauncher

import android.view.Gravity
import com.knownassurajit.app.launcher.voidlauncher.data.HomescreenPreferences
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {

    private val prefs = HomescreenPreferences(
        appHorizontalAlignment = Gravity.END,
        appVerticalAlignment = Gravity.TOP,
        clockHorizontalAlignment = Gravity.CENTER_HORIZONTAL,
        clockVerticalAlignment = Gravity.CENTER_VERTICAL,
        showClock = false,
        showDate = false,
        showScreenTime = false,
        showHomeApps = true,
        maxApps = 7,
        clockSectionWeight = 0.4f,
        clockSizeScale = 1.2f,
        dateSizeScale = 0.8f,
        screenTimeSizeScale = 0.9f,
        homeSectionOrder = "apps_first",
        homeTextSizeScale = 1.5f,
        appDrawerTextSizeScale = 1.1f,
        appSpacingDp = 30f,
        showStatusBar = true,
        leftSwipeAction = Prefs.SwipeAction.NOTES,
        rightSwipeAction = Prefs.SwipeAction.NONE,
        enableGestures = false,
        enableSwipeDownNotifications = false,
        appFont = "inter",
        use24HourClock = true,
        showSeconds = true,
        privateSpacePlacement = Prefs.PrivateSpacePlacement.SEARCH_BAR,
        animationSpeed = Prefs.AnimationSpeed.FAST,
        contentSwipeToBack = false,
        clockOpacity = 0.6f,
        homeAppsOpacity = 0.7f,
        clockColor = "primary",
        homeAppsColor = "muted",
        clockFontWeight = 500,
        homeAppsFontWeight = 700,
        homeSidePaddingDp = 32f
    )

    @Test
    fun everyHomeSettingIsCopied() {
        val state = MainUiState().withHomescreenPreferences(prefs)
        // Regression: refreshing from Prefs on resume used to skip "show seconds".
        assertTrue(state.showSeconds)
        assertEquals(7, state.homeAppsCount)
        assertEquals("apps_first", state.homeSectionOrder)
        assertEquals(0.6f, state.clockOpacity, 0f)
        assertEquals(0.7f, state.homeAppsOpacity, 0f)
        assertEquals("primary", state.clockColor)
        assertEquals("muted", state.homeAppsColor)
        assertEquals(500, state.clockFontWeight)
        assertEquals(700, state.homeAppsFontWeight)
        assertEquals(32f, state.homeSidePaddingDp, 0f)
        assertEquals(Prefs.PrivateSpacePlacement.SEARCH_BAR, state.privateSpacePlacement)
    }

    @Test
    fun timeAndAppsAreNotTouched() {
        val before = MainUiState(currentTime = "10:08", batteryLevel = 42)
        val after = before.withHomescreenPreferences(prefs)
        assertEquals("10:08", after.currentTime)
        assertEquals(42, after.batteryLevel)
    }
}
