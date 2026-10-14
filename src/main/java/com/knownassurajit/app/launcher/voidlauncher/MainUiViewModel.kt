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
import android.app.usage.UsageStatsManager
import com.knownassurajit.app.launcher.voidlauncher.data.HomeAppsCap
import com.knownassurajit.app.launcher.voidlauncher.data.HomescreenPreferences
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
    val homeSidePaddingDp: Float = HomeStyle.DEFAULT_SIDE_PADDING_DP
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
    homeSidePaddingDp = hs.homeSidePaddingDp
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
            prefs.homescreenPreferences.collect { hsPrefs ->
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

    private fun loadScreenTime() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val appOps = appContext.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), appContext.packageName)
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), appContext.packageName)
                }
                if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
                    _uiState.update { it.copy(screenTime = "Screen time: Permission required") }
                    return@launch
                }

                val usageStatsManager = getApplication<android.app.Application>().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    calendar.timeInMillis,
                    System.currentTimeMillis()
                )
                
                val totalMillis = stats
                    .filter { it.packageName != appContext.packageName }
                    .sumOf { it.totalTimeInForeground }
                if (totalMillis > 0) {
                    val hours = totalMillis / (1000 * 60 * 60)
                    val minutes = (totalMillis / (1000 * 60)) % 60
                    val text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                    _uiState.update { it.copy(screenTime = "Screen time: $text") }
                } else {
                    _uiState.update { it.copy(screenTime = "Screen time: None") }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(screenTime = "Screen time: Data unavailable") }
            }
        }
    }

    override fun onCleared() {
        try {
            appContext.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        super.onCleared()
    }
}
