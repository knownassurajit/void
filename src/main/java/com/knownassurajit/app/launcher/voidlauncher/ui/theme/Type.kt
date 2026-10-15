package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.FontFamilies

// Google Sans digits are proportional by default; the subset keeps its `tnum` feature (docs/features/fonts.md).
internal const val TABULAR_FIGURES_FEATURE = "tnum"

/** Fixed-width digits for text that changes while it is on screen (clock, date, screen time), so it does not jitter. */
fun TextStyle.withTabularFigures(): TextStyle = copy(fontFeatureSettings = TABULAR_FIGURES_FEATURE)

/**
 * One family drives all 15 Material roles, so the whole app retypesets. Tokens: 1.25x line-height rule with
 * Regular and Medium weights. Per family, [FontSpec.lineHeightScale] stretches tall faces and
 * [FontSpec.letterSpacing] drops positive tracking for monospaced ones. Weights are left to the font matcher, so
 * a family without Medium renders Regular and the system "Bold text" setting keeps working.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun getTypography(spec: FontSpec, family: FontFamily = FontFamilies.familyFor(spec)): Typography {
    fun role(weight: FontWeight, size: Float, lineHeight: Float, letterSpacing: Float? = null) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (lineHeight * spec.lineHeightScale).sp,
        letterSpacing = letterSpacing?.let { spec.letterSpacing(it).sp } ?: TextUnit.Unspecified
    )

    // Expressive emphasized roles: one step heavier than their base role (Regular -> Medium, Medium -> Bold), in
    // the same family, so headings stand out without leaving the weights every bundled family ships.
    return Typography(
        displayLarge = role(FontWeight.Normal, 57f, 72f, letterSpacing = -0.25f),
        displayMedium = role(FontWeight.Normal, 45f, 56f),
        displaySmall = role(FontWeight.Normal, 36f, 44f),
        headlineLarge = role(FontWeight.Medium, 32f, 40f),
        headlineMedium = role(FontWeight.Medium, 28f, 36f),
        headlineSmall = role(FontWeight.Medium, 24f, 32f),
        titleLarge = role(FontWeight.Medium, 22f, 28f),
        titleMedium = role(FontWeight.Medium, 16f, 20f, letterSpacing = 0.15f),
        titleSmall = role(FontWeight.Medium, 14f, 18f, letterSpacing = 0.1f),
        bodyLarge = role(FontWeight.Normal, 16f, 20f, letterSpacing = 0.5f),
        bodyMedium = role(FontWeight.Normal, 14f, 18f, letterSpacing = 0.25f),
        bodySmall = role(FontWeight.Normal, 12f, 16f, letterSpacing = 0.4f),
        labelLarge = role(FontWeight.Medium, 14f, 18f, letterSpacing = 0.1f),
        labelMedium = role(FontWeight.Medium, 12f, 16f, letterSpacing = 0.5f),
        labelSmall = role(FontWeight.Medium, 11f, 14f, letterSpacing = 0.5f),
        displayLargeEmphasized = role(FontWeight.Medium, 57f, 72f, letterSpacing = -0.25f),
        displayMediumEmphasized = role(FontWeight.Medium, 45f, 56f),
        displaySmallEmphasized = role(FontWeight.Medium, 36f, 44f),
        headlineLargeEmphasized = role(FontWeight.Bold, 32f, 40f),
        headlineMediumEmphasized = role(FontWeight.Bold, 28f, 36f),
        headlineSmallEmphasized = role(FontWeight.Bold, 24f, 32f),
        titleLargeEmphasized = role(FontWeight.Bold, 22f, 28f),
        titleMediumEmphasized = role(FontWeight.Bold, 16f, 20f, letterSpacing = 0.15f),
        titleSmallEmphasized = role(FontWeight.Bold, 14f, 18f, letterSpacing = 0.1f),
        bodyLargeEmphasized = role(FontWeight.Medium, 16f, 20f, letterSpacing = 0.5f),
        bodyMediumEmphasized = role(FontWeight.Medium, 14f, 18f, letterSpacing = 0.25f),
        bodySmallEmphasized = role(FontWeight.Medium, 12f, 16f, letterSpacing = 0.4f),
        labelLargeEmphasized = role(FontWeight.Bold, 14f, 18f, letterSpacing = 0.1f),
        labelMediumEmphasized = role(FontWeight.Bold, 12f, 16f, letterSpacing = 0.5f),
        labelSmallEmphasized = role(FontWeight.Bold, 11f, 14f, letterSpacing = 0.5f)
    )
}
