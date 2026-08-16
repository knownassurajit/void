package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutHelperTest {

    @Test
    fun resolveHeight_prefersTargetCells() {
        val height = WidgetLayoutHelper.resolveHeightDp(
            minHeightDp = 40,
            targetCellHeight = 2,
            minResizeHeightDp = 40
        )
        assertEquals(2 * WidgetLayoutHelper.CELL_DP, height)
    }

    @Test
    fun resolveHeight_usesProviderMinWhenNoCells() {
        val height = WidgetLayoutHelper.resolveHeightDp(
            minHeightDp = 110,
            targetCellHeight = 0
        )
        assertEquals(110, height)
    }

    @Test
    fun resolveHeight_doesNotForce240Default() {
        val height = WidgetLayoutHelper.resolveHeightDp(minHeightDp = 40, targetCellHeight = 1)
        assertTrue(height < 240)
        assertTrue(height >= WidgetLayoutHelper.MIN_HEIGHT_DP)
    }

    @Test
    fun defaultSpan_clampsWideWidgetToGrid() {
        val span = WidgetLayoutHelper.defaultSpan(
            minWidthDp = 630,
            minHeightDp = 79,
            targetCellWidth = 0,
            targetCellHeight = 0
        )
        assertEquals(WidgetLayoutHelper.GRID_COLUMNS, span.columns)
        assertEquals(2, span.rows)
    }

    @Test
    fun defaultSpan_usesActualCellSize() {
        val span = WidgetLayoutHelper.defaultSpan(
            minWidthDp = 180,
            minHeightDp = 236,
            cellDp = 86
        )
        assertEquals(3, span.columns)
        assertEquals(3, span.rows)
    }

    @Test
    fun defaultSpan_usesTargetCells() {
        val span = WidgetLayoutHelper.defaultSpan(
            minWidthDp = 40,
            minHeightDp = 40,
            targetCellWidth = 2,
            targetCellHeight = 1
        )
        assertEquals(2, span.columns)
        assertEquals(1, span.rows)
    }

    @Test
    fun clampSpan_staysOnGrid() {
        val span = WidgetLayoutHelper.clampSpan(WidgetLayoutHelper.WidgetSpan(9, 0))
        assertEquals(4, span.columns)
        assertEquals(1, span.rows)
    }

    @Test
    fun bumpSpan_changesOneAxis() {
        val start = WidgetLayoutHelper.WidgetSpan(2, 1)
        val wider = WidgetLayoutHelper.bumpSpan(start, deltaColumns = 1, deltaRows = 0)
        val taller = WidgetLayoutHelper.bumpSpan(start, deltaColumns = 0, deltaRows = 1)
        assertEquals(3, wider.columns)
        assertEquals(1, wider.rows)
        assertEquals(2, taller.columns)
        assertEquals(2, taller.rows)
    }

    @Test
    fun snapSpan_snapsToNextCell() {
        val start = WidgetLayoutHelper.WidgetSpan(2, 1)
        val step = WidgetLayoutHelper.CELL_DP + WidgetLayoutHelper.GRID_SPACING_DP
        val wider = WidgetLayoutHelper.snapSpan(
            start = start,
            deltaXDp = step.toFloat(),
            deltaYDp = 0f,
            cellDp = WidgetLayoutHelper.CELL_DP
        )
        val taller = WidgetLayoutHelper.snapSpan(
            start = start,
            deltaXDp = 0f,
            deltaYDp = step.toFloat(),
            cellDp = WidgetLayoutHelper.CELL_DP
        )
        assertEquals(3, wider.columns)
        assertEquals(1, wider.rows)
        assertEquals(2, taller.columns)
        assertEquals(2, taller.rows)
    }

    @Test
    fun snapSpan_ignoresSubCellNudge() {
        val start = WidgetLayoutHelper.WidgetSpan(2, 1)
        val next = WidgetLayoutHelper.snapSpan(
            start = start,
            deltaXDp = 10f,
            deltaYDp = 10f,
            cellDp = WidgetLayoutHelper.CELL_DP
        )
        assertEquals(2, next.columns)
        assertEquals(1, next.rows)
    }

    @Test
    fun parseAndEncodeSpan_roundTrip() {
        val encoded = WidgetLayoutHelper.encodeSpan(
            "com.app/.Widget",
            WidgetLayoutHelper.WidgetSpan(3, 2)
        )
        val parsed = WidgetLayoutHelper.parseSpan(encoded)
        assertEquals("com.app/.Widget", parsed?.first)
        assertEquals(3, parsed?.second?.columns)
        assertEquals(2, parsed?.second?.rows)
    }

    @Test
    fun previewCardWidth_keepsReadableAspect() {
        val wide = WidgetLayoutHelper.previewCardWidthDp(minWidthDp = 250, minHeightDp = 80)
        val tall = WidgetLayoutHelper.previewCardWidthDp(minWidthDp = 80, minHeightDp = 120)
        assertTrue(wide >= 96)
        assertTrue(tall >= 96)
        assertTrue(wide <= 220)
    }
}
