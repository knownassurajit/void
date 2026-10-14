package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDrawerScrollTest {

    private val pull = 300f
    private val velocity = 3000f

    @Test
    fun aLongPullPastTheTopClosesTheDrawer() {
        assertTrue(DrawerScrollConnection.shouldClose(pulledPx = 320f, releaseVelocityY = 0f, closePullPx = pull, closeVelocityPx = velocity))
    }

    @Test
    fun aShortPullClosesOnlyWhenReleasedFastDownward() {
        assertTrue(DrawerScrollConnection.shouldClose(pulledPx = 40f, releaseVelocityY = 3500f, closePullPx = pull, closeVelocityPx = velocity))
        assertFalse(DrawerScrollConnection.shouldClose(pulledPx = 40f, releaseVelocityY = 800f, closePullPx = pull, closeVelocityPx = velocity))
    }

    @Test
    fun aFastFlingThatNeverPulledAtTheTopDoesNotClose() {
        // Flinging down from the middle of the list only scrolls it to the top.
        assertFalse(DrawerScrollConnection.shouldClose(pulledPx = 0f, releaseVelocityY = 9000f, closePullPx = pull, closeVelocityPx = velocity))
    }
}
