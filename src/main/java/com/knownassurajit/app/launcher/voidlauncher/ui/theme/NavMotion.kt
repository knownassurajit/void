package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.IntOffset
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs

enum class NavAxis {
    Up,
    Down,
    Start,
    End,
    Fade
}

val LocalNavEnterAxis = compositionLocalOf { NavAxis.Fade }
val LocalContentSwipeToBack = compositionLocalOf { true }

object NavMotion {
    fun durationMs(speed: String): Int = when (speed) {
        Prefs.AnimationSpeed.FAST -> VoidMotion.fastMs
        Prefs.AnimationSpeed.SLOW -> VoidMotion.slowMs
        else -> VoidMotion.standardMs
    }

    fun axisForDestination(
        routeName: String,
        leftRoute: String?,
        rightRoute: String?
    ): NavAxis = when {
        routeName == "AppDrawerRoute" -> NavAxis.Up
        routeName == "NotificationPanelRoute" -> NavAxis.Down
        routeName == "SettingsRoute" -> NavAxis.Fade
        leftRoute != null && routeName == leftRoute -> NavAxis.Start
        rightRoute != null && routeName == rightRoute -> NavAxis.End
        else -> NavAxis.Fade
    }

    fun enter(axis: NavAxis, durationMs: Int): EnterTransition {
        val fade = tween<Float>(durationMs)
        val slide = VoidMotion.slideSpec<IntOffset>(durationMs)
        return when (axis) {
            NavAxis.Up -> slideInVertically(slide) { it } + fadeIn(fade)
            NavAxis.Down -> slideInVertically(slide) { -it } + fadeIn(fade)
            NavAxis.Start -> slideInHorizontally(slide) { -it } + fadeIn(fade)
            NavAxis.End -> slideInHorizontally(slide) { it } + fadeIn(fade)
            NavAxis.Fade -> fadeIn(fade) + slideInVertically(slide) { it / 12 }
        }
    }

    fun exit(axis: NavAxis, durationMs: Int): ExitTransition {
        val fade = tween<Float>(durationMs)
        val slide = VoidMotion.slideSpec<IntOffset>(durationMs)
        return when (axis) {
            NavAxis.Up -> slideOutVertically(slide) { -it } + fadeOut(fade)
            NavAxis.Down -> slideOutVertically(slide) { it } + fadeOut(fade)
            NavAxis.Start -> slideOutHorizontally(slide) { -it } + fadeOut(fade)
            NavAxis.End -> slideOutHorizontally(slide) { it } + fadeOut(fade)
            NavAxis.Fade -> fadeOut(fade)
        }
    }

    fun popEnter(axis: NavAxis, durationMs: Int): EnterTransition {
        val fade = tween<Float>(durationMs)
        val slide = VoidMotion.slideSpec<IntOffset>(durationMs)
        return when (axis) {
            NavAxis.Up -> slideInVertically(slide) { -it } + fadeIn(fade)
            NavAxis.Down -> slideInVertically(slide) { it } + fadeIn(fade)
            NavAxis.Start -> slideInHorizontally(slide) { it } + fadeIn(fade)
            NavAxis.End -> slideInHorizontally(slide) { -it } + fadeIn(fade)
            NavAxis.Fade -> fadeIn(fade)
        }
    }

    fun popExit(axis: NavAxis, durationMs: Int): ExitTransition {
        val fade = tween<Float>(durationMs)
        val slide = VoidMotion.slideSpec<IntOffset>(durationMs)
        return when (axis) {
            NavAxis.Up -> slideOutVertically(slide) { it } + fadeOut(fade)
            NavAxis.Down -> slideOutVertically(slide) { -it } + fadeOut(fade)
            NavAxis.Start -> slideOutHorizontally(slide) { -it } + fadeOut(fade)
            NavAxis.End -> slideOutHorizontally(slide) { it } + fadeOut(fade)
            NavAxis.Fade -> fadeOut(fade)
        }
    }
}
