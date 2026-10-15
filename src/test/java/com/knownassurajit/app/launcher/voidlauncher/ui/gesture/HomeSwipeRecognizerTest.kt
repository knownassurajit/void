package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSwipeRecognizerTest {

    // 8 dp touch slop on a 2x display: slop 16 px, axis lock at 24 px, commit at 112 px (56 dp),
    // flick at 250 px/s (125 dp/s), rubber band limit 128 px (64 dp).
    private val slop = 16f
    private val density = 2f
    private val commitPx = 112f
    private val flickPxPerSecond = 250f
    private val limitPx = 128f
    private val longPress = 400L

    private val everything: (PanelEdge) -> SwipeTarget = { edge ->
        if (edge == PanelEdge.None) SwipeTarget.None else SwipeTarget.Panel(NavRouteKind.Notes, edge)
    }

    private fun recognizer(targetFor: (PanelEdge) -> SwipeTarget = everything) =
        HomeSwipeRecognizer(slop, density, longPress, targetFor)

    private fun HomeSwipeRecognizer.move(dx: Float, dy: Float, elapsed: Long = 50L, consumed: Boolean = false) =
        onMove(dx, dy, consumed, elapsed)

    private fun HomeSwipeStep.offset(): Float = (this as HomeSwipeStep.Tracking).offsetPx

    private fun HomeSwipeStep.crossed(): Boolean = (this as HomeSwipeStep.Tracking).thresholdCrossed

    // ---- axis lock into each direction ----

    @Test
    fun fingerMovingRight_locksTheLeftEdgeOnTheHorizontalAxis() {
        val r = recognizer()
        assertTrue(r.move(30f, 0f) is HomeSwipeStep.Tracking)
        assertEquals(PanelEdge.Left, r.edge)
        assertEquals(SwipeAxis.Horizontal, r.axis)
        assertEquals(SwipeTarget.Panel(NavRouteKind.Notes, PanelEdge.Left), r.target)
    }

    @Test
    fun fingerMovingLeft_locksTheRightEdgeOnTheHorizontalAxis() {
        val r = recognizer()
        assertTrue(r.move(-30f, 0f) is HomeSwipeStep.Tracking)
        assertEquals(PanelEdge.Right, r.edge)
        assertEquals(SwipeAxis.Horizontal, r.axis)
    }

    @Test
    fun fingerMovingUp_locksTheBottomEdgeOnTheVerticalAxis() {
        val r = recognizer()
        assertTrue(r.move(0f, -30f) is HomeSwipeStep.Tracking)
        assertEquals(PanelEdge.Bottom, r.edge)
        assertEquals(SwipeAxis.Vertical, r.axis)
    }

    @Test
    fun fingerMovingDown_locksTheTopEdgeOnTheVerticalAxis() {
        val r = recognizer()
        assertTrue(r.move(0f, 30f) is HomeSwipeStep.Tracking)
        assertEquals(PanelEdge.Top, r.edge)
        assertEquals(SwipeAxis.Vertical, r.axis)
    }

    @Test
    fun beforeTheLockDistance_itHasNoDirectionAndMovesNothing() {
        val r = recognizer()
        // Under the touch slop (still anybody's), then past it but under the 24 px lock distance
        // (Home's drag, nothing to follow yet).
        assertEquals(HomeSwipeStep.Undecided, r.move(10f, 0f))
        assertEquals(HomeSwipeStep.Ignored, r.move(8f, 0f))
        assertEquals(PanelEdge.None, r.edge)
        assertNull(r.axis)
        assertEquals(SwipeTarget.None, r.target)
        assertTrue(r.move(8f, 0f) is HomeSwipeStep.Tracking)
    }

    @Test
    fun aDiagonalDrag_locksTheLongerAxisOnceItIsLongEnough() {
        val r = recognizer()
        // 45 degrees, 34 px long: not clearly along an axis and under the 48 px forced lock.
        assertEquals(HomeSwipeStep.Ignored, r.move(24f, 24f))
        // 52 px long, a little longer horizontally.
        assertTrue(r.move(15f, 10f) is HomeSwipeStep.Tracking)
        assertEquals(SwipeAxis.Horizontal, r.axis)
        assertEquals(PanelEdge.Left, r.edge)
    }

    @Test
    fun theTravelBeforeTheTouchSlopCountsTowardTheLock() {
        val r = recognizer()
        // Three events under the slop each, 30 px together: the lock sees all of it.
        assertEquals(HomeSwipeStep.Undecided, r.move(10f, 0f))
        assertEquals(HomeSwipeStep.Ignored, r.move(10f, 0f))
        assertTrue(r.move(10f, 0f) is HomeSwipeStep.Tracking)
    }

    @Test
    fun theDirectionIsChosenOnceAndNeverChanges() {
        val r = recognizer()
        r.move(30f, 0f)
        r.move(-400f, 300f)
        assertEquals(PanelEdge.Left, r.edge)
        assertEquals(SwipeAxis.Horizontal, r.axis)
    }

    // ---- unconfigured and wrong-way directions ----

    @Test
    fun aDirectionWithoutATarget_isIgnoredAtOnceAndStaysIgnored() {
        val r = recognizer { edge -> if (edge == PanelEdge.Left) everything(edge) else SwipeTarget.None }
        assertEquals(HomeSwipeStep.Ignored, r.move(-30f, 0f))
        assertEquals(PanelEdge.None, r.edge)
        assertNull(r.axis)
        // A drag that then reverses into the configured direction is still not this recognizer's.
        assertEquals(HomeSwipeStep.Ignored, r.move(200f, 0f))
        assertEquals(HomeSwipeStep.Ignored, r.move(5f, 0f))
        assertFalse(r.shouldCommit(5_000f, 0f))
        assertEquals(0f, r.releaseVelocity(5_000f, 0f), 0f)
    }

    @Test
    fun aDirectionWithoutATarget_stillTakesThePointerSoNoChildSeesATapOrALongPress() {
        // Regression: the drag used to be dropped unconsumed, so lifting the finger inside a home
        // app row clicked the app and a slow drag started the reorder.
        val r = recognizer { SwipeTarget.None }
        val step = r.move(-30f, 0f)
        assertEquals(HomeSwipeStep.Ignored, step)
        assertTrue(step.consumesPointer)
        assertTrue(r.move(-30f, 0f).consumesPointer)
    }

    @Test
    fun aDragPastTheSlopButNotYetLocked_alreadyTakesThePointer() {
        val r = recognizer()
        val step = r.move(18f, 0f)
        assertEquals(HomeSwipeStep.Ignored, step)
        assertTrue(step.consumesPointer)
    }

    @Test
    fun underTheSlop_thePointerIsLeftToTheChildren() {
        val r = recognizer()
        val step = r.move(10f, 0f)
        assertEquals(HomeSwipeStep.Undecided, step)
        assertFalse(step.consumesPointer)
    }

    @Test
    fun whatTheChildrenOwn_isNeverConsumed() {
        assertFalse(HomeSwipeStep.GiveUp.consumesPointer)
        assertFalse(recognizer().move(60f, 0f, consumed = true).consumesPointer)
        assertFalse(recognizer().move(2f, 0f, elapsed = 400L).consumesPointer)
    }

    @Test
    fun aFollowedSwipe_takesThePointer() {
        assertTrue(recognizer().move(30f, 0f).consumesPointer)
    }

    @Test
    fun aGiveUpStaysGivenUp_evenWhenTheNextMoveIsALockableDrag() {
        val r = recognizer()
        assertEquals(HomeSwipeStep.GiveUp, r.move(2f, 0f, elapsed = 400L))
        assertEquals(HomeSwipeStep.GiveUp, r.move(60f, 0f, elapsed = 450L))
    }

    // ---- the notification list from a sideways swipe ----

    @Test
    fun aSidewaysSwipeThatOpensTheNotificationList_followsTheFingerForTheHapticButDoesNotPullHome() {
        // The notification list always drops from the top, so Home must not slide sideways first.
        val r = recognizer { edge -> if (edge == PanelEdge.None) SwipeTarget.None else SwipeTarget.Notifications }
        assertEquals(0f, r.move(-30f, 0f).offset(), 0f)
        val far = r.move(-200f, 0f)
        assertEquals(0f, far.offset(), 0f)
        assertTrue(far.crossed())
        assertEquals(PanelEdge.Right, r.edge)
        assertEquals(0f, r.releaseVelocity(-800f, 0f), 0f)
        // And it still opens on release.
        assertTrue(r.shouldCommit(0f, 0f))
    }

    @Test
    fun aSwipeDownThatOpensTheNotificationList_pullsHomeDownAsAPanelDoes() {
        val r = recognizer { edge -> if (edge == PanelEdge.None) SwipeTarget.None else SwipeTarget.Notifications }
        r.move(0f, 30f)
        assertTrue(r.move(0f, 40f).offset() > 0f)
        assertTrue(r.releaseVelocity(0f, 800f) > 0f)
    }

    @Test
    fun theTargetIsResolvedOncePerGesture() {
        var asked = 0
        val r = recognizer { edge ->
            asked++
            everything(edge)
        }
        r.move(30f, 0f)
        r.move(30f, 0f)
        r.move(30f, 0f)
        assertEquals(1, asked)
    }

    @Test
    fun aFingerThatGoesBackPastWhereItStarted_movesNothingAndNeverCommits() {
        val r = recognizer()
        r.move(30f, 0f)
        val step = r.move(-200f, 0f)
        assertEquals(0f, step.offset(), 0f)
        assertFalse(r.shouldCommit(0f, 0f))
        // Not even a fast flick the right way, once the net travel is the wrong way and short.
        assertFalse(r.shouldCommit(5_000f, 0f))
    }

    // ---- Home follows the finger ----

    @Test
    fun theOffsetStartsAtZeroWhenTheAxisLocks_soHomeDoesNotJump() {
        val r = recognizer()
        assertEquals(0f, r.move(30f, 0f).offset(), 0f)
    }

    @Test
    fun homeFollowsTheFingerOneToOneAtFirst() {
        val r = recognizer()
        r.move(30f, 0f)
        // 2 px further: the rubber band has a slope of 1 at 0, so the offset is 2 px less a hair.
        assertEquals(2f, r.move(2f, 0f).offset(), 0.05f)
    }

    @Test
    fun homeMovesTheWayTheFingerMovesOnEveryEdge() {
        // The unit direction of the finger for each edge the content comes in from.
        val fingerDirection = mapOf(
            PanelEdge.Left to (1f to 0f),
            PanelEdge.Right to (-1f to 0f),
            PanelEdge.Top to (0f to 1f),
            PanelEdge.Bottom to (0f to -1f)
        )
        fingerDirection.forEach { (edge, direction) ->
            val r = recognizer()
            r.move(30f * direction.first, 30f * direction.second)
            val offset = r.move(10f * direction.first, 10f * direction.second).offset()
            assertEquals("$edge", edge, r.edge)
            assertTrue("$edge follows the finger", offset * (direction.first + direction.second) > 0f)
        }
    }

    @Test
    fun homeNeverMovesFartherThanTheRubberBandLimit_andTheSignFollowsTheFinger() {
        val right = recognizer()
        right.move(-30f, 0f)
        val far = right.move(-10_000f, 0f).offset()
        assertTrue(far < 0f)
        assertTrue(-far < limitPx)
        assertTrue(-far > limitPx * 0.95f)
    }

    @Test
    fun theOffsetMatchesTheRubberBandOfTheTravelPastTheLock() {
        val r = recognizer()
        r.move(24f, 0f)
        // 16 px past the lock distance.
        assertEquals(SwipeMath.rubberBand(16f, limitPx), r.move(16f, 0f).offset(), 0.001f)
    }

    // ---- commit on release ----

    @Test
    fun releaseAfter56dpOfTravel_commits() {
        val r = recognizer()
        r.move(commitPx, 0f)
        assertTrue(r.shouldCommit(0f, 0f))
    }

    @Test
    fun releaseJustUnder56dp_doesNotCommit() {
        val r = recognizer()
        r.move(commitPx - 1f, 0f)
        assertFalse(r.shouldCommit(0f, 0f))
    }

    @Test
    fun aShortFlickAtTheFlickSpeed_commitsOnEveryEdge() {
        listOf(
            PanelEdge.Left to (40f to 0f),
            PanelEdge.Right to (-40f to 0f),
            PanelEdge.Top to (0f to 40f),
            PanelEdge.Bottom to (0f to -40f)
        ).forEach { (edge, delta) ->
            val r = recognizer()
            r.move(delta.first, delta.second)
            val vx = Math.signum(delta.first) * flickPxPerSecond
            val vy = Math.signum(delta.second) * flickPxPerSecond
            assertTrue("$edge", r.shouldCommit(vx, vy))
            assertFalse("$edge slower", r.shouldCommit(vx * 0.99f, vy * 0.99f))
        }
    }

    @Test
    fun aFlickTheOtherWay_cancelsEvenAfterTheDistance() {
        val r = recognizer()
        r.move(300f, 0f)
        assertFalse(r.shouldCommit(-flickPxPerSecond, 0f))
    }

    @Test
    fun aSlowReturnAfterTheDistance_isDecidedByWhereTheFingerIs() {
        val r = recognizer()
        r.move(300f, 0f)
        assertTrue(r.shouldCommit(-flickPxPerSecond * 0.9f, 0f))
    }

    @Test
    fun releaseWithoutALock_doesNotCommit() {
        val r = recognizer()
        r.move(10f, 0f)
        assertFalse(r.shouldCommit(5_000f, 0f))
    }

    // ---- cancel by reversing ----

    @Test
    fun reversingTheFingerBackUnderTheDistance_cancelsTheSwipeThatWasPastIt() {
        val r = recognizer()
        assertTrue(r.move(150f, 0f).crossed())
        r.move(-60f, 0f)
        assertFalse(r.shouldCommit(0f, 0f))
    }

    @Test
    fun reversingAndGoingBackPastTheDistance_commitsAgain() {
        val r = recognizer()
        r.move(150f, 0f)
        r.move(-60f, 0f)
        r.move(40f, 0f)
        assertTrue(r.shouldCommit(0f, 0f))
    }

    // ---- haptic hysteresis ----

    @Test
    fun theThresholdCueFiresOnceWhenTheTravelReaches56dp() {
        val r = recognizer()
        assertFalse(r.move(60f, 0f).crossed())
        assertFalse(r.move(51f, 0f).crossed())
        assertTrue(r.move(1f, 0f).crossed())
        assertFalse(r.move(30f, 0f).crossed())
    }

    @Test
    fun theThresholdCueFiresAgainOnlyAfterTheTravelDropsBelowEightyPercent() {
        val r = recognizer()
        assertTrue(r.move(120f, 0f).crossed())
        // 100 px is above 80% of 112 px: still disarmed.
        assertFalse(r.move(-20f, 0f).crossed())
        assertFalse(r.move(20f, 0f).crossed())
        // 80 px is below it: re-armed, and the next crossing fires.
        assertFalse(r.move(-40f, 0f).crossed())
        assertTrue(r.move(40f, 0f).crossed())
    }

    @Test
    fun theThresholdCueFollowsTheTravelTowardTheTarget_notTheOtherWay() {
        val r = recognizer()
        r.move(30f, 0f)
        assertFalse(r.move(-500f, 0f).crossed())
    }

    // ---- a long press belongs to the children ----

    @Test
    fun aFingerThatHasNotMovedByTheLongPressTimeout_isALongPress() {
        val r = recognizer()
        assertEquals(HomeSwipeStep.Undecided, r.move(2f, 0f, elapsed = 399L))
        assertEquals(HomeSwipeStep.GiveUp, r.move(60f, 0f, elapsed = 400L))
    }

    @Test
    fun aSlowDragThatPassedTheSlopInTime_isNotALongPressHoweverLongItTakesToLock() {
        val r = recognizer()
        // 17 px at 350 ms is past the 16 px slop (the children's long press is cancelled) but not
        // yet the 24 px lock distance: the swipe is still on 700 ms later.
        assertEquals(HomeSwipeStep.Ignored, r.move(17f, 0f, elapsed = 350L))
        assertTrue(r.move(10f, 0f, elapsed = 1_050L) is HomeSwipeStep.Tracking)
    }

    // ---- a child or the system took the pointer ----

    @Test
    fun aConsumedPointer_givesUp() {
        val r = recognizer()
        assertEquals(HomeSwipeStep.GiveUp, r.move(60f, 0f, consumed = true))
    }

    @Test
    fun aPointerThatIsConsumedAfterTheLock_givesUp() {
        val r = recognizer()
        assertTrue(r.move(60f, 0f) is HomeSwipeStep.Tracking)
        assertEquals(HomeSwipeStep.GiveUp, r.move(10f, 0f, consumed = true))
        assertEquals(HomeSwipeStep.GiveUp, r.move(10f, 0f))
    }

    // ---- release velocity of Home ----

    @Test
    fun releaseVelocity_isZeroWhileHomeIsAtRest() {
        val r = recognizer()
        assertEquals(0f, r.releaseVelocity(1_000f, 0f), 0f)
        r.move(30f, 0f)
        assertEquals(0f, r.releaseVelocity(1_000f, 0f), 0f)
    }

    @Test
    fun releaseVelocity_isTheFingerSpeedSlowedByTheRubberBand() {
        val r = recognizer()
        r.move(24f, 0f)
        r.move(64f, 0f)
        val expected = 800f * SwipeMath.rubberBandSlope(64f, limitPx)
        assertEquals(expected, r.releaseVelocity(800f, 123f), 0.001f)
        assertTrue(expected < 800f)
    }

    @Test
    fun releaseVelocity_keepsThePhysicalSignOfTheFinger() {
        val left = recognizer()
        left.move(-24f, 0f)
        left.move(-64f, 0f)
        // The finger lifts off moving left at 800 px/s: Home, off to the left, moves left too.
        assertTrue(left.releaseVelocity(-800f, 0f) < 0f)
        // And a finger coming back toward the start moves Home back toward rest.
        assertTrue(left.releaseVelocity(800f, 0f) > 0f)
    }

    @Test
    fun releaseVelocity_usesTheVerticalSpeedOnAVerticalSwipe() {
        val r = recognizer()
        r.move(0f, -24f)
        r.move(0f, -64f)
        assertTrue(r.releaseVelocity(999f, -800f) < 0f)
        assertEquals(-800f * SwipeMath.rubberBandSlope(64f, limitPx), r.releaseVelocity(999f, -800f), 0.001f)
    }
}
