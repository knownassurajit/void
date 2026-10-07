package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs

/**
 * Material 3 **standard** spring tokens as (dampingRatio, stiffness) pairs, copied from material3
 * 1.4.0 `StandardMotionTokens`. `MotionScheme` is internal in every released material3 (1.3.2 on
 * our classpath has none), so the numbers live here instead.
 *
 * Standard, not Expressive: a damping ratio of 0.9 overshoots by about 0.15%, so a full-screen
 * panel never exposes the black window edge. Spatial springs move things (offset, scale, size);
 * effects springs fade things (alpha, colour) and never overshoot.
 */
object VoidMotion {
    const val spatialFastDamping = 0.9f
    const val spatialFastStiffness = 1400f
    const val spatialDefaultDamping = 0.9f
    const val spatialDefaultStiffness = 700f
    const val spatialSlowDamping = 0.9f
    const val spatialSlowStiffness = 300f

    const val effectsFastDamping = 1.0f
    const val effectsFastStiffness = 3800f
    const val effectsDefaultDamping = 1.0f
    const val effectsDefaultStiffness = 1600f
    const val effectsSlowDamping = 1.0f
    const val effectsSlowStiffness = 800f

    /**
     * Where a spring on a scale (a `Float` between 0.8 and 1.1) stops. Compose's default of 0.01
     * ends the spring with up to 1% of scale left, which snaps the screen by about 5 px at each
     * edge of a 1080 px display.
     */
    const val scaleVisibilityThreshold = 0.002f

    private const val FAST_FACTOR = 0.6f
    private const val STANDARD_FACTOR = 1.0f
    private const val SLOW_FACTOR = 1.6f

    /**
     * How long an animation plays at a [Prefs.AnimationSpeed] key, relative to the Material
     * default: 0.6 fast, 1.0 standard, 1.6 slow. An unknown key plays at standard speed.
     */
    fun speedFactor(speedKey: String): Float = when (speedKey) {
        Prefs.AnimationSpeed.FAST -> FAST_FACTOR
        Prefs.AnimationSpeed.SLOW -> SLOW_FACTOR
        else -> STANDARD_FACTOR
    }

    /**
     * A spring's settle time scales with `1 / sqrt(stiffness)`, so playing it [speedFactor] times
     * longer needs `stiffness / factor^2`. Damping is left alone: the feel stays the same, only
     * the clock changes.
     */
    fun stiffnessFor(speedKey: String, stiffness: Float): Float {
        val factor = speedFactor(speedKey)
        return stiffness / (factor * factor)
    }
}

/**
 * The Material 3 standard springs scaled by the animation-speed preference. The function names
 * mirror `MotionScheme`, so moving to it once it is public is a mechanical change.
 *
 * Springs on [androidx.compose.ui.unit.IntOffset] or [androidx.compose.ui.unit.IntSize] must be
 * given their `VisibilityThreshold` as [visibilityThreshold]; otherwise the spring keeps running
 * for sub-pixel distances.
 */
class VoidMotionScheme(private val speedKey: String) {
    fun <T> fastSpatialSpec(visibilityThreshold: T? = null): SpringSpec<T> = scaled(
        VoidMotion.spatialFastDamping,
        VoidMotion.spatialFastStiffness,
        visibilityThreshold
    )

    fun <T> defaultSpatialSpec(visibilityThreshold: T? = null): SpringSpec<T> = scaled(
        VoidMotion.spatialDefaultDamping,
        VoidMotion.spatialDefaultStiffness,
        visibilityThreshold
    )

    fun <T> slowSpatialSpec(visibilityThreshold: T? = null): SpringSpec<T> = scaled(
        VoidMotion.spatialSlowDamping,
        VoidMotion.spatialSlowStiffness,
        visibilityThreshold
    )

    /** [slowSpatialSpec] for a scale: it stops at [VoidMotion.scaleVisibilityThreshold] instead of Compose's 0.01. */
    fun slowScaleSpec(): SpringSpec<Float> = slowSpatialSpec(VoidMotion.scaleVisibilityThreshold)

    fun <T> fastEffectsSpec(visibilityThreshold: T? = null): SpringSpec<T> = scaled(
        VoidMotion.effectsFastDamping,
        VoidMotion.effectsFastStiffness,
        visibilityThreshold
    )

    fun <T> defaultEffectsSpec(visibilityThreshold: T? = null): SpringSpec<T> = scaled(
        VoidMotion.effectsDefaultDamping,
        VoidMotion.effectsDefaultStiffness,
        visibilityThreshold
    )

    fun <T> slowEffectsSpec(visibilityThreshold: T? = null): SpringSpec<T> = scaled(
        VoidMotion.effectsSlowDamping,
        VoidMotion.effectsSlowStiffness,
        visibilityThreshold
    )

    private fun <T> scaled(damping: Float, stiffness: Float, visibilityThreshold: T?): SpringSpec<T> =
        spring(damping, VoidMotion.stiffnessFor(speedKey, stiffness), visibilityThreshold)
}

@Composable
fun rememberVoidMotionScheme(speedKey: String): VoidMotionScheme =
    remember(speedKey) { VoidMotionScheme(speedKey) }
