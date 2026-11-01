package com.knownassurajit.app.launcher.voidlauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.Gravity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.knownassurajit.app.launcher.voidlauncher.data.HomeAppsCap
import com.knownassurajit.app.launcher.voidlauncher.data.HomescreenPreferences
import com.knownassurajit.app.launcher.voidlauncher.helper.ScreenTimeMath
import com.knownassurajit.app.launcher.voidlauncher.helper.ScreenTimeResult
import com.knownassurajit.app.launcher.voidlauncher.helper.ScreenTimeSource
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeStyle
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeTextColor
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MainUiState(
    val currentTime: String = "",
    val currentDate: String = "",
    val batteryLevel: Int = 100,
    val screenTime: String = "",
    val homeApps: List<HomeApp> = emptyList(),
    val clockHorizontalAlignment: Int = Gravity.START,
    val clockVerticalAlignment: Int = Gravity.BOTTOM,
    val appHorizontalAlignment: Int = Gravity.START,
    val appVerticalAlignment: Int = Gravity.BOTTOM,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showScreenTime: Boolean = true,
    val showHomeApps: Boolean = true,
    val showStatusBar: Boolean = false,
    val homeAppsCount: Int = HomeAppsCap.DEFAULT,
    val leftSwipeAction: String = SwipeAction.WIDGETS,
    val rightSwipeAction: String = SwipeAction.NOTIFICATION_SUMMARY,
    val clockSectionWeight: Float = 0.35f,
    val clockSizeScale: Float = 1.0f,
    val dateSizeScale: Float = 1.0f,
    val screenTimeSizeScale: Float = 1.0f,
    val homeSectionOrder: String = "clock_first",
    val homeTextSizeScale: Float = 1.0f,
    val appDrawerTextSizeScale: Float = 1.0f,
    val appSpacingDp: Float = 16f,
    val enableGestures: Boolean = true,
    val enableSwipeDownNotifications: Boolean = true,
    val appFont: String = "google_sans",
    val use24HourClock: Boolean = false,
    val showSeconds: Boolean = false,
    val animationSpeed: String = Prefs.AnimationSpeed.STANDARD,
    val contentSwipeToBack: Boolean = true,
    val privateSpacePlacement: String = Prefs.PrivateSpacePlacement.BOTTOM,
    val clockOpacity: Float = HomeStyle.MAX_OPACITY,
    val homeAppsOpacity: Float = HomeStyle.MAX_OPACITY,
    val clockColor: String = HomeTextColor.Default.key,
    val homeAppsColor: String = HomeTextColor.Default.key,
    val clockFontWeight: Int = HomeStyle.DEFAULT_CLOCK_WEIGHT,
    val homeAppsFontWeight: Int = HomeStyle.DEFAULT_APP_WEIGHT,
    val homeSidePaddingDp: Float = HomeStyle.DEFAULT_SIDE_PADDING_DP,
    val showAppScreenTime: Boolean = true
)

/** Copies every Home setting from [hs]; the one mapping shared by the change listener and [MainUiViewModel.refreshFromPrefs]. */
internal fun MainUiState.withHomescreenPreferences(hs: HomescreenPreferences): MainUiState = copy(
    clockHorizontalAlignment = hs.clockHorizontalAlignment,
    clockVerticalAlignment = hs.clockVerticalAlignment,
    appHorizontalAlignment = hs.appHorizontalAlignment,
    appVerticalAlignment = hs.appVerticalAlignment,
    showClock = hs.showClock,
    showDate = hs.showDate,
    showScreenTime = hs.showScreenTime,
    showHomeApps = hs.showHomeApps,
    homeAppsCount = hs.maxApps,
    showStatusBar = hs.showStatusBar,
    leftSwipeAction = hs.leftSwipeAction,
    rightSwipeAction = hs.rightSwipeAction,
    clockSectionWeight = hs.clockSectionWeight,
    clockSizeScale = hs.clockSizeScale,
    dateSizeScale = hs.dateSizeScale,
    screenTimeSizeScale = hs.screenTimeSizeScale,
    homeSectionOrder = hs.homeSectionOrder,
    homeTextSizeScale = hs.homeTextSizeScale,
    appDrawerTextSizeScale = hs.appDrawerTextSizeScale,
    appSpacingDp = hs.appSpacingDp,
    enableGestures = hs.enableGestures,
    enableSwipeDownNotifications = hs.enableSwipeDownNotifications,
    appFont = hs.appFont,
    use24HourClock = hs.use24HourClock,
    showSeconds = hs.showSeconds,
    animationSpeed = hs.animationSpeed,
    contentSwipeToBack = hs.contentSwipeToBack,
    privateSpacePlacement = hs.privateSpacePlacement,
    clockOpacity = hs.clockOpacity,
    homeAppsOpacity = hs.homeAppsOpacity,
    clockColor = hs.clockColor,
    homeAppsColor = hs.homeAppsColor,
    clockFontWeight = hs.clockFontWeight,
    homeAppsFontWeight = hs.homeAppsFontWeight,
    homeSidePaddingDp = hs.homeSidePaddingDp,
    showAppScreenTime = hs.showAppScreenTime
)

data class HomeApp(
    val position: Int,
    val label: String,
    val packageName: String,
    val activityClassName: String?,
    val userString: String,
    val isShortcut: Boolean = false,
    val shortcutId: String = ""
)

/**
 * UI-state ViewModel. Exposes a single immutable [MainUiState] StateFlow that the
 * Compose layer collects with lifecycle awareness. Owns the time ticker, battery
 * receiver, and prefs-driven UI state propagation.
 *
 * App-launching and persistence side effects live in [MainViewModel].
 */
class MainUiViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefs = Prefs.get(appContext)

    private val _uiState = MutableStateFlow(
        MainUiState(
            // Seed with persisted value so first composition uses the correct visibility whenever possible.
            showStatusBar = prefs.showStatusBar
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val screenTimeSource = ScreenTimeSource(appContext)

    private val timeFormat12 = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val timeFormat12Seconds = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
    private val timeFormat24Seconds = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) ?: 100
            _uiState.update { it.copy(batteryLevel = level) }
        }
    }

    init {
        // Start time ticker
        viewModelScope.launch {
            while (isActive) {
                val now = Date()
                val currentState = _uiState.value
                val formatter = when {
                    currentState.use24HourClock && currentState.showSeconds -> timeFormat24Seconds
                    currentState.use24HourClock -> timeFormat24
                    currentState.showSeconds -> timeFormat12Seconds
                    else -> timeFormat12
                }
                val timeStr = if (currentState.use24HourClock) {
                    formatter.format(now)
                } else {
                    formatter.format(now).uppercase(Locale.getDefault())
                }
                _uiState.update {
                    it.copy(
                        currentTime = timeStr,
                        currentDate = dateFormat.format(now).uppercase(Locale.getDefault())
                    )
                }
                // Update continuously every second so settings take effect instantly.
                delay(1000L)
            }
        }

        // Register battery receiver
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                appContext,
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}

        // Load prefs
        refreshFromPrefs()

        // Load screen time and refresh periodically
        loadScreenTime()
        viewModelScope.launch {
            while (isActive) {
                delay(60_000L)
                loadScreenTime()
            }
        }

        // Observe pref changes — fully reactive propagation
        viewModelScope.launch {
            var lastMaxApps = -1
            var lastShowScreenTime = prefs.showScreenTimeWidget
            prefs.homescreenPreferences.collect { hsPrefs ->
                // Switching the line on must not wait for the next minute tick.
                if (hsPrefs.showScreenTime && !lastShowScreenTime) loadScreenTime()
                lastShowScreenTime = hsPrefs.showScreenTime
                val reloadApps = HomeAppsCap.shouldReloadHomeApps(lastMaxApps, hsPrefs.maxApps)
                lastMaxApps = hsPrefs.maxApps
                val apps = if (reloadApps) loadHomeApps(hsPrefs.maxApps) else _uiState.value.homeApps
                _uiState.update { it.withHomescreenPreferences(hsPrefs).copy(homeApps = apps) }
                // Re-format time immediately when format changes
                val now = Date()
                val formatter = when {
                    hsPrefs.use24HourClock && hsPrefs.showSeconds -> timeFormat24Seconds
                    hsPrefs.use24HourClock -> timeFormat24
                    hsPrefs.showSeconds -> timeFormat12Seconds
                    else -> timeFormat12
                }
                val timeStr = if (hsPrefs.use24HourClock) {
                    formatter.format(now)
                } else {
                    formatter.format(now).uppercase(Locale.getDefault())
                }
                _uiState.update { it.copy(currentTime = timeStr) }
            }
        }
    }

    private fun loadHomeApps(count: Int): List<HomeApp> {
        val apps = mutableListOf<HomeApp>()
        val limit = count.coerceIn(0, 10)
        for (i in 1..limit) {
            val name = prefs.getAppName(i)
            if (name.isNotBlank()) {
                apps.add(
                    HomeApp(
                        position = i,
                        label = name,
                        packageName = prefs.getAppPackage(i),
                        activityClassName = prefs.getAppActivityClassName(i),
                        userString = prefs.getAppUser(i),
                        isShortcut = prefs.getIsShortcut(i),
                        shortcutId = prefs.getShortcutId(i)
                    )
                )
            }
        }
        return apps
    }

    fun refreshFromPrefs() {
        val hsPrefs = prefs.currentHomescreenPreferences()
        val apps = loadHomeApps(hsPrefs.maxApps.coerceIn(0, 10))
        _uiState.update { it.withHomescreenPreferences(hsPrefs).copy(homeApps = apps) }
    }

    /**
     * Reads today's total off the main thread. Nothing is queried while the feature is off, and the
     * total is the sum of the per-app figures the app library shows (see [ScreenTimeSource]).
     */
    private fun loadScreenTime() {
        if (!prefs.showScreenTimeWidget) return
        viewModelScope.launch(Dispatchers.IO) {
            val value = when (val result = screenTimeSource.loadToday()) {
                ScreenTimeResult.PermissionRequired -> appContext.getString(R.string.screen_time_permission_required)
                ScreenTimeResult.Unavailable -> appContext.getString(R.string.screen_time_data_unavailable)
                is ScreenTimeResult.Usage ->
                    if (result.totalMillis > 0L) ScreenTimeMath.hoursMinutes(result.totalMillis) else appContext.getString(R.string.screen_time_none)
            }
            val text = appContext.getString(R.string.screen_time_line, value)
            _uiState.update { it.copy(screenTime = text) }
        }
    }

    override fun onCleared() {
        try {
            appContext.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        super.onCleared()
    }
}
