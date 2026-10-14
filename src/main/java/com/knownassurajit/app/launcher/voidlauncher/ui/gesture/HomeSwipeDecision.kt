package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlin.math.hypot
import kotlin.math.max

/**
 * What a swipe on Home opens. The target also says which edge the finger brings it in from, so the
 * panel transition, the app launch animation and Home itself move the way the finger moves.
 */
sealed interface SwipeTarget {
    /**
     * A screen of the app. [edge] is the edge the finger brings it in from; the `NavHost` transition
     * chooses the edge it really enters from (`PanelOrigin.resolve`), which differs only for a panel
     * assigned to both swipe actions (the left one wins), a setting the picker does not allow.
     */
    data class Panel(val route: NavRouteKind, val edge: PanelEdge) : SwipeTarget

    /** An app of the device, launched with a window animation that comes in from [edge] where the system plays it. */
    data class App(val packageName: String, val edge: PanelEdge) : SwipeTarget

    /** The notification list (the system shade replaces it later). */
    data object Notifications : SwipeTarget

    /** Nothing is configured for this direction: the swipe does not start. */
    data object None : SwipeTarget
}

/** The optional screens a swipe action may open; a disabled one turns its swipe action into nothing. */
data class SwipeFeatureFlags(val notes: Boolean, val widgets: Boolean, val notificationSummary: Boolean) {
    fun allows(route: NavRouteKind): Boolean = when (route) {
        NavRouteKind.Notes -> notes
        NavRouteKind.Widgets -> widgets
        NavRouteKind.NotificationSummary -> notificationSummary
        else -> false
    }
}

/**
 * What the swipe [action] assigned to a side does when the finger moves so that content comes in
 * from [edge] (the left action: from the left, with the finger moving right). [appPackage] is the
 * app chosen for that side.
 *
 * A panel action is nothing while its feature is off. `ACCESSIBILITY` and unknown values are
 * nothing too: the picker offers the first, but no code runs it yet.
 */
fun resolveSwipe(action: String, edge: PanelEdge, flags: SwipeFeatureFlags, appPackage: String): SwipeTarget {
    if (edge == PanelEdge.None) return SwipeTarget.None
    val route = NavRouteKind.forSwipeAction(action)
    return when {
        route != null -> if (flags.allows(route)) SwipeTarget.Panel(route, edge) else SwipeTarget.None
        action == SwipeAction.NOTIFICATIONS -> SwipeTarget.Notifications
        action == SwipeAction.APP -> if (appPackage.isBlank()) SwipeTarget.None else SwipeTarget.App(appPackage, edge)
        else -> SwipeTarget.None
    }
}

/** The preferences that decide what each direction of a Home swipe does. */
data class HomeSwipeSettings(
    /** "Enable gestures": the left and right swipes. Swipe up is not gated, as before. */
    val gesturesEnabled: Boolean,
    val swipeDownEnabled: Boolean,
    val leftAction: String,
    val rightAction: String,
    val leftAppPackage: String,
    val rightAppPackage: String,
    val flags: SwipeFeatureFlags
) {
    /**
     * What a drag that brings content in from [edge] opens: the left action for the left edge (the
     * finger moves right), the right action for the right edge (the finger moves left), the drawer
     * from the bottom (swipe up) and the notification list from the top (swipe down).
     */
    fun targetFor(edge: PanelEdge): SwipeTarget = when (edge) {
        PanelEdge.Left ->
            if (gesturesEnabled) resolveSwipe(leftAction, PanelEdge.Right, flags, leftAppPackage) else SwipeTarget.None
        PanelEdge.Right ->
            if (gesturesEnabled) resolveSwipe(rightAction, PanelEdge.Left, flags, rightAppPackage) else SwipeTarget.None
        PanelEdge.Bottom -> SwipeTarget.Panel(NavRouteKind.AppDrawer, edge)
        PanelEdge.Top -> if (swipeDownEnabled) SwipeTarget.Notifications else SwipeTarget.None
        PanelEdge.None -> SwipeTarget.None
    }
}

/**
 * Which way the finger moves to bring content in from this edge, along the horizontal axis for
 * [PanelEdge.Left] and [PanelEdge.Right] and the vertical one for [PanelEdge.Top] and
 * [PanelEdge.Bottom]: `+1` toward larger coordinates (right or down), `-1` toward smaller ones
 * (left or up), `0` without an edge. It is the physical direction, never mirrored for
 * right-to-left layouts.
 */
fun PanelEdge.fingerSign(): Int = when (this) {
    PanelEdge.Left, PanelEdge.Top -> 1
    PanelEdge.Right, PanelEdge.Bottom -> -1
    PanelEdge.None -> 0
}

/** The edge content comes in from when the finger moves by [delta] along [axis]. */
fun edgeForFinger(axis: SwipeAxis, delta: Float): PanelEdge = when (axis) {
    SwipeAxis.Horizontal -> if (delta > 0f) PanelEdge.Left else PanelEdge.Right
    SwipeAxis.Vertical -> if (delta > 0f) PanelEdge.Top else PanelEdge.Bottom
}

/**
 * Fires once when a value reaches [thresholdPx] and again only after it has dropped below
 * [REARM_FRACTION] of it, so a finger that wobbles around the threshold does not buzz.
 */
class ThresholdLatch(private val thresholdPx: Float) {
    private var armed = true

    /** Whether [valuePx] just crossed the threshold. */
    fun update(valuePx: Float): Boolean {
        if (armed && valuePx >= thresholdPx) {
            armed = false
            return true
        }
        if (!armed && valuePx < thresholdPx * REARM_FRACTION) armed = true
        return false
    }

    companion object {
        const val REARM_FRACTION = 0.8f
    }
}

/** What [HomeSwipeRecognizer] makes of the pointer so far. */
internal sealed interface HomeSwipeStep {
    /**
     * Whether the pointer change must be consumed. A consumed change cancels the children's tap and
     * long press, so a drag that Home owns is neither a click on a home app nor a reorder.
     */
    val consumesPointer: Boolean

    /** Not a Home swipe (a long press, a child or the system took the pointer): stop watching, consume nothing. */
    data object GiveUp : HomeSwipeStep {
        override val consumesPointer: Boolean = false
    }

    /** Under the touch slop: not a drag yet, so the pointer is still the children's (a tap or a long press may follow). */
    data object Undecided : HomeSwipeStep {
        override val consumesPointer: Boolean = false
    }

    /**
     * Past the touch slop, with nothing for Home to follow: the axis has not locked yet, or it
     * locked toward a direction without a target (nothing configured, a disabled feature). The
     * drag is Home's, so it is consumed until the finger lifts, but nothing moves and nothing opens.
     */
    data object Ignored : HomeSwipeStep {
        override val consumesPointer: Boolean = true
    }

    /**
     * The swipe is locked to a configured direction. Home is [offsetPx] away from rest along the
     * locked axis (negative is left or up) and [thresholdCrossed] is true on the move that took the
     * travel to the commit distance, which is when the haptic cue is due.
     */
    data class Tracking(val offsetPx: Float, val thresholdCrossed: Boolean) : HomeSwipeStep {
        override val consumesPointer: Boolean = true
    }
}

/**
 * The decisions of a Home swipe, away from the pointer events so that they can be unit tested. It
 * is fed what the finger moved since the previous event ([onMove]) and adds it up from the
 * touch-down point, so the travel before the touch slop counts. The movement must be the finger's
 * own (the difference between two positions of the same event), not a difference from the
 * touch-down position.
 *
 * The direction is chosen once, when the axis locks ([SwipeMath.lockAxis]): the sign of the travel
 * on that axis picks the edge content comes in from, and [targetFor] says what that opens. A
 * direction without a target is [HomeSwipeStep.Ignored] for the rest of the drag: Home owns the
 * pointer (the children must not see a tap or a long press) but follows nothing. From then on
 * Home follows the finger toward the target only: a finger that goes back past where it started
 * moves nothing, and never opens the opposite target.
 *
 * Whether the swipe commits is decided on release ([shouldCommit]), so reversing the finger back
 * under the commit distance cancels it.
 *
 * A finger that has not moved a touch slop by [longPressTimeoutMillis] is a long press (the home
 * apps reorder or the app picker), which belongs to the children: the recognizer gives up.
 */
internal class HomeSwipeRecognizer(
    private val slopPx: Float,
    private val density: Float,
    private val longPressTimeoutMillis: Long,
    private val targetFor: (PanelEdge) -> SwipeTarget
) {
    private val axisLock = AxisLock(slopPx)
    private val latch = ThresholdLatch(VoidDimens.swipeCommitDistance.value * density)
    private val peekLimitPx = VoidDimens.swipePeekLimit.value * density
    private var slopCrossed = false
    private var finished = false
    private var ignoring = false
    private var pullsHome = true
    private var lockedAlongPx = 0f

    /** How far the finger has moved from the touch-down point, horizontally and vertically, in pixels. */
    var travelX: Float = 0f
        private set
    var travelY: Float = 0f
        private set

    /** The edge content comes in from, valid once locked ([PanelEdge.None] before). */
    var edge: PanelEdge = PanelEdge.None
        private set

    /** What the swipe opens, valid once locked. */
    var target: SwipeTarget = SwipeTarget.None
        private set

    /** The axis Home follows the finger on, once locked. */
    val axis: SwipeAxis? get() = if (edge == PanelEdge.None) null else axisLock.axis

    /**
     * [movementX] and [movementY] are what the finger moved since the previous event, [consumed] is
     * true when a child took the pointer or the system cancelled it (a cancel arrives as a consumed
     * release), and [elapsedMillis] is the time since the touch-down.
     */
    fun onMove(movementX: Float, movementY: Float, consumed: Boolean, elapsedMillis: Long): HomeSwipeStep {
        travelX += movementX
        travelY += movementY
        if (finished) return HomeSwipeStep.GiveUp
        if (consumed) return giveUp()
        if (!slopCrossed) {
            if (elapsedMillis >= longPressTimeoutMillis) return giveUp()
            if (hypot(travelX, travelY) < slopPx) return HomeSwipeStep.Undecided
            slopCrossed = true
        }
        if (ignoring) return HomeSwipeStep.Ignored
        val locked = axisLock.update(travelX, travelY) ?: return HomeSwipeStep.Ignored
        if (edge == PanelEdge.None) {
            val candidate = edgeForFinger(locked, travelAlong(locked))
            val resolved = targetFor(candidate)
            if (resolved == SwipeTarget.None) {
                ignoring = true
                return HomeSwipeStep.Ignored
            }
            edge = candidate
            target = resolved
            // The notification list always drops from the top: a sideways swipe that opens it must
            // not pull Home sideways as well.
            pullsHome = !(resolved == SwipeTarget.Notifications && locked == SwipeAxis.Horizontal)
            lockedAlongPx = alongPx()
        }
        val along = alongPx()
        val offset = if (pullsHome) {
            edge.fingerSign() * SwipeMath.rubberBand(max(0f, along - lockedAlongPx), peekLimitPx)
        } else {
            0f
        }
        return HomeSwipeStep.Tracking(offset, thresholdCrossed = latch.update(along))
    }

    /**
     * Whether releasing now, with the finger moving at [velocityX], [velocityY] px/s, opens the
     * target: the travel and the speed are those toward the target, see [SwipeMath.shouldCommit].
     */
    fun shouldCommit(velocityX: Float, velocityY: Float): Boolean {
        val locked = axis ?: return false
        return SwipeMath.shouldCommit(
            travelPx = alongPx(),
            velocityPxPerSecond = edge.fingerSign() * velocityAlong(locked, velocityX, velocityY),
            slopPx = slopPx,
            density = density
        )
    }

    /**
     * How fast Home moves when the finger lets go at [velocityX], [velocityY] px/s: the finger
     * speed along the locked axis, slowed down where the rubber band is stiffer. Zero while Home
     * is at rest.
     */
    fun releaseVelocity(velocityX: Float, velocityY: Float): Float {
        val locked = axis ?: return 0f
        if (!pullsHome) return 0f
        val pulled = max(0f, alongPx() - lockedAlongPx)
        if (pulled == 0f) return 0f
        return velocityAlong(locked, velocityX, velocityY) * SwipeMath.rubberBandSlope(pulled, peekLimitPx)
    }

    private fun giveUp(): HomeSwipeStep {
        finished = true
        return HomeSwipeStep.GiveUp
    }

    private fun travelAlong(axis: SwipeAxis): Float = if (axis == SwipeAxis.Horizontal) travelX else travelY

    private fun velocityAlong(axis: SwipeAxis, velocityX: Float, velocityY: Float): Float =
        if (axis == SwipeAxis.Horizontal) velocityX else velocityY

    /** The travel toward the target, negative when the finger went the other way. */
    private fun alongPx(): Float {
        val locked = axisLock.axis ?: return 0f
        return edge.fingerSign() * travelAlong(locked)
    }
}

/** Where the strips that ask the system to leave the screen edges to the app are laid out. */
object GestureEdgeStrip {
    /**
     * Width in pixels of a full-height strip along a screen edge that is excluded from the system
     * back gesture: the system gesture inset of that edge ([systemGestureInsetPx], zero when the
     * navigation mode has no back gesture), but at least [VoidDimens.edgeExclusionMinWidth] (the
     * platform default for the back gesture strip, which the user's back sensitivity setting scales),
     * plus [VoidDimens.edgeExclusionMargin] so a swipe that starts just inside the strip is not lost.
     */
    fun exclusionWidthPx(systemGestureInsetPx: Float, density: Float): Float =
        max(systemGestureInsetPx, VoidDimens.edgeExclusionMinWidth.value * density) +
            VoidDimens.edgeExclusionMargin.value * density
}

fun homeSwipeSettings(
    state: com.knownassurajit.app.launcher.voidlauncher.MainUiState,
    leftAppPackage: String,
    rightAppPackage: String,
    flags: SwipeFeatureFlags
): HomeSwipeSettings = HomeSwipeSettings(
    gesturesEnabled = state.enableGestures,
    swipeDownEnabled = state.enableSwipeDownNotifications,
    leftAction = state.leftSwipeAction,
    rightAction = state.rightSwipeAction,
    leftAppPackage = leftAppPackage,
    rightAppPackage = rightAppPackage,
    flags = flags
)

fun dispatchSwipeTarget(
    target: SwipeTarget,
    onOpenApps: () -> Unit,
    onOpenNotificationSummary: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLaunchApp: (packageName: String, edge: PanelEdge) -> Boolean
): Boolean = when (target) {
    is SwipeTarget.Panel -> when (target.route) {
        NavRouteKind.AppDrawer -> { onOpenApps(); true }
        NavRouteKind.NotificationSummary -> { onOpenNotificationSummary(); true }
        NavRouteKind.Widgets -> { onOpenWidgets(); true }
        NavRouteKind.Notes -> { onOpenNotes(); true }
        else -> false
    }
    SwipeTarget.Notifications -> { onOpenNotifications(); true }
    is SwipeTarget.App -> onLaunchApp(target.packageName, target.edge)
    SwipeTarget.None -> false
}

