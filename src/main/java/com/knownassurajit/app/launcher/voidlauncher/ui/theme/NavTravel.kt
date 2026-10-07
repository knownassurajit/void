package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import kotlin.math.roundToInt

/**
 * The **physical** screen edge a panel enters from. It is never mirrored for right-to-left
 * layouts: a panel follows the finger, and the finger does not mirror.
 *
 * [None] is a panel that does not come from an edge (Settings): it scales and fades in place.
 */
enum class PanelEdge { Left, Right, Top, Bottom, None }

/** The navigation destinations the motion system tells apart. */
enum class NavRouteKind {
    Home,
    AppDrawer,
    Settings,
    NotificationPanel,
    NotificationSummary,
    Notes,
    Widgets;

    companion object {
        /** The panel a Home swipe action opens, or `null` for actions that open no routed panel. */
        fun forSwipeAction(action: String): NavRouteKind? = when (action) {
            Prefs.SwipeAction.NOTIFICATION_SUMMARY -> NotificationSummary
            Prefs.SwipeAction.WIDGETS -> Widgets
            Prefs.SwipeAction.NOTES -> Notes
            else -> null
        }
    }
}

/** Decides which edge each destination enters from. */
object PanelOrigin {
    /**
     * The drawer rises from the bottom, the notification list drops from the top, and Settings
     * has no edge. The panel assigned to the left swipe action enters from the left (the finger
     * moves right), the one assigned to the right swipe action from the right. A destination
     * assigned to both sides enters from the left. Anything else, Home included, has no edge.
     */
    fun resolve(route: NavRouteKind?, leftRoute: NavRouteKind?, rightRoute: NavRouteKind?): PanelEdge = when {
        route == NavRouteKind.AppDrawer -> PanelEdge.Bottom
        route == NavRouteKind.NotificationPanel -> PanelEdge.Top
        route == NavRouteKind.Settings -> PanelEdge.None
        route != null && route == leftRoute -> PanelEdge.Left
        route != null && route == rightRoute -> PanelEdge.Right
        else -> PanelEdge.None
    }
}

/** The two layers of a panel transition. */
enum class NavLayer {
    /** The screen opening or closing. */
    Panel,

    /** The screen under the panel. */
    Covered
}

enum class NavDirection { Push, Pop }

/** Where a layer starts and ends a transition, as offsets from its resting position in pixels. */
data class NavPath(val start: IntOffset, val end: IntOffset) {
    fun reversed(): NavPath = NavPath(start = end, end = start)
}

/**
 * All travel geometry of a panel transition, as pure functions of pixels so the direction of every
 * layer can be unit tested. `NavMotion` feeds [enterStart], [exitEnd], [popEnterStart] and
 * [popExitEnd], which are the start or end of [path], straight to `slideIn` and `slideOut`.
 *
 * Offsets are in screen coordinates: x grows to the right, y grows downward.
 *
 * A push moves the panel from out of sight on its edge to rest, and the covered screen from rest
 * to a parked offset. The covered screen travels the same way as the panel (a shorter distance, as
 * parallax), so the two layers move together instead of crossing. A pop is the same two paths
 * played backwards.
 */
object NavTravel {
    /** How far the covered screen travels, as a fraction of the screen width, for Left and Right panels. */
    const val horizontalParallax = 0.30f

    /** How far the covered screen travels, as a fraction of the screen height, for Top and Bottom panels. */
    const val verticalParallax = 0.20f

    /** Scale a [PanelEdge.None] panel starts a push at (and ends a pop at). */
    const val sharedAxisPanelScale = 0.8f

    /** Scale the covered screen reaches at the end of a push (and starts a pop at) next to a [PanelEdge.None] panel. */
    const val sharedAxisCoveredScale = 1.1f

    /** The offsets [layer] travels between when a panel from [edge] is pushed or popped. */
    fun path(edge: PanelEdge, layer: NavLayer, direction: NavDirection, fullSize: IntSize): NavPath {
        val push = when (layer) {
            NavLayer.Panel -> NavPath(start = panelParked(edge, fullSize), end = IntOffset.Zero)
            NavLayer.Covered -> NavPath(start = IntOffset.Zero, end = coveredParked(edge, fullSize))
        }
        return if (direction == NavDirection.Push) push else push.reversed()
    }

    /** Where the opening panel starts, from [path]: the offset `NavMotion.enter` slides in from. */
    fun enterStart(edge: PanelEdge, fullSize: IntSize): IntOffset =
        path(edge, NavLayer.Panel, NavDirection.Push, fullSize).start

    /** Where the screen under an opening panel ends, from [path]: the offset `NavMotion.exit` slides out to. */
    fun exitEnd(edge: PanelEdge, fullSize: IntSize): IntOffset =
        path(edge, NavLayer.Covered, NavDirection.Push, fullSize).end

    /** Where the screen under a closing panel starts, from [path]: the offset `NavMotion.popEnter` slides in from. */
    fun popEnterStart(edge: PanelEdge, fullSize: IntSize): IntOffset =
        path(edge, NavLayer.Covered, NavDirection.Pop, fullSize).start

    /** Where the closing panel ends, from [path]: the offset `NavMotion.popExit` slides out to. */
    fun popExitEnd(edge: PanelEdge, fullSize: IntSize): IntOffset =
        path(edge, NavLayer.Panel, NavDirection.Pop, fullSize).end

    /** Where the panel waits out of sight on its edge. */
    private fun panelParked(edge: PanelEdge, fullSize: IntSize): IntOffset = when (edge) {
        PanelEdge.Left -> IntOffset(-fullSize.width, 0)
        PanelEdge.Right -> IntOffset(fullSize.width, 0)
        PanelEdge.Top -> IntOffset(0, -fullSize.height)
        PanelEdge.Bottom -> IntOffset(0, fullSize.height)
        PanelEdge.None -> IntOffset.Zero
    }

    /** Where the covered screen waits while the panel is open: shifted the way the panel travels. */
    private fun coveredParked(edge: PanelEdge, fullSize: IntSize): IntOffset {
        val dx = (fullSize.width * horizontalParallax).roundToInt()
        val dy = (fullSize.height * verticalParallax).roundToInt()
        return when (edge) {
            PanelEdge.Left -> IntOffset(dx, 0)
            PanelEdge.Right -> IntOffset(-dx, 0)
            PanelEdge.Top -> IntOffset(0, dy)
            PanelEdge.Bottom -> IntOffset(0, -dy)
            PanelEdge.None -> IntOffset.Zero
        }
    }
}
