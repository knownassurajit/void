package com.knownassurajit.app.launcher.voidlauncher.data

import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeSideMigrationTest {

    @Test
    fun storedSides_swapSoEveryGestureKeepsItsAction() {
        // Before: the "left" action ran when the finger moved right. After: "left" is the finger moving left.
        val swapped = SwipeSideMigration.swapped(
            mapOf(
                "LEFT_SWIPE_ACTION" to SwipeAction.NOTES,
                "RIGHT_SWIPE_ACTION" to SwipeAction.APP,
                "APP_PACKAGE_SWIPE_RIGHT" to "com.example.camera",
                "IS_SHORTCUT_SWIPE_RIGHT" to true
            )
        )
        assertEquals(SwipeAction.APP, swapped["LEFT_SWIPE_ACTION"])
        assertEquals(SwipeAction.NOTES, swapped["RIGHT_SWIPE_ACTION"])
        assertEquals("com.example.camera", swapped["APP_PACKAGE_SWIPE_LEFT"])
        assertNull(swapped["APP_PACKAGE_SWIPE_RIGHT"])
        assertEquals(true, swapped["IS_SHORTCUT_SWIPE_LEFT"])
    }

    @Test
    fun unsetActions_keepTheirOldGestures() {
        // A fresh install had Summary on the finger-right swipe and Widgets on the finger-left swipe.
        val swapped = SwipeSideMigration.swapped(emptyMap<String, Any>())
        assertEquals(SwipeAction.WIDGETS, swapped["LEFT_SWIPE_ACTION"])
        assertEquals(SwipeAction.NOTIFICATION_SUMMARY, swapped["RIGHT_SWIPE_ACTION"])
    }
}
