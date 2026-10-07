package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.ui.unit.IntOffset
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
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

    private companion object {
        val SPRING = Regex("""SpringSpec@([0-9a-f]+)""")
    }
}
