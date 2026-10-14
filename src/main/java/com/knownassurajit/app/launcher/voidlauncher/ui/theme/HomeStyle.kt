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
