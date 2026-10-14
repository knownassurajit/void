package com.knownassurajit.app.launcher.voidlauncher.data.font

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FontLoadStateTest {

    private fun key(id: String, coverage: FontCoverage) = FontLoadKey(id, coverage)

    @Test
    fun missingEntry_isIdle() {
        assertEquals(FontLoadState.Idle, emptyMap<FontLoadKey, FontLoadState>().stateOf("poppins", FontCoverage.Full))
    }

    @Test
    fun fullReady_coversSample_butSampleReadyDoesNotCoverFull() {
        val fullReady: FontLoadStates = mapOf(key("poppins", FontCoverage.Full) to FontLoadState.Ready)
        assertTrue(fullReady.isReady("poppins", FontCoverage.Sample))
        assertTrue(fullReady.isReady("poppins", FontCoverage.Full))

        val sampleReady: FontLoadStates = mapOf(key("poppins", FontCoverage.Sample) to FontLoadState.Ready)
        assertTrue(sampleReady.isReady("poppins", FontCoverage.Sample))
        assertFalse(sampleReady.isReady("poppins", FontCoverage.Full))
        assertFalse(sampleReady.isReady("lato", FontCoverage.Sample))
    }

    @Test
    fun rowState_followsTheFullLoadOnly() {
        val failed = FontLoadState.Failed(FontFailure.Timeout)
        assertEquals(FontRowState.Available, emptyMap<FontLoadKey, FontLoadState>().rowState("lato"))
        assertEquals(
            FontRowState.Downloading,
            mapOf(key("lato", FontCoverage.Full) to FontLoadState.Loading).rowState("lato")
        )
        assertEquals(
            FontRowState.Downloading,
            mapOf(
                key("lato", FontCoverage.Sample) to failed,
                key("lato", FontCoverage.Full) to FontLoadState.Loading
            ).rowState("lato")
        )
        assertEquals(FontRowState.Failed, mapOf(key("lato", FontCoverage.Full) to failed).rowState("lato"))
        assertEquals(
            FontRowState.Downloaded,
            mapOf(key("lato", FontCoverage.Full) to FontLoadState.Ready).rowState("lato")
        )
    }

    @Test
    fun rowState_ignoresThePassiveSamplePrefetch() {
        listOf(
            FontLoadState.Loading,
            FontLoadState.Failed(FontFailure.Timeout),
            FontLoadState.Ready
        ).forEach { sample ->
            // A sample in flight is no download, a failed one is no error, and the Regular weight alone is no
            // "downloaded" family: a tap still has weights to fetch.
            assertEquals(
                "sample=$sample",
                FontRowState.Available,
                mapOf(key("lato", FontCoverage.Sample) to sample).rowState("lato")
            )
        }
    }

    @Test
    fun advice_separatesConnectionProblemsFromProviderAndCatalogProblems() {
        assertEquals(FontFailureAdvice.ProviderUnavailable, FontFailure.ProviderNotFound.advice())
        assertEquals(FontFailureAdvice.ProviderUnavailable, FontFailure.WrongCertificates.advice())
        assertEquals(FontFailureAdvice.ProviderUnavailable, FontFailure.SecurityViolation.advice())
        assertEquals(FontFailureAdvice.FontMissing, FontFailure.FontNotFound.advice())
        assertEquals(FontFailureAdvice.FontMissing, FontFailure.MalformedQuery.advice())
        listOf(FontFailure.Timeout, FontFailure.FontLoadError, FontFailure.FontUnavailable, FontFailure.Unknown)
            .forEach { assertEquals("$it", FontFailureAdvice.CheckConnection, it.advice()) }
    }

    @Test
    fun fontAvailability_explainsWhyASavedDownloadableFontIsNotInUse() {
        val failed = FontLoadState.Failed(FontFailure.Timeout)
        assertEquals(
            FontAvailability.Available,
            fontAvailability(FontResolutionReason.Exact, FontLoadState.Idle)
        )
        assertEquals(
            FontAvailability.Available,
            fontAvailability(FontResolutionReason.UnknownId, FontLoadState.Idle)
        )
        assertEquals(
            FontAvailability.NeedsPlayServices,
            fontAvailability(FontResolutionReason.GmsUnavailable, failed)
        )
        // A cached family that is still being loaded on a cold start is loading, not "not downloaded".
        assertEquals(FontAvailability.Loading, fontAvailability(FontResolutionReason.NotReady, FontLoadState.Idle))
        assertEquals(FontAvailability.Loading, fontAvailability(FontResolutionReason.NotReady, FontLoadState.Loading))
        assertEquals(FontAvailability.NotDownloaded, fontAvailability(FontResolutionReason.NotReady, failed))
    }

    @Test
    fun classify_mapsProviderReasonCodesFromTheCauseChain() {
        val expected = mapOf(
            -1 to FontFailure.ProviderNotFound,
            -2 to FontFailure.WrongCertificates,
            -3 to FontFailure.FontLoadError,
            -4 to FontFailure.SecurityViolation,
            1 to FontFailure.FontNotFound,
            2 to FontFailure.FontUnavailable,
            3 to FontFailure.MalformedQuery
        )
        expected.forEach { (code, failure) ->
            // Shape of the real error: preload wraps the loader's "Failed to load ... (reason=N, ...)".
            val error = IllegalStateException(
                "Unable to load font Font(GoogleFont(\"Lato\"))",
                IllegalStateException("Failed to load Font(GoogleFont(\"Lato\")) (reason=$code, details)")
            )
            assertEquals("reason=$code", failure, classifyFontFailure(error))
        }
    }

    @Test
    fun classify_timeoutAnywhereInTheChain_isTimeout() {
        val timeout = runBlocking {
            try {
                withTimeout(1) { delay(10_000) }
                error("expected a timeout")
            } catch (e: TimeoutCancellationException) {
                e
            }
        }
        assertEquals(FontFailure.Timeout, classifyFontFailure(timeout))
        assertEquals(FontFailure.Timeout, classifyFontFailure(IllegalStateException("Unable to load font", timeout)))
    }

    @Test
    fun classify_unrecognisedErrors_areUnknown() {
        assertEquals(FontFailure.Unknown, classifyFontFailure(IllegalStateException("boom")))
        assertEquals(FontFailure.Unknown, classifyFontFailure(IllegalStateException("reason=99")))
        assertEquals(FontFailure.Unknown, classifyFontFailure(RuntimeException()))
    }
}
