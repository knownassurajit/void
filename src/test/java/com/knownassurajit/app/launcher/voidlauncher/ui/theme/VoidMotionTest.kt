package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoidMotionTest {

    private val standard = VoidMotionScheme(Prefs.AnimationSpeed.STANDARD)
    private val fast = VoidMotionScheme(Prefs.AnimationSpeed.FAST)
    private val slow = VoidMotionScheme(Prefs.AnimationSpeed.SLOW)

    private fun SpringSpec<Float>.pair(): Pair<Float, Float> = dampingRatio to stiffness

    @Test
    fun speedFactor_mapsSpeedKeys() {
        assertEquals(0.6f, VoidMotion.speedFactor(Prefs.AnimationSpeed.FAST), 0f)
        assertEquals(1.0f, VoidMotion.speedFactor(Prefs.AnimationSpeed.STANDARD), 0f)
        assertEquals(1.6f, VoidMotion.speedFactor(Prefs.AnimationSpeed.SLOW), 0f)
    }

    @Test
    fun speedFactor_unknownKeyPlaysAtStandardSpeed() {
        assertEquals(1.0f, VoidMotion.speedFactor("warp"), 0f)
        assertEquals(1.0f, VoidMotion.speedFactor(""), 0f)
    }

    @Test
    fun stiffnessFor_isStiffnessOverFactorSquared() {
        assertEquals(300f / (0.6f * 0.6f), VoidMotion.stiffnessFor(Prefs.AnimationSpeed.FAST, 300f), 0.01f)
        assertEquals(300f, VoidMotion.stiffnessFor(Prefs.AnimationSpeed.STANDARD, 300f), 0f)
        assertEquals(300f / (1.6f * 1.6f), VoidMotion.stiffnessFor(Prefs.AnimationSpeed.SLOW, 300f), 0.01f)
    }

    @Test
    fun stiffnessFor_fastIsStifferThanStandardIsStifferThanSlow() {
        listOf(300f, 700f, 1400f, 800f, 1600f, 3800f).forEach { base ->
            val fastStiffness = VoidMotion.stiffnessFor(Prefs.AnimationSpeed.FAST, base)
            val standardStiffness = VoidMotion.stiffnessFor(Prefs.AnimationSpeed.STANDARD, base)
            val slowStiffness = VoidMotion.stiffnessFor(Prefs.AnimationSpeed.SLOW, base)
            assertTrue("$base", fastStiffness > standardStiffness)
            assertTrue("$base", standardStiffness > slowStiffness)
        }
    }

    @Test
    fun standardScheme_matchesTheMaterialStandardTokens() {
        assertEquals(0.9f to 1400f, standard.fastSpatialSpec<Float>().pair())
        assertEquals(0.9f to 700f, standard.defaultSpatialSpec<Float>().pair())
        assertEquals(0.9f to 300f, standard.slowSpatialSpec<Float>().pair())
        assertEquals(1.0f to 3800f, standard.fastEffectsSpec<Float>().pair())
        assertEquals(1.0f to 1600f, standard.defaultEffectsSpec<Float>().pair())
        assertEquals(1.0f to 800f, standard.slowEffectsSpec<Float>().pair())
    }

    @Test
    fun speedPreference_changesStiffnessButNeverDamping() {
        listOf(fast, slow).forEach { scheme ->
            assertEquals(0.9f, scheme.fastSpatialSpec<Float>().dampingRatio, 0f)
            assertEquals(0.9f, scheme.defaultSpatialSpec<Float>().dampingRatio, 0f)
            assertEquals(0.9f, scheme.slowSpatialSpec<Float>().dampingRatio, 0f)
            assertEquals(1.0f, scheme.fastEffectsSpec<Float>().dampingRatio, 0f)
            assertEquals(1.0f, scheme.defaultEffectsSpec<Float>().dampingRatio, 0f)
            assertEquals(1.0f, scheme.slowEffectsSpec<Float>().dampingRatio, 0f)
        }
    }

    @Test
    fun speedPreference_scalesEverySpring() {
        assertTrue(fast.slowSpatialSpec<Float>().stiffness > standard.slowSpatialSpec<Float>().stiffness)
        assertTrue(standard.slowSpatialSpec<Float>().stiffness > slow.slowSpatialSpec<Float>().stiffness)
        assertTrue(fast.defaultEffectsSpec<Float>().stiffness > standard.defaultEffectsSpec<Float>().stiffness)
        assertTrue(standard.defaultEffectsSpec<Float>().stiffness > slow.defaultEffectsSpec<Float>().stiffness)
        assertEquals(300f / (0.6f * 0.6f), fast.slowSpatialSpec<Float>().stiffness, 0.01f)
        assertEquals(800f / (1.6f * 1.6f), slow.slowEffectsSpec<Float>().stiffness, 0.01f)
    }

    @Test
    fun withinEachFamily_fastIsStifferThanDefaultIsStifferThanSlow() {
        assertTrue(standard.fastSpatialSpec<Float>().stiffness > standard.defaultSpatialSpec<Float>().stiffness)
        assertTrue(standard.defaultSpatialSpec<Float>().stiffness > standard.slowSpatialSpec<Float>().stiffness)
        assertTrue(standard.fastEffectsSpec<Float>().stiffness > standard.defaultEffectsSpec<Float>().stiffness)
        assertTrue(standard.defaultEffectsSpec<Float>().stiffness > standard.slowEffectsSpec<Float>().stiffness)
    }

    @Test
    fun visibilityThreshold_isPassedThroughForOffsetsAndSizes() {
        assertEquals(
            IntOffset.VisibilityThreshold,
            standard.slowSpatialSpec(IntOffset.VisibilityThreshold).visibilityThreshold
        )
        assertEquals(
            IntSize.VisibilityThreshold,
            fast.defaultSpatialSpec(IntSize.VisibilityThreshold).visibilityThreshold
        )
    }

    @Test
    fun scaleSpec_isTheSlowSpatialSpringWithAFinerThanDefaultThreshold() {
        listOf(fast, standard, slow).forEach { scheme ->
            val spec = scheme.slowScaleSpec()
            assertEquals(VoidMotion.scaleVisibilityThreshold, spec.visibilityThreshold)
            assertEquals(scheme.slowSpatialSpec<Float>().dampingRatio, spec.dampingRatio, 0f)
            assertEquals(scheme.slowSpatialSpec<Float>().stiffness, spec.stiffness, 0f)
        }
        // Compose's own default for a Float spring is Spring.DefaultDisplacementThreshold (0.01).
        assertTrue(VoidMotion.scaleVisibilityThreshold < 0.01f)
        assertTrue(VoidMotion.scaleVisibilityThreshold > 0f)
    }

    @Test
    fun visibilityThreshold_defaultsToNone() {
        assertNull(standard.slowSpatialSpec<Float>().visibilityThreshold)
    }
}
