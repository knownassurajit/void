package com.knownassurajit.app.launcher.voidlauncher.data.font

import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FontPickerSessionTest {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val committed = CopyOnWriteArrayList<String>()
    private val failures = CopyOnWriteArrayList<Pair<String, FontFailure>>()
    private val requests = CopyOnWriteArrayList<Pair<String, FontCoverage>>()
    private val loads = HashMap<String, CompletableDeferred<FontLoadState>>()

    private val poppins = font("poppins")
    private val lato = font("lato")
    private val dongle = font("dongle")

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun font(id: String): FontSpec = requireNotNull(FontCatalog.find(id))

    // Families without a pending load are ready at once, like bundled and system fonts in the real coordinator.
    private fun pending(spec: FontSpec): CompletableDeferred<FontLoadState> =
        loads.getOrPut(spec.id) { CompletableDeferred() }

    private fun session(): FontPickerSession = FontPickerSession(
        scope = scope,
        ensureLoaded = { spec, coverage ->
            requests += spec.id to coverage
            loads[spec.id] ?: CompletableDeferred(FontLoadState.Ready)
        },
        commit = { committed += it },
        onFailed = { spec, reason -> failures += spec.id to reason }
    )

    private fun <T> blocking(block: suspend () -> T): T = runBlocking { withTimeout(10_000) { block() } }

    @Test
    fun bundledAndSystemFonts_commitWithoutWaiting() = blocking {
        val session = session()

        session.select(font("google_sans")).join()
        session.select(font("system")).join()

        assertEquals(listOf("google_sans", "system"), committed.toList())
        assertTrue(failures.isEmpty())
    }

    @Test
    fun downloadableFont_commitsOnlyOnceItsFullLoadIsReady() = blocking {
        val load = pending(poppins)
        val selection = session().select(poppins)

        delay(100)
        assertTrue("nothing may be saved while the download runs", committed.isEmpty())
        assertEquals(listOf(poppins.id to FontCoverage.Full), requests.toList())

        load.complete(FontLoadState.Ready)
        selection.join()
        assertEquals(listOf("poppins"), committed.toList())
        assertTrue(failures.isEmpty())
    }

    @Test
    fun failedDownload_neverCommits_andReportsTheReason() = blocking {
        pending(poppins).complete(FontLoadState.Failed(FontFailure.Timeout))

        session().select(poppins).join()

        assertTrue("a failed download must leave the saved font alone", committed.isEmpty())
        assertEquals(listOf(poppins.id to FontFailure.Timeout), failures.toList())
    }

    @Test
    fun aLoadThatEndedWithoutAResult_isAFailure_notACommit() = blocking {
        pending(poppins).complete(FontLoadState.Idle)

        session().select(poppins).join()

        assertTrue(committed.isEmpty())
        assertEquals(listOf(poppins.id to FontFailure.Unknown), failures.toList())
    }

    @Test
    fun lastSelectionWins_andACancelledOneCanNeitherCommitNorReport() = blocking {
        val slow = pending(poppins)
        val session = session()

        val first = session.select(poppins)
        val second = session.select(lato)
        second.join()
        assertEquals(listOf("lato"), committed.toList())

        slow.complete(FontLoadState.Ready)
        first.join()
        assertTrue(first.isCancelled)
        assertEquals("the earlier download must not overwrite the later choice", listOf("lato"), committed.toList())

        val failing = pending(dongle)
        val third = session.select(dongle)
        val fourth = session.select(lato)
        fourth.join()
        failing.complete(FontLoadState.Failed(FontFailure.Timeout))
        third.join()
        assertTrue("a superseded selection is not an error", failures.isEmpty())
    }

    @Test
    fun samplePrefetch_isRequestedOncePerFamilyAndSession() {
        val session = session()

        session.prefetchSample(poppins)
        session.prefetchSample(poppins)
        session.prefetchSample(lato)

        assertEquals(
            listOf(poppins.id to FontCoverage.Sample, lato.id to FontCoverage.Sample),
            requests.toList()
        )

        // The picker reopened: a new session may try again.
        session().prefetchSample(poppins)
        assertEquals(3, requests.size)
    }

    @Test
    fun samplePrefetch_skipsLargeDownloadsAndFontsThatNeedNoDownload() {
        val session = session()

        session.prefetchSample(dongle)
        session.prefetchSample(font("google_sans"))
        session.prefetchSample(font("system"))

        assertTrue(dongle.isLargeDownload)
        assertTrue(requests.isEmpty())
    }
}
