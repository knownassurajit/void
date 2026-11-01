package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import androidx.compose.ui.geometry.Offset
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper.WidgetSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetEditChromeTest {

    @Test
    fun aWidgetThatGrowsBothWaysHasTheCornerAndEdgeHandlesOnceItIsBigEnough() {
        assertEquals(
            ResizeHandles(corner = true, rightEdge = true, bottomEdge = true),
            resizeHandlesFor(resizesHorizontally = true, resizesVertically = true, span = WidgetSpan(columns = 4, rows = 2))
        )
    }

    @Test
    fun aSmallWidgetKeepsTheCornerAloneSoTheHandlesDoNotCrowdEachOther() {
        assertEquals(
            ResizeHandles(corner = true, rightEdge = false, bottomEdge = false),
            resizeHandlesFor(true, true, WidgetSpan(columns = 1, rows = 1))
        )
        // Tall but one cell wide: only the right edge has room.
        assertEquals(
            ResizeHandles(corner = true, rightEdge = true, bottomEdge = false),
            resizeHandlesFor(true, true, WidgetSpan(columns = 1, rows = 3))
        )
    }

    @Test
    fun aWidgetThatGrowsOneWayHasTheHandleOnThatEdgeOnly() {
        assertEquals(
            ResizeHandles(corner = false, rightEdge = true, bottomEdge = false),
            resizeHandlesFor(true, false, WidgetSpan(1, 1))
        )
        assertEquals(
            ResizeHandles(corner = false, rightEdge = false, bottomEdge = true),
            resizeHandlesFor(false, true, WidgetSpan(1, 1))
        )
    }

    @Test
    fun aWidgetThatCannotBeResizedHasNoHandles() {
        assertFalse(resizeHandlesFor(false, false, WidgetSpan(4, 4)).any)
    }

    private val all = ResizeHandles(corner = true, rightEdge = true, bottomEdge = true)

    // A 400 x 300 px widget, handle squares of 48 px, edge zones 64 px long.
    private fun hit(x: Float, y: Float, handles: ResizeHandles = all) =
        isOnResizeHandle(Offset(x, y), 400f, 300f, handles, target = 48f, edgeLength = 64f)

    @Test
    fun theCornerSquareIsAHandle() {
        assertTrue(hit(390f, 290f))
        assertTrue(hit(352f, 252f))
        assertFalse(hit(340f, 290f))
    }

    @Test
    fun theMiddleOfTheRightEdgeIsAHandleButItsTopIsNot() {
        assertTrue(hit(390f, 150f))
        assertTrue(hit(390f, 118f))
        assertTrue(hit(390f, 182f))
        assertFalse(hit(390f, 40f))
    }

    @Test
    fun theMiddleOfTheBottomEdgeIsAHandleButItsLeftEndIsNot() {
        assertTrue(hit(200f, 290f))
        assertFalse(hit(40f, 290f))
    }

    @Test
    fun theBodyOfTheWidgetIsNotAHandle() {
        assertFalse(hit(200f, 150f))
        assertFalse(hit(10f, 10f))
    }

    @Test
    fun aHandleThatIsNotShownIsNotHit() {
        val cornerOnly = ResizeHandles(corner = true, rightEdge = false, bottomEdge = false)
        assertFalse(hit(390f, 150f, cornerOnly))
        assertFalse(hit(200f, 290f, cornerOnly))
        assertTrue(hit(390f, 290f, cornerOnly))
        assertFalse(hit(390f, 290f, ResizeHandles(corner = false, rightEdge = false, bottomEdge = false)))
    }
}
