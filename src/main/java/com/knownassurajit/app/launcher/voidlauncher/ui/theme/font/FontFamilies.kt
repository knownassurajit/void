package com.knownassurajit.app.launcher.voidlauncher.ui.theme.font

import androidx.annotation.FontRes
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontLoadKey
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSource
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import com.knownassurajit.app.launcher.voidlauncher.data.font.GoogleFontsProvider
import java.util.concurrent.ConcurrentHashMap
import androidx.compose.ui.text.googlefonts.Font as GoogleFontsFont

// The stable Font(resId, ...) overload passes empty variation settings; the wght axis must be set explicitly,
// otherwise every declared weight would render the file's default instance.
@OptIn(ExperimentalTextApi::class)
private fun variableFont(@FontRes resId: Int, weight: FontWeight): Font = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

private fun weightsOf(spec: FontSpec): List<FontWeight> = spec.weights.map(::FontWeight)

// Google Sans (SIL OFL 1.1) ships as one variable file: GRAD and opsz are pinned, wght (400..700) is kept.
// Provenance and the exact subsetting recipe live in docs/features/fonts.md.
val GoogleSansFamily: FontFamily = FontFamily(
    variableFont(R.font.google_sans_variable, FontWeight.Normal),
    variableFont(R.font.google_sans_variable, FontWeight.Medium),
    variableFont(R.font.google_sans_variable, FontWeight.Bold)
)

/**
 * Compose font families for the catalog. Families are memoised: a downloadable family must be the *same value* the
 * loader preloaded, because Compose keys its permanent font cache by font equality.
 */
object FontFamilies {

    private val googleFontsProvider = GoogleFont.Provider(
        providerAuthority = GoogleFontsProvider.AUTHORITY,
        providerPackage = GoogleFontsProvider.PACKAGE,
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val staticInter = staticFamily(R.font.inter_regular, R.font.inter_medium, R.font.inter_bold)
    private val staticPlusJakartaSans = staticFamily(
        R.font.plus_jakarta_sans_regular,
        R.font.plus_jakarta_sans_medium,
        R.font.plus_jakarta_sans_bold
    )
    private val staticManrope = staticFamily(R.font.manrope_regular, R.font.manrope_medium, R.font.manrope_bold)
    private val staticDmSans = staticFamily(R.font.dm_sans_regular, R.font.dm_sans_medium, R.font.dm_sans_bold)

    // Geist ships as one unmodified variable file (wght 100..900), one entry per declared weight.
    private val variableGeist = FontFamily(
        variableFont(R.font.geist_variable, FontWeight.Normal),
        variableFont(R.font.geist_variable, FontWeight.Medium),
        variableFont(R.font.geist_variable, FontWeight.Bold)
    )

    /** Families for every [FontSource.Bundled] catalog entry, by id. */
    internal val bundledFamilies: Map<String, FontFamily> = mapOf(
        "google_sans" to GoogleSansFamily,
        "dm_sans" to staticDmSans,
        "geist" to variableGeist,
        "inter" to staticInter,
        "manrope" to staticManrope,
        "plus_jakarta" to staticPlusJakartaSans
    )

    private val downloadableFamilies = ConcurrentHashMap<FontLoadKey, FontFamily>()

    /**
     * The family to hand to text styles. For a downloadable spec this must only enter composition after its load
     * for the same [coverage] finished, see `FontLoadCoordinator`.
     */
    fun familyFor(spec: FontSpec, coverage: FontCoverage = FontCoverage.Full): FontFamily =
        when (spec.source) {
            FontSource.System -> FontFamily.Default
            FontSource.Bundled -> bundledFamilies.getValue(spec.id)
            FontSource.GoogleFonts ->
                downloadableFamilies.getOrPut(FontLoadKey(spec.id, coverage)) { downloadableFamily(spec, coverage) }
        }

    // Per weight the chain is [download, bundled default]: if the download is ever missing, text keeps a real face.
    private fun downloadableFamily(spec: FontSpec, coverage: FontCoverage): FontFamily {
        val googleFont = GoogleFont(requireNotNull(spec.gmsName) { "${spec.id} has no Google Fonts name" })
        val weights = if (coverage == FontCoverage.Sample) {
            listOf(FontWeight.Normal)
        } else {
            weightsOf(spec)
        }
        return FontFamily(
            weights.flatMap { weight ->
                listOf(
                    GoogleFontsFont(googleFont, googleFontsProvider, weight),
                    variableFont(R.font.google_sans_variable, weight)
                )
            }
        )
    }

    private fun staticFamily(@FontRes regular: Int, @FontRes medium: Int, @FontRes bold: Int): FontFamily =
        FontFamily(
            Font(regular, FontWeight.Normal),
            Font(medium, FontWeight.Medium),
            Font(bold, FontWeight.Bold)
        )
}
