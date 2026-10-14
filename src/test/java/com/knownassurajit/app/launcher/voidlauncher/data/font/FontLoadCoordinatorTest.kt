package com.knownassurajit.app.launcher.voidlauncher.data.font

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FontLoadCoordinatorTest {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val poppins = requireNotNull(FontCatalog.find("poppins"))

    // Small downloadable families, none of them poppins and none a large download.
    private val others: List<FontSpec> = listOf("lato", "karla", "krub", "hind", "jost").map {
        requireNotNull(FontCatalog.find(it))
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun coordinator(
        timeoutMs: Long = 5_000L,
        events: MutableList<FontLoadEvent> = CopyOnWriteArrayList(),
        loader: suspend (FontSpec, FontCoverage) -> Unit
    ) = FontLoadCoordinator(scope, loader, timeoutMs, onEvent = { events += it })

    private fun <T> blocking(block: suspend () -> T): T = runBlocking { withTimeout(10_000) { block() } }

    @Test
    fun bundledAndSystemFonts_areReadyWithoutLoading() = blocking {
        val calls = AtomicInteger()
        val coordinator = coordinator { _, _ -> calls.incrementAndGet() }

        listOf("google_sans", "geist", "system").forEach { id ->
            val spec = requireNotNull(FontCatalog.find(id))
            assertEquals(FontLoadState.Ready, coordinator.ensureLoaded(spec, FontCoverage.Full).await())
        }

        assertEquals(0, calls.get())
        assertTrue(coordinator.states.value.isEmpty())
    }

    @Test
    fun successfulLoad_movesLoadingToReady_andIsLoggedOnce() = blocking {
        val release = CompletableDeferred<Unit>()
        val events = CopyOnWriteArrayList<FontLoadEvent>()
        val coordinator = coordinator(events = events) { _, _ -> release.await() }

        val load = coordinator.ensureLoaded(poppins, FontCoverage.Full)
        assertEquals(FontLoadState.Loading, coordinator.states.value.stateOf("poppins", FontCoverage.Full))

        release.complete(Unit)
        assertEquals(FontLoadState.Ready, load.await())
        assertEquals(FontLoadState.Ready, coordinator.states.value.stateOf("poppins", FontCoverage.Full))
        assertEquals(1, events.size)
        assertNull(events.single().failure)
        assertEquals(poppins.weights.size, events.single().weightCount)
    }

    @Test
    fun concurrentRequests_shareOneLoad() = blocking {
        val calls = AtomicInteger()
        val release = CompletableDeferred<Unit>()
        val coordinator = coordinator { _, _ ->
            calls.incrementAndGet()
            release.await()
        }

        val first = coordinator.ensureLoaded(poppins, FontCoverage.Full)
        val second = coordinator.ensureLoaded(poppins, FontCoverage.Full)
        assertSame(first, second)

        release.complete(Unit)
        assertEquals(FontLoadState.Ready, first.await())
        assertEquals(1, calls.get())
    }

    @Test
    fun readyFont_isNotLoadedAgain() = blocking {
        val calls = AtomicInteger()
        val coordinator = coordinator { _, _ -> calls.incrementAndGet() }

        coordinator.ensureLoaded(poppins, FontCoverage.Full).await()
        assertEquals(FontLoadState.Ready, coordinator.ensureLoaded(poppins, FontCoverage.Full).await())
        assertEquals(FontLoadState.Ready, coordinator.ensureLoaded(poppins, FontCoverage.Sample).await())
        assertEquals(1, calls.get())
    }

    @Test
    fun sampleRequest_joinsARunningFullLoad() = blocking {
        val calls = AtomicInteger()
        val release = CompletableDeferred<Unit>()
        val coordinator = coordinator { _, _ ->
            calls.incrementAndGet()
            release.await()
        }

        val full = coordinator.ensureLoaded(poppins, FontCoverage.Full)
        val sample = coordinator.ensureLoaded(poppins, FontCoverage.Sample)
        assertSame(full, sample)

        release.complete(Unit)
        sample.await()
        assertEquals(1, calls.get())
    }

    @Test
    fun sampleLoad_doesNotMakeTheFullFamilyReady() = blocking {
        val coverages = CopyOnWriteArrayList<FontCoverage>()
        val coordinator = coordinator { _, coverage -> coverages += coverage }

        coordinator.ensureLoaded(poppins, FontCoverage.Sample).await()
        assertTrue(coordinator.states.value.isReady("poppins", FontCoverage.Sample))
        assertEquals(FontLoadState.Idle, coordinator.states.value.stateOf("poppins", FontCoverage.Full))

        coordinator.ensureLoaded(poppins, FontCoverage.Full).await()
        assertEquals(listOf(FontCoverage.Sample, FontCoverage.Full), coverages.toList())
    }

    @Test
    fun failure_isClassified_logged_andRetriedOnNextRequest() = blocking {
        val attempts = AtomicInteger()
        val events = CopyOnWriteArrayList<FontLoadEvent>()
        val coordinator = coordinator(events = events) { _, _ ->
            if (attempts.incrementAndGet() == 1) {
                throw IllegalStateException("Failed to load font (reason=1, not found)")
            }
        }

        val failed = coordinator.ensureLoaded(poppins, FontCoverage.Full).await()
        assertEquals(FontLoadState.Failed(FontFailure.FontNotFound), failed)
        assertEquals(failed, coordinator.states.value.stateOf("poppins", FontCoverage.Full))
        assertEquals(FontFailure.FontNotFound, events.single().failure)

        val retried = coordinator.ensureLoaded(poppins, FontCoverage.Full).await()
        assertEquals(FontLoadState.Ready, retried)
        assertEquals(2, attempts.get())
    }

    @Test
    fun slowLoad_failsWithTimeout() = blocking {
        val coordinator = coordinator(timeoutMs = 50) { _, _ -> awaitCancellation() }

        assertEquals(
            FontLoadState.Failed(FontFailure.Timeout),
            coordinator.ensureLoaded(poppins, FontCoverage.Full).await()
        )
    }

    @Test
    fun cancelledLoad_isNotRecordedAsFailed() = blocking {
        val started = CompletableDeferred<Unit>()
        val events = CopyOnWriteArrayList<FontLoadEvent>()
        val ownScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = FontLoadCoordinator(
            scope = ownScope,
            loader = { _, _ ->
                started.complete(Unit)
                awaitCancellation()
            },
            onEvent = { events += it }
        )

        val load = coordinator.ensureLoaded(poppins, FontCoverage.Full)
        started.await()
        ownScope.cancel()
        load.join()

        assertTrue(load.isCancelled)
        assertEquals(FontLoadState.Idle, coordinator.states.value.stateOf("poppins", FontCoverage.Full))
        assertTrue("a cancelled load must not be logged as a failure", events.isEmpty())
    }

    @Test
    fun cancellationWrappedByTheResolver_isNotRecordedAsFailed() = blocking {
        val started = CompletableDeferred<Unit>()
        val events = CopyOnWriteArrayList<FontLoadEvent>()
        val ownScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = FontLoadCoordinator(
            scope = ownScope,
            loader = { _, _ ->
                started.complete(Unit)
                try {
                    awaitCancellation()
                } catch (cancelled: CancellationException) {
                    // Compose's preload rethrows whatever it catches as an IllegalStateException.
                    throw IllegalStateException("Unable to load font", cancelled)
                }
            },
            onEvent = { events += it }
        )

        val load = coordinator.ensureLoaded(poppins, FontCoverage.Full)
        started.await()
        ownScope.cancel()
        load.join()

        assertTrue(load.isCancelled)
        assertEquals(FontLoadState.Idle, coordinator.states.value.stateOf("poppins", FontCoverage.Full))
        assertTrue("a wrapped cancellation must not be logged as a failure", events.isEmpty())
    }

    @Test
    fun sampleLoads_runOneAtATime() = blocking {
        val running = AtomicInteger()
        val peak = AtomicInteger()
        val release = CompletableDeferred<Unit>()
        val coordinator = coordinator { _, _ ->
            peak.accumulateAndGet(running.incrementAndGet()) { a, b -> maxOf(a, b) }
            try {
                release.await()
            } finally {
                running.decrementAndGet()
            }
        }

        val loads = others.map { coordinator.ensureLoaded(it, FontCoverage.Sample) }
        delay(200)
        assertEquals("only one sample may be in the provider queue", 1, running.get())

        release.complete(Unit)
        loads.forEach { assertEquals(FontLoadState.Ready, it.await()) }
        assertEquals(FontLoadCoordinator.SAMPLE_PARALLELISM, peak.get())
    }

    @Test
    fun fullLoad_doesNotWaitBehindQueuedSamples() = blocking {
        val blockSamples = CompletableDeferred<Unit>()
        val coordinator = coordinator { _, coverage ->
            if (coverage == FontCoverage.Sample) blockSamples.await()
        }
        val samples = others.map { coordinator.ensureLoaded(it, FontCoverage.Sample) }

        // Would hit the helper's 10 s limit if it had to wait for the blocked samples.
        assertEquals(FontLoadState.Ready, coordinator.ensureLoaded(poppins, FontCoverage.Full).await())
        assertEquals(FontLoadState.Loading, coordinator.states.value.stateOf("lato", FontCoverage.Sample))

        blockSamples.complete(Unit)
        samples.forEach { assertEquals(FontLoadState.Ready, it.await()) }
    }

    @Test
    fun sampleTimeout_startsOnlyOnceTheLoadHoldsItsTurn() = blocking {
        val (first, second) = others
        val releases = mapOf(first.id to CompletableDeferred<Unit>(), second.id to CompletableDeferred<Unit>())
        val coordinator = coordinator(timeoutMs = 2_000) { spec, _ -> releases.getValue(spec.id).await() }

        val firstLoad = coordinator.ensureLoaded(first, FontCoverage.Sample)
        val secondLoad = coordinator.ensureLoaded(second, FontCoverage.Sample)
        delay(200)
        releases.getValue(first.id).complete(Unit)
        assertEquals(FontLoadState.Ready, firstLoad.await())

        // 400 ms after submission but only 200 ms after taking its turn.
        delay(200)
        releases.getValue(second.id).complete(Unit)
        assertEquals(FontLoadState.Ready, secondLoad.await())
    }

    @Test
    fun sampleFailure_isRecordedPerFamily_andDoesNotBlockTheQueue() = blocking {
        val (first, second) = others
        val coordinator = coordinator { spec, _ ->
            if (spec.id == first.id) throw IllegalStateException("Failed to load font (reason=-1)")
        }

        val failed = coordinator.ensureLoaded(first, FontCoverage.Sample)
        val next = coordinator.ensureLoaded(second, FontCoverage.Sample)

        assertEquals(FontLoadState.Failed(FontFailure.ProviderNotFound), failed.await())
        assertEquals(FontLoadState.Ready, next.await())
    }
}
