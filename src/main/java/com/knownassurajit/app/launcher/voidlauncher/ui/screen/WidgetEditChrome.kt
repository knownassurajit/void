package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlin.math.abs

// ── Resize handles ──

/** What a resize handle changes: the width, the height, or both (the corner). */
internal enum class ResizeAxis { Width, Height, Both }

/** Which resize handles a selected widget shows; see [resizeHandlesFor]. */
internal data class ResizeHandles(val corner: Boolean, val rightEdge: Boolean, val bottomEdge: Boolean) {
    val any: Boolean get() = corner || rightEdge || bottomEdge
}

/**
 * The handles a widget gets from the directions its provider can be resized in. A widget that grows
 * both ways has the corner and, once it is big enough to take them without crowding the corner, a
 * handle in the middle of its right and bottom edges; a widget that grows one way has just the
 * handle on that edge. Handles are on the right and bottom edge only because a widget keeps its
 * top-left cell when it is resized.
 */
internal fun resizeHandlesFor(
    resizesHorizontally: Boolean,
    resizesVertically: Boolean,
    span: WidgetLayoutHelper.WidgetSpan
): ResizeHandles = when {
    resizesHorizontally && resizesVertically -> ResizeHandles(
        corner = true,
        rightEdge = span.rows >= MIN_CELLS_FOR_EDGE_HANDLE,
        bottomEdge = span.columns >= MIN_CELLS_FOR_EDGE_HANDLE
    )
    resizesHorizontally -> ResizeHandles(corner = false, rightEdge = true, bottomEdge = false)
    resizesVertically -> ResizeHandles(corner = false, rightEdge = false, bottomEdge = true)
    else -> ResizeHandles(corner = false, rightEdge = false, bottomEdge = false)
}

private const val MIN_CELLS_FOR_EDGE_HANDLE = 2

/**
 * Whether a touch at [position] on a widget of [width] x [height] px lands on one of its resize
 * [handles]. The widget's own long-press and drag must leave such a touch alone, so it reads the
 * same geometry as the handles: each is a square of [target] px against the right or bottom edge,
 * and the edge handles are [edgeLength] px long, centred on their edge.
 */
internal fun isOnResizeHandle(
    position: Offset,
    width: Float,
    height: Float,
    handles: ResizeHandles,
    target: Float,
    edgeLength: Float
): Boolean {
    val nearRight = position.x >= width - target
    val nearBottom = position.y >= height - target
    val onCorner = handles.corner && nearRight && nearBottom
    val onRight = handles.rightEdge && nearRight && abs(position.y - height / 2f) <= edgeLength / 2f
    val onBottom = handles.bottomEdge && nearBottom && abs(position.x - width / 2f) <= edgeLength / 2f
    return onCorner || onRight || onBottom
}

internal val RESIZE_HANDLE_TARGET = 48.dp
internal val RESIZE_HANDLE_EDGE_LENGTH = 64.dp

private val HANDLE_INSET = 6.dp
private val HANDLE_BORDER = 2.dp
private val CORNER_DOT = 22.dp
private val EDGE_PILL_LONG = 40.dp
private val EDGE_PILL_SHORT = 10.dp

/**
 * One resize handle of a selected widget. Dragging it changes the widget's span in whole cells
 * along [axis] ([WidgetLayoutHelper.snapSpan]); [onResize] says what the drag asks for and the
 * caller fits it around the other widgets. The handle is a primary-colored pill with a surface
 * outline, so it shows on any widget.
 */
@Composable
internal fun BoxScope.WidgetResizeHandle(
    axis: ResizeAxis,
    handle: HandleSide,
    description: String,
    span: () -> WidgetLayoutHelper.WidgetSpan,
    metrics: GridMetrics,
    onResize: (WidgetLayoutHelper.WidgetSpan) -> Unit
) {
    val density = LocalDensity.current
    val spanLatest = rememberUpdatedState(span)
    val onResizeLatest = rememberUpdatedState(onResize)
    val (zone, pill) = when (handle) {
        HandleSide.Corner -> Alignment.BottomEnd to CORNER_DOT.let { it to it }
        HandleSide.Right -> Alignment.CenterEnd to (EDGE_PILL_SHORT to EDGE_PILL_LONG)
        HandleSide.Bottom -> Alignment.BottomCenter to (EDGE_PILL_LONG to EDGE_PILL_SHORT)
    }
    val zoneModifier = when (handle) {
        HandleSide.Corner -> Modifier.size(RESIZE_HANDLE_TARGET)
        HandleSide.Right -> Modifier.size(RESIZE_HANDLE_TARGET, RESIZE_HANDLE_EDGE_LENGTH)
        HandleSide.Bottom -> Modifier.size(RESIZE_HANDLE_EDGE_LENGTH, RESIZE_HANDLE_TARGET)
    }
    Box(
        modifier = Modifier
            .align(zone)
            .then(zoneModifier)
            .zIndex(1f)
            .semantics { contentDescription = description }
            .pointerInput(axis, metrics) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    val origin = spanLatest.value()
                    var dragX = 0f
                    var dragY = 0f
                    drag(down.id) { change ->
                        val delta = change.positionChange()
                        change.consume()
                        dragX += delta.x
                        dragY += delta.y
                        val snapped = WidgetLayoutHelper.snapSpan(
                            start = origin,
                            deltaXDp = if (axis == ResizeAxis.Height) 0f else with(density) { dragX.toDp().value },
                            deltaYDp = if (axis == ResizeAxis.Width) 0f else with(density) { dragY.toDp().value },
                            cellDp = metrics.cellDp,
                            spacingDp = metrics.spacingDp
                        )
                        if (snapped != spanLatest.value()) onResizeLatest.value(snapped)
                    }
                }
            },
        contentAlignment = zone
    ) {
        Box(
            modifier = Modifier
                .padding(HANDLE_INSET)
                .size(pill.first, pill.second)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .border(HANDLE_BORDER, MaterialTheme.colorScheme.surface, CircleShape)
        )
    }
}

/** The edge of a widget a handle sits on. */
internal enum class HandleSide { Corner, Right, Bottom }

// ── Edit bar ──

/**
 * The bar that appears while a widget is selected for customizing. It names the widget, says in one
 * line how to move and resize it, and puts the actions that used to be tiny corner icons on the
 * widget (settings, remove) and the way out (Done) in plain, labelled buttons. It floats over the
 * grid, so showing it never moves a widget under the finger.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun WidgetEditBar(
    title: String,
    hasSettings: Boolean,
    onSettings: () -> Unit,
    onRemove: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VoidDimens.sectionSpacing)
    ) {
        Column(modifier = Modifier.padding(VoidDimens.sectionSpacing)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onDone) { Text(stringResource(R.string.done)) }
            }
            Text(
                text = stringResource(R.string.widget_edit_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = VoidDimens.compactSpacing)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VoidDimens.rowSpacing),
                horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
            ) {
                if (hasSettings) {
                    FilledTonalButton(onClick = onSettings, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.widget_edit_settings), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                FilledTonalButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.remove), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ── Header ──

/**
 * The title and the Add widget button, in the header that slides in over the grid when the user
 * pulls down. The second line is the only place that says how to customize a widget, so it is part
 * of the header rather than something the user has to find. The top padding is the status bar, so
 * the header slides in from the very top of the screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun WidgetsHeader(onAddWidget: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraLargeIncreased.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(top = LocalFixedStatusBarHeight.current)
                .padding(
                    start = VoidDimens.screenPadding,
                    end = VoidDimens.sectionSpacing,
                    top = VoidDimens.rowSpacing,
                    bottom = VoidDimens.sectionSpacing
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.widgets),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FilledTonalButton(onClick = onAddWidget) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.add_widget))
                }
            }
            Text(
                text = stringResource(R.string.widget_header_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = VoidDimens.compactSpacing)
            )
        }
    }
}
