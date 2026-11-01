package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Measures how long the real springs take to settle, with Compose's own solver, so that "Fast plays
 * in 60% of the time, Slow in 160%" is checked on durations and not only on the stiffness formula.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MotionDurationTest {

    /** Seconds a spring takes to travel [distancePx] from rest and stop within its visibility threshold. */
    private fun settleSeconds(spec: FiniteAnimationSpec<Float>, distancePx: Float): Float {
        val nanos = spec.vectorize(Float.VectorConverter).getDurationNanos(
            AnimationVector1D(0f),
            AnimationVector1D(distancePx),
            AnimationVector1D(0f)
        )
        return nanos / NANOS_PER_SECOND
    }

    private fun panelSeconds(speed: String): Float =
        settleSeconds(VoidMotionScheme(speed).slowSpatialSpec(ONE_PIXEL), PANEL_WIDTH_PX)

    @Test
    fun aPanelSettlesInAboutHalfASecondAtStandardSpeed() {
        // docs/architecture/motion.md: "At standard speed a panel settles to one pixel in about 0.45 s".
        val standard = panelSeconds(Prefs.AnimationSpeed.STANDARD)
        assertTrue("a panel took $standard s at standard speed", standard in 0.35f..0.6f)
    }

    @Test
    fun fastPlaysInSixtyPercentOfTheTimeAndSlowInOneHundredSixtyPercent() {
        val standard = panelSeconds(Prefs.AnimationSpeed.STANDARD)
        assertEquals(0.6f, panelSeconds(Prefs.AnimationSpeed.FAST) / standard, 0.03f)
        assertEquals(1.6f, panelSeconds(Prefs.AnimationSpeed.SLOW) / standard, 0.05f)
    }

    @Test
    fun everySpringOfTheSchemeScalesTheSameWay() {
        val picks: List<(VoidMotionScheme) -> FiniteAnimationSpec<Float>> = listOf(
            { it.fastSpatialSpec(ONE_PIXEL) },
            { it.defaultSpatialSpec(ONE_PIXEL) },
            { it.slowSpatialSpec(ONE_PIXEL) },
            { it.fastEffectsSpec(ONE_PIXEL) },
            { it.defaultEffectsSpec(ONE_PIXEL) },
            { it.slowEffectsSpec(ONE_PIXEL) }
        )
        val standard = VoidMotionScheme(Prefs.AnimationSpeed.STANDARD)
        val fast = VoidMotionScheme(Prefs.AnimationSpeed.FAST)
        val slow = VoidMotionScheme(Prefs.AnimationSpeed.SLOW)
        picks.forEachIndexed { index, pick ->
            val base = settleSeconds(pick(standard), 1000f)
            assertEquals("spring $index at fast speed", 0.6f, settleSeconds(pick(fast), 1000f) / base, 0.04f)
            assertEquals("spring $index at slow speed", 1.6f, settleSeconds(pick(slow), 1000f) / base, 0.06f)
        }
    }

    /** The expressive springs behind `MaterialTheme.motionScheme` (damping, stiffness). */
    private class ExpressiveLike : MotionScheme {
        override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = spring(0.8f, 380f)
        override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = spring(0.6f, 800f)
        override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = spring(0.8f, 200f)
        override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = spring(1f, 1600f)
        override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = spring(1f, 3800f)
        override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = spring(1f, 800f)
    }

    @Test
    fun theThemeSchemeFollowsTheSpeedSettingToo() {
        fun seconds(speed: String) =
            settleSeconds(SpeedScaledMotionScheme(ExpressiveLike(), speed).defaultSpatialSpec<Float>(), 400f)
        val standard = seconds(Prefs.AnimationSpeed.STANDARD)
        assertEquals(0.6f, seconds(Prefs.AnimationSpeed.FAST) / standard, 0.04f)
        assertEquals(1.6f, seconds(Prefs.AnimationSpeed.SLOW) / standard, 0.06f)
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val PANEL_WIDTH_PX = 1080f
        const val ONE_PIXEL = 1f
    }
}
