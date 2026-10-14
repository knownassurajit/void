package com.knownassurajit.app.launcher.voidlauncher.data

import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction

/**
 * Swipe sides used to be named after the edge content came in from: the "left swipe action" ran
 * when the finger moved right. They are now named after the finger, as everywhere else on Android
 * ("swipe left" moves the finger left). This one-time swap of every stored left/right swipe setting
 * keeps each gesture doing what it did before the rename: only the labels change meaning.
 */
internal object SwipeSideMigration {
    const val DONE_KEY = "SWIPE_SIDES_BY_FINGER"

    /** Every stored setting that belongs to one swipe side, as (left key, right key). */
    val PAIRS: List<Pair<String, String>> = listOf(
        "LEFT_SWIPE_ACTION" to "RIGHT_SWIPE_ACTION",
        "SWIPE_LEFT_ENABLED" to "SWIPE_RIGHT_ENABLED",
        "APP_NAME_SWIPE_LEFT" to "APP_NAME_SWIPE_RIGHT",
        "APP_PACKAGE_SWIPE_LEFT" to "APP_PACKAGE_SWIPE_RIGHT",
        "APP_ACTIVITY_CLASS_NAME_SWIPE_LEFT" to "APP_ACTIVITY_CLASS_NAME_SWIPE_RIGHT",
        "APP_USER_SWIPE_LEFT" to "APP_USER_SWIPE_RIGHT",
        "SHORTCUT_ID_SWIPE_LEFT" to "SHORTCUT_ID_SWIPE_RIGHT",
        "IS_SHORTCUT_SWIPE_LEFT" to "IS_SHORTCUT_SWIPE_RIGHT"
    )

    /** The defaults before the rename, so an unset action keeps its old gesture too. */
    private val OLD_DEFAULTS: Map<String, Any> = mapOf(
        "LEFT_SWIPE_ACTION" to SwipeAction.NOTIFICATION_SUMMARY,
        "RIGHT_SWIPE_ACTION" to SwipeAction.WIDGETS
    )

    /** The values to write: each key gets its partner's stored value (`null`: remove the key). */
    fun swapped(stored: Map<String, *>): Map<String, Any?> = buildMap {
        PAIRS.forEach { (left, right) ->
            put(left, stored[right] ?: OLD_DEFAULTS[right])
            put(right, stored[left] ?: OLD_DEFAULTS[left])
        }
    }
}
