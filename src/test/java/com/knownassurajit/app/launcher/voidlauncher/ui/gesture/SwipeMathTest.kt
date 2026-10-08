package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class SwipeMathTest {

    // A typical 8 dp touch slop on a 2x display. The lock distance is 1.5 slops = 24 px = 12 dp.
    private val slop = 16f

    private fun lock(dx: Float, dy: Float): SwipeAxis? = SwipeMath.lockAxis(dx, dy, slop)

    /** A drag of [distance] px at [degrees] from the horizontal axis. */
    private fun lockAt(degrees: Double, distance: Float): SwipeAxis? {
        val radians = Math.toRadians(degrees)
        return lock((distance * cos(radians)).toFloat(), (distance * sin(radians)).toFloat())
    }

    // ---- lockAxis ----

    @Test
    fun lockAxis_staysUndecidedUntilTheDragIsLongEnough() {
        assertNull(lock(0f, 0f))
        assertNull(lock(23f, 0f))
        assertNull(lock(0f, -23f))
        assertEquals(SwipeAxis.Horizontal, lock(24f, 0f))
        assertEquals(SwipeAxis.Vertical, lock(0f, 24f))
    }

    @Test
    fun lockAxis_ignoresTheSignOfTheDrag() {
        assertEquals(SwipeAxis.Horizontal, lock(-40f, 3f))
        assertEquals(SwipeAxis.Horizontal, lock(40f, -3f))
        assertEquals(SwipeAxis.Vertical, lock(3f, -40f))
        assertEquals(SwipeAxis.Vertical, lock(-3f, 40f))
    }

    @Test
    fun lockAxis_dragWithin34DegreesOfAnAxisLocksEarly() {
        // 30 degrees from the horizontal (60 from the vertical), 30 px long: well under the 48 px forced lock.
        assertEquals(SwipeAxis.Horizontal, lockAt(30.0, 30f))
        assertEquals(SwipeAxis.Vertical, lockAt(60.0, 30f))
        assertEquals(SwipeAxis.Horizontal, lockAt(0.0, 30f))
        assertEquals(SwipeAxis.Vertical, lockAt(90.0, 30f))
    }

    @Test
    fun lockAxis_coneEdgeIsAtADominanceOfOneAndAHalf() {
        assertEquals(SwipeAxis.Horizontal, lock(30f, 20f))
        assertNull(lock(29.8f, 20f))
        assertEquals(SwipeAxis.Vertical, lock(20f, 30f))
        assertNull(lock(20f, 29.8f))
    }

    @Test
    fun lockAxis_diagonalWaitsForTheForcedLockDistanceThenPicksTheLongerAxis() {
        // 45 degrees: 40 px long is not enough (the forced lock is 3 slops = 48 px) ...
        assertNull(lockAt(45.0, 40f))
        // ... and 55 px locks to the longer axis, never "no axis".
        assertEquals(SwipeAxis.Horizontal, lock(40f, 36f))
        assertEquals(SwipeAxis.Vertical, lock(36f, 40f))
    }

    @Test
    fun lockAxis_exactDiagonalGoesToTheVerticalAxis() {
        assertEquals(SwipeAxis.Vertical, lock(40f, 40f))
        assertEquals(SwipeAxis.Vertical, lock(-40f, 40f))
    }

    @Test
    fun lockAxis_aDiagonalIsNeverRejected() {
        (1..89 step 4).forEach { degrees ->
            assertNotNull("$degrees degrees", lockAt(degrees.toDouble(), 60f))
        }
    }

    @Test
    fun lockAxis_dragIsMeasuredFromTheDownPoint_soTravelBeforeTheSlopCounts() {
        // The drag position is the distance from the touch-down point: 30 px in one event locks at once,
        // exactly like 10 events of 3 px each.
        assertEquals(SwipeAxis.Horizontal, lock(30f, 0f))
        val steps = (1..10).map { lock(it * 3f, 0f) }
        assertNull(steps[6])
        assertEquals(SwipeAxis.Horizontal, steps[7])
    }

    @Test
    fun axisLock_neverChangesOnceLocked() {
        val axisLock = AxisLock(slop)
        assertNull(axisLock.update(5f, 0f))
        assertNull(axisLock.axis)
        assertEquals(SwipeAxis.Horizontal, axisLock.update(30f, 2f))
        // The finger now swings to a vertical drag: the axis stays.
        assertEquals(SwipeAxis.Horizontal, axisLock.update(30f, 300f))
        assertEquals(SwipeAxis.Horizontal, axisLock.update(0f, 500f))
        assertEquals(SwipeAxis.Horizontal, axisLock.axis)
    }

    @Test
    fun axisLock_staysUndecidedWhileTheDragIsShort() {
        val axisLock = AxisLock(slop)
        assertNull(axisLock.update(10f, 10f))
        assertNull(axisLock.update(12f, 3f))
        assertNull(axisLock.axis)
    }

    // ---- rubberBand ----

    @Test
    fun rubberBand_startsAtZeroAndFollowsTheFingerOneToOne() {
        assertEquals(0f, SwipeMath.rubberBand(0f, 24f), 0f)
        val x = 0.01f
        assertEquals(1f, SwipeMath.rubberBand(x, 24f) / x, 0.001f)
    }

    @Test
    fun rubberBand_neverReachesTheLimitButApproachesIt() {
        val limit = 24f
        listOf(1f, 24f, 100f, 1_000f, 100_000f).forEach {
            assertTrue("$it", SwipeMath.rubberBand(it, limit) < limit)
        }
        assertEquals(limit, SwipeMath.rubberBand(1_000_000f, limit), 0.01f)
    }

    @Test
    fun rubberBand_halfwayAtTheLimitDistance() {
        assertEquals(12f, SwipeMath.rubberBand(24f, 24f), 0.0001f)
    }

    @Test
    fun rubberBand_growsWithEveryPixelOfTravel() {
        var previous = -1f
        (0..400).forEach {
            val value = SwipeMath.rubberBand(it.toFloat(), 24f)
            assertTrue("$it", value > previous)
            previous = value
        }
    }

    @Test
    fun rubberBand_isMirroredForNegativeTravel() {
        listOf(1f, 24f, 100f).forEach {
            assertEquals(-SwipeMath.rubberBand(it, 24f), SwipeMath.rubberBand(-it, 24f), 0f)
        }
    }

    @Test
    fun rubberBand_withoutALimitDoesNotMove() {
        assertEquals(0f, SwipeMath.rubberBand(50f, 0f), 0f)
        assertEquals(0f, SwipeMath.rubberBand(50f, -5f), 0f)
    }

    // ---- shouldCommit ----

    private fun commit(travelPx: Float, velocityPxPerSecond: Float = 0f, density: Float = 2f): Boolean =
        SwipeMath.shouldCommit(travelPx, velocityPxPerSecond, slop, density)

    @Test
    fun shouldCommit_byDistance_is56dp() {
        // 56 dp at 2x is 112 px.
        assertTrue(commit(112f))
        assertTrue(commit(400f))
        assertFalse(commit(111f))
        assertFalse(commit(0f))
    }

    @Test
    fun shouldCommit_byDistance_isTheSameFingerTravelOnEveryDensity() {
        // The same 56 dp is 84 px at 1.5x, 112 px at 2x, 196 px at 3.5x.
        listOf(1.5f, 2f, 3.5f).forEach { density ->
            assertTrue("$density", commit(56f * density, density = density))
            assertFalse("$density", commit(56f * density - 1f, density = density))
        }
    }

    @Test
    fun shouldCommit_aShortDragFarFromEnoughForADensityIsEnoughForAnother() {
        // 100 px is 50 dp at 2x and 66.7 dp at 1.5x.
        assertFalse(commit(100f, density = 2f))
        assertTrue(commit(100f, density = 1.5f))
    }

    @Test
    fun shouldCommit_byFlick_is125dpPerSecond() {
        // 125 dp/s at 2x is 250 px/s; 40 px is past the 24 px lock distance but far from 56 dp.
        assertTrue(commit(40f, velocityPxPerSecond = 250f))
        assertTrue(commit(40f, velocityPxPerSecond = 2_000f))
        assertFalse(commit(40f, velocityPxPerSecond = 249f))
    }

    @Test
    fun shouldCommit_flickVelocityIsConvertedWithTheDensity() {
        // 250 px/s is 125 dp/s at 2x but only 71 dp/s at 3.5x.
        assertTrue(commit(60f, velocityPxPerSecond = 250f, density = 2f))
        assertFalse(commit(60f, velocityPxPerSecond = 250f, density = 3.5f))
        assertTrue(commit(60f, velocityPxPerSecond = 438f, density = 3.5f))
    }

    @Test
    fun shouldCommit_aFlickShorterThanTheLockDistanceIsATwitch() {
        assertFalse(commit(23f, velocityPxPerSecond = 5_000f))
        assertTrue(commit(24f, velocityPxPerSecond = 5_000f))
    }

    @Test
    fun shouldCommit_velocityTheWrongWayNeverCommits() {
        // Travelled far enough to count as a drag, but the finger is now moving back.
        assertFalse(commit(40f, velocityPxPerSecond = -2_000f))
        assertFalse(commit(40f, velocityPxPerSecond = -250f))
    }

    @Test
    fun shouldCommit_travelTheWrongWayNeverCommits() {
        assertFalse(commit(-400f))
        assertFalse(commit(-400f, velocityPxPerSecond = 5_000f))
    }

    @Test
    fun shouldCommit_aSlowDragPastTheDistanceStillCommits() {
        assertTrue(commit(200f, velocityPxPerSecond = 0f))
        // Moving back, but slower than a flick (249 px/s is 124.5 dp/s at 2x): the distance decides.
        assertTrue(commit(200f, velocityPxPerSecond = -249f))
    }

    @Test
    fun shouldCommit_aFlickBackCancelsEvenAfterTheDistance() {
        // 100 dp at 2x, then the finger flicks back at 125 dp/s or faster (250 px/s): it settles back.
        assertFalse(commit(200f, velocityPxPerSecond = -250f))
        assertFalse(commit(200f, velocityPxPerSecond = -300f))
        assertFalse(commit(2_000f, velocityPxPerSecond = -5_000f))
    }

    @Test
    fun shouldCommit_aFlickBackIsMeasuredInDp() {
        // -250 px/s is -125 dp/s at 2x, but only -71 dp/s at 3.5x (the distance wins there).
        assertFalse(commit(200f, velocityPxPerSecond = -250f, density = 2f))
        assertTrue(commit(400f, velocityPxPerSecond = -250f, density = 3.5f))
        assertFalse(commit(400f, velocityPxPerSecond = -438f, density = 3.5f))
    }

    @Test
    fun lockDistance_isOneAndAHalfSlops() {
        assertEquals(24f, SwipeMath.lockDistancePx(16f), 0f)
        assertEquals(12f, SwipeMath.lockDistancePx(8f), 0f)
    }
}
