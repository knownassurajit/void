package com.knownassurajit.app.launcher.voidlauncher.helper

/**
 * Resolves clock vs apps vertical weights so settings changes cannot invert or
 * collapse a section. Default split is 35% clock / 65% apps.
 */
object HomeLayoutHelper {
    const val DEFAULT_CLOCK_WEIGHT = 0.35f
    const val MIN_CLOCK_WEIGHT = 0.22f
    const val MAX_CLOCK_WEIGHT = 0.48f

    data class SectionWeights(val clock: Float, val apps: Float)

    fun sectionWeights(
        clockEnabled: Boolean,
        appsEnabled: Boolean,
        requestedClockWeight: Float
    ): SectionWeights {
        val clock = requestedClockWeight.coerceIn(MIN_CLOCK_WEIGHT, MAX_CLOCK_WEIGHT)
        return when {
            clockEnabled && appsEnabled -> SectionWeights(clock = clock, apps = 1f - clock)
            clockEnabled -> SectionWeights(clock = 1f, apps = 0f)
            appsEnabled -> SectionWeights(clock = 0f, apps = 1f)
            else -> SectionWeights(clock = 0f, apps = 0f)
        }
    }
}
