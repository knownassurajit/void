package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sign

class NavTravelTest {

    private val screen = IntSize(width = 1080, height = 2400)

    private val edgePanels = listOf(PanelEdge.Left, PanelEdge.Right, PanelEdge.Top, PanelEdge.Bottom)

    private fun path(edge: PanelEdge, layer: NavLayer, direction: NavDirection): NavPath =
        NavTravel.path(edge, layer, direction, screen)

    /** How far a layer moves over its path. */
    private fun NavPath.travel(): IntOffset = IntOffset(end.x - start.x, end.y - start.y)

    private fun IntOffset.distance(): Int = abs(x) + abs(y)

    private fun IntOffset.signs(): Pair<Float, Float> = x.toFloat().sign to y.toFloat().sign

    @Test
    fun panel_entersFromItsPhysicalEdge() {
        val start = { edge: PanelEdge -> path(edge, NavLayer.Panel, NavDirection.Push).start }
        assertEquals(IntOffset(-1080, 0), start(PanelEdge.Left))
        assertEquals(IntOffset(1080, 0), start(PanelEdge.Right))
        assertEquals(IntOffset(0, -2400), start(PanelEdge.Top))
        assertEquals(IntOffset(0, 2400), start(PanelEdge.Bottom))
    }

    @Test
    fun panel_endsAtRest() {
        edgePanels.forEach { edge ->
            assertEquals(IntOffset.Zero, path(edge, NavLayer.Panel, NavDirection.Push).end)
        }
    }

    @Test
    fun push_coveredScreenMovesTheSamePhysicalWayAsThePanel() {
        // Regression: the covered screen used to slide out toward the edge the panel came in from,
        // so the two layers crossed and Home visibly glitched against the incoming panel.
        edgePanels.forEach { edge ->
            val panel = path(edge, NavLayer.Panel, NavDirection.Push).travel().signs()
            val covered = path(edge, NavLayer.Covered, NavDirection.Push).travel().signs()
            assertEquals("$edge", panel, covered)
            assertTrue("$edge must move", panel != 0f to 0f)
        }
    }

    @Test
    fun push_travelDirectionIsAwayFromTheEdge() {
        val travel = { edge: PanelEdge, layer: NavLayer -> path(edge, layer, NavDirection.Push).travel() }
        NavLayer.entries.forEach { layer ->
            assertTrue("Left $layer moves right", travel(PanelEdge.Left, layer).x > 0)
            assertTrue("Right $layer moves left", travel(PanelEdge.Right, layer).x < 0)
            assertTrue("Top $layer moves down", travel(PanelEdge.Top, layer).y > 0)
            assertTrue("Bottom $layer moves up", travel(PanelEdge.Bottom, layer).y < 0)
        }
    }

    @Test
    fun push_everyLayerTravelsOnlyAlongThePanelAxis() {
        NavLayer.entries.forEach { layer ->
            listOf(PanelEdge.Left, PanelEdge.Right).forEach { edge ->
                assertEquals(0, path(edge, layer, NavDirection.Push).travel().y)
            }
            listOf(PanelEdge.Top, PanelEdge.Bottom).forEach { edge ->
                assertEquals(0, path(edge, layer, NavDirection.Push).travel().x)
            }
        }
    }

    @Test
    fun push_coveredScreenTravelsAParallaxFractionOfThePanel() {
        assertEquals(324, path(PanelEdge.Left, NavLayer.Covered, NavDirection.Push).end.x)
        assertEquals(-324, path(PanelEdge.Right, NavLayer.Covered, NavDirection.Push).end.x)
        assertEquals(480, path(PanelEdge.Top, NavLayer.Covered, NavDirection.Push).end.y)
        assertEquals(-480, path(PanelEdge.Bottom, NavLayer.Covered, NavDirection.Push).end.y)
        edgePanels.forEach { edge ->
            val panel = path(edge, NavLayer.Panel, NavDirection.Push).travel()
            val covered = path(edge, NavLayer.Covered, NavDirection.Push).travel()
            assertTrue("$edge covered travels less than the panel", covered.distance() < panel.distance())
        }
    }

    @Test
    fun parallaxConstants_areThirtyPercentHorizontalAndTwentyVertical() {
        assertEquals(0.30f, NavTravel.horizontalParallax, 0f)
        assertEquals(0.20f, NavTravel.verticalParallax, 0f)
    }

    @Test
    fun pop_isThePushPlayedBackwards() {
        PanelEdge.entries.forEach { edge ->
            NavLayer.entries.forEach { layer ->
                val push = path(edge, layer, NavDirection.Push)
                val pop = path(edge, layer, NavDirection.Pop)
                assertEquals("$edge $layer start", push.end, pop.start)
                assertEquals("$edge $layer end", push.start, pop.end)
            }
        }
        // One literal case, so a broken NavPath.reversed() cannot hide behind a self-comparison.
        assertEquals(
            NavPath(start = IntOffset.Zero, end = IntOffset(-1080, 0)),
            path(PanelEdge.Left, NavLayer.Panel, NavDirection.Pop)
        )
        assertEquals(
            NavPath(start = IntOffset(0, -480), end = IntOffset.Zero),
            path(PanelEdge.Bottom, NavLayer.Covered, NavDirection.Pop)
        )
    }

    @Test
    fun navPath_reversedSwapsStartAndEnd() {
        val path = NavPath(start = IntOffset(1, 2), end = IntOffset(3, 4))
        assertEquals(NavPath(start = IntOffset(3, 4), end = IntOffset(1, 2)), path.reversed())
    }

    @Test
    fun enterStart_isThePanelParkedOnItsEdge() {
        assertEquals(IntOffset(-1080, 0), NavTravel.enterStart(PanelEdge.Left, screen))
        assertEquals(IntOffset(1080, 0), NavTravel.enterStart(PanelEdge.Right, screen))
        assertEquals(IntOffset(0, -2400), NavTravel.enterStart(PanelEdge.Top, screen))
        assertEquals(IntOffset(0, 2400), NavTravel.enterStart(PanelEdge.Bottom, screen))
    }

    @Test
    fun exitEnd_movesTheCoveredScreenTheWayThePanelMoves() {
        // Regression: this was the negated offset, so Home left toward the edge the panel came from.
        // The panel moves by -enterStart (from its edge to rest); the covered screen must move by exitEnd.
        edgePanels.forEach { edge ->
            val panelTravel = NavTravel.enterStart(edge, screen).let { IntOffset(-it.x, -it.y) }
            val coveredTravel = NavTravel.exitEnd(edge, screen)
            assertEquals("$edge", panelTravel.signs(), coveredTravel.signs())
            assertTrue("$edge covered travels less", coveredTravel.distance() < panelTravel.distance())
        }
        assertEquals(IntOffset(324, 0), NavTravel.exitEnd(PanelEdge.Left, screen))
        assertEquals(IntOffset(-324, 0), NavTravel.exitEnd(PanelEdge.Right, screen))
        assertEquals(IntOffset(0, 480), NavTravel.exitEnd(PanelEdge.Top, screen))
        assertEquals(IntOffset(0, -480), NavTravel.exitEnd(PanelEdge.Bottom, screen))
    }

    @Test
    fun popEnterStart_isWhereExitEndLeftTheCoveredScreen() {
        edgePanels.forEach { edge ->
            assertEquals("$edge", NavTravel.exitEnd(edge, screen), NavTravel.popEnterStart(edge, screen))
        }
    }

    @Test
    fun popExitEnd_isWhereTheEnterStartParkedThePanel() {
        edgePanels.forEach { edge ->
            assertEquals("$edge", NavTravel.enterStart(edge, screen), NavTravel.popExitEnd(edge, screen))
        }
    }

    @Test
    fun popExitEnd_andPopEnterStart_moveTheTwoLayersTheSamePhysicalWay() {
        // On a pop the panel leaves toward its edge by popExitEnd, the covered screen returns from
        // popEnterStart to rest; both travel toward the panel's edge.
        edgePanels.forEach { edge ->
            val panelTravel = NavTravel.popExitEnd(edge, screen)
            val coveredTravel = NavTravel.popEnterStart(edge, screen).let { IntOffset(-it.x, -it.y) }
            assertEquals("$edge", panelTravel.signs(), coveredTravel.signs())
        }
    }

    @Test
    fun transitionOffsets_ofAPanelWithoutAnEdge_areAllZero() {
        assertEquals(IntOffset.Zero, NavTravel.enterStart(PanelEdge.None, screen))
        assertEquals(IntOffset.Zero, NavTravel.exitEnd(PanelEdge.None, screen))
        assertEquals(IntOffset.Zero, NavTravel.popEnterStart(PanelEdge.None, screen))
        assertEquals(IntOffset.Zero, NavTravel.popExitEnd(PanelEdge.None, screen))
    }

    @Test
    fun pop_coveredScreenStillMovesTheSamePhysicalWayAsThePanel() {
        edgePanels.forEach { edge ->
            val panel = path(edge, NavLayer.Panel, NavDirection.Pop).travel().signs()
            val covered = path(edge, NavLayer.Covered, NavDirection.Pop).travel().signs()
            assertEquals("$edge", panel, covered)
        }
    }

    @Test
    fun pop_panelLeavesTowardItsEdge() {
        assertTrue(path(PanelEdge.Left, NavLayer.Panel, NavDirection.Pop).travel().x < 0)
        assertTrue(path(PanelEdge.Right, NavLayer.Panel, NavDirection.Pop).travel().x > 0)
        assertTrue(path(PanelEdge.Top, NavLayer.Panel, NavDirection.Pop).travel().y < 0)
        assertTrue(path(PanelEdge.Bottom, NavLayer.Panel, NavDirection.Pop).travel().y > 0)
    }

    @Test
    fun none_hasNoTranslationInEitherDirection() {
        NavLayer.entries.forEach { layer ->
            NavDirection.entries.forEach { direction ->
                val path = path(PanelEdge.None, layer, direction)
                assertEquals(IntOffset.Zero, path.start)
                assertEquals(IntOffset.Zero, path.end)
            }
        }
    }

    @Test
    fun none_isSharedAxisZ_panelGrowsFromEightyPercentAndCoveredScreenGrowsToOneTen() {
        assertEquals(0.8f, NavTravel.sharedAxisPanelScale, 0f)
        assertEquals(1.1f, NavTravel.sharedAxisCoveredScale, 0f)
    }

    @Test
    fun travel_scalesWithTheScreenSize() {
        val landscape = IntSize(width = 2400, height = 1080)
        assertEquals(
            IntOffset(-2400, 0),
            NavTravel.path(PanelEdge.Left, NavLayer.Panel, NavDirection.Push, landscape).start
        )
        assertEquals(
            IntOffset(0, -216),
            NavTravel.path(PanelEdge.Bottom, NavLayer.Covered, NavDirection.Push, landscape).end
        )
    }
}
