package com.knownassurajit.app.launcher.voidlauncher.ui.theme.font

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSource
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FontFamiliesTest {

    private fun FontFamily.fonts() = (this as? FontListFontFamily)?.fonts ?: error("expected a font list family")

    private fun specs(source: FontSource): List<FontSpec> = FontCatalog.all.filter { it.source == source }

    @Test
    fun everyBundledCatalogFont_hasAFamily_andNothingElseDoes() {
        assertEquals(
            specs(FontSource.Bundled).map { it.id }.toSet(),
            FontFamilies.bundledFamilies.keys
        )
    }

    @Test
    fun bundledFamilies_declareExactlyTheCatalogWeights() {
        specs(FontSource.Bundled).forEach { spec ->
            val weights = FontFamilies.familyFor(spec).fonts().map { it.weight.weight }
            assertEquals("weights of ${spec.id}", spec.weights, weights)
        }
    }

    @Test
    fun downloadableFamilies_declareOnlyCatalogWeights_withABundledFallbackPerWeight() {
        specs(FontSource.GoogleFonts).forEach { spec ->
            val fonts = FontFamilies.familyFor(spec, FontCoverage.Full).fonts()
            assertEquals("weights of ${spec.id}", spec.weights.flatMap { listOf(it, it) }, fonts.map { it.weight.weight })
            assertTrue(FontCatalog.SUPPORTED_WEIGHTS.containsAll(fonts.map { it.weight.weight }))
            fonts.filterIndexed { index, _ -> index % 2 == 0 }.forEach {
                assertTrue("${spec.id}: $it", it.toString().contains("GoogleFont(\"${spec.gmsName}\""))
            }
            // Behind every download comes the bundled default face, so a missing download never leaves no font.
            fonts.filterIndexed { index, _ -> index % 2 == 1 }.forEach {
                assertTrue("${spec.id}: $it", it is ResourceFont && it.resId == R.font.google_sans_variable)
            }
        }
    }

    @Test
    fun sampleFamily_isRegularOnly() {
        specs(FontSource.GoogleFonts).forEach { spec ->
            val weights = FontFamilies.familyFor(spec, FontCoverage.Sample).fonts().map { it.weight }.distinct()
            assertEquals(listOf(FontWeight.Normal), weights)
        }
    }

    @Test
    fun downloadableFamilies_areMemoised_soTheLoadedFontsAreTheOnesComposed() {
        val spec = requireNotNull(FontCatalog.find("poppins"))
        assertSame(FontFamilies.familyFor(spec), FontFamilies.familyFor(spec))
        assertSame(
            FontFamilies.familyFor(spec, FontCoverage.Sample),
            FontFamilies.familyFor(spec, FontCoverage.Sample)
        )
    }

    @Test
    fun sampleFont_isEqualToTheFullFamilyRegularFont_soOneDownloadServesBoth() {
        val spec = requireNotNull(FontCatalog.find("poppins"))
        val sampleFont = FontFamilies.familyFor(spec, FontCoverage.Sample).fonts().first()
        val fullRegular = FontFamilies.familyFor(spec, FontCoverage.Full).fonts().first()
        assertEquals(sampleFont, fullRegular)
    }

    @Test
    fun systemFont_usesThePlatformDefault() {
        assertSame(FontFamily.Default, FontFamilies.familyFor(requireNotNull(FontCatalog.find("system"))))
    }
}
