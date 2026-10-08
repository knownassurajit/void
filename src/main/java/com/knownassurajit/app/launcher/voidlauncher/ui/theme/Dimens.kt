package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.ui.unit.dp

object VoidDimens {
    val screenPadding = 20.dp
    val sectionSpacing = 16.dp
    val rowSpacing = 8.dp
    val compactSpacing = 4.dp
    val widgetPreviewWidth = 72.dp
    val widgetPreviewHeight = 48.dp
    val fontStateIconSize = 24.dp
    val fontProgressStrokeWidth = 2.dp

    /**
     * Drag distance along the dismiss direction that commits a swipe on release: the Material 3
     * `SwipeToDismissBox` positional threshold (`SwipeToDismissBoxDefaults.positionalThreshold`).
     */
    val swipeCommitDistance = 56.dp

    /**
     * Release speed, per second, that commits a swipe that travelled at least as far as the axis
     * lock distance: the Material 3 `SwipeToDismissBox` `DismissVelocityThreshold`.
     */
    val swipeFlickVelocity = 125.dp

    /** Gap the shrinking screen keeps from the screen edge in a predictive back (Android predictive-back design). */
    val predictiveBackEdgeMargin = 8.dp

    /**
     * The limit of the rubber band that lets Home follow a finger that is opening a panel: Home
     * moves with the finger at first and never farther than this, because the panel itself only
     * starts when the swipe is released.
     */
    val swipePeekLimit = 64.dp

    /** The narrowest strip along a screen edge asked to stay out of the system back gesture: the platform's back gesture inset. */
    val edgeExclusionMinWidth = 30.dp

    /** Extra width of that strip past the inset, so a swipe that starts just inside the system strip still reaches the app. */
    val edgeExclusionMargin = 8.dp
}

object VoidAlpha {
    const val disabled = 0.38f
    const val muted = 0.60f
    const val divider = 0.12f

    /** Opacity of the screen under an open panel: one minus the Material 3 scrim of 0.32. */
    const val coveredByPanel = 0.68f
}
