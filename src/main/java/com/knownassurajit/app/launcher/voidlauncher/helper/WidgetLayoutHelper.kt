package com.knownassurajit.app.launcher.voidlauncher.helper

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.view.ViewGroup
import kotlin.math.roundToInt

/**
 * Maps AppWidgetProviderInfo size metadata to host dimensions and widget options.
 * Provider minWidth/minHeight are in dp. Do not force a large default height.
 */
object WidgetLayoutHelper {
    const val CELL_DP = 70
    const val GRID_COLUMNS = 4
    const val GRID_SPACING_DP = 8
    const val MIN_HEIGHT_DP = 40
    const val MAX_HEIGHT_DP = 640
    const val MAX_ROWS = 6

    data class WidgetSpan(val columns: Int, val rows: Int)

    fun defaultSpan(
        minWidthDp: Int,
        minHeightDp: Int,
        targetCellWidth: Int = 0,
        targetCellHeight: Int = 0,
        gridColumns: Int = GRID_COLUMNS,
        cellDp: Int = CELL_DP
    ): WidgetSpan {
        val cell = cellDp.coerceAtLeast(1).toFloat()
        val columns = when {
            targetCellWidth > 0 -> targetCellWidth
            minWidthDp > 0 -> kotlin.math.ceil(minWidthDp / cell).toInt()
            else -> gridColumns
        }.coerceIn(1, gridColumns)
        val rows = when {
            targetCellHeight > 0 -> targetCellHeight
            minHeightDp > 0 -> kotlin.math.ceil(minHeightDp / cell).toInt()
            else -> 1
        }.coerceIn(1, MAX_ROWS)
        return WidgetSpan(columns = columns, rows = rows)
    }

    fun clampSpan(
        span: WidgetSpan,
        gridColumns: Int = GRID_COLUMNS
    ): WidgetSpan = WidgetSpan(
        columns = span.columns.coerceIn(1, gridColumns),
        rows = span.rows.coerceIn(1, MAX_ROWS)
    )

    fun heightForRows(rows: Int, cellDp: Int): Int =
        (rows.coerceIn(1, MAX_ROWS) * cellDp).coerceIn(MIN_HEIGHT_DP, MAX_HEIGHT_DP)

    fun bumpSpan(
        span: WidgetSpan,
        deltaColumns: Int,
        deltaRows: Int,
        gridColumns: Int = GRID_COLUMNS
    ): WidgetSpan = clampSpan(
        WidgetSpan(span.columns + deltaColumns, span.rows + deltaRows),
        gridColumns
    )

    fun snapSpan(
        start: WidgetSpan,
        deltaXDp: Float,
        deltaYDp: Float,
        cellDp: Int,
        spacingDp: Int = GRID_SPACING_DP,
        gridColumns: Int = GRID_COLUMNS
    ): WidgetSpan {
        val step = (cellDp + spacingDp).coerceAtLeast(1).toFloat()
        val columns = start.columns + kotlin.math.round(deltaXDp / step).toInt()
        val rows = start.rows + kotlin.math.round(deltaYDp / step).toInt()
        return clampSpan(WidgetSpan(columns, rows), gridColumns)
    }

    fun encodeSpan(key: String, span: WidgetSpan): String =
        "$key|${span.columns}|${span.rows}"

    fun parseSpan(entry: String): Pair<String, WidgetSpan>? {
        val rowSep = entry.lastIndexOf('|')
        if (rowSep <= 0) return null
        val rows = entry.substring(rowSep + 1).toIntOrNull() ?: return null
        val rest = entry.substring(0, rowSep)
        val colSep = rest.lastIndexOf('|')
        if (colSep <= 0) return null
        val columns = rest.substring(colSep + 1).toIntOrNull() ?: return null
        val key = rest.substring(0, colSep)
        if (key.isBlank()) return null
        return key to clampSpan(WidgetSpan(columns, rows))
    }

    fun resolveHeightDp(
        minHeightDp: Int,
        targetCellHeight: Int = 0,
        minResizeHeightDp: Int = 0
    ): Int {
        val fromCells = if (targetCellHeight > 0) targetCellHeight * CELL_DP else 0
        val preferred = when {
            fromCells > 0 -> fromCells
            minHeightDp > 0 -> minHeightDp
            minResizeHeightDp > 0 -> minResizeHeightDp
            else -> CELL_DP * 2
        }
        return preferred.coerceIn(MIN_HEIGHT_DP, MAX_HEIGHT_DP)
    }

    fun previewCardWidthDp(minWidthDp: Int, minHeightDp: Int, previewHeightDp: Int = 96): Int {
        val aspect = minWidthDp.toFloat() / minHeightDp.coerceAtLeast(1).toFloat()
        val clamped = aspect.coerceIn(0.75f, 2.4f)
        return (previewHeightDp * clamped).toInt().coerceIn(96, 220)
    }

    fun optionsBundle(widthDp: Int, heightDp: Int): Bundle {
        return Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
        }
    }

    fun applyHostSize(
        hostView: AppWidgetHostView,
        appWidgetId: Int,
        widthDp: Int,
        heightDp: Int
    ) {
        val density = hostView.resources.displayMetrics.density
        val heightPx = (heightDp * density).roundToInt()
        hostView.setPadding(0, 0, 0, 0)
        hostView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            heightPx
        )
        val options = optionsBundle(widthDp.coerceAtLeast(1), heightDp.coerceAtLeast(1))
        AppWidgetManager.getInstance(hostView.context)
            .updateAppWidgetOptions(appWidgetId, options)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hostView.updateAppWidgetSize(
                options,
                listOf(SizeF(widthDp.toFloat(), heightDp.toFloat()))
            )
        } else {
            @Suppress("DEPRECATION")
            hostView.updateAppWidgetSize(options, widthDp, heightDp, widthDp, heightDp)
        }
        hostView.requestLayout()
    }
}
