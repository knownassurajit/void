package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeaderRevealMathTest {

    private val height = 200f
    private val fling = 400f

    @Test
    fun clamp_keepsTheOffsetBetweenHiddenAndFullyShown() {
        assertEquals(0f, HeaderRevealMath.clamp(-30f, height), 0f)
        assertEquals(120f, HeaderRevealMath.clamp(120f, height), 0f)
        assertEquals(200f, HeaderRevealMath.clamp(260f, height), 0f)
    }

    @Test
    fun clamp_ofAHeaderThatIsNotMeasuredYetIsHidden() {
        assertEquals(0f, HeaderRevealMath.clamp(50f, 0f), 0f)
        assertEquals(0f, HeaderRevealMath.clamp(50f, -10f), 0f)
    }

    @Test
    fun fraction_isTheShareThatIsOut() {
        assertEquals(0f, HeaderRevealMath.fraction(0f, height), 0f)
        assertEquals(0.5f, HeaderRevealMath.fraction(100f, height), 0f)
        assertEquals(1f, HeaderRevealMath.fraction(400f, height), 0f)
        assertEquals(0f, HeaderRevealMath.fraction(100f, 0f), 0f)
    }

    @Test
    fun settle_opensWhenMoreThanHalfIsOutAndClosesOtherwise() {
        assertEquals(height, HeaderRevealMath.settleTarget(100f, height, 0f, fling), 0f)
        assertEquals(height, HeaderRevealMath.settleTarget(140f, height, 0f, fling), 0f)
        assertEquals(0f, HeaderRevealMath.settleTarget(99f, height, 0f, fling), 0f)
        assertEquals(0f, HeaderRevealMath.settleTarget(0f, height, 0f, fling), 0f)
    }

    @Test
    fun settle_aFlingDecidesWhateverTheDistance() {
        // A quick flick down opens a header that is barely out; a quick flick up closes one that is nearly out.
        assertEquals(height, HeaderRevealMath.settleTarget(10f, height, fling, fling), 0f)
        assertEquals(0f, HeaderRevealMath.settleTarget(190f, height, -fling, fling), 0f)
    }

    @Test
    fun settle_aSlowReleaseIsNotAFling() {
        assertEquals(0f, HeaderRevealMath.settleTarget(10f, height, fling - 1f, fling), 0f)
        assertEquals(height, HeaderRevealMath.settleTarget(190f, height, -(fling - 1f), fling), 0f)
    }

    @Test
    fun settle_withoutAMeasuredHeightStaysHidden() {
        assertEquals(0f, HeaderRevealMath.settleTarget(100f, 0f, fling, fling), 0f)
    }
}

class RevealHeaderStateTest {

    private fun measured(height: Float = 200f) = RevealHeaderState().also { it.onMeasured(height) }

    @Test
    fun aNewHeaderIsHidden() {
        val state = measured()
        assertTrue(state.isHidden)
        assertFalse(state.isFullyShown)
        assertEquals(0f, state.fraction, 0f)
    }

    @Test
    fun dragBy_revealsAndReportsWhatItTook() {
        val state = measured()
        assertEquals(60f, state.dragBy(60f), 0f)
        assertEquals(0.3f, state.fraction, 0.0001f)
        // Only 140 px are left to reveal.
        assertEquals(140f, state.dragBy(500f), 0f)
        assertTrue(state.isFullyShown)
    }

    @Test
    fun dragBy_upHidesAgainAndNeverGoesBelowHidden() {
        val state = measured()
        state.dragBy(100f)
        assertEquals(-100f, state.dragBy(-300f), 0f)
        assertTrue(state.isHidden)
        assertEquals(0f, state.dragBy(-50f), 0f)
    }

    @Test
    fun dragBeforeTheHeaderIsMeasuredTakesNothing() {
        val state = RevealHeaderState()
        assertEquals(0f, state.dragBy(80f), 0f)
        assertTrue(state.isHidden)
    }

    @Test
    fun aHeaderThatShrinksKeepsItsOffsetInsideTheNewHeight() {
        val state = measured(200f)
        state.dragBy(200f)
        state.onMeasured(120f)
        assertEquals(120f, state.offsetPx, 0f)
        assertTrue(state.isFullyShown)
    }
}

class RevealHeaderConnectionTest {

    private var enabled = true
    private val state = RevealHeaderState().also { it.onMeasured(200f) }
    private val connection = RevealHeaderConnection(
        state = state,
        flingVelocityPx = 400f,
        settleSpec = { spring() },
        isEnabled = { enabled }
    )

    private fun pre(dy: Float) = connection.onPreScroll(Offset(0f, dy), NestedScrollSource.UserInput)

    private fun post(unusedDy: Float) = connection.onPostScroll(Offset.Zero, Offset(0f, unusedDy), NestedScrollSource.UserInput)

    @Test
    fun theFingerMovingDownPullsTheHeaderInAndTheFingerMovingUpPushesItOut() {
        pre(80f)
        assertEquals(80f, state.offsetPx, 0f)
        pre(-30f)
        assertEquals(50f, state.offsetPx, 0f)
    }

    @Test
    fun theListKeepsEveryPixelOfTheScroll() {
        // The header floats over the list: it never takes scroll away from it.
        assertEquals(Offset.Zero, pre(80f))
        assertEquals(Offset.Zero, pre(-80f))
    }

    @Test
    fun aPullTheListCouldNotUseIsTakenWhileTheHeaderIsStillComingOut() {
        pre(60f)
        // The list is at its top and could not move: the 60 px are left over and go to the header's pull, not to a stretch.
        assertEquals(Offset(0f, 60f), post(60f))
    }

    @Test
    fun onceTheHeaderIsFullyOutTheRestOfThePullStretchesTheList() {
        pre(250f)
        assertTrue(state.isFullyShown)
        assertEquals(Offset.Zero, post(50f))
    }

    @Test
    fun aPullWithoutAFingerIsLeftAlone() {
        pre(60f)
        assertEquals(Offset.Zero, connection.onPostScroll(Offset.Zero, Offset(0f, 60f), NestedScrollSource.SideEffect))
    }

    @Test
    fun leftoverUpwardScrollIsNeverTaken() {
        assertEquals(Offset.Zero, post(-40f))
    }

    @Test
    fun aDisabledConnectionDoesNothing() {
        enabled = false
        assertEquals(Offset.Zero, pre(80f))
        assertTrue(state.isHidden)
        assertEquals(Offset.Zero, post(80f))
    }
}
