package com.knownassurajit.app.launcher.voidlauncher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeReorderHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation smoke tests for critical launcher paths.
 * Full gesture e2e requires a device; these validate prefs/reorder contracts on-device.
 */
@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {

    @Test
    fun appContext_usesExpectedPackage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(context.packageName.contains("voidlauncher") || context.packageName.contains("void"))
    }

    @Test
    fun prefs_homescreenDefaults_areReadable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = Prefs(context)
        assertTrue(prefs.maxHomeApps in 1..15)
        assertTrue(prefs.clockSizeScale > 0f)
        assertTrue(prefs.homeSectionOrder == "clock_first" || prefs.homeSectionOrder == "apps_first")
    }

    @Test
    fun reorderHelper_moveFiveToTwo_onDevice() {
        val list = listOf("A", "B", "C", "D", "E")
        val result = HomeReorderHelper.moveItem(list, 4, 1)
        assertEquals(listOf("A", "E", "B", "C", "D"), result)
    }

    @Test
    fun prefs_widgetCustomizationKeys_roundTrip() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = Prefs(context)
        val previousOrder = prefs.widgetOrder
        val previousLabels = prefs.showWidgetLabels
        try {
            prefs.widgetOrder = listOf("provider.a", "provider.b")
            prefs.showWidgetLabels = false
            assertEquals(listOf("provider.a", "provider.b"), prefs.widgetOrder)
            assertEquals(false, prefs.showWidgetLabels)
        } finally {
            prefs.widgetOrder = previousOrder
            prefs.showWidgetLabels = previousLabels
        }
    }
}
