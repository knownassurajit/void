package com.knownassurajit.app.launcher.voidlauncher

import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.font.resolveAsTypeface
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.isAvailableOnDevice
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontLoadState
import com.knownassurajit.app.launcher.voidlauncher.data.font.GoogleFontsProvider
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.FontRepository
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.FontFamilies
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Font delivery checks for an EMULATOR with Google Play services (a Play Store image) and a network.
 * The provider tests skip themselves when the provider is missing, so they also document the no-Play-services
 * path. The download test only runs when the instrumentation argument [ALLOW_DOWNLOAD_ARGUMENT] is "true", so a
 * plain `connectedDebugAndroidTest` on a physical device never downloads fonts.
 */
@RunWith(AndroidJUnit4::class)
class FontProviderInstrumentedTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun bundledGeist_resolvesToATypeface() {
        val geist = requireNotNull(FontCatalog.find("geist"))
        val typeface = createFontFamilyResolver(context)
            .resolveAsTypeface(FontFamilies.familyFor(geist))
            .value
        assertNotNull(typeface)
    }

    @Test
    fun providerCertificates_matchTheInstalledPlayServices() {
        assumeTrue(FeatureAvailability.isGoogleFontsProviderAvailable(context))
        val provider = GoogleFont.Provider(
            providerAuthority = GoogleFontsProvider.AUTHORITY,
            providerPackage = GoogleFontsProvider.PACKAGE,
            certificates = R.array.com_google_android_gms_fonts_certs
        )
        assertTrue(provider.isAvailableOnDevice(context))
    }

    @Test
    fun downloadableFont_loadsFully_andCoversTheSample() = runBlocking {
        assumeTrue(
            "pass -e $ALLOW_DOWNLOAD_ARGUMENT true on an emulator to run the download",
            InstrumentationRegistry.getArguments().getString(ALLOW_DOWNLOAD_ARGUMENT) == "true"
        )
        assumeTrue(FeatureAvailability.isGoogleFontsProviderAvailable(context))
        val repository = FontRepository.get(context)
        val workSans = requireNotNull(FontCatalog.find("work_sans"))

        assertEquals(FontLoadState.Ready, repository.ensureLoaded(workSans, FontCoverage.Full).await())
        assertEquals(FontLoadState.Ready, repository.ensureLoaded(workSans, FontCoverage.Sample).await())
    }

    private companion object {
        const val ALLOW_DOWNLOAD_ARGUMENT = "allowFontDownload"
    }
}
