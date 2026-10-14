package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import androidx.compose.ui.unit.Density
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.GoogleSansFamily
import org.junit.Assert.assertEquals
import org.junit.Test

/** The fonts [GoogleSansFamily] declares, as resource fonts; fails with a readable message otherwise. */
internal fun googleSansFonts(): List<ResourceFont> {
    val family = GoogleSansFamily as? FontListFontFamily
        ?: error("GoogleSansFamily must be built from a font list")
    return family.fonts.map { font -> font as? ResourceFont ?: error("expected a resource font, was $font") }
}

/**
 * Guards the wiring that makes Medium and Bold differ from Regular.
 *
 * The stable `Font(resId, weight, style)` overload passes empty variation settings, so a single
 * variable file would render every weight at its default `wght` instance and Compose would not
 * synthesize bold because the declared weight already matches.
 */
@OptIn(ExperimentalTextApi::class)
class GoogleSansFamilyTest {

    @Test
    fun declaresRegularMediumAndBold() {
        assertEquals(
            listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.Bold),
            googleSansFonts().map { it.weight }
        )
    }

    @Test
    fun everyWeight_isServedByTheBundledVariableAsset() {
        assertEquals(
            List(googleSansFonts().size) { R.font.google_sans_variable },
            googleSansFonts().map { it.resId }
        )
    }

    @Test
    fun everyWeight_setsOnlyTheWeightAxisToItsDeclaredWeight() {
        googleSansFonts().forEach { font ->
            val settings = font.variationSettings.settings
            assertEquals("variation axes for ${font.weight}", listOf("wght"), settings.map { it.axisName })
            assertEquals(
                "wght value for ${font.weight}",
                font.weight.weight.toFloat(),
                settings.single().toVariationValue(Density(1f)),
                0f
            )
        }
    }
}
