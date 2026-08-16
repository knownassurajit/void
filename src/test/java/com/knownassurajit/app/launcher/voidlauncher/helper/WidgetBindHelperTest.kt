package com.knownassurajit.app.launcher.voidlauncher.helper

import android.appwidget.AppWidgetManager
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetBindHelperTest {

    @Test
    fun bindAction_matchesPlatformContract() {
        assertEquals(
            "android.appwidget.action.APPWIDGET_BIND",
            AppWidgetManager.ACTION_APPWIDGET_BIND
        )
    }

    @Test
    fun extraKeys_matchPlatformContract() {
        assertEquals("appWidgetId", AppWidgetManager.EXTRA_APPWIDGET_ID)
        assertEquals("appWidgetProvider", AppWidgetManager.EXTRA_APPWIDGET_PROVIDER)
    }
}
