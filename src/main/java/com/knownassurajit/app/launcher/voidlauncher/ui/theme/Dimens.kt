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

    /** The smallest touch target (Material and Android accessibility guidance). */
    val minTouchTarget = 48.dp

    /** The smallest height of a panel's title row, so a trailing button never makes titles jump between panels. */
    val panelTitleMinHeight = minTouchTarget

    /** Space above and below an app's name in the app library. */
    val drawerRowPadding = 14.dp

    /** Inner padding of a notification or summary card, and the gap between cards. */
    val cardPadding = 16.dp
    val cardGap = 8.dp

    /** Elevation of a bar that floats over content (the widget edit bar), and its tonal lift. */
    val floatingBarElevation = 8.dp
    val floatingBarTonalElevation = 3.dp

    /** Elevation of a header that slides in over content (the Widgets header). */
    val overlayHeaderElevation = 6.dp

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

    /** How far a pull past the top of the app list closes the drawer (an overscroll of about two rows). */
    val drawerClosePull = 96.dp

    /** Release speed, per second, that closes the drawer after any pull past the top of its list. */
    val drawerCloseVelocity = 1200.dp

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
