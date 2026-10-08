package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.navigationevent.NavigationEvent
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavMotionTest {

    private val motion = VoidMotionScheme(Prefs.AnimationSpeed.STANDARD)

    private fun resolve(
        route: NavRouteKind?,
        left: NavRouteKind? = null,
        right: NavRouteKind? = null
    ): PanelEdge = PanelOrigin.resolve(route, left, right)

    // The text of one transition part ("Fade", "Slide", "Scale"), or null when the part is absent.
    private fun part(transition: Any, name: String): String? {
        val line = transition.toString().lines().first { it.startsWith("$name - ") }
        return line.removePrefix("$name - ").removeSuffix(",").takeUnless { it == "null" }
    }

    // `SpringSpec` has a value-based hashCode and no toString of its own, so a transition prints it
    // as "SpringSpec@<hash>". Two springs with the same damping, stiffness and threshold print alike.
    private fun springOf(transition: Any, name: String): String? =
        part(transition, name)?.let { SPRING.find(it)?.groupValues?.get(1) }

    private fun SpringSpec<*>.id(): String = Integer.toHexString(hashCode())

    private val speeds = listOf(Prefs.AnimationSpeed.FAST, Prefs.AnimationSpeed.STANDARD, Prefs.AnimationSpeed.SLOW)

    @Test
    fun drawerRisesFromTheBottomAndNotificationsDropFromTheTop() {
        assertEquals(PanelEdge.Bottom, resolve(NavRouteKind.AppDrawer))
        assertEquals(PanelEdge.Top, resolve(NavRouteKind.NotificationPanel))
    }

    @Test
    fun settingsHasNoEdge() {
        assertEquals(PanelEdge.None, resolve(NavRouteKind.Settings))
    }

    @Test
    fun fixedPanels_keepTheirEdgeWhateverTheSwipeAssignments() {
        val sides = listOf(NavRouteKind.Notes, NavRouteKind.Widgets, NavRouteKind.AppDrawer, NavRouteKind.Settings)
        sides.forEach { assigned ->
            assertEquals(PanelEdge.Bottom, resolve(NavRouteKind.AppDrawer, left = assigned, right = assigned))
            assertEquals(PanelEdge.Top, resolve(NavRouteKind.NotificationPanel, left = assigned, right = assigned))
            assertEquals(PanelEdge.None, resolve(NavRouteKind.Settings, left = assigned, right = assigned))
        }
    }

    @Test
    fun panelAssignedToTheLeftSwipe_entersFromTheLeftEdge() {
        assertEquals(
            PanelEdge.Left,
            resolve(NavRouteKind.Notes, left = NavRouteKind.Notes, right = NavRouteKind.Widgets)
        )
    }

    @Test
    fun panelAssignedToTheRightSwipe_entersFromTheRightEdge() {
        assertEquals(
            PanelEdge.Right,
            resolve(NavRouteKind.Widgets, left = NavRouteKind.Notes, right = NavRouteKind.Widgets)
        )
    }

    @Test
    fun panelAssignedToBothSwipes_entersFromTheLeftEdge() {
        assertEquals(
            PanelEdge.Left,
            resolve(NavRouteKind.Widgets, left = NavRouteKind.Widgets, right = NavRouteKind.Widgets)
        )
    }

    @Test
    fun unassignedPanelsHomeAndUnknownRoutes_haveNoEdge() {
        assertEquals(
            PanelEdge.None,
            resolve(NavRouteKind.NotificationSummary, left = NavRouteKind.Notes, right = NavRouteKind.Widgets)
        )
        assertEquals(PanelEdge.None, resolve(NavRouteKind.Home, left = NavRouteKind.Notes, right = NavRouteKind.Widgets))
        assertEquals(PanelEdge.None, resolve(null, left = NavRouteKind.Notes, right = NavRouteKind.Widgets))
        assertEquals(PanelEdge.None, resolve(NavRouteKind.Notes))
    }

    @Test
    fun unknownRouteWithUnassignedSwipes_hasNoEdge() {
        // The production case: routeKind() is null for an unrecognised destination and forSwipeAction
        // is null for every action that opens no routed panel, so `null == null` must not match.
        assertEquals(PanelEdge.None, resolve(null))
        assertEquals(PanelEdge.None, resolve(null, left = null, right = NavRouteKind.Widgets))
        assertEquals(PanelEdge.None, resolve(null, left = NavRouteKind.Notes, right = null))
    }

    @Test
    fun swipeActions_mapToTheirPanels() {
        assertEquals(NavRouteKind.NotificationSummary, NavRouteKind.forSwipeAction(Prefs.SwipeAction.NOTIFICATION_SUMMARY))
        assertEquals(NavRouteKind.Widgets, NavRouteKind.forSwipeAction(Prefs.SwipeAction.WIDGETS))
        assertEquals(NavRouteKind.Notes, NavRouteKind.forSwipeAction(Prefs.SwipeAction.NOTES))
    }

    @Test
    fun swipeActionsWithoutARoutedPanel_mapToNothing() {
        listOf(
            Prefs.SwipeAction.NOTIFICATIONS,
            Prefs.SwipeAction.APP,
            Prefs.SwipeAction.ACCESSIBILITY,
            Prefs.SwipeAction.NONE,
            "unknown"
        ).forEach { assertNull(it, NavRouteKind.forSwipeAction(it)) }
    }

    @Test
    fun everyEdgeAndSpeed_buildsTheExpectedPartsWithTheExpectedSprings() {
        speeds.forEach { speed ->
            val scheme = VoidMotionScheme(speed)
            val slide = scheme.slowSpatialSpec(IntOffset.VisibilityThreshold).id()
            val scale = scheme.slowScaleSpec().id()
            PanelEdge.entries.forEach { edge ->
                val label = "$speed $edge"
                val enter = NavMotion.enter(edge, scheme)
                val exit = NavMotion.exit(edge, scheme)
                val popEnter = NavMotion.popEnter(edge, scheme)
                val popExit = NavMotion.popExit(edge, scheme)
                if (edge == PanelEdge.None) {
                    assertEquals(label, scale, springOf(enter, "Scale"))
                    assertEquals(label, scheme.defaultEffectsSpec<Float>().id(), springOf(enter, "Fade"))
                    assertEquals(label, scale, springOf(exit, "Scale"))
                    assertEquals(label, scheme.fastEffectsSpec<Float>().id(), springOf(exit, "Fade"))
                    assertEquals(label, scale, springOf(popEnter, "Scale"))
                    assertEquals(label, scheme.fastEffectsSpec<Float>().id(), springOf(popEnter, "Fade"))
                    assertEquals(label, scale, springOf(popExit, "Scale"))
                    assertEquals(label, scheme.defaultEffectsSpec<Float>().id(), springOf(popExit, "Fade"))
                    listOf(enter, exit, popEnter, popExit).forEach { assertNull(label, part(it, "Slide")) }
                } else {
                    assertEquals(label, slide, springOf(enter, "Slide"))
                    assertEquals(label, slide, springOf(exit, "Slide"))
                    assertEquals(label, slide, springOf(popEnter, "Slide"))
                    assertEquals(label, slide, springOf(popExit, "Slide"))
                    assertEquals(label, scheme.slowEffectsSpec<Float>().id(), springOf(exit, "Fade"))
                    assertEquals(label, scheme.slowEffectsSpec<Float>().id(), springOf(popEnter, "Fade"))
                    // The panel itself never fades; only the screen under it dims. Nothing scales.
                    listOf(enter, popExit).forEach { assertNull(label, part(it, "Fade")) }
                    listOf(enter, exit, popEnter, popExit).forEach { assertNull(label, part(it, "Scale")) }
                }
            }
        }
    }

    @Test
    fun springIdentity_tellsSpeedsAndFamiliesApart() {
        val ids = speeds.map { VoidMotionScheme(it).slowSpatialSpec(IntOffset.VisibilityThreshold).id() }
        assertEquals(ids.size, ids.toSet().size)
        assertNotEquals(
            motion.slowSpatialSpec(IntOffset.VisibilityThreshold).id(),
            motion.slowEffectsSpec(IntOffset.VisibilityThreshold).id()
        )
        assertNotEquals(motion.slowScaleSpec().id(), motion.slowSpatialSpec<Float>().id())
    }

    @Test
    fun panelFromAnEdge_slidesInWithoutFadeOrScale() {
        val enter = NavMotion.enter(PanelEdge.Right, motion)
        assertTrue(part(enter, "Slide") != null)
        assertNull(part(enter, "Fade"))
        assertNull(part(enter, "Scale"))
    }

    @Test
    fun panelFromAnEdge_slidesOutWithoutFadeOrScale() {
        val exit = NavMotion.popExit(PanelEdge.Right, motion)
        assertTrue(part(exit, "Slide") != null)
        assertNull(part(exit, "Fade"))
        assertNull(part(exit, "Scale"))
    }

    @Test
    fun screenUnderAnEdgePanel_slidesAndDimsToTheScrimOpacity() {
        val exit = NavMotion.exit(PanelEdge.Left, motion)
        assertTrue(part(exit, "Slide") != null)
        assertTrue(part(exit, "Fade").orEmpty().contains("alpha=0.68"))
        assertNull(part(exit, "Scale"))
    }

    @Test
    fun screenUnderAnEdgePanel_returnsFromTheScrimOpacity() {
        val enter = NavMotion.popEnter(PanelEdge.Left, motion)
        assertTrue(part(enter, "Slide") != null)
        assertTrue(part(enter, "Fade").orEmpty().contains("alpha=0.68"))
        assertNull(part(enter, "Scale"))
    }

    @Test
    fun panelWithoutAnEdge_scalesAndFadesInPlace() {
        val enter = NavMotion.enter(PanelEdge.None, motion)
        assertNull(part(enter, "Slide"))
        assertTrue(part(enter, "Scale").orEmpty().contains("scale=0.8"))
        assertTrue(part(enter, "Fade") != null)

        val exit = NavMotion.popExit(PanelEdge.None, motion)
        assertNull(part(exit, "Slide"))
        assertTrue(part(exit, "Scale").orEmpty().contains("scale=0.8"))
        assertTrue(part(exit, "Fade") != null)
    }

    @Test
    fun screenUnderAPanelWithoutAnEdge_growsAndFadesInPlace() {
        val exit = NavMotion.exit(PanelEdge.None, motion)
        assertNull(part(exit, "Slide"))
        assertTrue(part(exit, "Scale").orEmpty().contains("scale=1.1"))
        assertTrue(part(exit, "Fade").orEmpty().contains("alpha=0.0"))

        val enter = NavMotion.popEnter(PanelEdge.None, motion)
        assertNull(part(enter, "Slide"))
        assertTrue(part(enter, "Scale").orEmpty().contains("scale=1.1"))
        assertTrue(part(enter, "Fade").orEmpty().contains("alpha=0.0"))
    }

    // ---- predictive back ----

    // `TweenSpec` has a value-based hashCode and prints as "TweenSpec@<hash>", like `SpringSpec`.
    private fun tweenOf(transition: Any, name: String): String? =
        part(transition, name)?.let { TWEEN.find(it)?.groupValues?.get(1) }

    // The easing is written out, not read from `PredictiveBackMath.easing`, so that changing the curve fails this test.
    private fun tween(durationMillis: Int, delayMillis: Int = 0, linear: Boolean = false): String =
        Integer.toHexString(
            tween<Float>(
                durationMillis,
                delayMillis,
                if (linear) LinearEasing else CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
            ).hashCode()
        )

    private val edgeMarginPx = 21f

    @Test
    fun predictivePopExit_shrinksMovesAndFadesOutOnTheGuidanceTimings() {
        val exit = NavMotion.predictivePopExit(NavigationEvent.EDGE_LEFT, motion, edgeMarginPx)
        assertTrue(part(exit, "Scale").orEmpty().contains("scale=0.9"))
        assertTrue(part(exit, "Fade").orEmpty().contains("alpha=0.0"))
        assertTrue(part(exit, "Slide") != null)
        // Scale and slide play the whole 300 ms on the guidance curve; the fade is over after 35% of it.
        assertEquals(tween(300), tweenOf(exit, "Scale"))
        assertEquals(tween(300), tweenOf(exit, "Slide"))
        assertEquals(tween(105, linear = true), tweenOf(exit, "Fade"))
    }

    @Test
    fun predictivePopEnter_growsFromTheOutsideAndFadesInAfterThirtyFivePercent() {
        val enter = NavMotion.predictivePopEnter(motion)
        assertTrue(part(enter, "Scale").orEmpty().contains("scale=1.1"))
        assertTrue(part(enter, "Fade").orEmpty().contains("alpha=0.0"))
        assertNull(part(enter, "Slide"))
        assertEquals(tween(300), tweenOf(enter, "Scale"))
        assertEquals(tween(195, delayMillis = 105, linear = true), tweenOf(enter, "Fade"))
    }

    @Test
    fun predictiveTransitions_playForTheAnimationSpeedAndEndTogether() {
        val durations = mapOf(Prefs.AnimationSpeed.FAST to 180, Prefs.AnimationSpeed.STANDARD to 300, Prefs.AnimationSpeed.SLOW to 480)
        durations.forEach { (speed, duration) ->
            val scheme = VoidMotionScheme(speed)
            val exit = NavMotion.predictivePopExit(NavigationEvent.EDGE_NONE, scheme, edgeMarginPx)
            val enter = NavMotion.predictivePopEnter(scheme)
            assertEquals(speed, tween(duration), tweenOf(exit, "Scale"))
            assertEquals(speed, tween(duration), tweenOf(enter, "Scale"))
            val fadeOut = PredictiveBackMath.fadeOutMillis(duration)
            assertEquals(speed, tween(fadeOut, linear = true), tweenOf(exit, "Fade"))
            assertEquals(
                speed,
                tween(duration - fadeOut, delayMillis = fadeOut, linear = true),
                tweenOf(enter, "Fade")
            )
        }
    }

    @Test
    fun predictiveTransitions_areSprungNowhere() {
        // A seek maps the gesture progress to time, so springs would end at an unrelated moment.
        val exit = NavMotion.predictivePopExit(NavigationEvent.EDGE_RIGHT, motion, edgeMarginPx)
        val enter = NavMotion.predictivePopEnter(motion)
        listOf(exit to listOf("Scale", "Slide", "Fade"), enter to listOf("Scale", "Fade")).forEach { (transition, parts) ->
            parts.forEach { assertNull("$it", springOf(transition, it)) }
        }
    }

    // ---- leaving Settings ----

    @Test
    fun settingsClosed_isTrueWhenSettingsIsLeftForAnyScreen() {
        assertTrue(NavRouteKind.settingsClosed(NavRouteKind.Settings, NavRouteKind.AppDrawer))
        assertTrue(NavRouteKind.settingsClosed(NavRouteKind.Settings, NavRouteKind.Home))
        assertTrue(NavRouteKind.settingsClosed(NavRouteKind.Settings, null))
    }

    @Test
    fun settingsClosed_isFalseOtherwise() {
        assertFalse(NavRouteKind.settingsClosed(null, NavRouteKind.Settings))
        assertFalse(NavRouteKind.settingsClosed(NavRouteKind.Settings, NavRouteKind.Settings))
        assertFalse(NavRouteKind.settingsClosed(NavRouteKind.Home, NavRouteKind.AppDrawer))
        assertFalse(NavRouteKind.settingsClosed(NavRouteKind.AppDrawer, NavRouteKind.Settings))
        assertFalse(NavRouteKind.settingsClosed(null, null))
    }

    // ---- shouldPopPanel ----

    @Test
    fun shouldPopPanel_popsThePanelOnTopWhileAScreenIsLeftUnderIt() {
        assertTrue(shouldPopPanel(panelIsTop = true, hasEntryBelow = true))
    }

    @Test
    fun shouldPopPanel_ignoresADismissFromAPanelThatIsAlreadyClosing() {
        // Regression: Settings closing over the drawer, a second dismiss from Settings popped the drawer too.
        assertFalse(shouldPopPanel(panelIsTop = false, hasEntryBelow = true))
        assertFalse(shouldPopPanel(panelIsTop = false, hasEntryBelow = false))
    }

    @Test
    fun shouldPopPanel_neverPopsHome() {
        assertFalse(shouldPopPanel(panelIsTop = true, hasEntryBelow = false))
    }

    // ---- isAtRest ----

    @Test
    fun isAtRest_whenVisibleAndHeadedForVisible() {
        assertTrue(isAtRest(EnterExitState.Visible, EnterExitState.Visible))
    }

    @Test
    fun isAtRest_notWhileATransitionStillMovesTheScreen() {
        // Regression: a swipe that started while a panel was still leaving stacked a second panel.
        val others = EnterExitState.entries.flatMap { current -> EnterExitState.entries.map { target -> current to target } }
            .filterNot { it == EnterExitState.Visible to EnterExitState.Visible }
        assertEquals(8, others.size)
        others.forEach { (current, target) -> assertFalse("$current -> $target", isAtRest(current, target)) }
    }

    private companion object {
        val SPRING = Regex("""SpringSpec@([0-9a-f]+)""")
        val TWEEN = Regex("""TweenSpec@([0-9a-f]+)""")
    }
}
