package com.knownassurajit.app.launcher.voidlauncher.helper

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontLoadCoordinator
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontLoadEvent
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontLoadState
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontLoadStates
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontResolution
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontResolutionReason
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.FontFamilies
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow

/**
 * Downloads Google Fonts families through Google Play services and tells the UI when each one is safe to use.
 *
 * Only the main process may touch this (the theme and Settings do; the accessibility process never does). Creating
 * it is cheap: the font resolver is built, and Google Play services contacted, only when a downloadable family is
 * requested. VOID itself makes no network request: the font provider in Google Play services performs the
 * download and keeps its own cache.
 */
class FontRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val reportedFallbacks = ConcurrentHashMap.newKeySet<String>()

    // Created on first use: it snapshots the system font-weight adjustment of the process.
    private val resolver: FontFamily.Resolver by lazy { createFontFamilyResolver(appContext) }

    private val coordinator = FontLoadCoordinator(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        loader = { spec, coverage -> preload(spec, coverage) },
        onEvent = { event -> logLoad(event) }
    )

    val states: StateFlow<FontLoadStates> get() = coordinator.states

    /** Single-flight load of [spec]; the result is [FontLoadState.Ready] only once Compose cached every typeface. */
    fun ensureLoaded(spec: FontSpec, coverage: FontCoverage): Deferred<FontLoadState> =
        coordinator.ensureLoaded(spec, coverage)

    /** Logs, once per process and reason, that the saved font could not be used as is. */
    fun reportFallback(requestedId: String?, resolution: FontResolution) {
        if (resolution.reason == FontResolutionReason.Exact) return
        val requested = FontCatalog.loggableId(requestedId)
        if (reportedFallbacks.add("$requested/${resolution.reason}")) {
            Log.i(TAG, "font_fallback requested=$requested resolved=${resolution.spec.id} reason=${resolution.reason}")
        }
    }

    // FontFamily.Resolver.preload throws on any failure and caches nothing for it, so a failed attempt stays retryable.
    private suspend fun preload(spec: FontSpec, coverage: FontCoverage) {
        resolver.preload(FontFamilies.familyFor(spec, coverage))
    }

    private fun logLoad(event: FontLoadEvent) {
        val failure = event.failure
        if (failure == null) {
            Log.i(
                TAG,
                "font_load_ok id=${event.key.id} coverage=${event.key.coverage} " +
                    "weights=${event.weightCount} ms=${event.elapsedMs}"
            )
        } else {
            Log.w(
                TAG,
                "font_load_failed id=${event.key.id} coverage=${event.key.coverage} " +
                    "reason=$failure ms=${event.elapsedMs}"
            )
        }
    }

    companion object {
        private const val TAG = "FontRepository"

        @Volatile
        private var instance: FontRepository? = null

        fun get(context: Context): FontRepository {
            instance?.let { return it }
            return synchronized(this) {
                instance ?: FontRepository(context).also { instance = it }
            }
        }
    }
}
