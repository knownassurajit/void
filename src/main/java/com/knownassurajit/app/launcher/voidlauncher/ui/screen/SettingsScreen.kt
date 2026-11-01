package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Gravity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatLineSpacing
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HorizontalSplit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.SwipeLeft
import androidx.compose.material.icons.outlined.SwipeRight
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VerticalAlignCenter
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.WidthNormal
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.knownassurajit.app.launcher.voidlauncher.BuildConfig
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

/** Top-level functional categories for settings navigation. */
enum class SettingsCategory {
    APPEARANCE,
    HOME_SCREEN,
    APP_DRAWER,
    GESTURES,
    PANELS,
    PERMISSIONS,
    ABOUT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    var showAppScreenTime by remember { mutableStateOf(prefs.showAppScreenTime) }
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
    var currentCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }

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
        homeSidePaddingDp = homeSidePadding,
    )
    val holdHome: (Boolean) -> Unit = { holding -> activePreviewTarget = if (holding) SettingsPreviewTarget.HOME else null }
    val holdDrawer: (Boolean) -> Unit = { holding -> activePreviewTarget = if (holding) SettingsPreviewTarget.DRAWER else null }

    BackHandler(enabled = searchQuery.isNotBlank()) {
        searchQuery = ""
    }
    BackHandler(enabled = (searchQuery.isBlank() && currentCategory != null)) {
        currentCategory = null
    }

    CompositionLocalProvider(LocalSettingsPrefs provides prefs) {
        Box(
            modifier = Modifier
                .padding(top = LocalFixedStatusBarHeight.current)
                .navigationBarsPadding()
                // While the search field has the keyboard up, every result can still be scrolled above it.
                .imePadding()
                .screenBackSwipe(
                    onBack = {
                        when {
                            showFontPicker -> showFontPicker = false
                            searchQuery.isNotBlank() -> searchQuery = ""
                            currentCategory != null -> currentCategory = null
                            else -> onBack()
                        }
                    },
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
                val isSearching = searchQuery.isNotBlank()

                if (isSearching) {
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
                                        icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                                        external = true,
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
                                        icon = { Icon(Icons.Outlined.SignalCellularAlt, contentDescription = null) },
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
                                        icon = { Icon(Icons.Outlined.Animation, contentDescription = null) },
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
                                showHomeApps = showHomeApps,
                                onShowHomeAppsChange = { showHomeApps = it; prefs.showHomeApps = it },
                                homeSectionOrder = homeSectionOrder,
                                onHomeSectionOrderChange = { homeSectionOrder = it; prefs.homeSectionOrder = it },
                                clockSectionWeight = clockSectionWeight,
                                onClockSectionWeightChange = { clockSectionWeight = it; prefs.clockSectionWeight = it },
                                homeSidePadding = homeSidePadding,
                                onHomeSidePaddingChange = { homeSidePadding = it; prefs.homeSidePaddingDp = it },
                                holdHome = holdHome
                            )
                        }
                        if (search.screenTime) {
                            ScreenTimeSection(
                                showScreenTime = showScreenTime,
                                onShowScreenTimeChange = { showScreenTime = it; prefs.showScreenTimeWidget = it },
                                showAppScreenTime = showAppScreenTime,
                                onShowAppScreenTimeChange = { showAppScreenTime = it; prefs.showAppScreenTime = it },
                                screenTimeSizeScale = screenTimeSizeScale,
                                onScreenTimeSizeScaleChange = { screenTimeSizeScale = it; prefs.screenTimeSizeScale = it },
                                snackbarHostState = snackbarHostState,
                                holdHome = holdHome
                            )
                        }
                        if (search.clock) {
                            ClockFormatSection(
                                use24HourClock = use24HourClock,
                                onUse24HourClockChange = { use24HourClock = it; prefs.use24HourClock = it },
                                showSeconds = showSeconds,
                                onShowSecondsChange = { showSeconds = it; prefs.showClockSeconds = it },
                                clockHorizontalAlignment = clockHorizontalAlignment,
                                onClockHorizontalAlignmentChange = { clockHorizontalAlignment = it; prefs.clockAlignment = it },
                                clockVerticalAlignment = clockVerticalAlignment,
                                onClockVerticalAlignmentChange = { clockVerticalAlignment = it; prefs.clockVerticalAlignment = it },
                                clockSizeScale = clockSizeScale,
                                onClockSizeScaleChange = { clockSizeScale = it; prefs.clockSizeScale = it },
                                dateSizeScale = dateSizeScale,
                                onDateSizeScaleChange = { dateSizeScale = it; prefs.dateSizeScale = it },
                                holdHome = holdHome
                            )
                            ClockStyleSection(
                                clockFontWeight = clockFontWeight,
                                onClockFontWeightChange = { clockFontWeight = it; prefs.clockFontWeight = it },
                                clockColor = clockColor,
                                onClockColorChange = { clockColor = it; prefs.clockColor = it },
                                clockOpacity = clockOpacity,
                                onClockOpacityChange = { clockOpacity = it; prefs.clockOpacity = it },
                                holdHome = holdHome
                            )
                        }
                        if (search.homeApps) {
                            HomeAppsLayoutSection(
                                maxHomeApps = maxHomeApps,
                                onMaxHomeAppsChange = { maxHomeApps = it; prefs.maxHomeApps = it },
                                appHorizontalAlignment = appHorizontalAlignment,
                                onAppHorizontalAlignmentChange = { appHorizontalAlignment = it; prefs.homeAlignment = it },
                                appVerticalAlignment = appVerticalAlignment,
                                onAppVerticalAlignmentChange = { appVerticalAlignment = it; prefs.homeVerticalAlignment = it },
                                appSpacing = appSpacing,
                                onAppSpacingChange = { appSpacing = it; prefs.appSpacingDp = it },
                                holdHome = holdHome
                            )
                            HomeAppsStyleSection(
                                homeTextSize = homeTextSize,
                                onHomeTextSizeChange = { homeTextSize = it; prefs.homeTextSizeScale = it },
                                homeAppsFontWeight = homeAppsFontWeight,
                                onHomeAppsFontWeightChange = { homeAppsFontWeight = it; prefs.homeAppsFontWeight = it },
                                homeAppsColor = homeAppsColor,
                                onHomeAppsColorChange = { homeAppsColor = it; prefs.homeAppsColor = it },
                                homeAppsOpacity = homeAppsOpacity,
                                onHomeAppsOpacityChange = { homeAppsOpacity = it; prefs.homeAppsOpacity = it },
                                holdHome = holdHome
                            )
                        }

                        // ── App drawer ──
                        if (search.appDrawer) {
                            SettingsGroupHeader(stringResource(R.string.settings_group_app_drawer))
                            AppDrawerListSection(
                                showAlphabetCategories = showAlphabetCategories,
                                onShowAlphabetCategoriesChange = { showAlphabetCategories = it; prefs.showAlphabetCategories = it },
                                autoShowKeyboard = autoShowKeyboard,
                                onAutoShowKeyboardChange = { autoShowKeyboard = it; prefs.autoShowKeyboard = it },
                                drawerTextSize = drawerTextSize,
                                onDrawerTextSizeChange = { drawerTextSize = it; prefs.appDrawerTextSizeScale = it },
                                showAppScreenTime = showAppScreenTime,
                                showScreenTime = showScreenTime,
                                onShowAppScreenTimeChange = { showAppScreenTime = it; prefs.showAppScreenTime = it },
                                holdDrawer = holdDrawer
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
                            SettingsGroupHeader(stringResource(R.string.settings_group_gestures_navigation))
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
                            SettingsGroupHeader(stringResource(R.string.settings_group_panels))
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

                        // ── About ──
                        if (search.about) {
                            SettingsGroupHeader(stringResource(R.string.settings_group_about))
                            AboutSection(onShowCredits = { showDeveloperInfo = true })
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
                } else {
                    val motion = rememberVoidMotionScheme(animationSpeed)
                    AnimatedContent(
                        targetState = currentCategory,
                        transitionSpec = {
                            if (targetState != null && initialState == null) {
                                (fadeIn(animationSpec = motion.defaultEffectsSpec()) +
                                    slideInHorizontally(animationSpec = motion.defaultSpatialSpec()) { it / 4 })
                                    .togetherWith(
                                        fadeOut(animationSpec = motion.defaultEffectsSpec()) +
                                            slideOutHorizontally(animationSpec = motion.defaultSpatialSpec()) { -it / 4 }
                                    )
                            } else if (targetState == null && initialState != null) {
                                (fadeIn(animationSpec = motion.defaultEffectsSpec()) +
                                    slideInHorizontally(animationSpec = motion.defaultSpatialSpec()) { -it / 4 })
                                    .togetherWith(
                                        fadeOut(animationSpec = motion.defaultEffectsSpec()) +
                                            slideOutHorizontally(animationSpec = motion.defaultSpatialSpec()) { it / 4 }
                                    )
                            } else {
                                fadeIn(animationSpec = motion.defaultEffectsSpec())
                                    .togetherWith(fadeOut(animationSpec = motion.defaultEffectsSpec()))
                            }
                        },
                        label = "SettingsCategoryContent"
                    ) { category ->
                        when (category) {
                            null -> {
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

                                    SettingsSection(
                                        title = null,
                                        items = listOf {
                                            SettingActionItem(
                                                title = stringResource(R.string.change_default_launcher),
                                                subtitle = stringResource(R.string.manage_default_home_settings),
                                                icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                                                external = true,
                                                onClick = {
                                                    context.startSafely(
                                                        Intent(android.provider.Settings.ACTION_HOME_SETTINGS),
                                                        fallback = Intent(android.provider.Settings.ACTION_SETTINGS)
                                                    )
                                                }
                                            )
                                        }
                                    )

                                    SettingsSection(
                                        title = null,
                                        items = listOf(
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.settings_group_appearance),
                                                    subtitle = stringResource(R.string.settings_category_appearance_subtitle),
                                                    icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.APPEARANCE }
                                                )
                                            },
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.settings_group_home),
                                                    subtitle = stringResource(R.string.settings_category_home_subtitle),
                                                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.HOME_SCREEN }
                                                )
                                            },
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.settings_group_app_drawer),
                                                    subtitle = stringResource(R.string.settings_category_app_drawer_subtitle),
                                                    icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.APP_DRAWER }
                                                )
                                            },
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.settings_group_gestures_navigation),
                                                    subtitle = stringResource(R.string.settings_category_gestures_subtitle),
                                                    icon = { Icon(Icons.Outlined.Swipe, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.GESTURES }
                                                )
                                            },
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.settings_group_panels),
                                                    subtitle = stringResource(R.string.settings_category_panels_subtitle),
                                                    icon = { Icon(Icons.Outlined.Widgets, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.PANELS }
                                                )
                                            },
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.permissions),
                                                    subtitle = stringResource(R.string.settings_category_permissions_subtitle),
                                                    icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.PERMISSIONS }
                                                )
                                            },
                                            {
                                                SettingActionItem(
                                                    title = stringResource(R.string.settings_group_about),
                                                    subtitle = stringResource(R.string.settings_category_about_subtitle),
                                                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                                                    onClick = { currentCategory = SettingsCategory.ABOUT }
                                                )
                                            }
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.APPEARANCE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.settings_group_appearance),
                                        onBack = { currentCategory = null }
                                    )
                                    FontStyleSection(appFont = appFont, onOpenFontPicker = { showFontPicker = true })
                                    SettingsSection(
                                        title = stringResource(R.string.settings_section_system_bars),
                                        items = listOf {
                                            SettingToggleItem(
                                                title = stringResource(R.string.notification_bar),
                                                subtitle = stringResource(R.string.settings_status_bar_subtitle),
                                                icon = { Icon(Icons.Outlined.SignalCellularAlt, contentDescription = null) },
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
                                                icon = { Icon(Icons.Outlined.Animation, contentDescription = null) },
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
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.HOME_SCREEN -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.settings_group_home),
                                        onBack = { currentCategory = null }
                                    )
                                    HomePreviewCard(state = previewState)
                                    HomeLayoutSection(
                                        showClock = showClock,
                                        onShowClockChange = { showClock = it; prefs.showClockWidget = it },
                                        showDate = showDate,
                                        onShowDateChange = { showDate = it; prefs.showDateWidget = it },
                                        showHomeApps = showHomeApps,
                                        onShowHomeAppsChange = { showHomeApps = it; prefs.showHomeApps = it },
                                        homeSectionOrder = homeSectionOrder,
                                        onHomeSectionOrderChange = { homeSectionOrder = it; prefs.homeSectionOrder = it },
                                        clockSectionWeight = clockSectionWeight,
                                        onClockSectionWeightChange = { clockSectionWeight = it; prefs.clockSectionWeight = it },
                                        homeSidePadding = homeSidePadding,
                                        onHomeSidePaddingChange = { homeSidePadding = it; prefs.homeSidePaddingDp = it },
                                        holdHome = holdHome
                                    )
                                    ClockFormatSection(
                                        use24HourClock = use24HourClock,
                                        onUse24HourClockChange = { use24HourClock = it; prefs.use24HourClock = it },
                                        showSeconds = showSeconds,
                                        onShowSecondsChange = { showSeconds = it; prefs.showClockSeconds = it },
                                        clockHorizontalAlignment = clockHorizontalAlignment,
                                        onClockHorizontalAlignmentChange = { clockHorizontalAlignment = it; prefs.clockAlignment = it },
                                        clockVerticalAlignment = clockVerticalAlignment,
                                        onClockVerticalAlignmentChange = { clockVerticalAlignment = it; prefs.clockVerticalAlignment = it },
                                        clockSizeScale = clockSizeScale,
                                        onClockSizeScaleChange = { clockSizeScale = it; prefs.clockSizeScale = it },
                                        dateSizeScale = dateSizeScale,
                                        onDateSizeScaleChange = { dateSizeScale = it; prefs.dateSizeScale = it },
                                        holdHome = holdHome
                                    )
                                    ClockStyleSection(
                                        clockFontWeight = clockFontWeight,
                                        onClockFontWeightChange = { clockFontWeight = it; prefs.clockFontWeight = it },
                                        clockColor = clockColor,
                                        onClockColorChange = { clockColor = it; prefs.clockColor = it },
                                        clockOpacity = clockOpacity,
                                        onClockOpacityChange = { clockOpacity = it; prefs.clockOpacity = it },
                                        holdHome = holdHome
                                    )
                                    HomeAppsLayoutSection(
                                        maxHomeApps = maxHomeApps,
                                        onMaxHomeAppsChange = { maxHomeApps = it; prefs.maxHomeApps = it },
                                        appHorizontalAlignment = appHorizontalAlignment,
                                        onAppHorizontalAlignmentChange = { appHorizontalAlignment = it; prefs.homeAlignment = it },
                                        appVerticalAlignment = appVerticalAlignment,
                                        onAppVerticalAlignmentChange = { appVerticalAlignment = it; prefs.homeVerticalAlignment = it },
                                        appSpacing = appSpacing,
                                        onAppSpacingChange = { appSpacing = it; prefs.appSpacingDp = it },
                                        holdHome = holdHome
                                    )
                                    HomeAppsStyleSection(
                                        homeTextSize = homeTextSize,
                                        onHomeTextSizeChange = { homeTextSize = it; prefs.homeTextSizeScale = it },
                                        homeAppsFontWeight = homeAppsFontWeight,
                                        onHomeAppsFontWeightChange = { homeAppsFontWeight = it; prefs.homeAppsFontWeight = it },
                                        homeAppsColor = homeAppsColor,
                                        onHomeAppsColorChange = { homeAppsColor = it; prefs.homeAppsColor = it },
                                        homeAppsOpacity = homeAppsOpacity,
                                        onHomeAppsOpacityChange = { homeAppsOpacity = it; prefs.homeAppsOpacity = it },
                                        holdHome = holdHome
                                    )
                                    ScreenTimeSection(
                                        showScreenTime = showScreenTime,
                                        onShowScreenTimeChange = { showScreenTime = it; prefs.showScreenTimeWidget = it },
                                        showAppScreenTime = showAppScreenTime,
                                        onShowAppScreenTimeChange = { showAppScreenTime = it; prefs.showAppScreenTime = it },
                                        screenTimeSizeScale = screenTimeSizeScale,
                                        onScreenTimeSizeScaleChange = { screenTimeSizeScale = it; prefs.screenTimeSizeScale = it },
                                        snackbarHostState = snackbarHostState,
                                        holdHome = holdHome
                                    )
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.APP_DRAWER -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.settings_group_app_drawer),
                                        onBack = { currentCategory = null }
                                    )
                                    AppDrawerListSection(
                                        showAlphabetCategories = showAlphabetCategories,
                                        onShowAlphabetCategoriesChange = { showAlphabetCategories = it; prefs.showAlphabetCategories = it },
                                        autoShowKeyboard = autoShowKeyboard,
                                        onAutoShowKeyboardChange = { autoShowKeyboard = it; prefs.autoShowKeyboard = it },
                                        drawerTextSize = drawerTextSize,
                                        onDrawerTextSizeChange = { drawerTextSize = it; prefs.appDrawerTextSizeScale = it },
                                        showAppScreenTime = showAppScreenTime,
                                        showScreenTime = showScreenTime,
                                        onShowAppScreenTimeChange = { showAppScreenTime = it; prefs.showAppScreenTime = it },
                                        holdDrawer = holdDrawer
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
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.GESTURES -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.settings_group_gestures_navigation),
                                        onBack = { currentCategory = null }
                                    )
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
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.PANELS -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.settings_group_panels),
                                        onBack = { currentCategory = null }
                                    )
                                    FeaturesSection(
                                        panels = EnabledPanels(enableSummary, enableWidgets, enableNotes),
                                        onEnableSummaryChange = { enableSummary = it; prefs.enableNotificationSummary = it },
                                        onEnableWidgetsChange = { enableWidgets = it; prefs.enableWidgets = it },
                                        onEnableNotesChange = { enableNotes = it; prefs.enableNotes = it },
                                        notesTitleAlignment = notesTitleAlignment,
                                        onNotesTitleAlignmentChange = { notesTitleAlignment = it; prefs.notesTitleAlignment = it },
                                        snackbarHostState = snackbarHostState
                                    )
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.PERMISSIONS -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.permissions),
                                        onBack = { currentCategory = null }
                                    )
                                    PermissionsSection()
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                            SettingsCategory.ABOUT -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                                ) {
                                    CategoryDetailHeader(
                                        title = stringResource(R.string.settings_group_about),
                                        onBack = { currentCategory = null }
                                    )
                                    AboutSection(onShowCredits = { showDeveloperInfo = true })
                                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing * 2))
                                }
                            }
                        }
                    }
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
                enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
                exit = fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
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

/** Header shown at the top of an individual settings category sub-screen. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryDetailHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 0.dp,
                end = VoidDimens.sectionSpacing - VoidDimens.iconButtonGlyphInset,
                bottom = VoidDimens.sectionSpacing
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back)
            )
        }
        Spacer(modifier = Modifier.width(VoidDimens.compactSpacing))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
    }
}

/** Starts [intent], or [fallback] when nothing handles it; a missing activity is never a crash. */
private fun Context.startSafely(intent: Intent, fallback: Intent? = null) {
    try {
        startActivity(intent)
    } catch (_: Exception) {
        if (fallback != null) {
            try {
                startActivity(fallback)
            } catch (_: Exception) {
                Log.w(TAG, "failed to start fallback activity")
            }
        } else {
            Log.w(TAG, "failed to start activity")
        }
    }
}

private data class SettingsSearchFlags(
    val defaultLauncher: Boolean,
    val appearance: Boolean,
    val homeLayout: Boolean,
    val screenTime: Boolean,
    val clock: Boolean,
    val homeApps: Boolean,
    val appDrawer: Boolean,
    val gestures: Boolean,
    val features: Boolean,
    val permissions: Boolean,
    val about: Boolean
) {
    val homeHeader: Boolean get() = homeLayout || screenTime || clock || homeApps
    val anyVisible: Boolean
        get() = defaultLauncher || appearance || homeHeader || appDrawer || gestures || features || permissions || about
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
        stringResource(R.string.settings_show_home_apps),
        stringResource(R.string.settings_section_order),
        stringResource(R.string.clock_first),
        stringResource(R.string.apps_first),
        stringResource(R.string.clock_section_weight),
        stringResource(R.string.settings_side_padding)
    )
    val screenTimeTerms = listOf(
        stringResource(R.string.screen_time),
        stringResource(R.string.settings_screen_time_home),
        stringResource(R.string.settings_screen_time_apps),
        stringResource(R.string.settings_screen_time_apps_subtitle),
        stringResource(R.string.perm_usage_access),
        stringResource(R.string.screen_time_size)
    )
    val clockTerms = listOf(
        stringResource(R.string.settings_section_clock),
        stringResource(R.string.settings_section_clock_style),
        stringResource(R.string.settings_24_hour),
        stringResource(R.string.settings_show_seconds),
        stringResource(R.string.settings_clock_horizontal),
        stringResource(R.string.settings_clock_vertical),
        stringResource(R.string.clock_size),
        stringResource(R.string.date_size),
        stringResource(R.string.settings_clock_weight),
        stringResource(R.string.settings_clock_color),
        stringResource(R.string.settings_clock_opacity)
    )
    val homeAppsTerms = listOf(
        stringResource(R.string.settings_section_home_apps),
        stringResource(R.string.settings_section_apps_layout),
        stringResource(R.string.settings_section_apps_style),
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
        add(stringResource(R.string.settings_screen_time_apps))
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
        stringResource(R.string.settings_group_panels),
        stringResource(R.string.swipe_action_notification_summary),
        stringResource(R.string.swipe_action_widgets),
        stringResource(R.string.swipe_action_notes),
        stringResource(R.string.settings_notes_title_alignment)
    )
    val permissionsTerms = listOf(
        stringResource(R.string.permissions_intro),
        stringResource(R.string.settings_section_core_permissions),
        stringResource(R.string.settings_section_feature_permissions),
        stringResource(R.string.perm_notification_listener),
        stringResource(R.string.perm_usage_access),
        stringResource(R.string.perm_accessibility),
        stringResource(R.string.perm_default_home),
        stringResource(R.string.perm_query_apps),
        stringResource(R.string.perm_widgets),
        stringResource(R.string.perm_private_space)
    )
    val aboutTerms = listOf(
        stringResource(R.string.about),
        stringResource(R.string.settings_group_about),
        stringResource(R.string.developer_credits),
        stringResource(R.string.credits_author),
        stringResource(R.string.credits_repository),
        stringResource(R.string.settings_source_code),
        stringResource(R.string.settings_version),
        stringResource(R.string.settings_system_settings)
    )
    val homeTitle = stringResource(R.string.settings_group_home)
    return SettingsSearchFlags(
        defaultLauncher = matchesSettingsQuery(
            query,
            listOf(stringResource(R.string.change_default_launcher), stringResource(R.string.manage_default_home_settings))
        ),
        appearance = group(stringResource(R.string.settings_group_appearance), appearanceTerms),
        homeLayout = group(homeTitle, homeLayoutTerms),
        screenTime = group(homeTitle, screenTimeTerms),
        clock = group(homeTitle, clockTerms),
        homeApps = group(homeTitle, homeAppsTerms),
        appDrawer = group(stringResource(R.string.settings_group_app_drawer), appDrawerTerms),
        gestures = group(stringResource(R.string.settings_group_gestures_navigation), gesturesTerms),
        features = group(stringResource(R.string.settings_group_panels), featuresTerms),
        permissions = group(stringResource(R.string.permissions), permissionsTerms),
        about = group(stringResource(R.string.settings_group_about), aboutTerms)
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
                CreditRow(Icons.Outlined.Person, stringResource(R.string.credits_author)) {
                    context.startSafely(Intent(Intent.ACTION_VIEW, "https://www.linkedin.com/in/knownassurajit/".toUri()))
                }
                CreditRow(Icons.Outlined.Code, stringResource(R.string.credits_repository)) {
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
            // A list row draws its trailing icon 16 dp inside its card, an icon button draws its glyph
            // 12 dp inside its target: the difference as end padding puts both glyphs on one edge.
            .padding(
                start = VoidDimens.compactSpacing,
                end = VoidDimens.sectionSpacing - VoidDimens.iconButtonGlyphInset,
                bottom = VoidDimens.sectionSpacing
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The name and the subtitle are one heading to a screen reader: "VOID, Settings".
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { heading() }
        ) {
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
                    icon = { Icon(Icons.Outlined.FontDownload, contentDescription = null) },
                    onClick = onOpenFontPicker
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_system_display),
                    subtitle = stringResource(R.string.settings_system_display_subtitle),
                    icon = { Icon(Icons.Outlined.Brightness4, contentDescription = null) },
                    external = true,
                    onClick = { context.startSafely(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)) }
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_system_wallpaper),
                    subtitle = stringResource(R.string.settings_system_wallpaper_subtitle),
                    icon = { Icon(Icons.Outlined.Wallpaper, contentDescription = null) },
                    external = true,
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
    showHomeApps: Boolean,
    onShowHomeAppsChange: (Boolean) -> Unit,
    homeSectionOrder: String,
    onHomeSectionOrderChange: (String) -> Unit,
    clockSectionWeight: Float,
    onClockSectionWeightChange: (Float) -> Unit,
    homeSidePadding: Float,
    onHomeSidePaddingChange: (Float) -> Unit,
    holdHome: (Boolean) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_home_layout),
        items = listOf(
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_show_clock),
                    subtitle = stringResource(R.string.settings_show_clock_subtitle),
                    icon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) },
                    checked = showClock,
                    onCheckedChange = onShowClockChange
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.show_date_time),
                    subtitle = stringResource(R.string.settings_show_date_subtitle),
                    icon = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
                    checked = showDate,
                    onCheckedChange = onShowDateChange
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_show_home_apps),
                    subtitle = stringResource(R.string.settings_show_home_apps_subtitle),
                    icon = { Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = null) },
                    checked = showHomeApps,
                    onCheckedChange = onShowHomeAppsChange
                )
            },
            {
                SettingChoiceItem(
                    title = stringResource(R.string.settings_section_order),
                    icon = { Icon(Icons.Outlined.SwapVert, contentDescription = null) },
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
                    icon = { Icon(Icons.Outlined.HorizontalSplit, contentDescription = null) },
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
                    icon = { Icon(Icons.Outlined.WidthNormal, contentDescription = null) },
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

@Composable
private fun ClockFormatSection(
    use24HourClock: Boolean,
    onUse24HourClockChange: (Boolean) -> Unit,
    showSeconds: Boolean,
    onShowSecondsChange: (Boolean) -> Unit,
    clockHorizontalAlignment: Int,
    onClockHorizontalAlignmentChange: (Int) -> Unit,
    clockVerticalAlignment: Int,
    onClockVerticalAlignmentChange: (Int) -> Unit,
    clockSizeScale: Float,
    onClockSizeScaleChange: (Float) -> Unit,
    dateSizeScale: Float,
    onDateSizeScaleChange: (Float) -> Unit,
    holdHome: (Boolean) -> Unit
) {
    val rows: SettingRows = mutableListOf()
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.settings_24_hour),
            subtitle = stringResource(R.string.settings_24_hour_subtitle),
            icon = { Icon(Icons.Outlined.AccessTime, contentDescription = null) },
            checked = use24HourClock,
            onCheckedChange = onUse24HourClockChange
        )
    }
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.settings_show_seconds),
            subtitle = stringResource(R.string.settings_show_seconds_subtitle),
            icon = { Icon(Icons.Outlined.Timer, contentDescription = null) },
            checked = showSeconds,
            onCheckedChange = onShowSecondsChange
        )
    }
    rows += {
        SettingChoiceItem(
            title = stringResource(R.string.settings_clock_horizontal),
            icon = { Icon(Icons.AutoMirrored.Outlined.FormatAlignLeft, contentDescription = null) },
            selected = clockHorizontalAlignment,
            options = horizontalOptions(),
            onSelected = onClockHorizontalAlignmentChange
        )
    }
    rows += {
        SettingChoiceItem(
            title = stringResource(R.string.settings_clock_vertical),
            icon = { Icon(Icons.Outlined.VerticalAlignCenter, contentDescription = null) },
            selected = clockVerticalAlignment,
            options = verticalOptions(),
            onSelected = onClockVerticalAlignmentChange
        )
    }
    rows += {
        SettingSliderItem(
            title = stringResource(R.string.clock_size),
            subtitle = stringResource(R.string.settings_clock_size_subtitle),
            icon = { Icon(Icons.Outlined.TextFields, contentDescription = null) },
            value = clockSizeScale,
            range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
            valueText = ::scaleText,
            onHoldingChanged = holdHome,
            onChanged = onClockSizeScaleChange
        )
    }
    rows += {
        SettingSliderItem(
            title = stringResource(R.string.date_size),
            subtitle = stringResource(R.string.settings_date_size_subtitle),
            icon = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
            value = dateSizeScale,
            range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
            valueText = ::scaleText,
            onHoldingChanged = holdHome,
            onChanged = onDateSizeScaleChange
        )
    }
    SettingsSection(stringResource(R.string.settings_section_clock), rows)
}

@Composable
private fun ClockStyleSection(
    clockFontWeight: Int,
    onClockFontWeightChange: (Int) -> Unit,
    clockColor: String,
    onClockColorChange: (String) -> Unit,
    clockOpacity: Float,
    onClockOpacityChange: (Float) -> Unit,
    holdHome: (Boolean) -> Unit
) {
    val rows: SettingRows = mutableListOf()
    addTextStyleRows(
        rows = rows,
        style = TextStyleRows(R.string.settings_clock_weight, R.string.settings_clock_color, R.string.settings_clock_opacity),
        fontWeight = clockFontWeight,
        color = clockColor,
        opacity = clockOpacity,
        onFontWeightChange = onClockFontWeightChange,
        onColorChange = onClockColorChange,
        onOpacityChange = onClockOpacityChange,
        holdHome = holdHome
    )
    SettingsSection(stringResource(R.string.settings_section_clock_style), rows)
}

@Composable
private fun HomeAppsLayoutSection(
    maxHomeApps: Int,
    onMaxHomeAppsChange: (Int) -> Unit,
    appHorizontalAlignment: Int,
    onAppHorizontalAlignmentChange: (Int) -> Unit,
    appVerticalAlignment: Int,
    onAppVerticalAlignmentChange: (Int) -> Unit,
    appSpacing: Float,
    onAppSpacingChange: (Float) -> Unit,
    holdHome: (Boolean) -> Unit
) {
    val rows: SettingRows = mutableListOf()
    rows += {
        SettingCounterItem(
            title = stringResource(R.string.settings_max_home_apps),
            subtitle = stringResource(R.string.settings_max_home_apps_subtitle),
            icon = { Icon(Icons.Outlined.FormatListNumbered, contentDescription = null) },
            value = maxHomeApps,
            range = 1..10,
            onChange = onMaxHomeAppsChange
        )
    }
    rows += {
        SettingChoiceItem(
            title = stringResource(R.string.settings_apps_horizontal),
            icon = { Icon(Icons.AutoMirrored.Outlined.FormatAlignLeft, contentDescription = null) },
            selected = appHorizontalAlignment,
            options = horizontalOptions(),
            onSelected = onAppHorizontalAlignmentChange
        )
    }
    rows += {
        SettingChoiceItem(
            title = stringResource(R.string.settings_apps_vertical),
            icon = { Icon(Icons.Outlined.VerticalAlignCenter, contentDescription = null) },
            selected = appVerticalAlignment,
            options = verticalOptions(),
            onSelected = onAppVerticalAlignmentChange
        )
    }
    rows += {
        SettingSliderItem(
            title = stringResource(R.string.settings_app_spacing),
            subtitle = stringResource(R.string.settings_app_spacing_subtitle),
            icon = { Icon(Icons.Outlined.FormatLineSpacing, contentDescription = null) },
            value = appSpacing,
            range = 0f..HomeStyle.MAX_APP_SPACING,
            valueText = { it.roundToInt().toString() },
            onHoldingChanged = holdHome,
            onChanged = onAppSpacingChange
        )
    }
    SettingsSection(stringResource(R.string.settings_section_apps_layout), rows)
}

@Composable
private fun HomeAppsStyleSection(
    homeTextSize: Float,
    onHomeTextSizeChange: (Float) -> Unit,
    homeAppsFontWeight: Int,
    onHomeAppsFontWeightChange: (Int) -> Unit,
    homeAppsColor: String,
    onHomeAppsColorChange: (String) -> Unit,
    homeAppsOpacity: Float,
    onHomeAppsOpacityChange: (Float) -> Unit,
    holdHome: (Boolean) -> Unit
) {
    val rows: SettingRows = mutableListOf()
    rows += {
        SettingSliderItem(
            title = stringResource(R.string.home_text_size),
            subtitle = stringResource(R.string.settings_home_text_size_subtitle),
            icon = { Icon(Icons.Outlined.TextFields, contentDescription = null) },
            value = homeTextSize,
            range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_APP_SIZE_SCALE,
            valueText = ::scaleText,
            onHoldingChanged = holdHome,
            onChanged = onHomeTextSizeChange
        )
    }
    addTextStyleRows(
        rows = rows,
        style = TextStyleRows(R.string.settings_apps_weight, R.string.settings_apps_color, R.string.settings_apps_opacity),
        fontWeight = homeAppsFontWeight,
        color = homeAppsColor,
        opacity = homeAppsOpacity,
        onFontWeightChange = onHomeAppsFontWeightChange,
        onColorChange = onHomeAppsColorChange,
        onOpacityChange = onHomeAppsOpacityChange,
        holdHome = holdHome
    )
    SettingsSection(stringResource(R.string.settings_section_apps_style), rows)
}

@Composable
private fun AppDrawerListSection(
    showAlphabetCategories: Boolean,
    onShowAlphabetCategoriesChange: (Boolean) -> Unit,
    autoShowKeyboard: Boolean,
    onAutoShowKeyboardChange: (Boolean) -> Unit,
    drawerTextSize: Float,
    onDrawerTextSizeChange: (Float) -> Unit,
    showAppScreenTime: Boolean,
    showScreenTime: Boolean,
    onShowAppScreenTimeChange: (Boolean) -> Unit,
    holdDrawer: (Boolean) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.settings_section_app_list),
        items = listOf(
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_show_alphabet),
                    subtitle = stringResource(R.string.settings_show_alphabet_subtitle),
                    icon = { Icon(Icons.Outlined.SortByAlpha, contentDescription = null) },
                    checked = showAlphabetCategories,
                    onCheckedChange = onShowAlphabetCategoriesChange
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.auto_show_keyboard),
                    subtitle = stringResource(R.string.settings_auto_keyboard_subtitle),
                    icon = { Icon(Icons.Outlined.Keyboard, contentDescription = null) },
                    checked = autoShowKeyboard,
                    onCheckedChange = onAutoShowKeyboardChange
                )
            },
            {
                SettingSliderItem(
                    title = stringResource(R.string.app_drawer_text_size),
                    subtitle = stringResource(R.string.settings_drawer_text_size_subtitle),
                    icon = { Icon(Icons.Outlined.TextFields, contentDescription = null) },
                    value = drawerTextSize,
                    range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_APP_SIZE_SCALE,
                    valueText = ::scaleText,
                    onHoldingChanged = holdDrawer,
                    onChanged = onDrawerTextSizeChange
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_screen_time_apps),
                    subtitle = stringResource(R.string.settings_screen_time_apps_subtitle),
                    icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                    checked = showAppScreenTime,
                    enabled = showScreenTime,
                    onCheckedChange = onShowAppScreenTimeChange
                )
            }
        )
    )
}

/**
 * Everything about screen time in one place: the total under the clock, the per-app figures in the
 * app library, the usage access they depend on, and the size of the Home line. The library switch is
 * off-limits while screen time itself is off, because the library shows nothing then.
 */
@Composable
private fun ScreenTimeSection(
    showScreenTime: Boolean,
    onShowScreenTimeChange: (Boolean) -> Unit,
    showAppScreenTime: Boolean,
    onShowAppScreenTimeChange: (Boolean) -> Unit,
    screenTimeSizeScale: Float,
    onScreenTimeSizeScaleChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState,
    holdHome: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usageRedirected = stringResource(R.string.settings_usage_access_redirected)
    val screenTimeEnabled = stringResource(R.string.settings_screen_time_enabled)
    // The system screen the user comes back from changes this, so it is read again on every resume.
    var usageAccessGranted by remember { mutableStateOf(FeatureAvailability.isUsageAccessGranted(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        usageAccessGranted = FeatureAvailability.isUsageAccessGranted(context)
    }
    SettingsSection(
        title = stringResource(R.string.screen_time),
        description = stringResource(R.string.settings_screen_time_description),
        items = listOf(
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_screen_time_home),
                    subtitle = stringResource(R.string.settings_screen_time_subtitle),
                    icon = { Icon(Icons.Outlined.Timelapse, contentDescription = null) },
                    checked = showScreenTime,
                    onCheckedChange = { enabled ->
                        onShowScreenTimeChange(enabled)
                        if (!enabled) return@SettingToggleItem
                        if (usageAccessGranted) {
                            scope.launch { snackbarHostState.showSnackbar(screenTimeEnabled) }
                        } else {
                            Log.i(TAG, "screen_time_usage_access_requested")
                            context.startSafely(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            scope.launch { snackbarHostState.showSnackbar(usageRedirected) }
                        }
                    }
                )
            },
            {
                SettingToggleItem(
                    title = stringResource(R.string.settings_screen_time_apps),
                    subtitle = stringResource(R.string.settings_screen_time_apps_subtitle),
                    icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                    checked = showAppScreenTime,
                    enabled = showScreenTime,
                    onCheckedChange = onShowAppScreenTimeChange
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.perm_usage_access),
                    subtitle = stringResource(
                        if (usageAccessGranted) R.string.settings_screen_time_access_granted else R.string.settings_screen_time_access_needed
                    ),
                    icon = { Icon(Icons.Outlined.QueryStats, contentDescription = null) },
                    external = true,
                    onClick = { context.startSafely(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
                )
            },
            {
                SettingSliderItem(
                    title = stringResource(R.string.screen_time_size),
                    subtitle = stringResource(R.string.settings_screen_time_size_subtitle),
                    icon = { Icon(Icons.Outlined.TextFields, contentDescription = null) },
                    value = screenTimeSizeScale,
                    range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
                    valueText = ::scaleText,
                    onHoldingChanged = holdHome,
                    onChanged = onScreenTimeSizeScaleChange
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
            icon = { Icon(Icons.Outlined.FormatBold, contentDescription = null) },
            selected = fontWeight,
            options = HomeStyle.fontWeights.map { weight -> weight to stringResource(weight.weightLabelRes()) },
            onSelected = onFontWeightChange
        )
    }
    rows += {
        SettingColorItem(
            title = stringResource(style.colorTitle),
            subtitle = "",
            icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
            color = HomeColor.parse(color),
            onPick = { onColorChange(it.key) }
        )
    }
    rows += {
        SettingSliderItem(
            title = stringResource(style.opacityTitle),
            subtitle = "",
            icon = { Icon(Icons.Outlined.Opacity, contentDescription = null) },
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
            icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
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
                icon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
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
                icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
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
            icon = { Icon(Icons.Outlined.Swipe, contentDescription = null) },
            checked = enableGestures,
            onCheckedChange = onEnableGesturesChange
        )
    }
    if (enableGestures) {
        rows += {
            SwipeActionSelector(
                label = stringResource(R.string.left_swipe_action),
                hint = stringResource(R.string.swipe_left_hint),
                icon = { Icon(Icons.Outlined.SwipeLeft, contentDescription = null) },
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
                icon = { Icon(Icons.Outlined.SwipeRight, contentDescription = null) },
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
            icon = { Icon(Icons.Outlined.SwipeDown, contentDescription = null) },
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
                icon = { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) },
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
            icon = { Icon(Icons.Outlined.Summarize, contentDescription = null) },
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
            icon = { Icon(Icons.Outlined.Widgets, contentDescription = null) },
            checked = panels.widgets,
            onCheckedChange = onEnableWidgetsChange
        )
    }
    rows += {
        SettingToggleItem(
            title = stringResource(R.string.swipe_action_notes),
            subtitle = stringResource(R.string.settings_notes_subtitle),
            icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
            checked = panels.notes,
            onCheckedChange = onEnableNotesChange
        )
    }
    if (panels.notes) {
        rows += {
            SettingChoiceItem(
                title = stringResource(R.string.settings_notes_title_alignment),
                icon = { Icon(Icons.AutoMirrored.Outlined.FormatAlignLeft, contentDescription = null) },
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
            external = true,
            onClick = { context.startSafely(intent(), fallback) }
        )
    }
    SettingsSection(
        title = stringResource(R.string.settings_section_core_permissions),
        description = stringResource(R.string.permissions_intro),
        items = listOf(
            action(R.string.perm_default_home, R.string.perm_default_home_body, Icons.Outlined.Home, {
                Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            }),
            action(R.string.settings_app_info, R.string.settings_app_info_subtitle, Icons.Outlined.AppSettingsAlt, { Intent(appDetails) }),
            action(R.string.perm_query_apps, R.string.perm_query_apps_body, Icons.Outlined.Apps, { Intent(appDetails) })
        )
    )
    SettingsSection(
        title = stringResource(R.string.settings_section_feature_permissions),
        items = listOf(
            action(R.string.perm_notification_listener, R.string.perm_notification_listener_body, Icons.Outlined.Notifications, {
                Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }),
            action(R.string.perm_usage_access, R.string.perm_usage_access_body, Icons.Outlined.QueryStats, {
                Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
            }),
            action(R.string.perm_accessibility, R.string.perm_accessibility_body, Icons.Outlined.Accessibility, {
                Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            }),
            action(R.string.perm_widgets, R.string.perm_widgets_body, Icons.Outlined.Widgets, { Intent(appDetails) }),
            action(
                R.string.perm_private_space,
                R.string.perm_private_space_body,
                Icons.Outlined.Security,
                { Intent("android.settings.PRIVATE_SPACE_SETTINGS") },
                fallback = Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
            )
        )
    )
}

@Composable
private fun AboutSection(onShowCredits: () -> Unit) {
    val context = LocalContext.current
    SettingsSection(
        title = stringResource(R.string.settings_system_settings),
        items = listOf(
            {
                SettingActionItem(
                    title = stringResource(R.string.change_default_launcher),
                    subtitle = stringResource(R.string.manage_default_home_settings),
                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    external = true,
                    onClick = {
                        context.startSafely(
                            Intent(android.provider.Settings.ACTION_HOME_SETTINGS),
                            fallback = Intent(android.provider.Settings.ACTION_SETTINGS)
                        )
                    }
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_system_settings),
                    subtitle = stringResource(R.string.settings_app_info_subtitle),
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    external = true,
                    onClick = { context.startSafely(Intent(android.provider.Settings.ACTION_SETTINGS)) }
                )
            }
        )
    )
    SettingsSection(
        title = stringResource(R.string.about),
        items = listOf(
            {
                SettingActionItem(
                    title = stringResource(R.string.developer_credits),
                    subtitle = stringResource(R.string.settings_credits_subtitle),
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    onClick = onShowCredits
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.credits_repository),
                    subtitle = stringResource(R.string.settings_source_code_subtitle),
                    icon = { Icon(Icons.Outlined.Code, contentDescription = null) },
                    external = true,
                    onClick = {
                        context.startSafely(Intent(Intent.ACTION_VIEW, "https://github.com/knownassurajit/void".toUri()))
                    }
                )
            },
            {
                SettingActionItem(
                    title = stringResource(R.string.settings_version),
                    subtitle = BuildConfig.VERSION_NAME,
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    trailing = null,
                    onClick = {}
                )
            }
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
