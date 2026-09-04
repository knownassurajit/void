package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.R

const val DEFAULT_APP_FONT = "google_sans"

val GoogleSansFamily = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_medium, FontWeight.Medium),
    Font(R.font.google_sans_bold, FontWeight.Bold)
)

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

// MD3 Expressive — 1.25× line-height rule, Regular + Medium weights only
fun getTypography(appFont: String): Typography {
    val family = resolveFontFamily(appFont)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 72.sp,           // 57 × 1.25 ≈ 72
        letterSpacing = (-0.25).sp
    ),
        displayMedium = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 56.sp            // 45 × 1.25 ≈ 56
    ),
        displaySmall = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp            // 36 × 1.22 (closest 4dp snap)
    ),
        headlineLarge = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp            // 32 × 1.25 = 40
    ),
        headlineMedium = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp            // 28 × 1.28 (closest 4dp snap)
    ),
        headlineSmall = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp            // 24 × 1.33 (closest 4dp snap)
    ),
        titleLarge = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp            // 22 × 1.27 (closest 4dp snap)
    ),
        titleMedium = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,           // 16 × 1.25 = 20
        letterSpacing = 0.15.sp
    ),
        titleSmall = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,           // 14 × 1.28 ≈ 18 (snap to even)
        letterSpacing = 0.1.sp
    ),
        bodyLarge = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,           // 16 × 1.25 = 20
        letterSpacing = 0.5.sp
    ),
        bodyMedium = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,           // 14 × 1.28 ≈ 18
        letterSpacing = 0.25.sp
    ),
        bodySmall = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,           // 12 × 1.33 (closest 4dp snap)
        letterSpacing = 0.4.sp
    ),
        labelLarge = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,           // 14 × 1.28 ≈ 18
        letterSpacing = 0.1.sp
    ),
        labelMedium = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,           // 12 × 1.33 (closest 4dp snap)
        letterSpacing = 0.5.sp
    ),
        labelSmall = TextStyle(
            fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,           // 11 × 1.27 ≈ 14
        letterSpacing = 0.5.sp
    )
    )
}
