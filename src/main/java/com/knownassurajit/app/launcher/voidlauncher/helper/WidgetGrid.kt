package com.knownassurajit.app.launcher.voidlauncher.helper

import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper.WidgetSpan
import kotlin.math.roundToInt

/**
 * Cell placement for widgets on the Widgets grid: where each widget sits, whether a move or resize
 * fits, and where a dropped widget lands. Pure (no Android types) so every rule is JVM-testable.
 */
object WidgetGrid {

    data class Cell(val column: Int, val row: Int)

    data class Placement(val key: String, val cell: Cell, val span: WidgetSpan) {
        val endColumn: Int get() = cell.column + span.columns
        val endRow: Int get() = cell.row + span.rows
    }

    fun overlaps(a: Placement, b: Placement): Boolean =
        a.cell.column < b.endColumn && b.cell.column < a.endColumn &&
            a.cell.row < b.endRow && b.cell.row < a.endRow

    /** [candidate] lies inside the columns and covers no other widget; its own old placement is ignored. */
    fun fits(others: List<Placement>, candidate: Placement, columns: Int = WidgetLayoutHelper.GRID_COLUMNS): Boolean =
        candidate.cell.column >= 0 && candidate.cell.row >= 0 && candidate.endColumn <= columns &&
            others.none { it.key != candidate.key && overlaps(it, candidate) }

    /** Rows the grid needs to show every placement. */
    fun rowCount(placements: List<Placement>): Int = placements.maxOfOrNull { it.endRow } ?: 0

    /** The first free cell in reading order (row by row, left to right) that takes [span]. */
    fun firstFreeCell(
        others: List<Placement>,
        key: String,
        span: WidgetSpan,
        columns: Int = WidgetLayoutHelper.GRID_COLUMNS
    ): Cell {
        val fitted = WidgetLayoutHelper.clampSpan(span, columns)
        // Below every widget there is always room, so the scan ends there at the latest.
        for (row in 0..rowCount(others)) {
            for (column in 0..(columns - fitted.columns)) {
                if (fits(others, Placement(key, Cell(column, row), fitted), columns)) return Cell(column, row)
            }
        }
        return Cell(0, rowCount(others))
    }

    /** The free cell taking [span] closest to [preferred] (ties go to the earlier cell in reading order). */
    fun nearestFreeCell(
        others: List<Placement>,
        key: String,
        span: WidgetSpan,
        preferred: Cell,
        columns: Int = WidgetLayoutHelper.GRID_COLUMNS
    ): Cell {
        val fitted = WidgetLayoutHelper.clampSpan(span, columns)
        var best: Cell? = null
        var bestDistance = Int.MAX_VALUE
        val lastRow = maxOf(rowCount(others), preferred.row)
        for (row in 0..lastRow) {
            for (column in 0..(columns - fitted.columns)) {
                if (!fits(others, Placement(key, Cell(column, row), fitted), columns)) continue
                val dr = row - preferred.row
                val dc = column - preferred.column
                val distance = dr * dr + dc * dc
                if (distance < bestDistance) {
                    best = Cell(column, row)
                    bestDistance = distance
                }
            }
        }
        return best ?: Cell(0, rowCount(others))
    }

    /**
     * Places every widget in [orderedKeys]. A stored cell is kept while it still fits; widgets without
     * one (new, or saved before cells existed) and those that now collide go to the first free cell, in
     * order, so an older order-only layout keeps its order.
     */
    fun resolve(
        orderedKeys: List<String>,
        stored: Map<String, Cell>,
        spans: Map<String, WidgetSpan>,
        columns: Int = WidgetLayoutHelper.GRID_COLUMNS
    ): List<Placement> {
        val placed = mutableListOf<Placement>()
        val pending = mutableListOf<String>()
        orderedKeys.distinct().forEach { key ->
            val span = WidgetLayoutHelper.clampSpan(spans[key] ?: WidgetSpan(columns, 2), columns)
            val cell = stored[key]
            val placement = cell?.let { Placement(key, it, span) }
            if (placement != null && fits(placed, placement, columns)) placed += placement else pending += key
        }
        pending.forEach { key ->
            val span = WidgetLayoutHelper.clampSpan(spans[key] ?: WidgetSpan(columns, 2), columns)
            placed += Placement(key, firstFreeCell(placed, key, span, columns), span)
        }
        return placed
    }

    /**
     * The largest span up to [requested] that fits at [cell] without covering another widget, widest
     * first; `null` when not even one cell is free there.
     */
    fun fitSpan(
        others: List<Placement>,
        key: String,
        cell: Cell,
        requested: WidgetSpan,
        columns: Int = WidgetLayoutHelper.GRID_COLUMNS
    ): WidgetSpan? {
        val wanted = WidgetLayoutHelper.clampSpan(requested, columns)
        for (width in wanted.columns downTo 1) {
            for (height in wanted.rows downTo 1) {
                val span = WidgetSpan(width, height)
                if (fits(others, Placement(key, cell, span), columns)) return span
            }
        }
        return null
    }

    /** The cell whose top-left corner is nearest to a top-left at ([xDp], [yDp]), keeping [span] inside the columns. */
    fun cellAt(
        xDp: Float,
        yDp: Float,
        stepDp: Float,
        span: WidgetSpan,
        columns: Int = WidgetLayoutHelper.GRID_COLUMNS
    ): Cell {
        val step = stepDp.coerceAtLeast(1f)
        val column = (xDp / step).roundToInt().coerceIn(0, (columns - span.columns).coerceAtLeast(0))
        val row = (yDp / step).roundToInt().coerceAtLeast(0)
        return Cell(column, row)
    }

    /** Size of [units] cells with the gaps between them. */
    fun extentDp(units: Int, cellDp: Int, spacingDp: Int = WidgetLayoutHelper.GRID_SPACING_DP): Int =
        units.coerceAtLeast(1) * cellDp + (units.coerceAtLeast(1) - 1) * spacingDp

    fun encodeCell(key: String, cell: Cell): String = "$key|${cell.column}|${cell.row}"

    fun parseCell(entry: String): Pair<String, Cell>? {
        val rowSep = entry.lastIndexOf('|')
        if (rowSep <= 0) return null
        val row = entry.substring(rowSep + 1).toIntOrNull() ?: return null
        val rest = entry.substring(0, rowSep)
        val colSep = rest.lastIndexOf('|')
        if (colSep <= 0) return null
        val column = rest.substring(colSep + 1).toIntOrNull() ?: return null
        val key = rest.substring(0, colSep)
        if (key.isBlank() || column < 0 || row < 0) return null
        return key to Cell(column, row)
    }
}
