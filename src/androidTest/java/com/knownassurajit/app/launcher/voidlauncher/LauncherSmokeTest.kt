package com.knownassurajit.app.launcher.voidlauncher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.knownassurajit.app.launcher.voidlauncher.data.HomeAppsCap
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeReorderHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        val prefs = Prefs.get(context)
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
        val prefs = Prefs.get(context)
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

    @Test
    fun clockToggle_doesNotCollapseOrWipeHomeApps() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = Prefs.get(context)
        val packagesBefore = (1..10).map { prefs.getAppPackage(it) }
        val filled = packagesBefore.count { it.isNotBlank() }
        val capBefore = prefs.maxHomeApps
        val clockBefore = prefs.showClockWidget
        assertTrue("Need filled home slots to prove the wipe path", filled >= 6)
        try {
            prefs.showClockWidget = false
            val afterOff = prefs.homescreenPreferences.value
            assertEquals(false, afterOff.showClock)
            assertEquals(capBefore, afterOff.maxApps)
            assertFalse(HomeAppsCap.shouldReloadHomeApps(capBefore, afterOff.maxApps))
            assertEquals(packagesBefore, (1..10).map { prefs.getAppPackage(it) })

            prefs.showClockWidget = true
            val afterOn = prefs.homescreenPreferences.value
            assertEquals(true, afterOn.showClock)
            assertEquals(capBefore, afterOn.maxApps)
            assertFalse(HomeAppsCap.shouldReloadHomeApps(capBefore, afterOn.maxApps))
            assertEquals(packagesBefore, (1..10).map { prefs.getAppPackage(it) })
        } finally {
            prefs.showClockWidget = clockBefore
        }
    }

    @Test
    fun widgetBindIntent_targetsSystemBindAction() {
        val provider = android.content.ComponentName("com.example.clock", "com.example.clock.Widget")
        val intent = com.knownassurajit.app.launcher.voidlauncher.helper.WidgetBindHelper.createBindIntent(
            appWidgetId = 9,
            provider = provider
        )
        assertEquals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_BIND, intent.action)
        assertEquals(9, intent.getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
    }
}
