package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The Widgets header on the real Compose input pipeline: it opens only from the strip at the top
 * ([RevealPullZone]: drag down or tap), never from a pull on the content, and scrolling the content
 * up puts it away. The rules are covered by the JVM `HeaderRevealMathTest`, `RevealHeaderStateTest`
 * and `RevealHeaderConnectionTest`.
 *
 * Run it on an emulator (`./gradlew connectedDebugAndroidTest`), not on the physical device.
 */
@RunWith(AndroidJUnit4::class)
class RevealHeaderInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private val state = RevealHeaderState()

    /** A 400 dp screen: a strip at the top, content below it that can scroll, and a header floating over both. */
    private fun setScreen() {
        rule.setContent {
            val scope = rememberCoroutineScope()
            val connection = remember {
                RevealHeaderConnection(
                    state = state,
                    flingVelocityPx = FLING_PX_PER_SECOND,
                    settleSpec = { spring() },
                    isEnabled = { true }
                )
            }
            Box(Modifier.size(400.dp)) {
                Column(
                    modifier = Modifier
                        .testTag(CONTENT)
                        .fillMaxSize()
                        .padding(top = PULL_ZONE_HEIGHT)
                        .nestedScroll(connection)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(1_200.dp))
                }
                RevealPullZone(
                    state = state,
                    description = "Show widget options",
                    flingVelocityPx = FLING_PX_PER_SECOND,
                    settleSpec = spring(),
                    onToggle = { scope.launch { if (state.isHidden) state.show(spring()) else state.hide(spring()) } },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag(STRIP)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(HEADER_HEIGHT)
                        .revealHeader(state)
                )
            }
        }
        rule.waitForIdle()
    }

    private fun swipeOn(tag: String, from: Float, distance: Dp, durationMillis: Long) {
        rule.onNodeWithTag(tag).performTouchInput {
            swipe(start = Offset(centerX, from), end = Offset(centerX, from + distance.toPx()), durationMillis = durationMillis)
        }
        rule.waitForIdle()
    }

    @Test
    fun aPullOnTheContentNeverOpensTheHeader() {
        setScreen()
        swipeOn(CONTENT, from = 40f, distance = 120.dp, durationMillis = 300)
        assertTrue(state.isHidden)
    }

    @Test
    fun aDragDownOnTheStripOpensTheHeader() {
        setScreen()
        swipeOn(STRIP, from = 4f, distance = 90.dp, durationMillis = 1_000)
        assertTrue("offset ${state.offsetPx} of ${state.heightPx}", state.isFullyShown)
    }

    @Test
    fun aTapOnTheStripOpensTheHeader() {
        setScreen()
        rule.onNodeWithTag(STRIP).performClick()
        rule.waitForIdle()
        assertTrue(state.isFullyShown)
    }

    @Test
    fun scrollingTheContentUpPutsAnOpenHeaderAway() {
        setScreen()
        rule.onNodeWithTag(STRIP).performClick()
        rule.waitForIdle()
        swipeOn(CONTENT, from = 300f, distance = (-200).dp, durationMillis = 300)
        assertTrue("offset ${state.offsetPx}", state.isHidden)
    }

    private companion object {
        const val CONTENT = "content"
        const val STRIP = "strip"
        val HEADER_HEIGHT = 100.dp
        const val FLING_PX_PER_SECOND = 400f
    }
}
