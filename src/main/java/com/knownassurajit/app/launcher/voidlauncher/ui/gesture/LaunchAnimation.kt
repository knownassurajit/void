package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import android.os.Build
import androidx.annotation.AnimRes
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge

/**
 * The window animation of an app that a swipe launches: the app slides in from the side the finger
 * moved toward ([enter], the whole width or height) while Home leaves the same way, a shorter
 * distance and dimmed ([exit], see `res/anim`). Both are `ActivityOptions.makeCustomAnimation` pairs.
 *
 * The names are the edge the app comes in from, the physical one: with the left swipe action the
 * finger moves right and the app comes in from the left. They are not the direction of the finger.
 *
 * The system plays these, not Compose, so they ignore the animation-speed setting and are scaled
 * only by the system animation scale (Android's "Remove animations" turns them off).
 *
 * Only up to Android 11 ([playsOn]): from Android 12 the system ignores a custom animation for a
 * launch that opens a new task and plays its own, and an app cannot opt in without a privileged
 * permission (`ActivityOptions.makeCustomTaskAnimation`).
 */
enum class LaunchAnimation(@AnimRes val enter: Int, @AnimRes val exit: Int) {
    FromLeft(R.anim.slide_in_left, R.anim.slide_out_right),
    FromRight(R.anim.slide_in_right, R.anim.slide_out_left),
    FromTop(R.anim.slide_in_top, R.anim.slide_out_bottom),
    FromBottom(R.anim.slide_in_bottom, R.anim.slide_out_top);

    companion object {
        /** The last Android version (11) whose launch of another app's task honours `ActivityOptions.makeCustomAnimation`. */
        const val LAST_SDK_WITH_CUSTOM_TASK_ANIMATION = Build.VERSION_CODES.R

        /** Whether a device on API level [sdkInt] plays these animations for an app that opens in its own task. */
        fun playsOn(sdkInt: Int): Boolean = sdkInt <= LAST_SDK_WITH_CUSTOM_TASK_ANIMATION
    }
}

/** The launch animation of an app that comes in from this edge, or `null` for [PanelEdge.None] (the default window animation). */
fun PanelEdge.launchAnimation(): LaunchAnimation? = when (this) {
    PanelEdge.Left -> LaunchAnimation.FromLeft
    PanelEdge.Right -> LaunchAnimation.FromRight
    PanelEdge.Top -> LaunchAnimation.FromTop
    PanelEdge.Bottom -> LaunchAnimation.FromBottom
    PanelEdge.None -> null
}
