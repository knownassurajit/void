package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** The axis a drag is locked to. */
enum class SwipeAxis { Horizontal, Vertical }

/**
 * Pure arithmetic of a swipe, so that the thresholds can be unit tested. Distances that depend on
 * the display are in pixels, as the pointer reports them, and are compared in dp (`px / density`),
 * so a swipe needs the same finger travel on every screen.
 */
object SwipeMath {
    /** The pointer must travel this many touch slops before an axis is picked (12 dp for the usual 8 dp slop). */
    const val lockSlopMultiplier = 1.5f

    /** A drag this many touch slops long gets an axis even if it is diagonal (24 dp for the usual 8 dp slop). */
    const val forcedLockSlopMultiplier = 3f

    /**
     * The main axis must be at least this many times longer than the other one for an early lock,
     * which means the drag is within about 34 degrees of the axis.
     */
    const val axisDominance = 1.5f

    /**
     * The axis a drag from the touch-down point is locked to, or `null` while it is too short to
     * tell. [dx] and [dy] are the distance from the down point, so the travel before the touch slop
     * counts. A drag within about 34 degrees of an axis locks to it at [lockSlopMultiplier] touch
     * slops; a more diagonal one (a thumb moves in an arc) locks to its longer axis at
     * [forcedLockSlopMultiplier] touch slops, and an exact diagonal goes to [SwipeAxis.Vertical].
     * Diagonals are never rejected. Callers lock once: see [AxisLock].
     */
    fun lockAxis(dx: Float, dy: Float, slopPx: Float): SwipeAxis? {
        val absX = abs(dx)
        val absY = abs(dy)
        val distance = hypot(absX, absY)
        if (distance < slopPx * lockSlopMultiplier) return null
        val dominant = if (absX > absY) SwipeAxis.Horizontal else SwipeAxis.Vertical
        val clearlyAlongAxis = max(absX, absY) >= min(absX, absY) * axisDominance
        return when {
            clearlyAlongAxis -> dominant
            distance >= slopPx * forcedLockSlopMultiplier -> dominant
            else -> null
        }
    }

    /** The shortest travel, in pixels, that counts as a drag: the distance [lockAxis] needs to lock a straight drag. */
    fun lockDistancePx(slopPx: Float): Float = slopPx * lockSlopMultiplier

    /**
     * Resistance for a drag that is not allowed to follow the finger all the way: `limit * x / (x +
     * limit)`. It follows the finger one to one at first (slope 1 at 0), then slows down and never
     * reaches [limit]. It is odd, so a negative [x] gives the mirrored result.
     *
     * Nothing calls it yet: it is reserved for a finger-tracked swipe (the peek of a panel that is
     * about to open), which does not exist today (see `docs/architecture/motion.md`, Limits).
     */
    fun rubberBand(x: Float, limit: Float): Float {
        if (limit <= 0f) return 0f
        val distance = abs(x)
        return sign(x) * limit * distance / (distance + limit)
    }

    /**
     * Whether a swipe that is released commits. [travelPx] and [velocityPxPerSecond] are measured
     * along the direction that commits, so a negative value means the finger went the other way.
     * The swipe commits when it travelled [VoidDimens.swipeCommitDistance], or when it is a flick of
     * at least [VoidDimens.swipeFlickVelocity] that already travelled the axis lock distance (a
     * twitch is not a flick). A flick of that speed the other way cancels it whatever the distance:
     * the finger is going back. Both limits and this order are those of the Material 3
     * `SwipeToDismissBox` (`AnchoredDraggableState` settles on the anchor the velocity points to
     * once it reaches the threshold, and only otherwise looks at the distance), which also decides
     * on release. [density] is pixels per dp.
     */
    fun shouldCommit(travelPx: Float, velocityPxPerSecond: Float, slopPx: Float, density: Float): Boolean {
        val flickDp = velocityPxPerSecond / density
        if (flickDp <= -VoidDimens.swipeFlickVelocity.value) return false
        val travelDp = travelPx / density
        if (travelDp >= VoidDimens.swipeCommitDistance.value) return true
        return flickDp >= VoidDimens.swipeFlickVelocity.value && travelPx >= lockDistancePx(slopPx)
    }
}

/** Locks to the first axis [SwipeMath.lockAxis] finds and never changes it again for the rest of the drag. */
class AxisLock(private val slopPx: Float) {
    var axis: SwipeAxis? = null
        private set

    /** The locked axis after taking a drag of [dx], [dy] from the down point into account, or `null` while undecided. */
    fun update(dx: Float, dy: Float): SwipeAxis? {
        if (axis == null) axis = SwipeMath.lockAxis(dx, dy, slopPx)
        return axis
    }
}
