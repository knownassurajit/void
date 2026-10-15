package com.knownassurajit.app.launcher.voidlauncher.data

/**
 * Single source of truth for the home-app cap.
 * Missing MAX_HOME_APPS used to resolve as 10 in one path and 4 in another.
 */
object HomeAppsCap {
    const val DEFAULT = 10
    const val MAX_SLOTS = 10

    fun resolve(
        hasMaxKey: Boolean,
        maxValue: Int,
        hasLegacyKey: Boolean,
        legacyValue: Int,
        filledSlots: Int
    ): Int {
        val raw = when {
            hasMaxKey -> maxValue
            hasLegacyKey -> maxOf(legacyValue, filledSlots)
            else -> maxOf(filledSlots, DEFAULT)
        }
        return raw.coerceIn(0, MAX_SLOTS)
    }

    fun countFilledSlots(packageAt: (Int) -> String): Int {
        var highest = 0
        for (index in 1..MAX_SLOTS) {
            if (packageAt(index).isNotBlank()) highest = index
        }
        return highest
    }

    fun shouldReloadHomeApps(previousMax: Int, newMax: Int): Boolean = previousMax != newMax
}
