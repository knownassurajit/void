package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetGrid
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidPanelTitle
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens

// ── Edit bar ──

/**
 * The bar that appears while a widget is picked for customizing. It names the widget and says in one
 * line how to move it; its size is changed with plain Width and Height buttons, one cell at a time,
 * only the ways the widget's app allows and never over another widget ([steps], from
 * `WidgetGrid.resizeSteps`); Settings, Remove and Done are labelled buttons. Nothing is drawn on the
 * widget itself: a handle there competes with the widget's own content for the finger. The bar
 * floats over the grid, so showing it never moves a widget.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun WidgetEditBar(
    title: String,
    span: WidgetLayoutHelper.WidgetSpan,
    resizesHorizontally: Boolean,
    resizesVertically: Boolean,
    steps: WidgetGrid.ResizeSteps,
    hasSettings: Boolean,
    onResize: (WidgetLayoutHelper.WidgetSpan) -> Unit,
    onSettings: () -> Unit,
    onRemove: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = VoidDimens.floatingBarTonalElevation,
        shadowElevation = VoidDimens.floatingBarElevation,
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
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                Button(onClick = onDone) { Text(stringResource(R.string.done)) }
            }
            Text(
                text = stringResource(R.string.widget_edit_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = VoidDimens.compactSpacing)
            )
            if (resizesHorizontally || resizesVertically) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = VoidDimens.rowSpacing),
                    horizontalArrangement = Arrangement.spacedBy(VoidDimens.sectionSpacing)
                ) {
                    if (resizesHorizontally) {
                        SizeStepper(
                            label = stringResource(R.string.widget_width),
                            value = span.columns,
                            decreaseLabel = stringResource(R.string.widget_action_narrower),
                            increaseLabel = stringResource(R.string.widget_action_wider),
                            onDecrease = steps.narrower?.let { { onResize(it) } },
                            onIncrease = steps.wider?.let { { onResize(it) } }
                        )
                    }
                    if (resizesVertically) {
                        SizeStepper(
                            label = stringResource(R.string.widget_height),
                            value = span.rows,
                            decreaseLabel = stringResource(R.string.widget_action_shorter),
                            increaseLabel = stringResource(R.string.widget_action_taller),
                            onDecrease = steps.shorter?.let { { onResize(it) } },
                            onIncrease = steps.taller?.let { { onResize(it) } }
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.widget_fixed_size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = VoidDimens.rowSpacing)
                )
            }
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

/**
 * One size of the widget: its name, a minus button, the number of cells and a plus button. A button
 * whose step is not possible ([onDecrease] or [onIncrease] `null`) is disabled. A screen reader
 * hears the name with the number ("Width, 2 cells") and the buttons by what they do.
 */
@Composable
private fun RowScope.SizeStepper(
    label: String,
    value: Int,
    decreaseLabel: String,
    increaseLabel: String,
    onDecrease: (() -> Unit)?,
    onIncrease: (() -> Unit)?
) {
    val spoken = pluralStringResource(R.plurals.widget_size_cells, value, label, value)
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = { onDecrease?.invoke() }, enabled = onDecrease != null) {
                Icon(Icons.Outlined.Remove, contentDescription = decreaseLabel)
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = spoken }
            )
            FilledTonalIconButton(onClick = { onIncrease?.invoke() }, enabled = onIncrease != null) {
                Icon(Icons.Outlined.Add, contentDescription = increaseLabel)
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
        shadowElevation = VoidDimens.overlayHeaderElevation,
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
            VoidPanelTitle(text = stringResource(R.string.widgets)) {
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
