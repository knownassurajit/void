package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontKind
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.FontFamilies
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.GoogleSansFamily

const val DEFAULT_APP_FONT = "google_sans"
const val TABULAR_FIGURES_FEATURE = "tnum"

fun TextStyle.withTabularFigures(): TextStyle = copy(fontFeatureSettings = TABULAR_FIGURES_FEATURE)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

val DmSansFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold)
)

val availableFonts: List<Triple<String, String, FontFamily>> = listOf(
    Triple("google_sans", "Google Sans", GoogleSansFamily),
    Triple("inter", "Inter", InterFamily),
    Triple("plus_jakarta", "Plus Jakarta Sans", PlusJakartaSansFamily),
    Triple("manrope", "Manrope", ManropeFamily),
    Triple("dm_sans", "DM Sans", DmSansFamily),
    Triple("system", "System", FontFamily.Default)
)

fun resolveFontFamily(appFont: String): FontFamily {
    return availableFonts.firstOrNull { it.first == appFont }?.third ?: GoogleSansFamily
}

fun createTypography(family: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 72.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 56.sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}

fun getTypography(spec: FontSpec, family: FontFamily = FontFamilies.familyFor(spec)): Typography {
    val base = createTypography(family)
    val scale = spec.lineHeightScale
    val isMono = spec.kind == FontKind.Mono

    fun adjust(style: TextStyle): TextStyle {
        var result = style
        if (scale != 1.0f && result.lineHeight.isSpecified) {
            result = result.copy(lineHeight = (result.lineHeight.value * scale).sp)
        }
        if (isMono && result.letterSpacing.isSpecified && result.letterSpacing.value > 0f) {
            result = result.copy(letterSpacing = 0.sp)
        }
        return result
    }

    return Typography(
        displayLarge = adjust(base.displayLarge),
        displayMedium = adjust(base.displayMedium),
        displaySmall = adjust(base.displaySmall),
        headlineLarge = adjust(base.headlineLarge),
        headlineMedium = adjust(base.headlineMedium),
        headlineSmall = adjust(base.headlineSmall),
        titleLarge = adjust(base.titleLarge),
        titleMedium = adjust(base.titleMedium),
        titleSmall = adjust(base.titleSmall),
        bodyLarge = adjust(base.bodyLarge),
        bodyMedium = adjust(base.bodyMedium),
        bodySmall = adjust(base.bodySmall),
        labelLarge = adjust(base.labelLarge),
        labelMedium = adjust(base.labelMedium),
        labelSmall = adjust(base.labelSmall)
    )
}

fun getTypography(appFont: String): Typography {
    val spec = FontCatalog.find(appFont)
    return if (spec != null) {
        getTypography(spec)
    } else {
        val family = resolveFontFamily(appFont)
        createTypography(family)
    }
}
