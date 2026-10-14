package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs

object VoidMotion {
    const val fastMs = 150
    const val standardMs = 250
    const val slowMs = 400

    const val scaleVisibilityThreshold = 0.002f

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

    fun <T> standard(): FiniteAnimationSpec<T> = tween(durationMillis = standardMs)

    fun <T> slideSpec(durationMs: Int): FiniteAnimationSpec<T> = tween(durationMillis = durationMs)

    fun speedFactor(speedKey: String): Float = when (speedKey) {
        Prefs.AnimationSpeed.FAST -> 0.6f
        Prefs.AnimationSpeed.SLOW -> 1.6f
        else -> 1.0f
    }

    fun stiffnessFor(speedKey: String, baseStiffness: Float): Float {
        val f = speedFactor(speedKey)
        return baseStiffness / (f * f)
    }
}

class VoidMotionScheme(val speedKey: String) {
    val speedFactor: Float = VoidMotion.speedFactor(speedKey)

    fun <T> fastSpatialSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = VoidMotion.spatialFastDamping,
        stiffness = VoidMotion.stiffnessFor(speedKey, VoidMotion.spatialFastStiffness),
        visibilityThreshold = visibilityThreshold
    )

    fun <T> defaultSpatialSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = VoidMotion.spatialDefaultDamping,
        stiffness = VoidMotion.stiffnessFor(speedKey, VoidMotion.spatialDefaultStiffness),
        visibilityThreshold = visibilityThreshold
    )

    fun <T> slowSpatialSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = VoidMotion.spatialSlowDamping,
        stiffness = VoidMotion.stiffnessFor(speedKey, VoidMotion.spatialSlowStiffness),
        visibilityThreshold = visibilityThreshold
    )

    fun <T> fastEffectsSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = VoidMotion.effectsFastDamping,
        stiffness = VoidMotion.stiffnessFor(speedKey, VoidMotion.effectsFastStiffness),
        visibilityThreshold = visibilityThreshold
    )

    fun <T> defaultEffectsSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = VoidMotion.effectsDefaultDamping,
        stiffness = VoidMotion.stiffnessFor(speedKey, VoidMotion.effectsDefaultStiffness),
        visibilityThreshold = visibilityThreshold
    )

    fun <T> slowEffectsSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = VoidMotion.effectsSlowDamping,
        stiffness = VoidMotion.stiffnessFor(speedKey, VoidMotion.effectsSlowStiffness),
        visibilityThreshold = visibilityThreshold
    )

    fun slowScaleSpec(): SpringSpec<Float> = slowSpatialSpec(visibilityThreshold = VoidMotion.scaleVisibilityThreshold)
}

@Composable
fun rememberVoidMotionScheme(speedKey: String): VoidMotionScheme =
    remember(speedKey) { VoidMotionScheme(speedKey) }
