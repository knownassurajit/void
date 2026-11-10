package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.IntOffset
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class SpeedScaledMotionSchemeTest {

    /** The six springs of Material's expressive scheme, with a visibility threshold on one so it can be seen to survive. */
    private class FakeScheme : MotionScheme {
        val defaultSpatial: FiniteAnimationSpec<Any> = spring<Any>(0.8f, 380f, visibilityThreshold = "1px")

        @Suppress("UNCHECKED_CAST")
        override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = defaultSpatial as FiniteAnimationSpec<T>
        override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = spring(0.6f, 800f)
        override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = spring(0.8f, 200f)
        override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = spring(1f, 1600f)
        override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = spring(1f, 3800f)
        override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = tween(300)
    }

    private val fake = FakeScheme()

    private fun FiniteAnimationSpec<Float>.asSpring(): SpringSpec<Float> = this as SpringSpec<Float>

    @Test
    fun standardSpeedHandsOutTheSchemesOwnSprings() {
        val scheme = SpeedScaledMotionScheme(fake, Prefs.AnimationSpeed.STANDARD)
        assertSame(fake.defaultSpatial, scheme.defaultSpatialSpec<Any>())
    }

    @Test
    fun anUnknownSpeedPlaysAtStandardSpeed() {
        val scheme = SpeedScaledMotionScheme(fake, "warp")
        assertSame(fake.defaultSpatial, scheme.defaultSpatialSpec<Any>())
    }

    @Test
    fun fastScalesStiffnessByTheSquareOfTheFactorAndKeepsTheFeel() {
        val scheme = SpeedScaledMotionScheme(fake, Prefs.AnimationSpeed.FAST)
        val spatial = scheme.fastSpatialSpec<Float>().asSpring()
        assertEquals(800f / (0.6f * 0.6f), spatial.stiffness, 0.01f)
        assertEquals(0.6f, spatial.dampingRatio, 0f)
        val effects = scheme.defaultEffectsSpec<Float>().asSpring()
        assertEquals(1600f / (0.6f * 0.6f), effects.stiffness, 0.01f)
        assertEquals(1f, effects.dampingRatio, 0f)
    }

    @Test
    fun slowScalesStiffnessDown() {
        val scheme = SpeedScaledMotionScheme(fake, Prefs.AnimationSpeed.SLOW)
        assertEquals(200f / (1.6f * 1.6f), scheme.slowSpatialSpec<Float>().asSpring().stiffness, 0.01f)
        assertEquals(0.8f, scheme.slowSpatialSpec<Float>().asSpring().dampingRatio, 0f)
    }

    @Test
    fun theVisibilityThresholdSurvivesScaling() {
        val scheme = SpeedScaledMotionScheme(fake, Prefs.AnimationSpeed.FAST)
        val scaled = scheme.defaultSpatialSpec<Any>() as SpringSpec<Any>
        assertEquals("1px", scaled.visibilityThreshold)
        assertEquals(380f / (0.6f * 0.6f), scaled.stiffness, 0.01f)
    }

    @Test
    fun aSpecThatIsNotASpringPassesThrough() {
        val scheme = SpeedScaledMotionScheme(fake, Prefs.AnimationSpeed.FAST)
        assertTrue(scheme.slowEffectsSpec<Float>() !is SpringSpec<*>)
    }

    @Test
    fun stoppingAtOnePixelPutsTheOffsetThresholdOnASpring() {
        val spring = spring<IntOffset>(0.8f, 380f).stoppingAtOnePixel() as SpringSpec<IntOffset>
        assertEquals(IntOffset.VisibilityThreshold, spring.visibilityThreshold)
        assertEquals(0.8f, spring.dampingRatio, 0f)
        assertEquals(380f, spring.stiffness, 0f)
    }

    @Test
    fun stoppingAtOnePixelLeavesOtherSpecsAlone() {
        val tween = tween<IntOffset>(100)
        assertSame(tween, tween.stoppingAtOnePixel())
    }
}
