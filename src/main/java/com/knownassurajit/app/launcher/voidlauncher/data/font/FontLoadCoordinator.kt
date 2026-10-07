package com.knownassurajit.app.launcher.voidlauncher.data.font

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout

/** Outcome of one finished load, for the Android layer to log. [failure] is null on success. */
data class FontLoadEvent(
    val key: FontLoadKey,
    val weightCount: Int,
    val elapsedMs: Long,
    val failure: FontFailure?
)

/**
 * Tracks and de-duplicates downloadable-font loads. Pure Kotlin: the platform work is the injected [loader].
 *
 * Invariants the theme relies on:
 * - A family is only treated as usable once its load returned normally ([FontLoadState.Ready]); Compose caches a
 *   failure that happens during composition for the whole process, so nothing may enter composition before that.
 * - Loads run in [scope] (the application's), never in the caller's: a row scrolling away or a screen closing only
 *   stops *waiting*, it cannot cancel a download or turn into a [FontLoadState.Failed].
 * - Concurrent requests for the same family share one load (single flight).
 * - Passive [FontCoverage.Sample] loads run one at a time and a full load never waits for them. Every request ends
 *   up on one single-threaded, uncancellable queue inside androidx.core, and the timeouts in both Compose and this
 *   class start when a request is submitted, so a burst of speculative requests would make the load the user asked
 *   for time out while it is still queued.
 */
class FontLoadCoordinator(
    private val scope: CoroutineScope,
    private val loader: suspend (FontSpec, FontCoverage) -> Unit,
    private val timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    private val onEvent: (FontLoadEvent) -> Unit = {},
    private val nanoTime: () -> Long = System::nanoTime
) {
    private val mutableStates = MutableStateFlow<FontLoadStates>(emptyMap())
    val states: StateFlow<FontLoadStates> = mutableStates.asStateFlow()

    private val lock = Any()
    private val inFlight = HashMap<FontLoadKey, Deferred<FontLoadState>>()
    private val sampleGate = Semaphore(SAMPLE_PARALLELISM)

    /**
     * Starts (or joins) the load of [spec] and returns its result. Bundled and system families are always
     * [FontLoadState.Ready]. A [FontLoadState.Failed] family is retried on the next call.
     */
    fun ensureLoaded(spec: FontSpec, coverage: FontCoverage): Deferred<FontLoadState> {
        if (!spec.needsDownload) return CompletableDeferred(FontLoadState.Ready)
        val key = FontLoadKey(spec.id, coverage)
        synchronized(lock) {
            if (mutableStates.value.isReady(spec.id, coverage)) return CompletableDeferred(FontLoadState.Ready)
            inFlight[key]?.let { return it }
            // A running full load already fetches the Regular weight a sample needs.
            if (coverage == FontCoverage.Sample) {
                inFlight[FontLoadKey(spec.id, FontCoverage.Full)]?.let { return it }
            }
            mutableStates.update { it + (key to FontLoadState.Loading) }
            val load = scope.async(start = CoroutineStart.LAZY) { load(spec, key) }
            inFlight[key] = load
            load.invokeOnCompletion {
                synchronized(lock) { if (inFlight[key] === load) inFlight.remove(key) }
            }
            load.start()
            return load
        }
    }

    private suspend fun load(spec: FontSpec, key: FontLoadKey): FontLoadState {
        var startedAt = nanoTime()
        val outcome: FontLoadState = try {
            // The timeout and the logged duration start once the load holds its turn, not while it waits for one.
            gated(key.coverage) {
                startedAt = nanoTime()
                withTimeout(timeoutMs) { loader(spec, key.coverage) }
            }
            FontLoadState.Ready
        } catch (_: TimeoutCancellationException) {
            FontLoadState.Failed(FontFailure.Timeout)
        } catch (cancelled: CancellationException) {
            // Cancellation is not a failed download: leave no state behind and let it propagate.
            forget(key)
            throw cancelled
        } catch (error: Exception) {
            if (!currentCoroutineContext().isActive) {
                // The resolver can wrap a cancellation in another exception type; still not a failure.
                forget(key)
                throw CancellationException("Font load cancelled").apply { initCause(error) }
            }
            FontLoadState.Failed(classifyFontFailure(error))
        }
        mutableStates.update { it + (key to outcome) }
        onEvent(
            FontLoadEvent(
                key = key,
                weightCount = if (key.coverage == FontCoverage.Full) spec.weights.size else 1,
                elapsedMs = TimeUnit.NANOSECONDS.toMillis(nanoTime() - startedAt),
                failure = (outcome as? FontLoadState.Failed)?.reason
            )
        )
        return outcome
    }

    private suspend fun gated(coverage: FontCoverage, block: suspend () -> Unit) {
        if (coverage == FontCoverage.Sample) sampleGate.withPermit { block() } else block()
    }

    private fun forget(key: FontLoadKey) {
        mutableStates.update { it - key }
    }

    companion object {
        /** Matches Compose's own per-font timeout for downloadable fonts. */
        const val DEFAULT_TIMEOUT_MS = 15_000L

        /** Passive sample requests allowed in the provider queue at once; a full load can sit behind at most this many. */
        const val SAMPLE_PARALLELISM = 1
    }
}
