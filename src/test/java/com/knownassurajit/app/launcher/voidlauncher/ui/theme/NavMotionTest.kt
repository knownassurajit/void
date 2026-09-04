package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class NavMotionTest {

    @Test
    fun drawerAndNotifications_followVerticalAxes() {
        assertEquals(NavAxis.Up, NavMotion.axisForDestination("AppDrawerRoute", null, null))
        assertEquals(NavAxis.Down, NavMotion.axisForDestination("NotificationPanelRoute", null, null))
        assertEquals(NavAxis.Fade, NavMotion.axisForDestination("SettingsRoute", null, null))
    }

    @Test
    fun assignedSwipePanels_enterFromOppositeEdge() {
        assertEquals(
            NavAxis.Start,
            NavMotion.axisForDestination("NotesRoute", "NotesRoute", "WidgetsRoute")
        )
        assertEquals(
            NavAxis.End,
            NavMotion.axisForDestination("WidgetsRoute", "NotesRoute", "WidgetsRoute")
        )
    }

    @Test
    fun durationMs_mapsSpeedKeys() {
        assertEquals(VoidMotion.fastMs, NavMotion.durationMs("fast"))
        assertEquals(VoidMotion.standardMs, NavMotion.durationMs("standard"))
        assertEquals(VoidMotion.slowMs, NavMotion.durationMs("slow"))
    }
}
