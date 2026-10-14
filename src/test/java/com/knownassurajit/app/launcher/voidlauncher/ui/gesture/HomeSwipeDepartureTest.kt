package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import androidx.compose.ui.unit.IntSize
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavTravel
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSwipeDepartureTest {

    private val app = SwipeTarget.App("an.app", PanelEdge.Left)
    private val panel = SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Right)

    // ---- what Home does after a commit ----

    @Test
    fun anAppThatLaunchedWhereTheSystemDoesNotMoveHome_carriesHomeOnInTheSwipeDirection() {
        assertEquals(
            HomeAfterCommit.Depart(PanelEdge.Left),
            homeAfterCommit(app, ran = true, systemMovesHome = false)
        )
        assertEquals(
            HomeAfterCommit.Depart(PanelEdge.Right),
            homeAfterCommit(SwipeTarget.App("an.app", PanelEdge.Right), ran = true, systemMovesHome = false)
        )
    }

    @Test
    fun anAppThatLaunchedWhereTheSystemMovesTheHomeWindow_settlesQuicklySoNothingMovesTwice() {
        assertEquals(HomeAfterCommit.SettleQuickly, homeAfterCommit(app, ran = true, systemMovesHome = true))
    }

    @Test
    fun aPanelOrTheNotificationList_handsOverToTheNavHostTransitionOnEveryAndroidVersion() {
        listOf(true, false).forEach { systemMovesHome ->
            assertEquals(HomeAfterCommit.HandOff, homeAfterCommit(panel, ran = true, systemMovesHome = systemMovesHome))
            assertEquals(
                HomeAfterCommit.HandOff,
                homeAfterCommit(SwipeTarget.Notifications, ran = true, systemMovesHome = systemMovesHome)
            )
        }
    }

    @Test
    fun anActionThatCouldNotRun_springsBackWhateverItWas() {
        listOf(true, false).forEach { systemMovesHome ->
            listOf(app, panel, SwipeTarget.Notifications, SwipeTarget.None).forEach { target ->
                assertEquals(
                    "$target systemMovesHome=$systemMovesHome",
                    HomeAfterCommit.SpringBack,
                    homeAfterCommit(target, ran = false, systemMovesHome = systemMovesHome)
                )
            }
        }
    }

    // ---- where Home stops ----

    private val size = IntSize(1_080, 2_400)

    @Test
    fun homeDepartsThirtyPercentOfTheWidthTheWayTheFingerMoves() {
        // Left edge: the finger moves right and Home with it.
        assertEquals(324f, departureOffsetPx(PanelEdge.Left, size), 0f)
        assertEquals(-324f, departureOffsetPx(PanelEdge.Right, size), 0f)
    }

    @Test
    fun homeDepartsTwentyPercentOfTheHeightOnTheVerticalAxis() {
        assertEquals(480f, departureOffsetPx(PanelEdge.Top, size), 0f)
        assertEquals(-480f, departureOffsetPx(PanelEdge.Bottom, size), 0f)
    }

    @Test
    fun withoutAnEdge_homeStaysPut() {
        assertEquals(0f, departureOffsetPx(PanelEdge.None, size), 0f)
    }

    @Test
    fun theDeparturePlace_isWhereAPanelOfThatEdgeParksTheScreenUnderIt() {
        PanelEdge.entries.forEach { edge ->
            val parked = NavTravel.exitEnd(edge, size)
            val along = when (edge) {
                PanelEdge.Left, PanelEdge.Right -> parked.x
                PanelEdge.Top, PanelEdge.Bottom -> parked.y
                PanelEdge.None -> 0
            }
            assertEquals("$edge", along.toFloat(), departureOffsetPx(edge, size), 0f)
        }
    }

    // ---- the dimming on the way ----

    @Test
    fun homeIsOpaqueWhereItStartsAndDimmedToTheCoveredOpacityWhereItStops() {
        val departure = HomeDeparture(fromPx = 40f, toPx = 324f)
        assertEquals(1f, departure.alphaAt(40f), 0f)
        assertEquals(VoidAlpha.coveredByPanel, departure.alphaAt(324f), 0.0001f)
    }

    @Test
    fun homeDimsInStepWithItsTravel() {
        val departure = HomeDeparture(fromPx = 0f, toPx = 300f)
        assertEquals((1f + VoidAlpha.coveredByPanel) / 2f, departure.alphaAt(150f), 0.0001f)
        assertTrue(departure.alphaAt(100f) > departure.alphaAt(200f))
    }

    @Test
    fun theDimmingFollowsTheTravelTheOtherWayToo() {
        val departure = HomeDeparture(fromPx = -30f, toPx = -330f)
        assertEquals(1f, departure.alphaAt(-30f), 0f)
        assertEquals((1f + VoidAlpha.coveredByPanel) / 2f, departure.alphaAt(-180f), 0.0001f)
        assertEquals(VoidAlpha.coveredByPanel, departure.alphaAt(-330f), 0.0001f)
    }

    @Test
    fun aSpringThatOvershootsOrRewindsNeverDimsPastTheEndsOfTheRange() {
        val departure = HomeDeparture(fromPx = 40f, toPx = 324f)
        assertEquals(VoidAlpha.coveredByPanel, departure.alphaAt(330f), 0.0001f)
        // Back past where it started (Home returning to rest after a recall): opaque.
        assertEquals(1f, departure.alphaAt(0f), 0f)
    }

    @Test
    fun aDepartureWithoutAnyTravel_isDimmedAtOnce() {
        assertEquals(VoidAlpha.coveredByPanel, HomeDeparture(fromPx = 50f, toPx = 50f).alphaAt(50f), 0.0001f)
    }
}
