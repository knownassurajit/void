package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.Gravity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.BuildConfig
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.availableFonts
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    var maxHomeApps by remember { mutableIntStateOf(prefs.maxHomeApps) }
    var showClock by remember { mutableStateOf(prefs.showClockWidget) }
    var showDate by remember { mutableStateOf(prefs.showDateWidget) }
    var clockHorizontalAlignment by remember { mutableIntStateOf(prefs.clockAlignment) }
    var clockVerticalAlignment by remember { mutableIntStateOf(prefs.clockVerticalAlignment) }
    var appHorizontalAlignment by remember { mutableIntStateOf(prefs.homeAlignment) }
    var appVerticalAlignment by remember { mutableIntStateOf(prefs.homeVerticalAlignment) }
    var showScreenTime by remember { mutableStateOf(prefs.showScreenTimeWidget) }
    var autoShowKeyboard by remember { mutableStateOf(prefs.autoShowKeyboard) }
    var showAlphabetCategories by remember { mutableStateOf(prefs.showAlphabetCategories) }
    var showStatusBar by remember { mutableStateOf(prefs.showStatusBar) }
    var homeTextSize by remember { mutableFloatStateOf(prefs.homeTextSizeScale) }
    var drawerTextSize by remember { mutableFloatStateOf(prefs.appDrawerTextSizeScale) }
    var appSpacing by remember { mutableFloatStateOf(prefs.appSpacingDp) }
    var leftSwipeAction by remember { mutableStateOf(prefs.leftSwipeAction) }
    var rightSwipeAction by remember { mutableStateOf(prefs.rightSwipeAction) }
    var clockSectionWeight by remember { mutableFloatStateOf(prefs.clockSectionWeight) }
    var privateSpaceEnabled by remember { mutableStateOf(prefs.privateSpaceEnabled) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showAppPicker by remember { mutableStateOf<String?>(null) }
    var showDeveloperInfo by remember { mutableStateOf(false) }
    var appFont by remember { mutableStateOf(prefs.appFont) }
    var use24HourClock by remember { mutableStateOf(prefs.use24HourClock) }
    var showSeconds by remember { mutableStateOf(prefs.showClockSeconds) }

    var enableGestures by remember { mutableStateOf(prefs.enableGestures) }
    var enableSummary by remember { mutableStateOf(prefs.enableNotificationSummary) }
    var enableWidgets by remember { mutableStateOf(prefs.enableWidgets) }
    var enableNotes by remember { mutableStateOf(prefs.enableNotes) }

    val isPrivateSpaceConfigured = remember {
        PrivateSpaceHelper.getPrivateSpaceProfile(context) != null
    }

    if (showDeveloperInfo) {
        DeveloperInfoDialog(onDismiss = { showDeveloperInfo = false })
    }

    Box(modifier = Modifier.padding(top = LocalFixedStatusBarHeight.current).navigationBarsPadding()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsHeader(
                    onShowCredits = { showDeveloperInfo = true },
                    onOpenSystemSettings = {
                        try {
                            context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                        } catch (_: Exception) {}
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                SettingActionItem(
                    title = stringResource(R.string.change_default_launcher),
                    subtitle = "Manage default home app settings",
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                )

                AestheticsSection(
                    appFont = appFont,
                    onFontChanged = { appFont = it }
                )

                ClockAndDateSection(
                    showClock = showClock,
                    onShowClockChange = { showClock = it },
                    showDate = showDate,
                    onShowDateChange = { showDate = it },
                    showScreenTime = showScreenTime,
                    onShowScreenTimeChange = { showScreenTime = it },
                    use24HourClock = use24HourClock,
                    onUse24HourClockChange = { use24HourClock = it },
                    showSeconds = showSeconds,
                    onShowSecondsChange = { showSeconds = it },
                    clockHorizontalAlignment = clockHorizontalAlignment,
                    onClockHorizontalAlignmentChange = { clockHorizontalAlignment = it },
                    clockVerticalAlignment = clockVerticalAlignment,
                    onClockVerticalAlignmentChange = { clockVerticalAlignment = it },
                    clockSectionWeight = clockSectionWeight,
                    onClockSectionWeightChange = { clockSectionWeight = it },
                    snackbarHostState = snackbarHostState
                )

                HomeAppsSection(
                    maxHomeApps = maxHomeApps,
                    onMaxHomeAppsChange = { maxHomeApps = it },
                    appHorizontalAlignment = appHorizontalAlignment,
                    onAppHorizontalAlignmentChange = { appHorizontalAlignment = it },
                    appVerticalAlignment = appVerticalAlignment,
                    onAppVerticalAlignmentChange = { appVerticalAlignment = it },
                    appSpacing = appSpacing,
                    onAppSpacingChange = { appSpacing = it }
                )

                TextSizeSection(
                    homeTextSize = homeTextSize,
                    onHomeTextSizeChange = { homeTextSize = it },
                    drawerTextSize = drawerTextSize,
                    onDrawerTextSizeChange = { drawerTextSize = it }
                )

                AppLibrarySection(
                    showAlphabetCategories = showAlphabetCategories,
                    onShowAlphabetCategoriesChange = { showAlphabetCategories = it },
                    privateSpaceEnabled = privateSpaceEnabled,
                    onPrivateSpaceEnabledChange = { privateSpaceEnabled = it },
                    isPrivateSpaceConfigured = isPrivateSpaceConfigured,
                    snackbarHostState = snackbarHostState
                )

                AppearanceSection(
                    showStatusBar = showStatusBar,
                    onShowStatusBarChange = { showStatusBar = it },
                    autoShowKeyboard = autoShowKeyboard,
                    onAutoShowKeyboardChange = { autoShowKeyboard = it }
                )

                GesturesSection(
                    enableGestures = enableGestures,
                    onEnableGesturesChange = { enableGestures = it },
                    enableSummary = enableSummary,
                    onEnableSummaryChange = { enableSummary = it },
                    enableWidgets = enableWidgets,
                    onEnableWidgetsChange = { enableWidgets = it },
                    enableNotes = enableNotes,
                    onEnableNotesChange = { enableNotes = it },
                    leftSwipeAction = leftSwipeAction,
                    onLeftSwipeActionChange = {
                        leftSwipeAction = it
                        if (it == SwipeAction.APP) showAppPicker = "left"
                    },
                    rightSwipeAction = rightSwipeAction,
                    onRightSwipeActionChange = {
                        rightSwipeAction = it
                        if (it == SwipeAction.APP) showAppPicker = "right"
                    },
                    snackbarHostState = snackbarHostState
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showAppPicker != null) {
            SettingsAppPickerSheet(
                onDismiss = { showAppPicker = null },
                onAppSelected = { pkg ->
                    if (showAppPicker == "left") prefs.leftSwipeAppPackage = pkg
                    if (showAppPicker == "right") prefs.rightSwipeAppPackage = pkg
                    showAppPicker = null
                }
            )
        }
    }
}

@Composable
private fun DeveloperInfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Developer Credits") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Made with 💜 by", style = MaterialTheme.typography.bodyMedium)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/knownassurajit/")))
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Surajit Das", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/knownassurajit/void")))
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("GitHub Repository", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SettingsHeader(
    onShowCredits: () -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "VOID",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Launcher Settings".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
        }
        Row {
            IconButton(onClick = onShowCredits) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Credits",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenSystemSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "System Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AestheticsSection(
    appFont: String,
    onFontChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    var showFontPicker by remember { mutableStateOf(false) }
    val currentFontName = availableFonts.firstOrNull { it.first == appFont }?.second ?: "Inter"

    SettingsSectionHeader("Aesthetics")
    SettingActionItem(
        title = "Launcher Font",
        subtitle = currentFontName,
        icon = { Icon(Icons.Default.FontDownload, contentDescription = null) },
        onClick = { showFontPicker = true }
    )

    if (showFontPicker) {
        AlertDialog(
            onDismissRequest = { showFontPicker = false },
            title = { Text("Select Font") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(availableFonts) { (key, displayName, family) ->
                        val isSelected = appFont == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onFontChanged(key)
                                    prefs.appFont = key
                                    showFontPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onFontChanged(key)
                                    prefs.appFont = key
                                    showFontPicker = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = family
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (key == "system") {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "System",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun ClockAndDateSection(
    showClock: Boolean,
    onShowClockChange: (Boolean) -> Unit,
    showDate: Boolean,
    onShowDateChange: (Boolean) -> Unit,
    showScreenTime: Boolean,
    onShowScreenTimeChange: (Boolean) -> Unit,
    use24HourClock: Boolean,
    onUse24HourClockChange: (Boolean) -> Unit,
    showSeconds: Boolean,
    onShowSecondsChange: (Boolean) -> Unit,
    clockHorizontalAlignment: Int,
    onClockHorizontalAlignmentChange: (Int) -> Unit,
    clockVerticalAlignment: Int,
    onClockVerticalAlignmentChange: (Int) -> Unit,
    clockSectionWeight: Float,
    onClockSectionWeightChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    SettingsSectionHeader("Clock & Date")
    SettingToggleItem(
        title = "Show Clock",
        subtitle = "Top-level time display",
        icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        checked = showClock,
        onCheckedChange = { onShowClockChange(it); prefs.showClockWidget = it },
        tooltipText = "Toggle clock visibility on the home screen. Tap the clock to open your alarms app."
    )
    SettingToggleItem(
        title = stringResource(R.string.show_date_time),
        subtitle = "Current date under the clock",
        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        checked = showDate,
        onCheckedChange = { onShowDateChange(it); prefs.showDateWidget = it }
    )
    SettingToggleItem(
        title = stringResource(R.string.screen_time),
        subtitle = "Device usage metrics",
        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
        checked = showScreenTime,
        tooltipText = "Shows daily screen time from Digital Wellbeing. Requires Usage Access permission.",
        onCheckedChange = {
            onShowScreenTimeChange(it); prefs.showScreenTimeWidget = it
            if (it) {
                val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
                }
                if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
                    try {
                        context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        scope.launch { snackbarHostState.showSnackbar("Redirected to Usage Access settings") }
                    } catch (e: Exception) {
                        scope.launch { snackbarHostState.showSnackbar("Could not open settings automatically") }
                    }
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Screen Time enabled") }
                }
            }
        }
    )
    SettingToggleItem(
        title = "24-Hour Format",
        subtitle = "Use military time notation",
        icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        checked = use24HourClock,
        onCheckedChange = { onUse24HourClockChange(it); prefs.use24HourClock = it },
        tooltipText = "Switch between 12h (AM/PM) and 24h format for the home screen clock."
    )
    SettingToggleItem(
        title = "Show Seconds",
        subtitle = "Display seconds on the home screen clock",
        icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        checked = showSeconds,
        onCheckedChange = { onShowSecondsChange(it); prefs.showClockSeconds = it },
        tooltipText = "Enabling seconds will cause the clock to update every second, which may have a minor impact on battery life."
    )
    SettingAlignmentItem(
        title = "Clock Horizontal Alignment",
        subtitle = "Left, Center, or Right",
        icon = { Icon(Icons.Default.LinearScale, contentDescription = null) },
        currentGravity = clockHorizontalAlignment,
        options = listOf(Gravity.START to "Left", Gravity.CENTER_HORIZONTAL to "Center", Gravity.END to "Right"),
        tooltipText = "Tap and hold this to align your time horizontally relative to the clock section.",
        onChanged = { onClockHorizontalAlignmentChange(it); prefs.clockAlignment = it }
    )
    SettingAlignmentItem(
        title = "Clock Vertical Alignment",
        subtitle = "Top, Middle, or Bottom",
        icon = { Icon(Icons.Default.Transform, contentDescription = null) },
        currentGravity = clockVerticalAlignment,
        options = listOf(Gravity.TOP to "Top", Gravity.CENTER_VERTICAL to "Middle", Gravity.BOTTOM to "Bottom"),
        tooltipText = "Change how your clock is placed vertically on the home screen UI.",
        onChanged = { onClockVerticalAlignmentChange(it); prefs.clockVerticalAlignment = it }
    )
    SettingSliderItem(
        title = "Clock Size",
        subtitle = "Adjust clock scale",
        icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
        value = if (clockSectionWeight < 0.5f) 1.0f else clockSectionWeight,
        range = 0.5f..1.5f,
        tooltipText = "Scale your clock font up or down. A smaller max value ensures 24h+seconds configurations fit without clipping.",
        onChanged = { onClockSectionWeightChange(it); prefs.clockSectionWeight = it }
    )
}

@Composable
private fun HomeAppsSection(
    maxHomeApps: Int,
    onMaxHomeAppsChange: (Int) -> Unit,
    appHorizontalAlignment: Int,
    onAppHorizontalAlignmentChange: (Int) -> Unit,
    appVerticalAlignment: Int,
    onAppVerticalAlignmentChange: (Int) -> Unit,
    appSpacing: Float,
    onAppSpacingChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )

    SettingsSectionHeader("Home Apps")
    SettingCounterItem(
        title = "Max apps on Home Screen",
        subtitle = "Limit applications shown",
        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
        value = maxHomeApps,
        tooltipText = "Specify how many priority apps (up to 10) you want available directly from the home screen.",
        onDecrease = { if (maxHomeApps > 1) { onMaxHomeAppsChange(maxHomeApps - 1); prefs.maxHomeApps = maxHomeApps - 1 } },
        onIncrease = { if (maxHomeApps < 10) { onMaxHomeAppsChange(maxHomeApps + 1); prefs.maxHomeApps = maxHomeApps + 1 } }
    )
    SettingAlignmentItem(
        title = "Apps Horizontal Alignment",
        subtitle = "Left, Center, or Right",
        icon = { Icon(Icons.Default.LinearScale, contentDescription = null) },
        currentGravity = appHorizontalAlignment,
        options = listOf(Gravity.START to "Left", Gravity.CENTER_HORIZONTAL to "Center", Gravity.END to "Right"),
        tooltipText = "Align your minimalist home apps array on the X-axis.",
        onChanged = { onAppHorizontalAlignmentChange(it); prefs.homeAlignment = it }
    )
    SettingAlignmentItem(
        title = "Apps Vertical Alignment",
        subtitle = "Top, Middle, or Bottom",
        icon = { Icon(Icons.Default.Transform, contentDescription = null) },
        currentGravity = appVerticalAlignment,
        options = listOf(Gravity.TOP to "Top", Gravity.CENTER_VERTICAL to "Middle", Gravity.BOTTOM to "Bottom"),
        tooltipText = "Align your minimalist home apps array on the Y-axis.",
        onChanged = { onAppVerticalAlignmentChange(it); prefs.homeVerticalAlignment = it }
    )
    SettingSliderItem(
        title = "App Spacing",
        subtitle = "Space between app labels",
        icon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
        value = appSpacing,
        range = 0f..48f,
        tooltipText = "Increase or decrease the dynamic visual gap separating each pinned tool.",
        onChanged = { onAppSpacingChange(it); prefs.appSpacingDp = it }
    )
}

@Composable
private fun TextSizeSection(
    homeTextSize: Float,
    onHomeTextSizeChange: (Float) -> Unit,
    drawerTextSize: Float,
    onDrawerTextSizeChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    SettingsSectionHeader("Text Size")
    SettingSliderItem(
        title = stringResource(R.string.home_text_size),
        subtitle = "Scale font on home",
        icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
        value = homeTextSize,
        range = 0.5f..2.0f,
        onChanged = { onHomeTextSizeChange(it); prefs.homeTextSizeScale = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.app_drawer_text_size),
        subtitle = "Scale font in drawer",
        icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
        value = drawerTextSize,
        range = 0.5f..2.0f,
        onChanged = { onDrawerTextSizeChange(it); prefs.appDrawerTextSizeScale = it }
    )
}

@Composable
private fun AppLibrarySection(
    showAlphabetCategories: Boolean,
    onShowAlphabetCategoriesChange: (Boolean) -> Unit,
    privateSpaceEnabled: Boolean,
    onPrivateSpaceEnabledChange: (Boolean) -> Unit,
    isPrivateSpaceConfigured: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    SettingsSectionHeader("App Library")
    SettingToggleItem(
        title = "Show Alphabet Categories",
        subtitle = "Group by starting letter",
        icon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
        checked = showAlphabetCategories,
        onCheckedChange = { onShowAlphabetCategoriesChange(it); prefs.showAlphabetCategories = it },
        tooltipText = "Groups apps A–Z in the app drawer for quick alphabetical navigation."
    )
    SettingToggleItem(
        title = "Enable Private Space",
        subtitle = if (isPrivateSpaceConfigured) "Unlock OS hidden profiles" else "Not configured in system settings",
        icon = { Icon(Icons.Default.Security, contentDescription = null) },
        checked = privateSpaceEnabled && isPrivateSpaceConfigured,
        onCheckedChange = {
            if (!isPrivateSpaceConfigured) {
                scope.launch { snackbarHostState.showSnackbar("Private Space is not configured.") }
            } else {
                onPrivateSpaceEnabledChange(it); prefs.privateSpaceEnabled = it
                scope.launch { snackbarHostState.showSnackbar(if (it) "Private Space Enabled" else "Private Space Disabled") }
            }
        },
        tooltipText = "Private Space isolates sensitive apps behind a separate authentication layer. Must be configured in system settings first."
    )
}

@Composable
private fun AppearanceSection(
    showStatusBar: Boolean,
    onShowStatusBarChange: (Boolean) -> Unit,
    autoShowKeyboard: Boolean,
    onAutoShowKeyboardChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    SettingsSectionHeader(stringResource(R.string.appearance))
    SettingToggleItem(
        title = stringResource(R.string.notification_bar),
        subtitle = "Show system status icons",
        icon = { Icon(Icons.Default.SpaceBar, contentDescription = null) },
        checked = showStatusBar,
        onCheckedChange = { onShowStatusBarChange(it); prefs.showStatusBar = it },
        tooltipText = "When disabled, the system status bar hides but can be revealed by swiping down from the top edge."
    )
    SettingToggleItem(
        title = stringResource(R.string.auto_show_keyboard),
        subtitle = "Pop up keyboard on search",
        icon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
        checked = autoShowKeyboard,
        onCheckedChange = { onAutoShowKeyboardChange(it); prefs.autoShowKeyboard = it },
        tooltipText = "Automatically pops up the keyboard when you enter the app drawer for faster search."
    )
}

@Composable
private fun GesturesSection(
    enableGestures: Boolean,
    onEnableGesturesChange: (Boolean) -> Unit,
    enableSummary: Boolean,
    onEnableSummaryChange: (Boolean) -> Unit,
    enableWidgets: Boolean,
    onEnableWidgetsChange: (Boolean) -> Unit,
    enableNotes: Boolean,
    onEnableNotesChange: (Boolean) -> Unit,
    leftSwipeAction: String,
    onLeftSwipeActionChange: (String) -> Unit,
    rightSwipeAction: String,
    onRightSwipeActionChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    SettingsSectionHeader(stringResource(R.string.gestures))
    SettingToggleItem(
        title = "Enable Gestures",
        subtitle = "Activate screen swipes",
        icon = { Icon(Icons.Default.Swipe, contentDescription = null) },
        checked = enableGestures,
        onCheckedChange = { onEnableGesturesChange(it); prefs.enableGestures = it }
    )
    if (enableGestures) {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = enableSummary,
                onClick = {
                    val newState = !enableSummary
                    if (newState) {
                        try {
                            val enabledListeners = android.provider.Settings.Secure.getString(
                                context.contentResolver, "enabled_notification_listeners"
                            ) ?: ""
                            val hasPermission = enabledListeners.contains(context.packageName)
                            if (!hasPermission) {
                                try {
                                    context.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    scope.launch { snackbarHostState.showSnackbar("Grant notification access to enable summaries") }
                                } catch (_: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar("Could not open notification settings") }
                                }
                                return@FilterChip
                            }
                        } catch (e: Exception) {
                            scope.launch { snackbarHostState.showSnackbar("Error checking notification access") }
                            return@FilterChip
                        }
                    }
                    onEnableSummaryChange(newState); prefs.enableNotificationSummary = newState
                },
                label = { Text("Notification Summary") },
                leadingIcon = if (enableSummary) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
            )

            FilterChip(
                selected = enableWidgets,
                onClick = {
                    val newState = !enableWidgets
                    onEnableWidgetsChange(newState); prefs.enableWidgets = newState
                },
                label = { Text("Widgets") },
                leadingIcon = if (enableWidgets) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
            )

            FilterChip(
                selected = enableNotes,
                onClick = {
                    val newState = !enableNotes
                    onEnableNotesChange(newState); prefs.enableNotes = newState
                },
                label = { Text("Notes") },
                leadingIcon = if (enableNotes) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
            )
        }

        SwipeActionSelector(
            label = stringResource(R.string.left_swipe_action),
            subtitle = "Assign left swipe behavior",
            icon = { Icon(Icons.Default.SwipeLeft, contentDescription = null) },
            currentAction = leftSwipeAction,
            excludeAction = rightSwipeAction,
            enableSummary = enableSummary,
            enableWidgets = enableWidgets,
            enableNotes = enableNotes
        ) {
            onLeftSwipeActionChange(it); prefs.leftSwipeAction = it
        }

        SwipeActionSelector(
            label = stringResource(R.string.right_swipe_action),
            subtitle = "Assign right swipe behavior",
            icon = { Icon(Icons.Default.SwipeRight, contentDescription = null) },
            currentAction = rightSwipeAction,
            excludeAction = leftSwipeAction,
            enableSummary = enableSummary,
            enableWidgets = enableWidgets,
            enableNotes = enableNotes
        ) {
            onRightSwipeActionChange(it); prefs.rightSwipeAction = it
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingActionItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    tooltipText: String? = null,
    onClick: () -> Unit
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                    if (subtitle.isNotEmpty()) {
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (tooltipText != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(tooltipText) } },
            state = rememberTooltipState()
        ) { content() }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onInfoClick: (() -> Unit)? = null,
    tooltipText: String? = null
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                        if (onInfoClick != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onInfoClick, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Outlined.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (subtitle.isNotEmpty()) {
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }

    if (tooltipText != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(tooltipText) } },
            state = rememberTooltipState()
        ) { content() }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingSliderItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    tooltipText: String? = null,
    onChanged: (Float) -> Unit
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon()
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                        if (subtitle.isNotEmpty()) {
                            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = String.format("%.1f", value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = value,
                    onValueChange = onChanged,
                    valueRange = range,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
    if (tooltipText != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(tooltipText) } },
            state = rememberTooltipState()
        ) { content() }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingAlignmentItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    currentGravity: Int,
    options: List<Pair<Int, String>>,
    tooltipText: String? = null,
    onChanged: (Int) -> Unit
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon()
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                        if (subtitle.isNotEmpty()) {
                            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (optionGravity, optionLabel) ->
                        val isSelected = currentGravity == optionGravity
                        Card(
                            modifier = Modifier.weight(1f).clickable { onChanged(optionGravity) },
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = optionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (tooltipText != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(tooltipText) } },
            state = rememberTooltipState()
        ) { content() }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingCounterItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    value: Int,
    tooltipText: String? = null,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                    if (subtitle.isNotEmpty()) {
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = "$value", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onIncrease) {
                        Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (tooltipText != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(tooltipText) } },
            state = rememberTooltipState()
        ) { content() }
    } else {
        content()
    }
}

@Composable
private fun SwipeActionSelector(
    label: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    currentAction: String,
    excludeAction: String,
    enableSummary: Boolean,
    enableWidgets: Boolean,
    enableNotes: Boolean,
    onChanged: (String) -> Unit
) {
    val allActions = mutableListOf<Pair<String, String>>()
    if (enableSummary && BuildConfig.SHOW_NOTIFICATION_SUMMARY_FEATURE) allActions.add(SwipeAction.NOTIFICATION_SUMMARY to "Notification Summary")
    if (enableWidgets && BuildConfig.SHOW_WIDGETS_FEATURE) allActions.add(SwipeAction.WIDGETS to "Widgets")
    if (enableNotes) allActions.add(SwipeAction.NOTES to "Notes")
    allActions.add(SwipeAction.NOTIFICATIONS to "System Dropdown")
    allActions.add(SwipeAction.APP to "Open App")
    allActions.add(SwipeAction.ACCESSIBILITY to "Accessibility Action")
    allActions.add(SwipeAction.NONE to "None")

    val available = allActions.filter {
        it.first != excludeAction || it.first == SwipeAction.NONE || it.first == SwipeAction.NOTIFICATIONS || it.first == SwipeAction.APP || it.first == SwipeAction.ACCESSIBILITY
    }
    val displayName = allActions.firstOrNull { it.first == currentAction }?.second ?: "None"
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Action") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    available.forEach { (actionKey, actionLabel) ->
                        val isSelected = actionKey == currentAction
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onChanged(actionKey)
                                    showDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onChanged(actionKey)
                                    showDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    SettingActionItem(
        title = label,
        subtitle = displayName,
        icon = icon,
        onClick = { showDialog = true }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsAppPickerSheet(
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val list = pm.queryIntentActivities(intent, 0)
            val mapped = list.map { it.loadLabel(pm).toString() to it.activityInfo.packageName }.sortedBy { it.first.lowercase() }
            apps = mapped
        }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Select App", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            androidx.compose.material3.OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search apps...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.extraLarge
            )
            androidx.compose.foundation.lazy.LazyColumn {
                val filtered = if (search.isBlank()) apps else apps.filter { it.first.contains(search, ignoreCase = true) }
                items(filtered.size) { i ->
                    val app = filtered[i]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(app.second) }
                            .padding(vertical = 14.dp)
                    ) {
                        Text(app.first, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
