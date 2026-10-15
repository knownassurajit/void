package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

/**
 * Whether a swipe may start on Home, and when not, why. It is derived from what the navigation
 * controller says about the Home entry (see [homeSwipeGate]), never from the internals of the
 * `NavHost` transition, and it is read inside the Home destination instead of being captured from
 * the outer composition by the `NavGraph` builder lambda: that lambda is not necessarily called
 * again for a destination that is already composed, so a captured value can stay behind.
 */
enum class HomeSwipeGate {
    /** Home is the screen on top and nothing moves it: a swipe may start. */
    Open,

    /** Another screen is on top of Home: no swipe, and Home does not take the back press. */
    NotOnTop,

    /**
     * Home is the screen on top but cannot be touched yet: a navigation transition still moves it
     * (a panel is leaving) or the activity is not resumed. A swipe that started now would stack a
     * second panel on top of the one that is leaving.
     */
    Busy;

    /** Whether a swipe may start. */
    val isOpen: Boolean get() = this == Open

    /** Whether Home is the destination on top of the back stack (it handles the back press). */
    val isOnTop: Boolean get() = this != NotOnTop

    /** The reason that goes into the log when a swipe is blocked. */
    val logReason: String
        get() = when (this) {
            Open -> "open"
            NotOnTop -> "not_on_top"
            Busy -> "busy"
        }
}

/**
 * The gate of the Home swipe from the two facts the navigation controller keeps: whether the Home
 * entry is the top entry of the back stack ([isOnTop], `currentBackStackEntry`) and whether it is
 * resumed ([isResumed], its lifecycle). The controller only resumes the top entry once no
 * transition involves it any more (`NavHost` marks the transition complete), and never while the
 * activity is not resumed, so "resumed" means "settled and touchable".
 */
fun homeSwipeGate(isOnTop: Boolean, isResumed: Boolean): HomeSwipeGate = when {
    !isOnTop -> HomeSwipeGate.NotOnTop
    !isResumed -> HomeSwipeGate.Busy
    else -> HomeSwipeGate.Open
}
