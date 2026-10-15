package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.MainUiState
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontAvailability
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.fontAvailability
import com.knownassurajit.app.launcher.voidlauncher.data.font.stateOf
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.FontRepository
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.FontPicker
import com.knownassurajit.app.launcher.voidlauncher.ui.components.HomePreviewContent
import com.knownassurajit.app.launcher.voidlauncher.ui.components.ScaledScreenPreview
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingActionItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingChoiceItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingColorItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingCounterItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingSliderItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingToggleItem
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingsGroupHeader
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingsPreviewPane
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingsPreviewTarget
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingsSection
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSearchField
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.components.weightLabelRes
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeStyle
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavMotion
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.rememberFontResolution
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.rememberVoidMotionScheme
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SettingsScreen"

val LocalSettingsPrefs = staticCompositionLocalOf<Prefs> { error("LocalSettingsPrefs missing") }

/** A section's rows; each becomes one segment of its [SettingsSection]. */
private typealias SettingRows = MutableList<@Composable () -> Unit>

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs.get(context) }

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
    var leftSwipeApp by remember { mutableStateOf(prefs.leftSwipeAppPackage) }
    var rightSwipeApp by remember { mutableStateOf(prefs.rightSwipeAppPackage) }
    var clockSectionWeight by remember { mutableFloatStateOf(prefs.clockSectionWeight) }
    var clockSizeScale by remember { mutableFloatStateOf(prefs.clockSizeScale) }
    var dateSizeScale by remember { mutableFloatStateOf(prefs.dateSizeScale) }
    var screenTimeSizeScale by remember { mutableFloatStateOf(prefs.screenTimeSizeScale) }
    var showHomeApps by remember { mutableStateOf(prefs.showHomeApps) }
    var homeSectionOrder by remember { mutableStateOf(prefs.homeSectionOrder) }
    var privateSpaceEnabled by remember { mutableStateOf(prefs.privateSpaceEnabled) }
    var privateSpaceIconColor by remember { mutableStateOf(prefs.privateSpaceIconColor) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showAppPicker by remember { mutableStateOf<String?>(null) }
    var showDeveloperInfo by remember { mutableStateOf(false) }
    var appFont by remember { mutableStateOf(prefs.appFont) }
    var showFontPicker by remember { mutableStateOf(false) }
    var use24HourClock by remember { mutableStateOf(prefs.use24HourClock) }
    var showSeconds by remember { mutableStateOf(prefs.showClockSeconds) }
    var activePreviewTarget by remember { mutableStateOf<SettingsPreviewTarget?>(null) }
    var clockOpacity by remember { mutableFloatStateOf(prefs.clockOpacity) }
    var homeAppsOpacity by remember { mutableFloatStateOf(prefs.homeAppsOpacity) }
    var clockColor by remember { mutableStateOf(prefs.clockColor) }
    var homeAppsColor by remember { mutableStateOf(prefs.homeAppsColor) }
    var clockFontWeight by remember { mutableIntStateOf(prefs.clockFontWeight) }
    var homeAppsFontWeight by remember { mutableIntStateOf(prefs.homeAppsFontWeight) }
    var homeSidePadding by remember { mutableFloatStateOf(prefs.homeSidePaddingDp) }

    var enableGestures by remember { mutableStateOf(prefs.enableGestures) }
    var enableSwipeDownNotifications by remember { mutableStateOf(prefs.enableSwipeDownNotifications) }
    var enableSummary by remember { mutableStateOf(prefs.enableNotificationSummary) }
    var enableWidgets by remember { mutableStateOf(prefs.enableWidgets) }
    var enableNotes by remember { mutableStateOf(prefs.enableNotes) }
    var notesTitleAlignment by remember { mutableIntStateOf(prefs.notesTitleAlignment) }
    var animationSpeed by remember { mutableStateOf(prefs.animationSpeed) }
    var contentSwipeToBack by remember { mutableStateOf(prefs.contentSwipeToBack) }
    var privateSpacePlacement by remember { mutableStateOf(prefs.privateSpacePlacement) }
    var searchQuery by remember { mutableStateOf("") }

    val isPrivateSpaceConfigured = remember { PrivateSpaceHelper.getPrivateSpaceProfile(context) != null }
    val privateSpaceAvailable = FeatureAvailability.isPrivateSpaceAvailable(context)

    if (showDeveloperInfo) {
        DeveloperInfoDialog(onDismiss = { showDeveloperInfo = false })
    }

    // Everything the previews draw, as Settings holds it right now (before Home has re-read Prefs).
    val previewState = MainUiState(
        showClock = showClock,
        showDate = showDate,
        showScreenTime = showScreenTime,
        showHomeApps = showHomeApps,
        homeAppsCount = maxHomeApps,
        clockHorizontalAlignment = clockHorizontalAlignment,
        clockVerticalAlignment = clockVerticalAlignment,
        appHorizontalAlignment = appHorizontalAlignment,
        appVerticalAlignment = appVerticalAlignment,
        clockSectionWeight = clockSectionWeight,
        clockSizeScale = clockSizeScale,
        dateSizeScale = dateSizeScale,
        screenTimeSizeScale = screenTimeSizeScale,
        homeSectionOrder = homeSectionOrder,
        homeTextSizeScale = homeTextSize,
        appDrawerTextSizeScale = drawerTextSize,
        appSpacingDp = appSpacing,
        appFont = appFont,
        use24HourClock = use24HourClock,
        showSeconds = showSeconds,
        clockOpacity = clockOpacity,
        homeAppsOpacity = homeAppsOpacity,
        clockColor = clockColor,
        homeAppsColor = homeAppsColor,
        clockFontWeight = clockFontWeight,
        homeAppsFontWeight = homeAppsFontWeight,
        homeSidePaddingDp = homeSidePadding
    )
    val holdHome: (Boolean) -> Unit = { holding -> activePreviewTarget = if (holding) SettingsPreviewTarget.HOME else null }
    val holdDrawer: (Boolean) -> Unit = { holding -> activePreviewTarget = if (holding) SettingsPreviewTarget.DRAWER else null }

    CompositionLocalProvider(LocalSettingsPrefs provides prefs) {
        Box(
            modifier = Modifier
                .padding(top = LocalFixedStatusBarHeight.current)
                .navigationBarsPadding()
                .screenBackSwipe(
                    // With the picker open, Dismiss (TalkBack) closes the picker, not Settings.
                    onBack = { if (showFontPicker) showFontPicker = false else onBack() },
                    enabled = LocalContentSwipeToBack.current && !showFontPicker
                )
        ) {
            Scaffold(
                // The picker overlay covers Settings: hide it from TalkBack and from keyboard focus while it is open.
                modifier = if (showFontPicker) {
                    Modifier.clearAndSetSemantics {}.focusProperties { canFocus = false }
                } else {
                    Modifier
                },
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0.dp),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { paddingValues ->
                val search = evaluateSettingsSearch(searchQuery, privateSpaceAvailable)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                ) {
                    SettingsHeader(
                        onShowCredits = { showDeveloperInfo = true },
                        onOpenSystemSettings = { context.startSafely(Intent(android.provider.Settings.ACTION_SETTINGS)) }
                    )
                    VoidSearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        hint = stringResource(R.string.settings_search_hint),
                        leadingIconDescription = stringResource(R.string.search)
                    )

                    if (search.defaultLauncher) {
                        SettingsSection(
                            title = null,
                            items = listOf {
                                SettingActionItem(
                                    title = stringResource(R.string.change_default_launcher),
                                    subtitle = stringResource(R.string.manage_default_home_settings),
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    onClick = {
                                        context.startSafely(
                                            Intent(android.provider.Settings.ACTION_HOME_SETTINGS),
                                            fallback = Intent(android.provider.Settings.ACTION_SETTINGS)
                                        )
                                    }
                                )
                            }
                        )
                    }

                    // ── Appearance ──
                    if (search.appearance) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_appearance))
                        FontStyleSection(appFont = appFont, onOpenFontPicker = { showFontPicker = true })
                        SettingsSection(
                            title = stringResource(R.string.settings_section_system_bars),
                            items = listOf {
                                SettingToggleItem(
                                    title = stringResource(R.string.notification_bar),
                                    subtitle = stringResource(R.string.settings_status_bar_subtitle),
                                    icon = { Icon(Icons.Default.SpaceBar, contentDescription = null) },
                                    checked = showStatusBar,
                                    onCheckedChange = { showStatusBar = it; prefs.showStatusBar = it }
                                )
                            }
                        )
                        SettingsSection(
                            title = stringResource(R.string.settings_section_motion),
                            items = listOf {
                                SettingChoiceItem(
                                    title = stringResource(R.string.animation_speed),
                                    subtitle = stringResource(R.string.animation_speed_subtitle),
                                    icon = { Icon(Icons.Default.Animation, contentDescription = null) },
                                    selected = animationSpeed,
                                    options = listOf(
                                        Prefs.AnimationSpeed.FAST to stringResource(R.string.animation_speed_fast),
                                        Prefs.AnimationSpeed.STANDARD to stringResource(R.string.animation_speed_standard),
                                        Prefs.AnimationSpeed.SLOW to stringResource(R.string.animation_speed_slow)
                                    ),
                                    onSelected = { animationSpeed = it; prefs.animationSpeed = it }
                                )
                            }
                        )
                    }

                    // ── Home screen ──
                    if (search.homeHeader) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_home))
                        HomePreviewCard(state = previewState)
                    }
                    if (search.homeLayout) {
                        HomeLayoutSection(
                            showClock = showClock,
                            onShowClockChange = { showClock = it; prefs.showClockWidget = it },
                            showDate = showDate,
                            onShowDateChange = { showDate = it; prefs.showDateWidget = it },
                            showScreenTime = showScreenTime,
                            onShowScreenTimeChange = { showScreenTime = it; prefs.showScreenTimeWidget = it },
                            showHomeApps = showHomeApps,
                            onShowHomeAppsChange = { showHomeApps = it; prefs.showHomeApps = it },
                            homeSectionOrder = homeSectionOrder,
                            onHomeSectionOrderChange = { homeSectionOrder = it; prefs.homeSectionOrder = it },
                            clockSectionWeight = clockSectionWeight,
                            onClockSectionWeightChange = { clockSectionWeight = it; prefs.clockSectionWeight = it },
                            homeSidePadding = homeSidePadding,
                            onHomeSidePaddingChange = { homeSidePadding = it; prefs.homeSidePaddingDp = it },
                            snackbarHostState = snackbarHostState,
                            holdHome = holdHome
                        )
                    }
                    if (search.clock) {
                        val rows: SettingRows = mutableListOf()
                        rows += {
                            SettingToggleItem(
                                title = stringResource(R.string.settings_24_hour),
                                subtitle = stringResource(R.string.settings_24_hour_subtitle),
                                icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                                checked = use24HourClock,
                                onCheckedChange = { use24HourClock = it; prefs.use24HourClock = it }
                            )
                        }
                        rows += {
                            SettingToggleItem(
                                title = stringResource(R.string.settings_show_seconds),
                                subtitle = stringResource(R.string.settings_show_seconds_subtitle),
                                icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                checked = showSeconds,
                                onCheckedChange = { showSeconds = it; prefs.showClockSeconds = it }
                            )
                        }
                        rows += {
                            SettingChoiceItem(
                                title = stringResource(R.string.settings_clock_horizontal),
                                icon = { Icon(Icons.Default.LinearScale, contentDescription = null) },
                                selected = clockHorizontalAlignment,
                                options = horizontalOptions(),
                                onSelected = { clockHorizontalAlignment = it; prefs.clockAlignment = it }
                            )
                        }
                        rows += {
                            SettingChoiceItem(
                                title = stringResource(R.string.settings_clock_vertical),
                                icon = { Icon(Icons.Default.Transform, contentDescription = null) },
                                selected = clockVerticalAlignment,
                                options = verticalOptions(),
                                onSelected = { clockVerticalAlignment = it; prefs.clockVerticalAlignment = it }
                            )
                        }
                        rows += {
                            SettingSliderItem(
                                title = stringResource(R.string.clock_size),
                                subtitle = stringResource(R.string.settings_clock_size_subtitle),
                                icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                value = clockSizeScale,
                                range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
                                valueText = ::scaleText,
                                onHoldingChanged = holdHome,
                                onChanged = { clockSizeScale = it; prefs.clockSizeScale = it }
                            )
                        }
                        rows += {
                            SettingSliderItem(
                                title = stringResource(R.string.date_size),
                                subtitle = stringResource(R.string.settings_date_size_subtitle),
                                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                value = dateSizeScale,
                                range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
                                valueText = ::scaleText,
                                onHoldingChanged = holdHome,
                                onChanged = { dateSizeScale = it; prefs.dateSizeScale = it }
                            )
                        }
                        rows += {
                            SettingSliderItem(
                                title = stringResource(R.string.screen_time_size),
                                subtitle = stringResource(R.string.settings_screen_time_size_subtitle),
                                icon = { Icon(Icons.Default.QueryStats, contentDescription = null) },
                                value = screenTimeSizeScale,
                                range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
                                valueText = ::scaleText,
                                onHoldingChanged = holdHome,
                                onChanged = { screenTimeSizeScale = it; prefs.screenTimeSizeScale = it }
                            )
                        }
                        addTextStyleRows(
                            rows = rows,
                            style = TextStyleRows(R.string.settings_clock_weight, R.string.settings_clock_color, R.string.settings_clock_opacity),
                            fontWeight = clockFontWeight,
                            color = clockColor,
                            opacity = clockOpacity,
                            onFontWeightChange = { clockFontWeight = it; prefs.clockFontWeight = it },
                            onColorChange = { clockColor = it; prefs.clockColor = it },
                            onOpacityChange = { clockOpacity = it; prefs.clockOpacity = it },
                            holdHome = holdHome
                        )
                        SettingsSection(stringResource(R.string.settings_section_clock), rows)
                    }
                    if (search.homeApps) {
                        val rows: SettingRows = mutableListOf()
                        rows += {
                            SettingCounterItem(
                                title = stringResource(R.string.settings_max_home_apps),
                                subtitle = stringResource(R.string.settings_max_home_apps_subtitle),
                                icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                                value = maxHomeApps,
                                range = 1..10,
                                onChange = { maxHomeApps = it; prefs.maxHomeApps = it }
                            )
                        }
                        rows += {
                            SettingChoiceItem(
                                title = stringResource(R.string.settings_apps_horizontal),
                                icon = { Icon(Icons.Default.LinearScale, contentDescription = null) },
                                selected = appHorizontalAlignment,
                                options = horizontalOptions(),
                                onSelected = { appHorizontalAlignment = it; prefs.homeAlignment = it }
                            )
                        }
                        rows += {
                            SettingChoiceItem(
                                title = stringResource(R.string.settings_apps_vertical),
                                icon = { Icon(Icons.Default.Transform, contentDescription = null) },
                                selected = appVerticalAlignment,
                                options = verticalOptions(),
                                onSelected = { appVerticalAlignment = it; prefs.homeVerticalAlignment = it }
                            )
                        }
                        rows += {
                            SettingSliderItem(
                                title = stringResource(R.string.settings_app_spacing),
                                subtitle = stringResource(R.string.settings_app_spacing_subtitle),
                                icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
                                value = appSpacing,
                                range = 0f..HomeStyle.MAX_APP_SPACING,
                                valueText = { "${it.roundToInt()}" },
                                onHoldingChanged = holdHome,
                                onChanged = { appSpacing = it; prefs.appSpacingDp = it }
                            )
                        }
                        rows += {
                            SettingSliderItem(
                                title = stringResource(R.string.home_text_size),
                                subtitle = stringResource(R.string.settings_home_text_size_subtitle),
                                icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                value = homeTextSize,
                                range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_APP_SIZE_SCALE,
                                valueText = ::scaleText,
                                onHoldingChanged = holdHome,
                                onChanged = { homeTextSize = it; prefs.homeTextSizeScale = it }
                            )
                        }
                        addTextStyleRows(
                            rows = rows,
                            style = TextStyleRows(R.string.settings_apps_weight, R.string.settings_apps_color, R.string.settings_apps_opacity),
                            fontWeight = homeAppsFontWeight,
                            color = homeAppsColor,
                            opacity = homeAppsOpacity,
                            onFontWeightChange = { homeAppsFontWeight = it; prefs.homeAppsFontWeight = it },
                            onColorChange = { homeAppsColor = it; prefs.homeAppsColor = it },
                            onOpacityChange = { homeAppsOpacity = it; prefs.homeAppsOpacity = it },
                            holdHome = holdHome
                        )
                        SettingsSection(stringResource(R.string.settings_section_home_apps), rows)
                    }

                    // ── App drawer ──
                    if (search.appDrawer) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_app_drawer))
                        SettingsSection(
                            title = stringResource(R.string.settings_section_app_list),
                            items = listOf(
                                {
                                    SettingToggleItem(
                                        title = stringResource(R.string.settings_show_alphabet),
                                        subtitle = stringResource(R.string.settings_show_alphabet_subtitle),
                                        icon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
                                        checked = showAlphabetCategories,
                                        onCheckedChange = { showAlphabetCategories = it; prefs.showAlphabetCategories = it }
                                    )
                                },
                                {
                                    SettingToggleItem(
                                        title = stringResource(R.string.auto_show_keyboard),
                                        subtitle = stringResource(R.string.settings_auto_keyboard_subtitle),
                                        icon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
                                        checked = autoShowKeyboard,
                                        onCheckedChange = { autoShowKeyboard = it; prefs.autoShowKeyboard = it }
                                    )
                                },
                                {
                                    SettingSliderItem(
                                        title = stringResource(R.string.app_drawer_text_size),
                                        subtitle = stringResource(R.string.settings_drawer_text_size_subtitle),
                                        icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                        value = drawerTextSize,
                                        range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_APP_SIZE_SCALE,
                                        valueText = ::scaleText,
                                        onHoldingChanged = holdDrawer,
                                        onChanged = { drawerTextSize = it; prefs.appDrawerTextSizeScale = it }
                                    )
                                }
                            )
                        )
                        PrivateSpaceSection(
                            privateSpaceEnabled = privateSpaceEnabled,
                            onPrivateSpaceEnabledChange = { privateSpaceEnabled = it; prefs.privateSpaceEnabled = it },
                            privateSpacePlacement = privateSpacePlacement,
                            onPrivateSpacePlacementChange = { privateSpacePlacement = it; prefs.privateSpacePlacement = it },
                            iconColor = privateSpaceIconColor,
                            onIconColorChange = { privateSpaceIconColor = it; prefs.privateSpaceIconColor = it },
                            isPrivateSpaceConfigured = isPrivateSpaceConfigured,
                            privateSpaceAvailable = privateSpaceAvailable,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    // ── Gestures ──
                    if (search.gestures) {
                        SettingsGroupHeader(stringResource(R.string.gestures))
                        GesturesSection(
                            enableGestures = enableGestures,
                            onEnableGesturesChange = { enableGestures = it; prefs.enableGestures = it },
                            enableSwipeDownNotifications = enableSwipeDownNotifications,
                            onEnableSwipeDownNotificationsChange = {
                                enableSwipeDownNotifications = it
                                prefs.enableSwipeDownNotifications = it
                            },
                            panels = EnabledPanels(enableSummary, enableWidgets, enableNotes),
                            left = SwipeSide(leftSwipeAction, leftSwipeApp),
                            onLeftSwipeActionChange = {
                                leftSwipeAction = it
                                prefs.leftSwipeAction = it
                                if (it == SwipeAction.APP) showAppPicker = "left"
                            },
                            right = SwipeSide(rightSwipeAction, rightSwipeApp),
                            onRightSwipeActionChange = {
                                rightSwipeAction = it
                                prefs.rightSwipeAction = it
                                if (it == SwipeAction.APP) showAppPicker = "right"
                            },
                            contentSwipeToBack = contentSwipeToBack,
                            onContentSwipeToBackChange = { contentSwipeToBack = it; prefs.contentSwipeToBack = it },
                            snackbarHostState = snackbarHostState
                        )
                    }

                    // ── Features ──
                    if (search.features) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_features))
                        FeaturesSection(
                            panels = EnabledPanels(enableSummary, enableWidgets, enableNotes),
                            onEnableSummaryChange = { enableSummary = it; prefs.enableNotificationSummary = it },
                            onEnableWidgetsChange = { enableWidgets = it; prefs.enableWidgets = it },
                            onEnableNotesChange = { enableNotes = it; prefs.enableNotes = it },
                            notesTitleAlignment = notesTitleAlignment,
                            onNotesTitleAlignmentChange = { notesTitleAlignment = it; prefs.notesTitleAlignment = it },
                            snackbarHostState = snackbarHostState
                        )
                    }

                    // ── Permissions ──
                    if (search.permissions) {
                        SettingsGroupHeader(stringResource(R.string.permissions))
                        PermissionsSection()
                    }

                    if (!search.anyVisible) {
                        Text(
                            text = stringResource(R.string.settings_no_matches),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(VoidDimens.sectionSpacing)
                        )
                    }

                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                }
            }

            val motion = rememberVoidMotionScheme(animationSpeed)
            AnimatedVisibility(
                visible = showFontPicker,
                modifier = Modifier.fillMaxSize(),
                enter = NavMotion.enter(PanelEdge.None, motion),
                exit = NavMotion.popExit(PanelEdge.None, motion)
            ) {
                FontPicker(
                    selectedId = FontCatalog.displayedId(appFont),
                    onFontSelected = { id ->
                        prefs.appFont = id
                        appFont = id
                        showFontPicker = false
                    },
                    onDismiss = { showFontPicker = false }
                )
            }

            AnimatedVisibility(
                visible = activePreviewTarget != null,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                activePreviewTarget?.let { target ->
                    SettingsPreviewPane(target = target, state = previewState)
                }
            }

            showAppPicker?.let { side ->
                SettingsAppPickerSheet(
                    onDismiss = { showAppPicker = null },
                    onAppSelected = { pkg ->
                        if (side == "left") {
                            prefs.leftSwipeAppPackage = pkg
                            leftSwipeApp = pkg
                        } else {
                            prefs.rightSwipeAppPackage = pkg
                            rightSwipeApp = pkg
                        }
                        showAppPicker = null
                    }
                )
            }
        }
    }
}

/** Starts [intent], or [fallback] when nothing handles it; a missing activity is never a crash. */
private fun Context.startSafely(intent: Intent, fallback: Intent? = null) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: android.content.ActivityNotFoundException) {
        Log.w(TAG, "settings_intent_unavailable action=${intent.action}")
        fallback?.let { runCatching { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    } catch (e: SecurityException) {
        Log.w(TAG, "settings_intent_denied action=${intent.action}")
    }
}

private data class SettingsSearchFlags(
    val defaultLauncher: Boolean,
    val appearance: Boolean,
    val homeLayout: Boolean,
    val clock: Boolean,
    val homeApps: Boolean,
    val appDrawer: Boolean,
    val gestures: Boolean,
    val features: Boolean,
    val permissions: Boolean
) {
    val homeHeader: Boolean get() = homeLayout || clock || homeApps
    val anyVisible: Boolean
        get() = defaultLauncher || appearance || homeHeader || appDrawer || gestures || features || permissions
}

@Composable
private fun evaluateSettingsSearch(query: String, privateSpaceAvailable: Boolean): SettingsSearchFlags {
    fun group(title: String, terms: List<String>): Boolean = matchesSettingsQuery(query, listOf(title) + terms)
    val appearanceTerms = listOf(
        stringResource(R.string.settings_section_font_style),
        stringResource(R.string.settings_launcher_font),
        stringResource(R.string.settings_system_display),
        stringResource(R.string.settings_system_wallpaper),
        stringResource(R.string.settings_section_system_bars),
        stringResource(R.string.notification_bar),
        stringResource(R.string.settings_status_bar_subtitle),
        stringResource(R.string.settings_section_motion),
        stringResource(R.string.animation_speed),
        stringResource(R.string.animation_speed_subtitle)
    ) + FontCatalog.searchTerms
    val homeLayoutTerms = listOf(
        stringResource(R.string.settings_section_home_layout),
        stringResource(R.string.settings_show_clock),
        stringResource(R.string.show_date_time),
        stringResource(R.string.screen_time),
        stringResource(R.string.settings_show_home_apps),
        stringResource(R.string.settings_section_order),
        stringResource(R.string.clock_first),
        stringResource(R.string.apps_first),
        stringResource(R.string.clock_section_weight),
        stringResource(R.string.settings_side_padding)
    )
    val clockTerms = listOf(
        stringResource(R.string.settings_section_clock),
        stringResource(R.string.settings_24_hour),
        stringResource(R.string.settings_show_seconds),
        stringResource(R.string.settings_clock_horizontal),
        stringResource(R.string.settings_clock_vertical),
        stringResource(R.string.clock_size),
        stringResource(R.string.date_size),
        stringResource(R.string.screen_time_size),
        stringResource(R.string.settings_clock_weight),
        stringResource(R.string.settings_clock_color),
        stringResource(R.string.settings_clock_opacity)
    )
    val homeAppsTerms = listOf(
        stringResource(R.string.settings_section_home_apps),
        stringResource(R.string.settings_max_home_apps),
        stringResource(R.string.settings_apps_horizontal),
        stringResource(R.string.settings_apps_vertical),
        stringResource(R.string.settings_app_spacing),
        stringResource(R.string.home_text_size),
        stringResource(R.string.settings_apps_weight),
        stringResource(R.string.settings_apps_color),
        stringResource(R.string.settings_apps_opacity)
    )
    val appDrawerTerms = buildList {
        add(stringResource(R.string.settings_section_app_list))
        add(stringResource(R.string.settings_show_alphabet))
        add(stringResource(R.string.auto_show_keyboard))
        add(stringResource(R.string.app_drawer_text_size))
        add(stringResource(R.string.private_space))
        add(stringResource(R.string.settings_enable_private_space))
        add(stringResource(R.string.settings_private_space_icon_color))
        if (privateSpaceAvailable) {
            add(stringResource(R.string.private_space_placement))
            add(stringResource(R.string.private_space_placement_bottom))
            add(stringResource(R.string.private_space_placement_search_bar))
        }
    }
    val gesturesTerms = listOf(
        stringResource(R.string.settings_enable_gestures),
        stringResource(R.string.left_swipe_action),
        stringResource(R.string.right_swipe_action),
        stringResource(R.string.swipe_down_notifications),
        stringResource(R.string.content_swipe_to_back),
        stringResource(R.string.swipe_action_notification_summary),
        stringResource(R.string.swipe_action_widgets),
        stringResource(R.string.swipe_action_notes),
        stringResource(R.string.swipe_action_system_notifications),
        stringResource(R.string.swipe_action_open_app)
    )
    val featuresTerms = listOf(
        stringResource(R.string.swipe_action_notification_summary),
        stringResource(R.string.swipe_action_widgets),
        stringResource(R.string.swipe_action_notes),
        stringResource(R.string.settings_notes_title_alignment)
    )
    val permissionsTerms = listOf(
        stringResource(R.string.permissions_intro),
        stringResource(R.string.perm_notification_listener),
        stringResource(R.string.perm_usage_access),
        stringResource(R.string.perm_accessibility),
        stringResource(R.string.perm_default_home),
        stringResource(R.string.perm_query_apps),
        stringResource(R.string.perm_widgets),
        stringResource(R.string.perm_private_space)
    )
    val homeTitle = stringResource(R.string.settings_group_home)
    return SettingsSearchFlags(
        defaultLauncher = matchesSettingsQuery(
            query,
            listOf(stringResource(R.string.change_default_launcher), stringResource(R.string.manage_default_home_settings))
        ),
        appearance = group(stringResource(R.string.settings_group_appearance), appearanceTerms),
        homeLayout = group(homeTitle, homeLayoutTerms),
        clock = group(homeTitle, clockTerms),
        homeApps = group(homeTitle, homeAppsTerms),
        appDrawer = group(stringResource(R.string.settings_group_app_drawer), appDrawerTerms),
        gestures = group(stringResource(R.string.gestures), gesturesTerms),
        features = group(stringResource(R.string.settings_group_features), featuresTerms),
        permissions = group(stringResource(R.string.permissions), permissionsTerms)
    )
}

private fun matchesSettingsQuery(query: String, terms: List<String>): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    return terms.any { it.contains(needle, ignoreCase = true) }
}

@Composable
private fun DeveloperInfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.developer_credits)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(VoidDimens.sectionSpacing)) {
                Text(stringResource(R.string.credits_made_by), style = MaterialTheme.typography.bodyMedium)
                CreditRow(Icons.Default.Person, stringResource(R.string.credits_author)) {
                    context.startSafely(Intent(Intent.ACTION_VIEW, "https://www.linkedin.com/in/knownassurajit/".toUri()))
                }
                CreditRow(Icons.Default.Code, stringResource(R.string.credits_repository)) {
                    context.startSafely(Intent(Intent.ACTION_VIEW, "https://github.com/knownassurajit/void".toUri()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
private fun CreditRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = VoidDimens.compactSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(VoidDimens.sectionSpacing - VoidDimens.compactSpacing))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsHeader(
    onShowCredits: () -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = VoidDimens.compactSpacing, bottom = VoidDimens.sectionSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.settings_title_subtitle).uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
        }
        IconButton(onClick = onShowCredits) {
            Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.developer_credits))
        }
        IconButton(onClick = onOpenSystemSettings) {
            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_system_settings))
        }
    }
}

@Composable
private fun FontStyleSection(appFont: String, onOpenFontPicker: () -> Unit) {
    val context = LocalContext.current
    val resolution = rememberFontResolution(appFont)
    val repository = remember(context) { FontRepository.get(context) }
    val states by repository.states.collectAsState()
    val saved = FontCatalog.find(appFont)
    // A saved downloadable font that is not usable yet says why: still loading, failed, or no provider.
    val currentFontName = when (fontAvailability(resolution.reason, states.stateOf(appFont, FontCoverage.Full))) {
        FontAvailability.Available -> resolution.spec.displayName
        FontAvailability.Loading -> stringResource(R.string.font_subtitle_loading, saved?.displayName.orEmpty())
        FontAvailability.NotDownloaded -> stringResource(R.string.font_subtitle_not_downloaded, saved?.displayName.orEmpty())
        FontAvailability.NeedsPlayServices -> stringResource(R.string.font_subtitle_needs_play_services, saved?.displayName.orEmpty())
    }
    SettingsSection(
        title = stringResource(R.string.settings_section_font_style),
        items = listOf(
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_launcher_font),
                    subtitle = currentFontName,
                    icon = { Icon(Icons.Default.FontDownload, contentDescription = null) },
                    onClick = onOpenFontPicker
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_system_display),
                    subtitle = stringResource(R.string.settings_system_display_subtitle),
                    icon = { Icon(Icons.Default.Brightness4, contentDescription = null) },
                    onClick = { context.startSafely(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)) }
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_system_wallpaper),
                    subtitle = stringResource(R.string.settings_system_wallpaper_subtitle),
                    icon = { Icon(Icons.Default.Wallpaper, contentDescription = null) },
                    onClick = { context.startSafely(Intent(Intent.ACTION_SET_WALLPAPER)) }
                )
            }
        )
    )
}

private const val HOME_PREVIEW_CARD_SCALE = 0.42f

/** The live Home preview at the top of the Home screen group: it redraws on every change below it. */
@Composable
private fun HomePreviewCard(state: MainUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VoidDimens.rowSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScaledScreenPreview(scale = HOME_PREVIEW_CARD_SCALE) {
            HomePreviewContent(state = state)
        }
        Text(
            text = stringResource(R.string.preview_hold_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = VoidDimens.rowSpacing)
        )
    }
}

@Composable
private fun HomeLayoutSection(
    showClock: Boolean,
    onShowClockChange: (Boolean) -> Unit,
    showDate: Boolean,
    onShowDateChange: (Boolean) -> Unit,
    showScreenTime: Boolean,
    onShowScreenTimeChange: (Boolean) -> Unit,
    showHomeApps: Boolean,
    onShowHomeAppsChange: (Boolean) -> Unit,
    homeSectionOrder: String,
    onHomeSectionOrderChange: (String) -> Unit,
    clockSectionWeight: Float,
    onClockSectionWeightChange: (Float) -> Unit,
    homeSidePadding: Float,
    onHomeSidePaddingChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState,
    holdHome: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usageRedirected = stringResource(R.string.settings_usage_access_redirected)
    val screenTimeEnabled = stringResource(R.string.settings_screen_time_enabled)
    SettingsSection(
        title = stringResource(R.string.settings_section_home_layout),
        items = listOf(
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_show_clock),
                    subtitle = stringResource(R.string.settings_show_clock_subtitle),
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                    checked = showClock,
                    onCheckedChange = onShowClockChange
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.show_date_time),
                    subtitle = stringResource(R.string.settings_show_date_subtitle),
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    checked = showDate,
                    onCheckedChange = onShowDateChange
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.screen_time),
                    subtitle = stringResource(R.string.settings_screen_time_subtitle),
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = null) },
                    checked = showScreenTime,
                    onCheckedChange = { enabled ->
                        onShowScreenTimeChange(enabled)
                        if (!enabled) return@SettingToggleItem
                        if (FeatureAvailability.isUsageAccessGranted(context)) {
                            scope.launch { snackbarHostState.showSnackbar(screenTimeEnabled) }
                        } else {
                            context.startSafely(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            scope.launch { snackbarHostState.showSnackbar(usageRedirected) }
                        }
                    }
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_show_home_apps),
                    subtitle = stringResource(R.string.settings_show_home_apps_subtitle),
                    icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                    checked = showHomeApps,
                    onCheckedChange = onShowHomeAppsChange
                )
            },
            {
                SettingChoiceItem(
                    title = stringResource(R.string.settings_section_order),
                    icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
                    selected = homeSectionOrder,
                    options = listOf(
                        "clock_first" to stringResource(R.string.clock_first),
                        "apps_first" to stringResource(R.string.apps_first)
                    ),
                    onSelected = onHomeSectionOrderChange
                )
            },
            {
                val clockPct = (clockSectionWeight * 100).roundToInt()
                SettingSliderItem(
                    title = stringResource(R.string.clock_section_weight),
                    subtitle = stringResource(R.string.settings_split_subtitle, clockPct, 100 - clockPct),
                    icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
                    value = clockSectionWeight,
                    range = HomeLayoutHelper.MIN_CLOCK_WEIGHT..HomeLayoutHelper.MAX_CLOCK_WEIGHT,
                    valueText = { "${(it * 100).roundToInt()}%" },
                    onHoldingChanged = holdHome,
                    onChanged = onClockSectionWeightChange
                )
            },
            {
                SettingSliderItem(
                    title = stringResource(R.string.settings_side_padding),
                    subtitle = stringResource(R.string.settings_side_padding_subtitle),
                    icon = { Icon(Icons.Default.SpaceBar, contentDescription = null) },
                    value = homeSidePadding,
                    range = HomeStyle.MIN_SIDE_PADDING_DP..HomeStyle.MAX_SIDE_PADDING_DP,
                    valueText = { "${it.roundToInt()} dp" },
                    onHoldingChanged = holdHome,
                    onChanged = onHomeSidePaddingChange
                )
            }
        )
    )
}

/** Titles of the weight, color and opacity rows of one Home text block (clock or apps). */
private data class TextStyleRows(val weightTitle: Int, val colorTitle: Int, val opacityTitle: Int)

private fun addTextStyleRows(
    rows: SettingRows,
    style: TextStyleRows,
    fontWeight: Int,
    color: String,
    opacity: Float,
    onFontWeightChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onOpacityChange: (Float) -> Unit,
    holdHome: (Boolean) -> Unit
) {
    rows += {
        SettingChoiceItem(
            title = stringResource(style.weightTitle),
            icon = { Icon(Icons.Default.FormatBold, contentDescription = null) },
            selected = fontWeight,
            options = HomeStyle.fontWeights.map { weight -> weight to stringResource(weight.weightLabelRes()) },
            onSelected = onFontWeightChange
        )
    }
    rows += {
        SettingColorItem(
            title = stringResource(style.colorTitle),
            subtitle = "",
            icon = { Icon(Icons.Default.Palette, contentDescription = null) },
            color = HomeColor.parse(color),
            onPick = { onColorChange(it.key) }
        )
    }
    rows += {
        SettingSliderItem(
            title = stringResource(style.opacityTitle),
            subtitle = "",
            icon = { Icon(Icons.Default.Opacity, contentDescription = null) },
            value = opacity,
            range = HomeStyle.MIN_OPACITY..HomeStyle.MAX_OPACITY,
            valueText = { "${(it * 100).roundToInt()}%" },
            onHoldingChanged = holdHome,
            onChanged = onOpacityChange
        )
    }
}

@Composable
private fun PrivateSpaceSection(
    privateSpaceEnabled: Boolean,
    onPrivateSpaceEnabledChange: (Boolean) -> Unit,
    privateSpacePlacement: String,
    onPrivateSpacePlacementChange: (String) -> Unit,
    iconColor: String,
    onIconColorChange: (String) -> Unit,
    isPrivateSpaceConfigured: Boolean,
    privateSpaceAvailable: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val notConfigured = stringResource(R.string.settings_private_space_not_configured)
    val enabledMessage = stringResource(R.string.settings_private_space_enabled)
    val disabledMessage = stringResource(R.string.settings_private_space_disabled)
    val active = privateSpaceEnabled && isPrivateSpaceConfigured
    val rows: SettingRows = mutableListOf()
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.settings_enable_private_space),
            subtitle = stringResource(
                if (isPrivateSpaceConfigured) R.string.settings_private_space_subtitle else R.string.settings_private_space_not_configured
            ),
            icon = { Icon(Icons.Default.Security, contentDescription = null) },
            checked = active,
            onCheckedChange = {
                if (!isPrivateSpaceConfigured) {
                    scope.launch { snackbarHostState.showSnackbar(notConfigured) }
                } else {
                    onPrivateSpaceEnabledChange(it)
                    scope.launch { snackbarHostState.showSnackbar(if (it) enabledMessage else disabledMessage) }
                }
            }
        )
    }
    // Placement and icon color only matter once the space is configured and turned on.
    if (active && privateSpaceAvailable) {
        rows += {
            SettingChoiceItem(
                title = stringResource(R.string.private_space_placement),
                subtitle = stringResource(R.string.private_space_placement_subtitle),
                icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                selected = privateSpacePlacement,
                options = listOf(
                    Prefs.PrivateSpacePlacement.BOTTOM to stringResource(R.string.private_space_placement_bottom),
                    Prefs.PrivateSpacePlacement.SEARCH_BAR to stringResource(R.string.private_space_placement_search_bar)
                ),
                onSelected = onPrivateSpacePlacementChange
            )
        }
    }
    if (active) {
        rows += {
            SettingColorItem(
                title = stringResource(R.string.settings_private_space_icon_color),
                subtitle = stringResource(R.string.settings_private_space_icon_color_subtitle),
                icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                color = HomeColor.parse(iconColor),
                onPick = { onIconColorChange(it.key) }
            )
        }
    }
    SettingsSection(stringResource(R.string.private_space), rows)
}

/** Which optional panels are switched on (a swipe can only open an enabled one). */
private data class EnabledPanels(val summary: Boolean, val widgets: Boolean, val notes: Boolean)

/** The action of one swipe direction and the app it opens when the action is "open an app". */
private data class SwipeSide(val action: String, val appPackage: String)

@Composable
private fun GesturesSection(
    enableGestures: Boolean,
    onEnableGesturesChange: (Boolean) -> Unit,
    enableSwipeDownNotifications: Boolean,
    onEnableSwipeDownNotificationsChange: (Boolean) -> Unit,
    panels: EnabledPanels,
    left: SwipeSide,
    onLeftSwipeActionChange: (String) -> Unit,
    right: SwipeSide,
    onRightSwipeActionChange: (String) -> Unit,
    contentSwipeToBack: Boolean,
    onContentSwipeToBackChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val grantNotificationAccess = stringResource(R.string.grant_notification_access)
    val rows: SettingRows = mutableListOf()
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.settings_enable_gestures),
            subtitle = stringResource(R.string.settings_enable_gestures_subtitle),
            icon = { Icon(Icons.Default.Swipe, contentDescription = null) },
            checked = enableGestures,
            onCheckedChange = onEnableGesturesChange
        )
    }
    if (enableGestures) {
        rows += {
            SwipeActionSelector(
                label = stringResource(R.string.left_swipe_action),
                hint = stringResource(R.string.swipe_left_hint),
                icon = { Icon(Icons.Default.SwipeLeft, contentDescription = null) },
                side = left,
                excludeAction = right.action,
                panels = panels,
                onChanged = onLeftSwipeActionChange
            )
        }
        rows += {
            SwipeActionSelector(
                label = stringResource(R.string.right_swipe_action),
                hint = stringResource(R.string.swipe_right_hint),
                icon = { Icon(Icons.Default.SwipeRight, contentDescription = null) },
                side = right,
                excludeAction = left.action,
                panels = panels,
                onChanged = onRightSwipeActionChange
            )
        }
    }
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.swipe_down_notifications),
            subtitle = stringResource(R.string.swipe_down_notifications_subtitle),
            icon = { Icon(Icons.Default.SwipeDown, contentDescription = null) },
            checked = enableSwipeDownNotifications,
            onCheckedChange = { enabled ->
                if (enabled && !FeatureAvailability.isNotificationListenerEnabled(context)) {
                    context.startSafely(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    scope.launch { snackbarHostState.showSnackbar(grantNotificationAccess) }
                }
                onEnableSwipeDownNotificationsChange(enabled)
            }
        )
    }
    SettingsSection(stringResource(R.string.settings_section_swipes), rows)
    SettingsSection(
        title = stringResource(R.string.settings_section_navigation),
        items = listOf {
            SettingToggleItem(
                title = stringResource(R.string.content_swipe_to_back),
                subtitle = stringResource(R.string.content_swipe_to_back_subtitle),
                icon = { Icon(Icons.Default.Swipe, contentDescription = null) },
                checked = contentSwipeToBack,
                onCheckedChange = onContentSwipeToBackChange
            )
        }
    )
}

@Composable
private fun FeaturesSection(
    panels: EnabledPanels,
    onEnableSummaryChange: (Boolean) -> Unit,
    onEnableWidgetsChange: (Boolean) -> Unit,
    onEnableNotesChange: (Boolean) -> Unit,
    notesTitleAlignment: Int,
    onNotesTitleAlignmentChange: (Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val grantSummaryAccess = stringResource(R.string.settings_summary_needs_access)
    val rows: SettingRows = mutableListOf()
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.swipe_action_notification_summary),
            subtitle = stringResource(R.string.settings_summary_subtitle),
            icon = { Icon(Icons.Default.Summarize, contentDescription = null) },
            checked = panels.summary,
            onCheckedChange = { enabled ->
                // The summary reads notifications: without access it is turned on only after the user grants it.
                if (enabled && !FeatureAvailability.isNotificationListenerEnabled(context)) {
                    context.startSafely(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    scope.launch { snackbarHostState.showSnackbar(grantSummaryAccess) }
                    return@SettingToggleItem
                }
                onEnableSummaryChange(enabled)
            }
        )
    }
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.swipe_action_widgets),
            subtitle = stringResource(R.string.settings_widgets_subtitle),
            icon = { Icon(Icons.Default.Widgets, contentDescription = null) },
            checked = panels.widgets,
            onCheckedChange = onEnableWidgetsChange
        )
    }
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.swipe_action_notes),
            subtitle = stringResource(R.string.settings_notes_subtitle),
            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
            checked = panels.notes,
            onCheckedChange = onEnableNotesChange
        )
    }
    if (panels.notes) {
        rows += {
            SettingChoiceItem(
                title = stringResource(R.string.settings_notes_title_alignment),
                icon = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = null) },
                selected = notesTitleAlignment,
                options = horizontalOptions(),
                onSelected = onNotesTitleAlignmentChange
            )
        }
    }
    SettingsSection(stringResource(R.string.settings_section_panels), rows)
}

@Composable
private fun PermissionsSection() {
    val context = LocalContext.current
    val appDetails = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null))
    fun action(titleRes: Int, bodyRes: Int, icon: ImageVector, intent: () -> Intent, fallback: Intent? = null): @Composable () -> Unit = {
        SettingActionItem(
            title = stringResource(titleRes),
            subtitle = stringResource(bodyRes),
            icon = { Icon(icon, contentDescription = null) },
            onClick = { context.startSafely(intent(), fallback) }
        )
    }
    SettingsSection(
        title = null,
        description = stringResource(R.string.permissions_intro),
        items = listOf(
            action(R.string.settings_app_info, R.string.settings_app_info_subtitle, Icons.Default.Settings, { Intent(appDetails) }),
            action(R.string.perm_notification_listener, R.string.perm_notification_listener_body, Icons.Default.Notifications, {
                Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }),
            action(R.string.perm_usage_access, R.string.perm_usage_access_body, Icons.Default.QueryStats, {
                Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
            }),
            action(R.string.perm_accessibility, R.string.perm_accessibility_body, Icons.Default.Lock, {
                Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            }),
            action(R.string.perm_default_home, R.string.perm_default_home_body, Icons.Default.Home, {
                Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            }),
            action(R.string.perm_query_apps, R.string.perm_query_apps_body, Icons.Default.Apps, { Intent(appDetails) }),
            action(R.string.perm_widgets, R.string.perm_widgets_body, Icons.Default.Widgets, { Intent(appDetails) }),
            action(
                R.string.perm_private_space,
                R.string.perm_private_space_body,
                Icons.Default.Security,
                { Intent("android.settings.PRIVATE_SPACE_SETTINGS") },
                fallback = Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
            )
        )
    )
}

@Composable
private fun horizontalOptions(): List<Pair<Int, String>> = listOf(
    Gravity.START to stringResource(R.string.left),
    Gravity.CENTER_HORIZONTAL to stringResource(R.string.center),
    Gravity.END to stringResource(R.string.right)
)

@Composable
private fun verticalOptions(): List<Pair<Int, String>> = listOf(
    Gravity.TOP to stringResource(R.string.top),
    Gravity.CENTER_VERTICAL to stringResource(R.string.middle),
    Gravity.BOTTOM to stringResource(R.string.bottom)
)

private fun scaleText(value: Float): String = String.format(Locale.getDefault(), "%.1f×", value)

/**
 * The actions a swipe can run, in picker order. A panel whose feature is off is not offered, and a
 * panel already on the other swipe is not either (a panel enters from one edge). The accessibility
 * action is not offered: nothing runs it yet, so picking it would make the swipe do nothing.
 */
internal fun swipeActionChoices(
    excludeAction: String,
    enableSummary: Boolean,
    enableWidgets: Boolean,
    enableNotes: Boolean
): List<String> = buildList {
    if (enableSummary) add(SwipeAction.NOTIFICATION_SUMMARY)
    if (enableWidgets) add(SwipeAction.WIDGETS)
    if (enableNotes) add(SwipeAction.NOTES)
    add(SwipeAction.NOTIFICATIONS)
    add(SwipeAction.APP)
    add(SwipeAction.NONE)
}.filter { it != excludeAction || it == SwipeAction.NONE || it == SwipeAction.NOTIFICATIONS || it == SwipeAction.APP }

@Composable
private fun swipeActionLabel(action: String): String = stringResource(
    when (action) {
        SwipeAction.NOTIFICATION_SUMMARY -> R.string.swipe_action_notification_summary
        SwipeAction.WIDGETS -> R.string.swipe_action_widgets
        SwipeAction.NOTES -> R.string.swipe_action_notes
        SwipeAction.NOTIFICATIONS -> R.string.swipe_action_system_notifications
        SwipeAction.APP -> R.string.swipe_action_open_app
        else -> R.string.swipe_action_none
    }
)

@Composable
private fun SwipeActionSelector(
    label: String,
    hint: String,
    icon: @Composable () -> Unit,
    side: SwipeSide,
    excludeAction: String,
    panels: EnabledPanels,
    onChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val choices = swipeActionChoices(excludeAction, panels.summary, panels.widgets, panels.notes)
    var showDialog by remember { mutableStateOf(false) }
    val appLabel = remember(side.appPackage) {
        if (side.appPackage.isBlank()) {
            null
        } else {
            runCatching {
                val pm = context.packageManager
                pm.getApplicationLabel(pm.getApplicationInfo(side.appPackage, 0)).toString()
            }.getOrNull()
        }
    }
    val actionText = swipeActionLabel(side.action)
    val current = if (side.action == SwipeAction.APP && appLabel != null) "$actionText · $appLabel" else actionText

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(label) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    choices.forEach { action ->
                        val selected = action == side.action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onChanged(action)
                                    showDialog = false
                                }
                                .padding(vertical = VoidDimens.compactSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Spacer(Modifier.width(VoidDimens.rowSpacing))
                            Text(
                                text = swipeActionLabel(action),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    SettingActionItem(
        title = label,
        subtitle = "$current\n$hint",
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

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            pm.queryIntentActivities(intent, 0)
                .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
                .distinctBy { it.second }
                .sortedBy { it.first.lowercase(Locale.getDefault()) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.rowSpacing)) {
            Text(
                stringResource(R.string.select_app),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = VoidDimens.rowSpacing)
            )
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(stringResource(R.string.search_apps)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = VoidDimens.rowSpacing),
                shape = MaterialTheme.shapes.extraLarge
            )
            val filtered = if (search.isBlank()) apps else apps.filter { it.first.contains(search, ignoreCase = true) }
            LazyColumn {
                items(filtered.size) { i ->
                    val app = filtered[i]
                    Text(
                        text = app.first,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(app.second) }
                            .padding(vertical = VoidDimens.sectionSpacing - VoidDimens.compactSpacing)
                    )
                }
            }
        }
    }
}
