package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The pull-down header of Widgets on the real Compose input pipeline: a pull on a list that has
 * nothing to scroll (a few widgets) reaches the header through nested scroll, the header follows the
 * finger, and a release springs it open or shut. The rules themselves are covered by the JVM
 * `HeaderRevealMathTest`, `RevealHeaderStateTest` and `RevealHeaderConnectionTest`.
 *
 * Run it on an emulator (`./gradlew connectedDebugAndroidTest`), not on the physical device. A
 * fling is 400 px/s or faster here; the touch slop (8 dp) is not passed on to the header.
 */
@RunWith(AndroidJUnit4::class)
class RevealHeaderInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private val state = RevealHeaderState()
    private var enabled = true

    /** A 400 dp square with a list that fits it (nothing to scroll) and a [HEADER_HEIGHT] header floating over it. */
    private fun setScreen() {
        rule.setContent {
            val connection = remember {
                RevealHeaderConnection(
                    state = state,
                    flingVelocityPx = FLING_PX_PER_SECOND,
                    settleSpec = { spring() },
                    isEnabled = { enabled }
                )
            }
            Box(Modifier.size(400.dp)) {
                Column(
                    modifier = Modifier
                        .testTag(GRID)
                        .fillMaxSize()
                        .nestedScroll(connection)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(100.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(HEADER_HEIGHT)
                        .revealHeader(state)
                        .testTag(HEADER)
                )
            }
        }
        rule.waitForIdle()
    }

    /** The finger moves down by [distance] in [durationMillis], from the middle of the list. */
    private fun pullDown(distance: Dp, durationMillis: Long) {
        rule.onNodeWithTag(GRID).performTouchInput {
            swipe(start = Offset(centerX, 40f), end = Offset(centerX, 40f + distance.toPx()), durationMillis = durationMillis)
        }
        rule.waitForIdle()
    }

    @Test
    fun theHeaderIsMeasuredAndHiddenAtFirst() {
        setScreen()
        assertTrue("the header should have been measured", state.heightPx > 0f)
        assertTrue(state.isHidden)
    }

    @Test
    fun aSlowPullPastHalfOpensTheHeader() {
        setScreen()
        pullDown(distance = 90.dp, durationMillis = 1_500)
        assertTrue("the header should be fully shown, offset ${state.offsetPx} of ${state.heightPx}", state.isFullyShown)
    }

    @Test
    fun aShortSlowPullLeavesTheHeaderShut() {
        setScreen()
        pullDown(distance = 30.dp, durationMillis = 1_500)
        assertTrue("the header should be hidden, offset ${state.offsetPx}", state.isHidden)
    }

    @Test
    fun aQuickFlickOpensTheHeaderWhateverTheDistance() {
        setScreen()
        pullDown(distance = 26.dp, durationMillis = 60)
        assertTrue("the header should be fully shown, offset ${state.offsetPx}", state.isFullyShown)
    }

    @Test
    fun aPullIsIgnoredWhileTheHeaderIsSwitchedOff() {
        enabled = false
        setScreen()
        pullDown(distance = 90.dp, durationMillis = 1_500)
        assertTrue(state.isHidden)
    }

    private companion object {
        const val GRID = "grid"
        const val HEADER = "header"
        val HEADER_HEIGHT = 100.dp
        const val FLING_PX_PER_SECOND = 400f
    }
}
