package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColors
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.resolve
import androidx.compose.ui.graphics.toArgb

/** Where an item sits in its [SettingsSection], which picks its segmented shape. */
@Immutable
data class SegmentPosition(val index: Int, val count: Int)

val LocalSegmentPosition = compositionLocalOf { SegmentPosition(0, 1) }

/** Connected toggle buttons fit this many options on a row; more fall back to wrapping chips. */
private const val MAX_CONNECTED_OPTIONS = 4

/** Gap between the segments of a section: the container shows through as a hairline. */
private val SEGMENT_GAP = 2.dp

/**
 * A category heading above a run of [SettingsSection]s ("Appearance", "Home screen", ...).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLargeEmphasized,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            start = VoidDimens.compactSpacing,
            top = VoidDimens.sectionSpacing + VoidDimens.rowSpacing,
            bottom = VoidDimens.compactSpacing
        )
    )
}

/**
 * Material 3 Expressive segmented list: an optional label, then [items] as one rounded group whose
 * segments are 2 dp apart, the outer corners large and the inner ones small.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSection(
    title: String?,
    items: List<@Composable () -> Unit>,
    description: String? = null
) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(top = VoidDimens.rowSpacing)) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = VoidDimens.sectionSpacing,
                    bottom = if (description == null) VoidDimens.rowSpacing else VoidDimens.compactSpacing
                )
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = VoidDimens.sectionSpacing,
                    end = VoidDimens.sectionSpacing,
                    bottom = VoidDimens.rowSpacing
                )
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(SEGMENT_GAP)) {
            items.forEachIndexed { index, item ->
                CompositionLocalProvider(LocalSegmentPosition provides SegmentPosition(index, items.size)) {
                    item()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun segmentShapes() = LocalSegmentPosition.current.let { ListItemDefaults.segmentedShapes(it.index, it.count) }

@Composable
private fun Subtitle(text: String) {
    if (text.isNotEmpty()) Text(text, maxLines = 3, overflow = TextOverflow.Ellipsis)
}

/** Opens something (a screen, a picker, system settings). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingActionItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = {
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = segmentShapes(),
        leadingContent = icon,
        supportingContent = if (subtitle.isEmpty()) null else { { Subtitle(subtitle) } },
        trailingContent = trailing
    ) { Text(title) }
}

/** A switch row: the whole row toggles; the thumb shows a check while on. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = segmentShapes(),
        leadingContent = icon,
        supportingContent = if (subtitle.isEmpty()) null else { { Subtitle(subtitle) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else {
                    null
                }
            )
        }
    ) { Text(title) }
}

/**
 * A slider row with its current value at the end. [onHoldingChanged] reports while the thumb is
 * pressed or dragged (Settings shows the full-screen preview meanwhile).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingSliderItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: (Float) -> String,
    onHoldingChanged: ((Boolean) -> Unit)? = null,
    onChanged: (Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHolding = isDragged || isPressed
    LaunchedEffect(isHolding) { onHoldingChanged?.invoke(isHolding) }
    val shownValue = valueText(value)

    SegmentedListItem(
        shapes = segmentShapes(),
        leadingContent = icon,
        trailingContent = {
            Text(shownValue, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        },
        supportingContent = {
            Column {
                Subtitle(subtitle)
                Slider(
                    value = value,
                    onValueChange = onChanged,
                    valueRange = range,
                    interactionSource = interactionSource,
                    modifier = Modifier.semantics { stateDescription = shownValue }
                )
            }
        }
    ) { Text(title) }
}

/**
 * One choice out of [options]: connected toggle buttons (Material 3 Expressive button group) for up
 * to four options, wrapping chips for more.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun <T> SettingChoiceItem(
    title: String,
    icon: @Composable () -> Unit,
    selected: T,
    options: List<Pair<T, String>>,
    subtitle: String = "",
    onSelected: (T) -> Unit
) {
    SegmentedListItem(
        shapes = segmentShapes(),
        leadingContent = icon,
        supportingContent = {
            Column {
                Subtitle(subtitle)
                if (options.size <= MAX_CONNECTED_OPTIONS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VoidDimens.rowSpacing)
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        options.forEachIndexed { index, (value, label) ->
                            ToggleButton(
                                checked = selected == value,
                                onCheckedChange = { onSelected(value) },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { role = Role.RadioButton },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                }
                            ) { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VoidDimens.rowSpacing)
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
                    ) {
                        options.forEach { (value, label) ->
                            FilterChip(
                                selected = selected == value,
                                onClick = { onSelected(value) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }
    ) { Text(title) }
}

/** A number changed one step at a time. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingCounterItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit
) {
    SegmentedListItem(
        shapes = segmentShapes(),
        leadingContent = icon,
        supportingContent = if (subtitle.isEmpty()) null else { { Subtitle(subtitle) } },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { onChange(value - 1) }, enabled = value > range.first) {
                    Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.decrease))
                }
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = VoidDimens.rowSpacing)
                )
                FilledTonalIconButton(onClick = { onChange(value + 1) }, enabled = value < range.last) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.increase))
                }
            }
        }
    ) { Text(title) }
}

/** A color row: shows the current swatch and code, opens the [ColorPickerDialog]. */
@Composable
fun SettingColorItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    color: HomeColor,
    onPick: (HomeColor) -> Unit
) {
    var picking by remember { mutableStateOf(false) }
    val resolved = color.resolve()
    val code = when (color) {
        is HomeColor.Role -> stringResource(color.role.labelRes())
        is HomeColor.Custom -> HomeColors.toHex(resolved.toArgb())
    }
    SettingActionItem(
        title = title,
        subtitle = if (subtitle.isEmpty()) code else "$subtitle · $code",
        icon = icon,
        onClick = { picking = true },
        trailing = {
            Box(
                modifier = Modifier
                    .size(VoidDimens.fontStateIconSize + VoidDimens.rowSpacing)
                    .background(resolved, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    )
    if (picking) {
        ColorPickerDialog(
            title = title,
            initial = color,
            onDismiss = { picking = false },
            onPick = {
                picking = false
                onPick(it)
            }
        )
    }
}
