package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColors
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeTextColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidColorPresets
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.resolve

private val SWATCH_SIZE = 36.dp
private val HUE_BAR_HEIGHT = 28.dp
private const val SV_PANEL_RATIO = 1.6f
private const val HUE_STOPS = 6

/**
 * Picks any color: drag on the saturation/value panel and the hue bar, type or paste a hex code, or
 * tap a theme color (which keeps following dark mode and the wallpaper) or a preset. [onPick] gets a
 * [HomeColor.Role] for a theme swatch and a [HomeColor.Custom] for everything else.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    title: String,
    initial: HomeColor,
    onDismiss: () -> Unit,
    onPick: (HomeColor) -> Unit
) {
    val initialArgb = initial.resolve().toArgb()
    val initialHsv = remember(initialArgb) { HomeColors.argbToHsv(initialArgb) }
    var role by remember { mutableStateOf((initial as? HomeColor.Role)?.role) }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }
    var hexText by remember { mutableStateOf(HomeColors.toHex(initialArgb).removePrefix("#")) }
    var hexError by remember { mutableStateOf(false) }

    val custom = HomeColors.hsvToArgb(hue, saturation, value)
    val roleColor = role?.resolve()
    val shown = roleColor ?: Color(custom)

    fun setFromArgb(argb: Int, keepHex: Boolean = false) {
        val hsv = HomeColors.argbToHsv(argb)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        role = null
        if (!keepHex) hexText = HomeColors.toHex(argb).removePrefix("#")
        hexError = false
    }

    fun setFromHsv(h: Float = hue, s: Float = saturation, v: Float = value) {
        hue = h
        saturation = s
        value = v
        role = null
        hexText = HomeColors.toHex(HomeColors.hsvToArgb(h, s, v)).removePrefix("#")
        hexError = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(VoidDimens.sectionSpacing)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(SWATCH_SIZE + VoidDimens.rowSpacing)
                            .clip(CircleShape)
                            .background(shown)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            val cleaned = input.removePrefix("#").filter { it.isLetterOrDigit() }.take(8).uppercase()
                            hexText = cleaned
                            val parsed = HomeColors.parseHex(cleaned)
                            hexError = parsed == null
                            if (parsed != null) setFromArgb(parsed, keepHex = true)
                        },
                        prefix = { Text("#") },
                        label = { Text(stringResource(R.string.color_hex)) },
                        isError = hexError,
                        supportingText = if (hexError) {
                            { Text(stringResource(R.string.color_hex_invalid)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = VoidDimens.sectionSpacing)
                    )
                }

                SaturationValuePanel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onChange = { s, v -> setFromHsv(s = s, v = v) }
                )
                HueBar(hue = hue, onChange = { h -> setFromHsv(h = h) })

                Text(
                    text = stringResource(R.string.color_theme_colors),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing), verticalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)) {
                    HomeTextColor.entries.forEach { entry ->
                        ColorSwatch(
                            color = entry.resolve(),
                            label = stringResource(entry.labelRes()),
                            selected = role == entry,
                            onClick = {
                                role = entry
                                hexError = false
                            }
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.color_presets),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing), verticalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)) {
                    VoidColorPresets.forEach { preset ->
                        val argb = preset.toArgb()
                        ColorSwatch(
                            color = preset,
                            label = HomeColors.toHex(argb),
                            selected = role == null && custom == argb,
                            onClick = { setFromArgb(argb) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPick(role?.let { HomeColor.Role(it) } ?: HomeColor.Custom(custom)) },
                enabled = !hexError
            ) { Text(stringResource(R.string.font_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun ColorSwatch(color: Color, label: String, selected: Boolean, onClick: () -> Unit) {
    val ring = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .size(SWATCH_SIZE)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 3.dp else 1.dp, ring, CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = label
                role = Role.RadioButton
                this.selected = selected
            },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(VoidDimens.sectionSpacing + VoidDimens.compactSpacing)
            )
        }
    }
}

/** Saturation grows to the right, value to the top, for the current [hue]. */
@Composable
private fun SaturationValuePanel(hue: Float, saturation: Float, value: Float, onChange: (Float, Float) -> Unit) {
    val hueColor = Color(HomeColors.hsvToArgb(hue, 1f, 1f))
    val thumbStroke = MaterialTheme.colorScheme.onSurface
    val description = stringResource(R.string.color_saturation_value)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(SV_PANEL_RATIO)
            .clip(MaterialTheme.shapes.large)
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                fun report(position: Offset) {
                    val s = (position.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (position.y / size.height).coerceIn(0f, 1f)
                    onChange(s, v)
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    report(down.position)
                    drag(down.id) { change ->
                        change.consume()
                        report(change.position)
                    }
                }
            }
            .drawBehind {
                drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                val center = Offset(saturation * size.width, (1f - value) * size.height)
                drawCircle(Color.White, radius = 10.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
                drawCircle(thumbStroke.copy(alpha = 0.6f), radius = 12.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
            }
    )
}

@Composable
private fun HueBar(hue: Float, onChange: (Float) -> Unit) {
    val stops = remember { (0..HUE_STOPS).map { Color(HomeColors.hsvToArgb(it * 360f / HUE_STOPS, 1f, 1f)) } }
    val description = stringResource(R.string.color_hue)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HUE_BAR_HEIGHT)
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                fun report(x: Float) = onChange((x / size.width).coerceIn(0f, 1f) * 359.9f)
                awaitEachGesture {
                    val down = awaitFirstDown()
                    report(down.position.x)
                    drag(down.id) { change ->
                        change.consume()
                        report(change.position.x)
                    }
                }
            }
            .drawBehind {
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(Brush.horizontalGradient(stops), cornerRadius = radius)
                val x = (hue / 360f) * size.width
                drawCircle(Color.White, radius = size.height / 2f - 2.dp.toPx(), center = Offset(x, size.height / 2f), style = Stroke(3.dp.toPx()))
            }
    )
}

internal fun HomeTextColor.labelRes(): Int = when (this) {
    HomeTextColor.Default -> R.string.home_color_default
    HomeTextColor.Primary -> R.string.home_color_primary
    HomeTextColor.Secondary -> R.string.home_color_secondary
    HomeTextColor.Tertiary -> R.string.home_color_tertiary
    HomeTextColor.Muted -> R.string.home_color_muted
}
