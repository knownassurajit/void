package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isUnspecified
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyPolicyTest {

    // A plain family keeps the tests off the Android font loader.
    private val family: FontFamily = FontFamily.Serif

    private fun typography(id: String): Typography =
        getTypography(requireNotNull(FontCatalog.find(id)), family)

    private fun Typography.roles(): List<TextStyle> = listOf(
        displayLarge, displayMedium, displaySmall,
        headlineLarge, headlineMedium, headlineSmall,
        titleLarge, titleMedium, titleSmall,
        bodyLarge, bodyMedium, bodySmall,
        labelLarge, labelMedium, labelSmall
    )

    @Test
    fun oneFamily_drivesAllFifteenRoles() {
        val roles = typography("poppins").roles()
        assertEquals(15, roles.size)
        roles.forEach { assertSame(family, it.fontFamily) }
    }

    @Test
    fun baselineFont_keepsTheOriginalLineHeights() {
        val type = typography("google_sans")
        assertEquals(72f, type.displayLarge.lineHeight.value, 0f)
        assertEquals(20f, type.bodyLarge.lineHeight.value, 0f)
        assertEquals(14f, type.labelSmall.lineHeight.value, 0f)
    }

    @Test
    fun tallFamily_scalesEveryLineHeightByItsPolicyScale() {
        val spec: FontSpec = requireNotNull(FontCatalog.find("hind"))
        val base = typography("google_sans").roles()
        val scaled = typography("hind").roles()
        base.zip(scaled).forEach { (plain, tall) ->
            assertEquals(plain.lineHeight.value * spec.lineHeightScale, tall.lineHeight.value, 0.001f)
            assertTrue(tall.lineHeight.value >= plain.lineHeight.value)
        }
        assertEquals(20f * 1.15f, typography("hind").bodyLarge.lineHeight.value, 0.001f)
    }

    @Test
    fun monoFamily_dropsPositiveTracking_butKeepsNegative() {
        val mono = typography("inconsolata")
        listOf(mono.titleMedium, mono.titleSmall, mono.bodyLarge, mono.bodyMedium, mono.bodySmall,
            mono.labelLarge, mono.labelMedium, mono.labelSmall).forEach {
            assertEquals(0f, it.letterSpacing.value, 0f)
        }
        assertEquals(-0.25f, mono.displayLarge.letterSpacing.value, 0f)
    }

    @Test
    fun proportionalFamily_keepsTheTokenTracking() {
        val type = typography("lato")
        assertEquals(0.5f, type.bodyLarge.letterSpacing.value, 0f)
        assertEquals(0.15f, type.titleMedium.letterSpacing.value, 0f)
        assertEquals(-0.25f, type.displayLarge.letterSpacing.value, 0f)
    }

    @Test
    fun rolesWithoutTracking_stayUnspecified() {
        val type = typography("inconsolata")
        assertTrue(type.displayMedium.letterSpacing.isUnspecified)
        assertTrue(type.headlineLarge.letterSpacing.isUnspecified)
        assertTrue(type.titleLarge.letterSpacing.isUnspecified)
    }

    @Test
    fun roles_onlyAskForRegularAndMedium() {
        FontCatalog.all.forEach { spec ->
            getTypography(spec, family).roles().forEach {
                assertTrue(it.fontWeight == FontWeight.Normal || it.fontWeight == FontWeight.Medium)
            }
        }
    }

    @Test
    fun synthesisIsNeverDisabled_soBoldTextSettingKeepsWorking() {
        FontCatalog.all.forEach { spec ->
            getTypography(spec, family).roles().forEach { assertEquals(null, it.fontSynthesis) }
        }
    }

    @Test
    fun defaultFamily_isTheFullFamilyTheLoaderPreloaded() {
        // Compose caches async fonts by font equality: the theme must compose exactly what FontRepository preloaded.
        val downloadable = requireNotNull(FontCatalog.find("poppins"))
        val type = getTypography(downloadable)
        type.roles().forEach {
            assertSame(FontFamilies.familyFor(downloadable, FontCoverage.Full), it.fontFamily)
        }

        val bundled = requireNotNull(FontCatalog.find("inter"))
        assertSame(FontFamilies.familyFor(bundled), getTypography(bundled).bodyLarge.fontFamily)
        assertSame(FontFamily.Default, getTypography(requireNotNull(FontCatalog.find("system"))).bodyLarge.fontFamily)
    }
}
