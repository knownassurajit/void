package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.knownassurajit.app.launcher.voidlauncher.helper.ScreenTimeResult
import com.knownassurajit.app.launcher.voidlauncher.helper.ScreenTimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Holds what the app library shows besides the app list: today's foreground time per app. The
 * system query runs off the main thread, so opening the drawer never waits for it.
 */
class AppDrawerViewModel(application: Application) : AndroidViewModel(application) {
    private val source = ScreenTimeSource(application.applicationContext)

    private val _usage = MutableStateFlow<Map<String, Long>>(emptyMap())

    /** Foreground milliseconds today per package. Empty until read, and whenever [refreshUsage] is told the figures are off or cannot be read. */
    val usage: StateFlow<Map<String, Long>> = _usage.asStateFlow()

    private var refreshJob: Job? = null
    private var lastRefreshAt = UsageRefreshPolicy.NEVER
    private var lastEnabled: Boolean? = null

    /**
     * Reads the usage again. [enabled] is whether the library shows usage at all (Screen time on, the
     * library switch on): when it is not, the figures are cleared and nothing is queried. Calls that
     * repeat the same request within a couple of seconds are ignored, because the drawer asks when it
     * opens and again when its lifecycle resumes, which is the same moment from the user's side.
     */
    fun refreshUsage(enabled: Boolean) {
        val now = SystemClock.elapsedRealtime()
        if (UsageRefreshPolicy.shouldSkip(enabled, lastEnabled, lastRefreshAt, now)) return
        lastEnabled = enabled
        lastRefreshAt = now
        refreshJob?.cancel()
        if (!enabled) {
            _usage.value = emptyMap()
            return
        }
        refreshJob = viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { source.loadToday() }
            _usage.value = (result as? ScreenTimeResult.Usage)?.millisByPackage.orEmpty()
        }
    }
}

/** When [AppDrawerViewModel.refreshUsage] may ignore a request. Pure, so the rule is JVM-testable. */
internal object UsageRefreshPolicy {
    const val MIN_REFRESH_INTERVAL_MS = 2_000L
    const val NEVER = Long.MIN_VALUE

    /**
     * A repeat of the previous request ([enabled] equals [lastEnabled]) that came less than
     * [MIN_REFRESH_INTERVAL_MS] after it. A change of the setting is never held back.
     */
    fun shouldSkip(enabled: Boolean, lastEnabled: Boolean?, lastRefreshAt: Long, now: Long): Boolean =
        lastEnabled == enabled && lastRefreshAt != NEVER && now - lastRefreshAt < MIN_REFRESH_INTERVAL_MS
}
