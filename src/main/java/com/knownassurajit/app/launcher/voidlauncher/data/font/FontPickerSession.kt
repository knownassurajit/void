package com.knownassurajit.app.launcher.voidlauncher.data.font

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The rules of one open font picker, kept out of the composables so they can be tested. Pure Kotlin: loading,
 * saving, and showing a failure are injected.
 *
 * - A family is committed only after its full load returned [FontLoadState.Ready]; a failure never commits, so a
 *   download that did not work cannot overwrite the saved font.
 * - The last selection wins: choosing another family cancels the wait of the previous one. Its download keeps
 *   running in the coordinator and is simply cached.
 * - Row previews are requested at most once per family per session, so scrolling up and down, or an offline device,
 *   cannot start the same request again and again. A new session (the picker reopened) tries again.
 */
class FontPickerSession(
    private val scope: CoroutineScope,
    private val ensureLoaded: (FontSpec, FontCoverage) -> Deferred<FontLoadState>,
    private val commit: (String) -> Unit,
    private val onFailed: (FontSpec, FontFailure) -> Unit
) {
    private val lock = Any()
    private var selection: Job? = null
    private val sampled = HashSet<String>()

    /** Applies [spec] once it is usable. Bundled and system families are usable at once. */
    fun select(spec: FontSpec): Job = synchronized(lock) {
        selection?.cancel()
        scope.launch {
            when (val state = ensureLoaded(spec, FontCoverage.Full).await()) {
                FontLoadState.Ready -> commit(spec.id)
                is FontLoadState.Failed -> onFailed(spec, state.reason)
                // A finished load is always Ready or Failed; anything else is treated as a failure, never a commit.
                else -> onFailed(spec, FontFailure.Unknown)
            }
        }.also { selection = it }
    }

    /** Starts the passive preview download for a row. Large downloads are only ever started by choosing them. */
    fun prefetchSample(spec: FontSpec) {
        if (!spec.needsDownload || spec.isLargeDownload) return
        val firstRequest = synchronized(lock) { sampled.add(spec.id) }
        if (firstRequest) ensureLoaded(spec, FontCoverage.Sample)
    }
}
