package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSwipeDecisionTest {

    private val allOn = SwipeFeatureFlags(notes = true, widgets = true, notificationSummary = true)
    private val allOff = SwipeFeatureFlags(notes = false, widgets = false, notificationSummary = false)
    private val sideEdges = listOf(PanelEdge.Left, PanelEdge.Right)
    private val realEdges = listOf(PanelEdge.Left, PanelEdge.Right, PanelEdge.Top, PanelEdge.Bottom)

    private fun resolve(
        action: String,
        edge: PanelEdge,
        flags: SwipeFeatureFlags = allOn,
        appPackage: String = "an.app"
    ): SwipeTarget = resolveSwipe(action, edge, flags, appPackage)

    // ---- resolveSwipe: every action on every edge ----

    @Test
    fun resolveSwipe_panelActionsOpenTheirPanelFromTheEdgeOfTheSwipe() {
        realEdges.forEach { edge ->
            assertEquals(
                "$edge",
                SwipeTarget.Panel(NavRouteKind.NotificationSummary, edge),
                resolve(SwipeAction.NOTIFICATION_SUMMARY, edge)
            )
            assertEquals("$edge", SwipeTarget.Panel(NavRouteKind.Widgets, edge), resolve(SwipeAction.WIDGETS, edge))
            assertEquals("$edge", SwipeTarget.Panel(NavRouteKind.Notes, edge), resolve(SwipeAction.NOTES, edge))
        }
    }

    @Test
    fun resolveSwipe_aPanelActionIsNothingWhileItsFeatureIsOff() {
        realEdges.forEach { edge ->
            assertEquals(SwipeTarget.None, resolve(SwipeAction.NOTES, edge, allOn.copy(notes = false)))
            assertEquals(SwipeTarget.None, resolve(SwipeAction.WIDGETS, edge, allOn.copy(widgets = false)))
            assertEquals(
                SwipeTarget.None,
                resolve(SwipeAction.NOTIFICATION_SUMMARY, edge, allOn.copy(notificationSummary = false))
            )
        }
    }

    @Test
    fun resolveSwipe_oneFeatureSwitchDoesNotTurnOffTheOtherPanels() {
        assertEquals(
            SwipeTarget.Panel(NavRouteKind.Widgets, PanelEdge.Right),
            resolve(SwipeAction.WIDGETS, PanelEdge.Right, allOn.copy(notes = false, notificationSummary = false))
        )
        assertEquals(SwipeTarget.None, resolve(SwipeAction.NOTES, PanelEdge.Left, allOff.copy(widgets = true)))
    }

    @Test
    fun resolveSwipe_notificationsIsNotGatedByAFeatureSwitch() {
        realEdges.forEach { edge ->
            assertEquals(SwipeTarget.Notifications, resolve(SwipeAction.NOTIFICATIONS, edge, allOff))
        }
    }

    @Test
    fun resolveSwipe_anAppComesInFromTheEdgeOfTheSwipe() {
        realEdges.forEach { edge ->
            assertEquals(SwipeTarget.App("an.app", edge), resolve(SwipeAction.APP, edge, appPackage = "an.app"))
        }
    }

    @Test
    fun resolveSwipe_anAppActionWithoutAChosenAppIsNothing() {
        assertEquals(SwipeTarget.None, resolve(SwipeAction.APP, PanelEdge.Left, appPackage = ""))
        assertEquals(SwipeTarget.None, resolve(SwipeAction.APP, PanelEdge.Right, appPackage = "  "))
    }

    @Test
    fun resolveSwipe_accessibilityNoneAndUnknownActionsAreNothing() {
        // ACCESSIBILITY is offered in the picker but nothing runs it yet.
        listOf(SwipeAction.ACCESSIBILITY, SwipeAction.NONE, "", "something_new").forEach { action ->
            realEdges.forEach { edge ->
                assertEquals("$action $edge", SwipeTarget.None, resolve(action, edge))
            }
        }
    }

    @Test
    fun resolveSwipe_aSwipeWithoutAnEdgeOpensNothing() {
        listOf(
            SwipeAction.NOTES,
            SwipeAction.NOTIFICATIONS,
            SwipeAction.APP,
            SwipeAction.WIDGETS,
            SwipeAction.NOTIFICATION_SUMMARY
        ).forEach { action ->
            assertEquals(action, SwipeTarget.None, resolve(action, PanelEdge.None))
        }
    }

    // ---- HomeSwipeSettings: what each direction opens ----

    private fun settings(
        gestures: Boolean = true,
        swipeDown: Boolean = true,
        left: String = SwipeAction.NOTES,
        right: String = SwipeAction.WIDGETS,
        leftApp: String = "left.app",
        rightApp: String = "right.app",
        flags: SwipeFeatureFlags = allOn
    ) = HomeSwipeSettings(gestures, swipeDown, left, right, leftApp, rightApp, flags)

    @Test
    fun targetFor_theLeftSwipeBringsContentFromTheRightAndTheRightSwipeFromTheLeft() {
        // Sides are named after the finger: a swipe left (content from the right edge) runs the left action.
        val s = settings()
        assertEquals(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Right), s.targetFor(PanelEdge.Right))
        assertEquals(SwipeTarget.Panel(NavRouteKind.Widgets, PanelEdge.Left), s.targetFor(PanelEdge.Left))
    }

    @Test
    fun targetFor_anAppComesInFromTheSideTheFingerBringsItFrom() {
        // Regression: a swipe right launched the app (and sent Home) toward the opposite edge, against the finger.
        val s = settings(left = SwipeAction.APP, right = SwipeAction.APP)
        assertEquals(SwipeTarget.App("left.app", PanelEdge.Right), s.targetFor(PanelEdge.Right))
        assertEquals(SwipeTarget.App("right.app", PanelEdge.Left), s.targetFor(PanelEdge.Left))
    }

    @Test
    fun targetFor_swipeUpOpensTheDrawerFromTheBottomWhateverTheOtherSettings() {
        val drawer = SwipeTarget.Panel(NavRouteKind.AppDrawer, PanelEdge.Bottom)
        assertEquals(drawer, settings().targetFor(PanelEdge.Bottom))
        assertEquals(drawer, settings(gestures = false, swipeDown = false).targetFor(PanelEdge.Bottom))
    }

    @Test
    fun targetFor_swipeDownOpensTheNotificationsOnlyWhenEnabled() {
        assertEquals(SwipeTarget.Notifications, settings(swipeDown = true).targetFor(PanelEdge.Top))
        assertEquals(SwipeTarget.None, settings(swipeDown = false).targetFor(PanelEdge.Top))
        // The horizontal gestures switch does not gate it.
        assertEquals(SwipeTarget.Notifications, settings(gestures = false).targetFor(PanelEdge.Top))
    }

    @Test
    fun targetFor_gesturesSwitchTurnsOffOnlyTheHorizontalSwipes() {
        val s = settings(gestures = false)
        sideEdges.forEach { assertEquals("$it", SwipeTarget.None, s.targetFor(it)) }
    }

    @Test
    fun targetFor_aSideWithADisabledFeatureOrNoActionIsNotConfigured() {
        val s = settings(left = SwipeAction.NOTES, right = SwipeAction.NONE, flags = allOn.copy(notes = false))
        assertEquals(SwipeTarget.None, s.targetFor(PanelEdge.Left))
        assertEquals(SwipeTarget.None, s.targetFor(PanelEdge.Right))
    }

    @Test
    fun targetFor_noEdgeIsNothing() {
        assertEquals(SwipeTarget.None, settings().targetFor(PanelEdge.None))
    }

    // ---- finger direction <-> edge ----

    @Test
    fun edgeForFinger_contentComesInFromTheSideTheFingerMovesAwayFrom() {
        assertEquals(PanelEdge.Left, edgeForFinger(SwipeAxis.Horizontal, 40f))
        assertEquals(PanelEdge.Right, edgeForFinger(SwipeAxis.Horizontal, -40f))
        assertEquals(PanelEdge.Top, edgeForFinger(SwipeAxis.Vertical, 40f))
        assertEquals(PanelEdge.Bottom, edgeForFinger(SwipeAxis.Vertical, -40f))
    }

    @Test
    fun fingerSign_isTheInverseOfEdgeForFinger() {
        val axisOf = mapOf(
            PanelEdge.Left to SwipeAxis.Horizontal,
            PanelEdge.Right to SwipeAxis.Horizontal,
            PanelEdge.Top to SwipeAxis.Vertical,
            PanelEdge.Bottom to SwipeAxis.Vertical
        )
        realEdges.forEach { edge ->
            assertEquals("$edge", edge, edgeForFinger(axisOf.getValue(edge), edge.fingerSign().toFloat()))
        }
    }

    @Test
    fun fingerSign_isOppositeForOppositeEdges() {
        assertEquals(-PanelEdge.Left.fingerSign(), PanelEdge.Right.fingerSign())
        assertEquals(-PanelEdge.Top.fingerSign(), PanelEdge.Bottom.fingerSign())
        assertEquals(1, PanelEdge.Left.fingerSign())
        assertEquals(-1, PanelEdge.Bottom.fingerSign())
    }

    @Test
    fun noEdge_hasNoFingerDirection() {
        assertEquals(0, PanelEdge.None.fingerSign())
    }

    // ---- ThresholdLatch ----

    @Test
    fun latch_firesOnceWhenTheThresholdIsReached() {
        val latch = ThresholdLatch(100f)
        assertFalse(latch.update(0f))
        assertFalse(latch.update(99.9f))
        assertTrue(latch.update(100f))
        assertFalse(latch.update(100f))
        assertFalse(latch.update(400f))
    }

    @Test
    fun latch_doesNotFireAgainUntilTheValueDropsBelowEightyPercent() {
        val latch = ThresholdLatch(100f)
        assertTrue(latch.update(120f))
        // Wobbling between 80% and the threshold never re-arms it.
        assertFalse(latch.update(85f))
        assertFalse(latch.update(100f))
        assertFalse(latch.update(80f))
        assertFalse(latch.update(130f))
        // Below 80% it re-arms, and the next crossing fires.
        assertFalse(latch.update(79.9f))
        assertTrue(latch.update(100f))
    }

    @Test
    fun latch_aValueUnderTheThresholdNeverFires() {
        val latch = ThresholdLatch(100f)
        (0..99).forEach { assertFalse(latch.update(it.toFloat())) }
    }

    // ---- GestureEdgeStrip ----

    @Test
    fun exclusionWidth_isTheDefaultBackInsetPlusTheMarginWhenTheSystemReportsNone() {
        // No back gesture (3-button navigation): 30 dp + 8 dp at 2x.
        assertEquals(76f, GestureEdgeStrip.exclusionWidthPx(systemGestureInsetPx = 0f, density = 2f), 0f)
    }

    @Test
    fun exclusionWidth_followsALargerSystemInsetOfTheUsersBackSensitivity() {
        // 48 dp inset at 2x = 96 px, plus 16 px of margin.
        assertEquals(112f, GestureEdgeStrip.exclusionWidthPx(systemGestureInsetPx = 96f, density = 2f), 0f)
    }

    @Test
    fun exclusionWidth_neverShrinksBelowTheDefaultInsetForASmallerReportedOne() {
        // A reported 12 dp inset (24 px at 2x) is still excluded 30 dp wide.
        assertEquals(76f, GestureEdgeStrip.exclusionWidthPx(systemGestureInsetPx = 24f, density = 2f), 0f)
    }

    @Test
    fun exclusionWidth_isTheSameNumberOfDpOnEveryDensity() {
        listOf(1f, 1.5f, 2.625f, 3.5f).forEach { density ->
            assertEquals("$density", 38f * density, GestureEdgeStrip.exclusionWidthPx(0f, density), 0.001f)
        }
    }
}
