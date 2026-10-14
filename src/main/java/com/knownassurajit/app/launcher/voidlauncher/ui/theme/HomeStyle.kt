package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import kotlin.math.roundToInt

/**
 * Colors Home text can take. Only theme roles, so a choice follows dark mode and the wallpaper
 * (dynamic color) instead of pinning a raw value.
 */
enum class HomeTextColor(val key: String) {
    Default("default"),
    Primary("primary"),
    Secondary("secondary"),
    Tertiary("tertiary"),
    Muted("muted");

    companion object {
        fun fromKey(key: String?): HomeTextColor = entries.firstOrNull { it.key == key } ?: Default
    }
}

/**
 * A color the user chose for Home text or for icons: a theme [Role] (follows dark mode and dynamic
 * color) or any [Custom] color from the picker. Stored as its [key]: the role key, or `#RRGGBB` /
 * `#AARRGGBB`.
 */
sealed interface HomeColor {
    val key: String

    data class Role(val role: HomeTextColor) : HomeColor {
        override val key: String get() = role.key
    }

    data class Custom(val argb: Int) : HomeColor {
        override val key: String get() = HomeColors.toHex(argb)
    }

    companion object {
        val Default: HomeColor = Role(HomeTextColor.Default)

        /** A hex value becomes [Custom]; anything else is read as a role, unknown keys as [Default]. */
        fun parse(key: String?): HomeColor =
            HomeColors.parseHex(key)?.let(::Custom) ?: Role(HomeTextColor.fromKey(key))
    }
}

@Composable
@ReadOnlyComposable
fun HomeColor.resolve(): Color = when (this) {
    is HomeColor.Role -> role.resolve()
    is HomeColor.Custom -> Color(argb)
}

/** Hex and HSV conversions for [HomeColor], pure so they are unit tested. */
object HomeColors {
    private const val OPAQUE = 0xFF
    private const val HUE_SECTOR = 60f

    /** `#RGB`, `#RRGGBB` or `#AARRGGBB`, with or without `#`, any case; `null` for anything else. */
    fun parseHex(text: String?): Int? {
        val digits = text?.trim()?.removePrefix("#") ?: return null
        if (digits.isEmpty() || digits.any { it !in '0'..'9' && it.lowercaseChar() !in 'a'..'f' }) return null
        return when (digits.length) {
            3 -> (OPAQUE shl 24) or digits.fold(0) { acc, c -> val v = c.digitToInt(16); (acc shl 8) or (v shl 4) or v }
            6 -> (OPAQUE shl 24) or digits.toInt(16)
            8 -> digits.toLong(16).toInt()
            else -> null
        }
    }

    /** `#RRGGBB` for an opaque color, `#AARRGGBB` otherwise. */
    fun toHex(argb: Int): String =
        if ((argb ushr 24) == OPAQUE) "#%06X".format(java.util.Locale.ROOT, argb and 0xFFFFFF)
        else "#%08X".format(java.util.Locale.ROOT, argb)

    /** Opaque color for [hue] in degrees (0..360), [saturation] and [value] in 0..1. */
    fun hsvToArgb(hue: Float, saturation: Float, value: Float): Int {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val c = v * s
        val x = c * (1f - kotlin.math.abs((h / HUE_SECTOR) % 2f - 1f))
        val m = v - c
        val (r, g, b) = when ((h / HUE_SECTOR).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        fun channel(f: Float) = ((f + m) * 255f).roundToInt().coerceIn(0, 255)
        return (OPAQUE shl 24) or (channel(r) shl 16) or (channel(g) shl 8) or channel(b)
    }

    /** Hue (0..360), saturation and value (0..1) of [argb]; alpha is ignored. */
    fun argbToHsv(argb: Int): FloatArray {
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        val max = maxOf(r, g, b)
        val delta = max - minOf(r, g, b)
        val hue = when {
            delta == 0f -> 0f
            max == r -> HUE_SECTOR * (((g - b) / delta) % 6f)
            max == g -> HUE_SECTOR * ((b - r) / delta + 2f)
            else -> HUE_SECTOR * ((r - g) / delta + 4f)
        }.let { if (it < 0f) it + 360f else it }
        val saturation = if (max == 0f) 0f else delta / max
        return floatArrayOf(hue, saturation, max)
    }
}

@Composable
@ReadOnlyComposable
fun HomeTextColor.resolve(): Color = when (this) {
    HomeTextColor.Default -> MaterialTheme.colorScheme.onBackground
    HomeTextColor.Primary -> MaterialTheme.colorScheme.primary
    HomeTextColor.Secondary -> MaterialTheme.colorScheme.secondary
    HomeTextColor.Tertiary -> MaterialTheme.colorScheme.tertiary
    HomeTextColor.Muted -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * The text styles and layout math of the Home screen, shared by Home itself and by the Settings
 * previews so a preview always draws what Home will draw.
 */
object HomeStyle {
    const val MIN_OPACITY = 0.2f
    const val MAX_OPACITY = 1f
    const val MIN_SIDE_PADDING_DP = 8f
    const val MAX_SIDE_PADDING_DP = 48f
    const val DEFAULT_SIDE_PADDING_DP = 20f
    const val DEFAULT_CLOCK_WEIGHT = 700
    const val DEFAULT_APP_WEIGHT = 400
    const val MIN_SIZE_SCALE = 0.5f
    const val MAX_CLOCK_SIZE_SCALE = 1.5f
    const val MAX_APP_SIZE_SCALE = 2.0f
    const val MAX_APP_SPACING = 48f

    /** The clock shrinks when it also shows seconds, so the longer time still fits one line. */
    private const val SECONDS_CLOCK_SCALE = 0.7f

    /** The app-spacing setting value that leaves the computed base gap unscaled. */
    private const val UNIT_APP_SPACING = 24f
    private const val MIN_BASE_GAP_DP = 4f
    private const val MAX_BASE_GAP_DP = 32f
    private const val MAX_GAP_DP = 64f

    /** Weights every bundled family ships, so a choice never falls back to a synthesized face. */
    val fontWeights: List<Int> = FontCatalog.SUPPORTED_WEIGHTS

    fun coerceWeight(weight: Int, default: Int): Int = if (weight in fontWeights) weight else default

    fun coerceOpacity(value: Float): Float =
        if (value.isNaN()) MAX_OPACITY else value.coerceIn(MIN_OPACITY, MAX_OPACITY)

    fun coerceSidePadding(value: Float): Float =
        if (value.isNaN()) DEFAULT_SIDE_PADDING_DP else value.coerceIn(MIN_SIDE_PADDING_DP, MAX_SIDE_PADDING_DP)

    /**
     * Gap between Home app labels: a share of the screen height for the number of apps shown, scaled
     * by the App Spacing setting (0 removes the gap).
     */
    fun appSpacingDp(screenHeightDp: Int, displayedCount: Int, appSpacingSetting: Float): Float {
        val base = ((screenHeightDp * 0.75f) / (displayedCount.coerceAtLeast(1) + 3)).coerceIn(MIN_BASE_GAP_DP, MAX_BASE_GAP_DP)
        val multiplier = if (appSpacingSetting <= 0f || appSpacingSetting.isNaN()) 0f else appSpacingSetting / UNIT_APP_SPACING
        return (base * multiplier).coerceIn(0f, MAX_GAP_DP)
    }

    fun clockStyle(typography: Typography, sizeScale: Float, showSeconds: Boolean, weight: Int): TextStyle =
        typography.displayLarge.copy(
            fontWeight = FontWeight(weight),
            fontSize = typography.displayLarge.fontSize * sizeScale * (if (showSeconds) SECONDS_CLOCK_SCALE else 1f),
            letterSpacing = (-1.5).sp
        ).withTabularFigures()

    fun dateStyle(typography: Typography, sizeScale: Float): TextStyle =
        typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            fontSize = typography.bodySmall.fontSize * sizeScale
        ).withTabularFigures()

    fun screenTimeStyle(typography: Typography, sizeScale: Float): TextStyle =
        typography.labelSmall.copy(
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.5.sp,
            fontSize = typography.labelSmall.fontSize * sizeScale
        ).withTabularFigures()

    fun appLabelStyle(typography: Typography, sizeScale: Float, weight: Int): TextStyle =
        typography.headlineLarge.copy(
            fontWeight = FontWeight(weight),
            fontSize = typography.headlineLarge.fontSize * sizeScale,
            letterSpacing = (-0.5).sp
        )
}
