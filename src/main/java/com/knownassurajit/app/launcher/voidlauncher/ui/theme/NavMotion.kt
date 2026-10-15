package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/** The edge the panel being composed entered from. Provided per destination by [NavPanel]. */
val LocalPanelEdge = compositionLocalOf { PanelEdge.None }
val LocalContentSwipeToBack = compositionLocalOf { true }

/**
 * Transitions between Home and its panels. Every function takes the [PanelEdge] of the **panel**
 * (the screen that is opening or closing), whichever layer it plays on:
 *
 * | Transition | Plays on | NavHost passes the edge of |
 * |---|---|---|
 * | [enter] | the panel opening | `targetState` |
 * | [exit] | the screen under it | `targetState` |
 * | [popEnter] | the screen under it, coming back | `initialState` |
 * | [popExit] | the panel closing | `initialState` |
 *
 * A panel from an edge slides in with no fade while the screen under it moves the same way by a
 * parallax distance and dims. A panel without an edge ([PanelEdge.None]) scales and fades instead
 * (Material shared axis Z). A pop plays the push backwards.
 *
 * A system back gesture on Android 14 or later (a swipe from a screen edge) plays
 * [predictivePopExit] and [predictivePopEnter] instead of the pop pair, whatever the panel: the
 * Material predictive-back animation, which follows the finger (see [PredictiveBackMath]). So does
 * a Back key press that lasts longer than a frame on Android 14 or later: the platform starts the
 * same predictive back for it, with no progress and, in the Android 14 and 16 sources, the left
 * edge, and these lambdas cannot tell the two apart. API 26 to 33, every in-app pop (the back
 * arrow, a swipe inside a panel, TalkBack's Dismiss action) and a key whose down and up arrive in
 * one frame play the pop pair.
 */
object NavMotion {
    /** The panel opens: it slides in from its edge, or (no edge) scales up and fades in. */
    fun enter(edge: PanelEdge, motion: VoidMotionScheme): EnterTransition =
        if (edge == PanelEdge.None) {
            scaleIn(motion.slowScaleSpec(), NavTravel.sharedAxisPanelScale) +
                fadeIn(motion.defaultEffectsSpec())
        } else {
            slideIn(motion.slowSpatialSpec(IntOffset.VisibilityThreshold)) { NavTravel.enterStart(edge, it) }
        }

    /** The screen under an opening panel moves the way the panel moves and dims (no edge: it scales up and fades out). */
    fun exit(edge: PanelEdge, motion: VoidMotionScheme): ExitTransition =
        if (edge == PanelEdge.None) {
            scaleOut(motion.slowScaleSpec(), NavTravel.sharedAxisCoveredScale) +
                fadeOut(motion.fastEffectsSpec())
        } else {
            slideOut(motion.slowSpatialSpec(IntOffset.VisibilityThreshold)) { NavTravel.exitEnd(edge, it) } +
                fadeOut(motion.slowEffectsSpec(), VoidAlpha.coveredByPanel)
        }

    /** The screen under a closing panel returns: [exit] played backwards. */
    fun popEnter(edge: PanelEdge, motion: VoidMotionScheme): EnterTransition =
        if (edge == PanelEdge.None) {
            scaleIn(motion.slowScaleSpec(), NavTravel.sharedAxisCoveredScale) +
                fadeIn(motion.fastEffectsSpec())
        } else {
            slideIn(motion.slowSpatialSpec(IntOffset.VisibilityThreshold)) { NavTravel.popEnterStart(edge, it) } +
                fadeIn(motion.slowEffectsSpec(), VoidAlpha.coveredByPanel)
        }

    /** The panel closes: [enter] played backwards. */
    fun popExit(edge: PanelEdge, motion: VoidMotionScheme): ExitTransition =
        if (edge == PanelEdge.None) {
            scaleOut(motion.slowScaleSpec(), NavTravel.sharedAxisPanelScale) +
                fadeOut(motion.defaultEffectsSpec())
        } else {
            slideOut(motion.slowSpatialSpec(IntOffset.VisibilityThreshold)) { NavTravel.popExitEnd(edge, it) }
        }

    /**
     * The screen being left by a back gesture that started at [swipeEdge] (a `NavigationEvent`
     * edge) shrinks to 90%, moves sideways the way the finger moves by at most `W / 20 - 8 dp`
     * ([edgeMarginPx] is the 8 dp), and has faded out by 35% of the gesture. The same for every
     * route.
     */
    fun predictivePopExit(swipeEdge: Int, motion: VoidMotionScheme, edgeMarginPx: Float): ExitTransition {
        val duration = PredictiveBackMath.durationMillis(motion.speedFactor)
        return scaleOut(tween(duration, easing = PredictiveBackMath.easing), PredictiveBackMath.exitScale) +
            slideOut(tween(duration, easing = PredictiveBackMath.easing)) { fullSize ->
                PredictiveBackMath.shiftOffset(swipeEdge, fullSize, edgeMarginPx)
            } +
            fadeOut(tween(PredictiveBackMath.fadeOutMillis(duration), easing = LinearEasing))
    }

    /** The screen under it grows from 110% to 100% and fades in from 35% of the gesture on. */
    fun predictivePopEnter(motion: VoidMotionScheme): EnterTransition {
        val duration = PredictiveBackMath.durationMillis(motion.speedFactor)
        return scaleIn(tween(duration, easing = PredictiveBackMath.easing), PredictiveBackMath.enterScale) +
            fadeIn(
                tween(
                    durationMillis = PredictiveBackMath.fadeInMillis(duration),
                    delayMillis = PredictiveBackMath.fadeInDelayMillis(duration),
                    easing = LinearEasing
                )
            )
    }
}

/**
 * Wraps the content of every destination except Home in an opaque black surface and tells it which
 * [edge] it entered from. No screen paints a background of its own, so without this surface the
 * screen under a sliding panel would show through it. Home stays transparent over the black window.
 */
@Composable
fun NavPanel(edge: PanelEdge, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPanelEdge provides edge) {
        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
            content()
        }
    }
}
