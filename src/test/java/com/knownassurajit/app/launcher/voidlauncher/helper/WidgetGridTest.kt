package com.knownassurajit.app.launcher.voidlauncher.helper

import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetGrid.Cell
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetGrid.Placement
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper.WidgetSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetGridTest {

    private fun place(key: String, column: Int, row: Int, columns: Int, rows: Int) =
        Placement(key, Cell(column, row), WidgetSpan(columns, rows))

    @Test
    fun overlaps_onlyWhenCellsAreShared() {
        val a = place("a", 0, 0, 2, 2)
        assertTrue(WidgetGrid.overlaps(a, place("b", 1, 1, 2, 2)))
        assertFalse(WidgetGrid.overlaps(a, place("b", 2, 0, 2, 2)))
        assertFalse(WidgetGrid.overlaps(a, place("b", 0, 2, 4, 1)))
    }

    @Test
    fun fits_rejectsOverlapAndOverflowButIgnoresItsOwnOldPlacement() {
        val others = listOf(place("a", 0, 0, 2, 2))
        assertFalse(WidgetGrid.fits(others, place("b", 1, 0, 2, 1)))
        assertFalse(WidgetGrid.fits(others, place("b", 3, 0, 2, 1)))
        assertTrue(WidgetGrid.fits(others, place("b", 2, 0, 2, 1)))
        assertTrue(WidgetGrid.fits(others, place("a", 1, 1, 2, 2)))
    }

    @Test
    fun firstFreeCell_scansInReadingOrder() {
        val others = listOf(place("a", 0, 0, 2, 1), place("b", 2, 0, 2, 1))
        assertEquals(Cell(0, 1), WidgetGrid.firstFreeCell(others, "c", WidgetSpan(1, 1)))
        assertEquals(Cell(2, 1), WidgetGrid.firstFreeCell(others + place("d", 0, 1, 2, 1), "c", WidgetSpan(2, 1)))
    }

    @Test
    fun nearestFreeCell_keepsAFreeTargetAndSlidesOffAnOccupiedOne() {
        val others = listOf(place("a", 0, 0, 2, 2))
        assertEquals(Cell(2, 3), WidgetGrid.nearestFreeCell(others, "b", WidgetSpan(2, 1), Cell(2, 3)))
        // Dropped on top of "a": the closest free spot, not the end of the grid.
        assertEquals(Cell(2, 0), WidgetGrid.nearestFreeCell(others, "b", WidgetSpan(2, 1), Cell(1, 0)))
    }

    @Test
    fun resolve_keepsStoredCellsAndPlacesLegacyWidgetsInOrder() {
        val spans = mapOf("a" to WidgetSpan(4, 2), "b" to WidgetSpan(2, 1), "c" to WidgetSpan(2, 1))
        // "b" and "c" were saved before cells existed: they follow "a" in their saved order.
        val placements = WidgetGrid.resolve(listOf("a", "b", "c"), mapOf("a" to Cell(0, 0)), spans)
        assertEquals(Cell(0, 0), placements.first { it.key == "a" }.cell)
        assertEquals(Cell(0, 2), placements.first { it.key == "b" }.cell)
        assertEquals(Cell(2, 2), placements.first { it.key == "c" }.cell)
    }

    @Test
    fun resolve_movesACollidingWidgetInsteadOfStackingIt() {
        val spans = mapOf("a" to WidgetSpan(2, 2), "b" to WidgetSpan(2, 2))
        val placements = WidgetGrid.resolve(listOf("a", "b"), mapOf("a" to Cell(0, 0), "b" to Cell(1, 1)), spans)
        val a = placements.first { it.key == "a" }
        val b = placements.first { it.key == "b" }
        assertFalse(WidgetGrid.overlaps(a, b))
        assertEquals(Cell(2, 0), b.cell)
    }

    @Test
    fun fitSpan_shrinksToTheLargestFreeSpanOrRefuses() {
        val others = listOf(place("a", 2, 0, 2, 1))
        assertEquals(WidgetSpan(2, 3), WidgetGrid.fitSpan(others, "b", Cell(0, 0), WidgetSpan(4, 3)))
        assertNull(WidgetGrid.fitSpan(others, "b", Cell(2, 0), WidgetSpan(1, 1)))
    }

    @Test
    fun cellAt_snapsToTheNearestCellAndKeepsTheSpanInsideTheColumns() {
        assertEquals(Cell(1, 2), WidgetGrid.cellAt(xDp = 95f, yDp = 150f, stepDp = 78f, span = WidgetSpan(1, 1)))
        assertEquals(Cell(2, 0), WidgetGrid.cellAt(xDp = 400f, yDp = -40f, stepDp = 78f, span = WidgetSpan(2, 1)))
    }

    @Test
    fun extentDp_addsTheGapsBetweenCells() {
        assertEquals(70, WidgetGrid.extentDp(1, 70, 8))
        assertEquals(226, WidgetGrid.extentDp(3, 70, 8))
    }

    @Test
    fun cells_roundTripThroughTheirEncodingAndRejectGarbage() {
        val key = "com.example/.ClockWidget"
        assertEquals(key to Cell(3, 7), WidgetGrid.parseCell(WidgetGrid.encodeCell(key, Cell(3, 7))))
        assertNull(WidgetGrid.parseCell("com.example/.W|x|1"))
        assertNull(WidgetGrid.parseCell("|1|2"))
        assertNull(WidgetGrid.parseCell("com.example/.W|-1|2"))
    }
}
