package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import androidx.compose.ui.unit.IntSize
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavDirection
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavLayer
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavTravel
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha

/** What Home does once a swipe has been released over the commit distance. */
internal sealed interface HomeAfterCommit {
    /**
     * The action opens a screen of VOID: the `NavHost` transition moves Home too, on spatial slow,
     * so Home's own offset settles on that spring and the two add up to one movement.
     */
    data object HandOff : HomeAfterCommit

    /**
     * An app is launching and the system's own animation moves the Home window (the custom
     * launch animation of Android 11 and earlier): Home just settles quickly, so the content
     * is not moved twice.
     */
    data object SettleQuickly : HomeAfterCommit

    /**
     * An app is launching and nothing else moves Home (Android 12 and later, where the system owns
     * the animation and ignores a launcher's): Home carries on in the direction of the swipe to
     * the place and dimming a panel gives it ([departureOffsetPx], [VoidAlpha.coveredByPanel]) and
     * stays there until VOID is resumed (see [HomeSwipeState.reset]) or the launch turns out not
     * to have taken VOID off the screen ([HomeSwipeState.settle]).
     */
    data class Depart(val edge: PanelEdge) : HomeAfterCommit

    /** The action could not run: Home springs back to rest as for a cancelled swipe. */
    data object SpringBack : HomeAfterCommit
}

/**
 * What Home does after a committed swipe. [ran] is whether the action could run (the launch of an
 * app already started when this is decided: it is never delayed for Home's motion) and
 * [systemMovesHome] whether the system's launch animation of this Android version moves the Home
 * window itself (see `LaunchAnimation.playsOn`).
 */
internal fun homeAfterCommit(target: SwipeTarget, ran: Boolean, systemMovesHome: Boolean): HomeAfterCommit = when {
    !ran -> HomeAfterCommit.SpringBack
    target is SwipeTarget.App -> if (systemMovesHome) HomeAfterCommit.SettleQuickly else HomeAfterCommit.Depart(target.edge)
    else -> HomeAfterCommit.HandOff
}

/**
 * Where Home stops when it departs on [edge] in an area of [size] pixels, along the axis of the
 * swipe (negative is left or up): the offset the screen under a panel from that edge is parked at
 * (`NavTravel`, 30% of the width or 20% of the height), so an app that opens and a panel that opens
 * move Home to the same place. Zero for [PanelEdge.None].
 */
internal fun departureOffsetPx(edge: PanelEdge, size: IntSize): Float {
    val parked = NavTravel.path(edge, NavLayer.Covered, NavDirection.Push, size).end
    return when (edge) {
        PanelEdge.Left, PanelEdge.Right -> parked.x.toFloat()
        PanelEdge.Top, PanelEdge.Bottom -> parked.y.toFloat()
        PanelEdge.None -> 0f
    }
}

/**
 * Home's travel from where the finger left it ([fromPx]) to where it is parked ([toPx]), along the
 * swipe axis. Its opacity follows the same progress, from fully opaque to [VoidAlpha.coveredByPanel],
 * so Home dims as it moves, like the screen under a panel.
 */
internal class HomeDeparture(private val fromPx: Float, private val toPx: Float) {
    /** The opacity of Home when it is [offsetPx] along the axis. */
    fun alphaAt(offsetPx: Float): Float {
        val span = toPx - fromPx
        val progress = if (span == 0f) 1f else ((offsetPx - fromPx) / span).coerceIn(0f, 1f)
        return 1f - (1f - VoidAlpha.coveredByPanel) * progress
    }
}
