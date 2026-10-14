package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import com.knownassurajit.app.launcher.voidlauncher.MainUiState
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeFeatureFlags
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeTarget
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.dispatchSwipeTarget
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.homeSwipeSettings
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSwipeDispatchTest {

    private val calls = mutableListOf<String>()
    private var launchResult = true

    private fun dispatch(target: SwipeTarget): Boolean = dispatchSwipeTarget(
        target = target,
        onOpenApps = { calls += "apps" },
        onOpenNotificationSummary = { calls += "summary" },
        onOpenWidgets = { calls += "widgets" },
        onOpenNotes = { calls += "notes" },
        onOpenNotifications = { calls += "notifications" },
        onLaunchApp = { packageName, edge ->
            calls += "launch $packageName $edge"
            launchResult
        }
    )

    @Test
    fun theDrawerPanel_opensTheDrawer() {
        assertTrue(dispatch(SwipeTarget.Panel(NavRouteKind.AppDrawer, PanelEdge.Bottom)))
        assertEquals(listOf("apps"), calls)
    }

    @Test
    fun everyOptionalPanel_runsItsOwnOpenCallbackOnly() {
        assertTrue(dispatch(SwipeTarget.Panel(NavRouteKind.NotificationSummary, PanelEdge.Left)))
        assertTrue(dispatch(SwipeTarget.Panel(NavRouteKind.Widgets, PanelEdge.Right)))
        assertTrue(dispatch(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Left)))
        assertEquals(listOf("summary", "widgets", "notes"), calls)
    }

    @Test
    fun theNotificationsTarget_opensTheNotificationList() {
        assertTrue(dispatch(SwipeTarget.Notifications))
        assertEquals(listOf("notifications"), calls)
    }

    @Test
    fun anApp_isLaunchedFromTheEdgeOfTheSwipe() {
        assertTrue(dispatch(SwipeTarget.App("an.app", PanelEdge.Left)))
        assertTrue(dispatch(SwipeTarget.App("another.app", PanelEdge.Right)))
        assertEquals(listOf("launch an.app Left", "launch another.app Right"), calls)
    }

    @Test
    fun anAppThatCouldNotBeLaunched_isAFailedAction() {
        launchResult = false
        assertFalse(dispatch(SwipeTarget.App("an.app", PanelEdge.Left)))
    }

    @Test
    fun nothing_runsNothingAndFails() {
        assertFalse(dispatch(SwipeTarget.None))
        assertTrue(calls.isEmpty())
    }

    @Test
    fun aRouteNoSwipeOpens_runsNothingAndFails() {
        listOf(NavRouteKind.Home, NavRouteKind.Settings, NavRouteKind.NotificationPanel).forEach { route ->
            assertFalse("$route", dispatch(SwipeTarget.Panel(route, PanelEdge.Left)))
        }
        assertTrue(calls.isEmpty())
    }

    // ---- homeSwipeSettings: the screen state and the preferences in their places ----

    private val flags = SwipeFeatureFlags(notes = true, widgets = false, notificationSummary = true)

    @Test
    fun theSettings_takeEachSideFromItsOwnActionAndItsOwnApp() {
        val settings = homeSwipeSettings(
            state = MainUiState(leftSwipeAction = SwipeAction.APP, rightSwipeAction = SwipeAction.APP),
            leftAppPackage = "left.app",
            rightAppPackage = "right.app",
            flags = flags
        )
        assertEquals(SwipeTarget.App("left.app", PanelEdge.Right), settings.targetFor(PanelEdge.Left))
        assertEquals(SwipeTarget.App("right.app", PanelEdge.Left), settings.targetFor(PanelEdge.Right))
    }

    @Test
    fun theSettings_carryTheFeatureSwitchesAndTheScreenSwitches() {
        val settings = homeSwipeSettings(
            state = MainUiState(
                leftSwipeAction = SwipeAction.NOTES,
                rightSwipeAction = SwipeAction.WIDGETS,
                enableGestures = true,
                enableSwipeDownNotifications = false
            ),
            leftAppPackage = "",
            rightAppPackage = "",
            flags = flags
        )
        assertEquals(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Right), settings.targetFor(PanelEdge.Left))
        // Widgets is switched off in the flags.
        assertEquals(SwipeTarget.None, settings.targetFor(PanelEdge.Right))
        // Swipe down has its own switch.
        assertEquals(SwipeTarget.None, settings.targetFor(PanelEdge.Top))
        assertEquals(SwipeTarget.Panel(NavRouteKind.AppDrawer, PanelEdge.Bottom), settings.targetFor(PanelEdge.Bottom))
    }

    @Test
    fun theSettings_turnOffTheSidesWithTheGesturesSwitch() {
        val settings = homeSwipeSettings(
            state = MainUiState(enableGestures = false, enableSwipeDownNotifications = true),
            leftAppPackage = "",
            rightAppPackage = "",
            flags = flags
        )
        assertEquals(SwipeTarget.None, settings.targetFor(PanelEdge.Left))
        assertEquals(SwipeTarget.None, settings.targetFor(PanelEdge.Right))
        assertEquals(SwipeTarget.Notifications, settings.targetFor(PanelEdge.Top))
    }
}
