package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.Gravity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
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
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
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
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.FontPicker
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSearchField
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavMotion
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.rememberFontResolution
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.rememberVoidMotionScheme
import com.knownassurajit.app.launcher.voidlauncher.MainUiState
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.HomePreviewContent
import com.knownassurajit.app.launcher.voidlauncher.ui.components.ScaledScreenPreview
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingsPreviewPane
import com.knownassurajit.app.launcher.voidlauncher.ui.components.SettingsPreviewTarget
import com.knownassurajit.app.launcher.voidlauncher.ui.components.weightLabelRes
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeStyle
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeTextColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.resolve
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.FilterChipDefaults
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

val LocalSettingsPrefs = staticCompositionLocalOf<Prefs> { error("LocalSettingsPrefs missing") }

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
    var clockSectionWeight by remember { mutableFloatStateOf(prefs.clockSectionWeight) }
    var clockSizeScale by remember { mutableFloatStateOf(prefs.clockSizeScale) }
    var dateSizeScale by remember { mutableFloatStateOf(prefs.dateSizeScale) }
    var screenTimeSizeScale by remember { mutableFloatStateOf(prefs.screenTimeSizeScale) }
    var showHomeApps by remember { mutableStateOf(prefs.showHomeApps) }
    var homeSectionOrder by remember { mutableStateOf(prefs.homeSectionOrder) }
    var privateSpaceEnabled by remember { mutableStateOf(prefs.privateSpaceEnabled) }
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
    var animationSpeed by remember { mutableStateOf(prefs.animationSpeed) }
    var contentSwipeToBack by remember { mutableStateOf(prefs.contentSwipeToBack) }
    var privateSpacePlacement by remember { mutableStateOf(prefs.privateSpacePlacement) }
    var searchQuery by remember { mutableStateOf("") }

    val isPrivateSpaceConfigured = remember {
        PrivateSpaceHelper.getPrivateSpaceProfile(context) != null
    }
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
    val onHoldingPreview: (SettingsPreviewTarget?) -> Unit = { activePreviewTarget = it }

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
                contentWindowInsets = WindowInsets(0.dp),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { paddingValues ->
                val search = EvaluateSettingsSearch(searchQuery, privateSpaceAvailable)
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

                    VoidSearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        hint = stringResource(R.string.settings_search_hint),
                        leadingIconDescription = stringResource(R.string.search)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (search.defaultLauncher) {
                        SettingActionItem(
                            title = stringResource(R.string.change_default_launcher),
                            subtitle = stringResource(R.string.manage_default_home_settings),
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
                    }

                    if (search.appearanceHeader) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_appearance))
                    }
                    if (search.appearance) {
                        AestheticsSection(
                            appFont = appFont,
                            onOpenFontPicker = { showFontPicker = true }
                        )
                        AppearanceSection(
                            showStatusBar = showStatusBar,
                            onShowStatusBarChange = { showStatusBar = it }
                        )
                    }

                    if (search.homeHeader) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_home))
                        HomePreviewCard(state = previewState)
                    }
                    if (search.homeLayout) {
                        HomeLayoutSection(
                            showClock = showClock,
                            onShowClockChange = { showClock = it },
                            showDate = showDate,
                            onShowDateChange = { showDate = it },
                            showScreenTime = showScreenTime,
                            onShowScreenTimeChange = { showScreenTime = it },
                            showHomeApps = showHomeApps,
                            onShowHomeAppsChange = { showHomeApps = it },
                            homeSectionOrder = homeSectionOrder,
                            onHomeSectionOrderChange = { homeSectionOrder = it },
                            clockSectionWeight = clockSectionWeight,
                            onClockSectionWeightChange = { clockSectionWeight = it },
                            homeSidePadding = homeSidePadding,
                            onHomeSidePaddingChange = { homeSidePadding = it },
                            snackbarHostState = snackbarHostState,
                            onHoldingPreview = onHoldingPreview
                        )
                    }
                    if (search.clock) {
                        ClockAndDateSection(
                            use24HourClock = use24HourClock,
                            onUse24HourClockChange = { use24HourClock = it },
                            showSeconds = showSeconds,
                            onShowSecondsChange = { showSeconds = it },
                            clockHorizontalAlignment = clockHorizontalAlignment,
                            onClockHorizontalAlignmentChange = { clockHorizontalAlignment = it },
                            clockVerticalAlignment = clockVerticalAlignment,
                            onClockVerticalAlignmentChange = { clockVerticalAlignment = it },
                            clockSizeScale = clockSizeScale,
                            onClockSizeScaleChange = { clockSizeScale = it },
                            dateSizeScale = dateSizeScale,
                            onDateSizeScaleChange = { dateSizeScale = it },
                            screenTimeSizeScale = screenTimeSizeScale,
                            onScreenTimeSizeScaleChange = { screenTimeSizeScale = it },
                            textStyle = HomeTextStyleChoice(clockFontWeight, clockColor, clockOpacity),
                            onFontWeightChange = { clockFontWeight = it; prefs.clockFontWeight = it },
                            onColorChange = { clockColor = it; prefs.clockColor = it },
                            onOpacityChange = { clockOpacity = it; prefs.clockOpacity = it },
                            onHoldingPreview = onHoldingPreview
                        )
                    }
                    if (search.homeApps) {
                        HomeAppsSection(
                            maxHomeApps = maxHomeApps,
                            onMaxHomeAppsChange = { maxHomeApps = it },
                            appHorizontalAlignment = appHorizontalAlignment,
                            onAppHorizontalAlignmentChange = { appHorizontalAlignment = it },
                            appVerticalAlignment = appVerticalAlignment,
                            onAppVerticalAlignmentChange = { appVerticalAlignment = it },
                            appSpacing = appSpacing,
                            onAppSpacingChange = { appSpacing = it },
                            homeTextSize = homeTextSize,
                            onHomeTextSizeChange = { homeTextSize = it },
                            textStyle = HomeTextStyleChoice(homeAppsFontWeight, homeAppsColor, homeAppsOpacity),
                            onFontWeightChange = { homeAppsFontWeight = it; prefs.homeAppsFontWeight = it },
                            onColorChange = { homeAppsColor = it; prefs.homeAppsColor = it },
                            onOpacityChange = { homeAppsOpacity = it; prefs.homeAppsOpacity = it },
                            onHoldingPreview = onHoldingPreview
                        )
                    }

                    if (search.appDrawerHeader) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_app_drawer))
                    }
                    if (search.appDrawer) {
                        AppDrawerSection(
                            showAlphabetCategories = showAlphabetCategories,
                            onShowAlphabetCategoriesChange = { showAlphabetCategories = it },
                            autoShowKeyboard = autoShowKeyboard,
                            onAutoShowKeyboardChange = { autoShowKeyboard = it },
                            drawerTextSize = drawerTextSize,
                            onDrawerTextSizeChange = { drawerTextSize = it },
                            onHoldingPreview = onHoldingPreview
                        )
                        PrivateSpaceSection(
                            privateSpaceEnabled = privateSpaceEnabled,
                            onPrivateSpaceEnabledChange = { privateSpaceEnabled = it },
                            privateSpacePlacement = privateSpacePlacement,
                            onPrivateSpacePlacementChange = { privateSpacePlacement = it },
                            isPrivateSpaceConfigured = isPrivateSpaceConfigured,
                            privateSpaceAvailable = privateSpaceAvailable,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    if (search.gesturesHeader) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_gestures_motion))
                    }
                    if (search.gestures) {
                        GesturesSection(
                            enableGestures = enableGestures,
                            onEnableGesturesChange = { enableGestures = it },
                            enableSwipeDownNotifications = enableSwipeDownNotifications,
                            onEnableSwipeDownNotificationsChange = { enableSwipeDownNotifications = it },
                            enableSummary = enableSummary,
                            enableWidgets = enableWidgets,
                            enableNotes = enableNotes,
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
                            animationSpeed = animationSpeed,
                            onAnimationSpeedChange = { animationSpeed = it },
                            contentSwipeToBack = contentSwipeToBack,
                            onContentSwipeToBackChange = { contentSwipeToBack = it },
                            snackbarHostState = snackbarHostState
                        )
                    }

                    if (search.featuresHeader) {
                        SettingsGroupHeader(stringResource(R.string.settings_group_features))
                    }
                    if (search.features) {
                        FeaturesSection(
                            enableSummary = enableSummary,
                            onEnableSummaryChange = { enableSummary = it },
                            enableWidgets = enableWidgets,
                            onEnableWidgetsChange = { enableWidgets = it },
                            enableNotes = enableNotes,
                            onEnableNotesChange = { enableNotes = it },
                            snackbarHostState = snackbarHostState
                        )
                    }

                    if (search.permissionsHeader) {
                        SettingsGroupHeader(stringResource(R.string.permissions))
                    }
                    if (search.permissions) {
                        PermissionsSection()
                    }

                    if (!search.anyVisible) {
                        Text(
                            text = stringResource(R.string.settings_no_matches),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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
    val appearanceHeader: Boolean get() = appearance
    val homeHeader: Boolean get() = homeLayout || clock || homeApps
    val appDrawerHeader: Boolean get() = appDrawer
    val gesturesHeader: Boolean get() = gestures
    val featuresHeader: Boolean get() = features
    val permissionsHeader: Boolean get() = permissions
    val anyVisible: Boolean
        get() = defaultLauncher || appearanceHeader || homeHeader || appDrawerHeader ||
            gesturesHeader || featuresHeader || permissionsHeader
}

@Composable
private fun EvaluateSettingsSearch(
    query: String,
    privateSpaceAvailable: Boolean
): SettingsSearchFlags {
    val appearanceGroup = matchesSettingsQuery(query, listOf(stringResource(R.string.settings_group_appearance)))
    val homeGroup = matchesSettingsQuery(query, listOf(stringResource(R.string.settings_group_home)))
    val appDrawerGroup = matchesSettingsQuery(query, listOf(stringResource(R.string.settings_group_app_drawer)))
    val gesturesGroup = matchesSettingsQuery(query, listOf(stringResource(R.string.settings_group_gestures_motion)))
    val featuresGroup = matchesSettingsQuery(query, listOf(stringResource(R.string.settings_group_features)))
    val permissionsGroup = matchesSettingsQuery(query, listOf(stringResource(R.string.permissions)))

    val appearanceTerms = listOf(
        stringResource(R.string.settings_section_font_style),
        stringResource(R.string.settings_launcher_font),
        stringResource(R.string.settings_system_display),
        stringResource(R.string.settings_system_wallpaper),
        stringResource(R.string.settings_section_system_bars),
        stringResource(R.string.notification_bar),
        stringResource(R.string.settings_status_bar_subtitle)
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
        stringResource(R.string.settings_side_padding),
        stringResource(R.string.settings_side_padding_subtitle)
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
        stringResource(R.string.settings_font_weight),
        stringResource(R.string.settings_text_color),
        stringResource(R.string.settings_opacity)
    )
    val homeAppsTerms = listOf(
        stringResource(R.string.settings_section_home_apps),
        stringResource(R.string.settings_max_home_apps),
        stringResource(R.string.settings_apps_horizontal),
        stringResource(R.string.settings_apps_vertical),
        stringResource(R.string.settings_app_spacing),
        stringResource(R.string.home_text_size),
        stringResource(R.string.text_size),
        stringResource(R.string.settings_font_weight),
        stringResource(R.string.settings_text_color),
        stringResource(R.string.settings_opacity)
    )
    val appDrawerTerms = buildList {
        add(stringResource(R.string.settings_show_alphabet))
        add(stringResource(R.string.auto_show_keyboard))
        add(stringResource(R.string.app_drawer_text_size))
        add(stringResource(R.string.text_size))
        add(stringResource(R.string.private_space))
        add(stringResource(R.string.settings_enable_private_space))
        if (privateSpaceAvailable) {
            add(stringResource(R.string.private_space_placement))
            add(stringResource(R.string.private_space_placement_subtitle))
            add(stringResource(R.string.private_space_placement_bottom))
            add(stringResource(R.string.private_space_placement_search_bar))
        }
    }
    val gesturesTerms = listOf(
        stringResource(R.string.gestures),
        "Enable Gestures",
        "Activate screen swipes",
        stringResource(R.string.swipe_down_notifications),
        stringResource(R.string.swipe_down_notifications_subtitle),
        stringResource(R.string.left_swipe_action),
        "Assign left swipe behavior",
        stringResource(R.string.right_swipe_action),
        "Assign right swipe behavior",
        stringResource(R.string.animation_speed),
        stringResource(R.string.animation_speed_subtitle),
        stringResource(R.string.animation_speed_fast),
        stringResource(R.string.animation_speed_standard),
        stringResource(R.string.animation_speed_slow),
        stringResource(R.string.content_swipe_to_back),
        stringResource(R.string.content_swipe_to_back_subtitle),
        "Notification Summary",
        "Widgets",
        "Notes",
        "System Dropdown",
        "Open App",
        "Accessibility Action",
        "None"
    )
    val featuresTerms = listOf(
        stringResource(R.string.swipe_action_notification_summary),
        stringResource(R.string.swipe_action_widgets),
        stringResource(R.string.swipe_action_notes)
    )
    val permissionsTerms = listOf(
        stringResource(R.string.permissions),
        stringResource(R.string.permissions_intro),
        stringResource(R.string.perm_notification_listener),
        stringResource(R.string.perm_notification_listener_body),
        stringResource(R.string.perm_usage_access),
        stringResource(R.string.perm_usage_access_body),
        stringResource(R.string.perm_accessibility),
        stringResource(R.string.perm_accessibility_body),
        stringResource(R.string.perm_default_home),
        stringResource(R.string.perm_default_home_body),
        stringResource(R.string.perm_query_apps),
        stringResource(R.string.perm_query_apps_body),
        stringResource(R.string.perm_widgets),
        stringResource(R.string.perm_widgets_body),
        stringResource(R.string.perm_private_space),
        stringResource(R.string.perm_private_space_body)
    )

    return SettingsSearchFlags(
        defaultLauncher = matchesSettingsQuery(
            query,
            listOf(
                stringResource(R.string.change_default_launcher),
                stringResource(R.string.manage_default_home_settings)
            )
        ),
        appearance = appearanceGroup || matchesSettingsQuery(query, appearanceTerms),
        homeLayout = homeGroup || matchesSettingsQuery(query, homeLayoutTerms),
        clock = homeGroup || matchesSettingsQuery(query, clockTerms),
        homeApps = homeGroup || matchesSettingsQuery(query, homeAppsTerms),
        appDrawer = appDrawerGroup || matchesSettingsQuery(query, appDrawerTerms),
        gestures = gesturesGroup || matchesSettingsQuery(query, gesturesTerms),
        features = featuresGroup || matchesSettingsQuery(query, featuresTerms),
        permissions = permissionsGroup || matchesSettingsQuery(query, permissionsTerms)
    )
}

private fun matchesSettingsQuery(query: String, terms: List<String>): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    return terms.any { it.contains(needle, ignoreCase = true) }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(
            top = VoidDimens.sectionSpacing,
            bottom = VoidDimens.compactSpacing
        )
    )
}

@Composable
private fun DeveloperInfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.developer_credits)) },
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
                Text(stringResource(R.string.close))
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
    onOpenFontPicker: () -> Unit
) {
    val context = LocalContext.current
    val resolution = rememberFontResolution(appFont)
    val repository = remember(context) { FontRepository.get(context) }
    val states by repository.states.collectAsState()
    val saved = FontCatalog.find(appFont)
    // A saved downloadable font that is not usable yet says why: still loading, failed, or no provider.
    val currentFontName = when (fontAvailability(resolution.reason, states.stateOf(appFont, FontCoverage.Full))) {
        FontAvailability.Available -> resolution.spec.displayName
        FontAvailability.Loading -> stringResource(R.string.font_subtitle_loading, saved?.displayName.orEmpty())
        FontAvailability.NotDownloaded ->
            stringResource(R.string.font_subtitle_not_downloaded, saved?.displayName.orEmpty())
        FontAvailability.NeedsPlayServices ->
            stringResource(R.string.font_subtitle_needs_play_services, saved?.displayName.orEmpty())
    }

    SettingsSectionHeader(stringResource(R.string.settings_section_font_style))
    SettingActionItem(
        title = stringResource(R.string.settings_launcher_font),
        subtitle = currentFontName,
        icon = { Icon(Icons.Default.FontDownload, contentDescription = null) },
        onClick = onOpenFontPicker
    )
    SettingActionItem(
        title = stringResource(R.string.settings_system_display),
        subtitle = stringResource(R.string.settings_system_display_subtitle),
        icon = { Icon(Icons.Default.Brightness4, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.settings_system_wallpaper),
        subtitle = stringResource(R.string.settings_system_wallpaper_subtitle),
        icon = { Icon(Icons.Default.Wallpaper, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    )
}

@Composable
private fun AppearanceSection(
    showStatusBar: Boolean,
    onShowStatusBarChange: (Boolean) -> Unit
) {
    val prefs = LocalSettingsPrefs.current

    SettingsSectionHeader(stringResource(R.string.settings_section_system_bars))
    SettingToggleItem(
        title = stringResource(R.string.notification_bar),
        subtitle = stringResource(R.string.settings_status_bar_subtitle),
        icon = { Icon(Icons.Default.SpaceBar, contentDescription = null) },
        checked = showStatusBar,
        onCheckedChange = { onShowStatusBarChange(it); prefs.showStatusBar = it },
        tooltipText = "When disabled, the system status bar hides but can be revealed by swiping down from the top edge."
    )
}

private const val HOME_PREVIEW_CARD_SCALE = 0.4f

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

/** Weight, color and opacity of one Home text block (clock or apps). */
private data class HomeTextStyleChoice(val fontWeight: Int, val color: String, val opacity: Float)

@OptIn(ExperimentalLayoutApi::class)
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
    onHoldingPreview: (SettingsPreviewTarget?) -> Unit
) {
    val context = LocalContext.current
    val prefs = LocalSettingsPrefs.current
    val scope = rememberCoroutineScope()
    val usageRedirected = stringResource(R.string.settings_usage_access_redirected)
    val usageFailed = stringResource(R.string.settings_usage_access_failed)
    val screenTimeEnabled = stringResource(R.string.settings_screen_time_enabled)

    SettingsSectionHeader(stringResource(R.string.settings_section_home_layout))
    SettingToggleItem(
        title = stringResource(R.string.settings_show_clock),
        subtitle = stringResource(R.string.settings_show_clock_subtitle),
        icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        checked = showClock,
        onCheckedChange = { onShowClockChange(it); prefs.showClockWidget = it },
        tooltipText = "Toggle clock visibility on the home screen. Tap the clock to open your alarms app."
    )
    SettingToggleItem(
        title = stringResource(R.string.show_date_time),
        subtitle = stringResource(R.string.settings_show_date_subtitle),
        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        checked = showDate,
        onCheckedChange = { onShowDateChange(it); prefs.showDateWidget = it }
    )
    SettingToggleItem(
        title = stringResource(R.string.screen_time),
        subtitle = stringResource(R.string.settings_screen_time_subtitle),
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
                        scope.launch { snackbarHostState.showSnackbar(usageRedirected) }
                    } catch (e: Exception) {
                        scope.launch { snackbarHostState.showSnackbar(usageFailed) }
                    }
                } else {
                    scope.launch { snackbarHostState.showSnackbar(screenTimeEnabled) }
                }
            }
        }
    )
    SettingToggleItem(
        title = stringResource(R.string.settings_show_home_apps),
        subtitle = stringResource(R.string.settings_show_home_apps_subtitle),
        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
        checked = showHomeApps,
        onCheckedChange = { onShowHomeAppsChange(it); prefs.showHomeApps = it }
    )
    SettingChoiceItem(
        title = stringResource(R.string.settings_section_order),
        icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
        selected = homeSectionOrder,
        options = listOf(
            "clock_first" to stringResource(R.string.clock_first),
            "apps_first" to stringResource(R.string.apps_first)
        ),
        onSelected = { onHomeSectionOrderChange(it); prefs.homeSectionOrder = it }
    )
    val clockPct = (clockSectionWeight * 100).roundToInt()
    SettingSliderItem(
        title = stringResource(R.string.clock_section_weight),
        subtitle = stringResource(R.string.settings_split_subtitle, clockPct, 100 - clockPct),
        icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
        value = clockSectionWeight,
        range = HomeLayoutHelper.MIN_CLOCK_WEIGHT..HomeLayoutHelper.MAX_CLOCK_WEIGHT,
        valueText = { "${(it * 100).roundToInt()}%" },
        tooltipText = "Default is 35% clock / 65% apps. Sections scroll if content is larger than the allocated space.",
        onHoldingChanged = { holding -> onHoldingPreview(if (holding) SettingsPreviewTarget.HOME else null) },
        onChanged = { onClockSectionWeightChange(it); prefs.clockSectionWeight = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.settings_side_padding),
        subtitle = stringResource(R.string.settings_side_padding_subtitle),
        icon = { Icon(Icons.Default.SpaceBar, contentDescription = null) },
        value = homeSidePadding,
        range = HomeStyle.MIN_SIDE_PADDING_DP..HomeStyle.MAX_SIDE_PADDING_DP,
        valueText = { "${it.roundToInt()} dp" },
        onHoldingChanged = { holding -> onHoldingPreview(if (holding) SettingsPreviewTarget.HOME else null) },
        onChanged = { onHomeSidePaddingChange(it); prefs.homeSidePaddingDp = it }
    )
}

@Composable
private fun ClockAndDateSection(
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
    screenTimeSizeScale: Float,
    onScreenTimeSizeScaleChange: (Float) -> Unit,
    textStyle: HomeTextStyleChoice,
    onFontWeightChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onHoldingPreview: (SettingsPreviewTarget?) -> Unit
) {
    val prefs = LocalSettingsPrefs.current
    val holdHome: (Boolean) -> Unit = { holding -> onHoldingPreview(if (holding) SettingsPreviewTarget.HOME else null) }

    SettingsSectionHeader(stringResource(R.string.settings_section_clock))
    SettingToggleItem(
        title = stringResource(R.string.settings_24_hour),
        subtitle = stringResource(R.string.settings_24_hour_subtitle),
        icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        checked = use24HourClock,
        onCheckedChange = { onUse24HourClockChange(it); prefs.use24HourClock = it },
        tooltipText = "Switch between 12h (AM/PM) and 24h format for the home screen clock."
    )
    SettingToggleItem(
        title = stringResource(R.string.settings_show_seconds),
        subtitle = stringResource(R.string.settings_show_seconds_subtitle),
        icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        checked = showSeconds,
        onCheckedChange = { onShowSecondsChange(it); prefs.showClockSeconds = it },
        tooltipText = "Enabling seconds will cause the clock to update every second, which may have a minor impact on battery life."
    )
    SettingAlignmentItem(
        title = stringResource(R.string.settings_clock_horizontal),
        subtitle = stringResource(R.string.settings_horizontal_subtitle),
        icon = { Icon(Icons.Default.LinearScale, contentDescription = null) },
        currentGravity = clockHorizontalAlignment,
        options = horizontalOptions(),
        onChanged = { onClockHorizontalAlignmentChange(it); prefs.clockAlignment = it }
    )
    SettingAlignmentItem(
        title = stringResource(R.string.settings_clock_vertical),
        subtitle = stringResource(R.string.settings_vertical_subtitle),
        icon = { Icon(Icons.Default.Transform, contentDescription = null) },
        currentGravity = clockVerticalAlignment,
        options = verticalOptions(),
        onChanged = { onClockVerticalAlignmentChange(it); prefs.clockVerticalAlignment = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.clock_size),
        subtitle = stringResource(R.string.settings_clock_size_subtitle),
        icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
        value = clockSizeScale,
        range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
        valueText = ::scaleText,
        onHoldingChanged = holdHome,
        onChanged = { onClockSizeScaleChange(it); prefs.clockSizeScale = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.date_size),
        subtitle = stringResource(R.string.settings_date_size_subtitle),
        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        value = dateSizeScale,
        range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
        valueText = ::scaleText,
        onHoldingChanged = holdHome,
        onChanged = { onDateSizeScaleChange(it); prefs.dateSizeScale = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.screen_time_size),
        subtitle = stringResource(R.string.settings_screen_time_size_subtitle),
        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
        value = screenTimeSizeScale,
        range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_CLOCK_SIZE_SCALE,
        valueText = ::scaleText,
        onHoldingChanged = holdHome,
        onChanged = { onScreenTimeSizeScaleChange(it); prefs.screenTimeSizeScale = it }
    )
    HomeTextStyleItems(
        style = textStyle,
        onFontWeightChange = onFontWeightChange,
        onColorChange = onColorChange,
        onOpacityChange = onOpacityChange,
        onHoldingPreview = holdHome
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
    onAppSpacingChange: (Float) -> Unit,
    homeTextSize: Float,
    onHomeTextSizeChange: (Float) -> Unit,
    textStyle: HomeTextStyleChoice,
    onFontWeightChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onHoldingPreview: (SettingsPreviewTarget?) -> Unit
) {
    val prefs = LocalSettingsPrefs.current
    val holdHome: (Boolean) -> Unit = { holding -> onHoldingPreview(if (holding) SettingsPreviewTarget.HOME else null) }

    SettingsSectionHeader(stringResource(R.string.settings_section_home_apps))
    SettingCounterItem(
        title = stringResource(R.string.settings_max_home_apps),
        subtitle = stringResource(R.string.settings_max_home_apps_subtitle),
        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
        value = maxHomeApps,
        tooltipText = "Specify how many priority apps (up to 10) you want available directly from the home screen.",
        onDecrease = { if (maxHomeApps > 1) { onMaxHomeAppsChange(maxHomeApps - 1); prefs.maxHomeApps = maxHomeApps - 1 } },
        onIncrease = { if (maxHomeApps < 10) { onMaxHomeAppsChange(maxHomeApps + 1); prefs.maxHomeApps = maxHomeApps + 1 } }
    )
    SettingAlignmentItem(
        title = stringResource(R.string.settings_apps_horizontal),
        subtitle = stringResource(R.string.settings_horizontal_subtitle),
        icon = { Icon(Icons.Default.LinearScale, contentDescription = null) },
        currentGravity = appHorizontalAlignment,
        options = horizontalOptions(),
        onChanged = { onAppHorizontalAlignmentChange(it); prefs.homeAlignment = it }
    )
    SettingAlignmentItem(
        title = stringResource(R.string.settings_apps_vertical),
        subtitle = stringResource(R.string.settings_vertical_subtitle),
        icon = { Icon(Icons.Default.Transform, contentDescription = null) },
        currentGravity = appVerticalAlignment,
        options = verticalOptions(),
        onChanged = { onAppVerticalAlignmentChange(it); prefs.homeVerticalAlignment = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.settings_app_spacing),
        subtitle = stringResource(R.string.settings_app_spacing_subtitle),
        icon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
        value = appSpacing,
        range = 0f..HomeStyle.MAX_APP_SPACING,
        valueText = { "${it.roundToInt()}" },
        onHoldingChanged = holdHome,
        onChanged = { onAppSpacingChange(it); prefs.appSpacingDp = it }
    )
    SettingSliderItem(
        title = stringResource(R.string.home_text_size),
        subtitle = stringResource(R.string.settings_home_text_size_subtitle),
        icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
        value = homeTextSize,
        range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_APP_SIZE_SCALE,
        valueText = ::scaleText,
        onHoldingChanged = holdHome,
        onChanged = { onHomeTextSizeChange(it); prefs.homeTextSizeScale = it }
    )
    HomeTextStyleItems(
        style = textStyle,
        onFontWeightChange = onFontWeightChange,
        onColorChange = onColorChange,
        onOpacityChange = onOpacityChange,
        onHoldingPreview = holdHome
    )
}

/** Weight, color and opacity rows, shared by the clock and the home apps sections. */
@Composable
private fun HomeTextStyleItems(
    style: HomeTextStyleChoice,
    onFontWeightChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onHoldingPreview: (Boolean) -> Unit
) {
    SettingChoiceItem(
        title = stringResource(R.string.settings_font_weight),
        icon = { Icon(Icons.Default.FormatBold, contentDescription = null) },
        selected = style.fontWeight,
        options = HomeStyle.fontWeights.map { weight -> weight to stringResource(weight.weightLabelRes()) },
        onSelected = onFontWeightChange
    )
    SettingColorItem(
        title = stringResource(R.string.settings_text_color),
        selected = HomeTextColor.fromKey(style.color),
        onSelected = { onColorChange(it.key) }
    )
    SettingSliderItem(
        title = stringResource(R.string.settings_opacity),
        subtitle = "",
        icon = { Icon(Icons.Default.Opacity, contentDescription = null) },
        value = style.opacity,
        range = HomeStyle.MIN_OPACITY..HomeStyle.MAX_OPACITY,
        valueText = { "${(it * 100).roundToInt()}%" },
        onHoldingChanged = onHoldingPreview,
        onChanged = onOpacityChange
    )
}

@Composable
private fun AppDrawerSection(
    showAlphabetCategories: Boolean,
    onShowAlphabetCategoriesChange: (Boolean) -> Unit,
    autoShowKeyboard: Boolean,
    onAutoShowKeyboardChange: (Boolean) -> Unit,
    drawerTextSize: Float,
    onDrawerTextSizeChange: (Float) -> Unit,
    onHoldingPreview: (SettingsPreviewTarget?) -> Unit
) {
    val prefs = LocalSettingsPrefs.current

    SettingsSectionHeader(stringResource(R.string.settings_section_app_list))
    SettingToggleItem(
        title = stringResource(R.string.settings_show_alphabet),
        subtitle = stringResource(R.string.settings_show_alphabet_subtitle),
        icon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) },
        checked = showAlphabetCategories,
        onCheckedChange = { onShowAlphabetCategoriesChange(it); prefs.showAlphabetCategories = it },
        tooltipText = "Groups apps A–Z in the app drawer for quick alphabetical navigation."
    )
    SettingToggleItem(
        title = stringResource(R.string.auto_show_keyboard),
        subtitle = stringResource(R.string.settings_auto_keyboard_subtitle),
        icon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
        checked = autoShowKeyboard,
        onCheckedChange = { onAutoShowKeyboardChange(it); prefs.autoShowKeyboard = it },
        tooltipText = "Automatically pops up the keyboard when you enter the app drawer for faster search."
    )
    SettingSliderItem(
        title = stringResource(R.string.app_drawer_text_size),
        subtitle = stringResource(R.string.settings_drawer_text_size_subtitle),
        icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
        value = drawerTextSize,
        range = HomeStyle.MIN_SIZE_SCALE..HomeStyle.MAX_APP_SIZE_SCALE,
        valueText = ::scaleText,
        onHoldingChanged = { holding -> onHoldingPreview(if (holding) SettingsPreviewTarget.DRAWER else null) },
        onChanged = { onDrawerTextSizeChange(it); prefs.appDrawerTextSizeScale = it }
    )
}

@Composable
private fun PrivateSpaceSection(
    privateSpaceEnabled: Boolean,
    onPrivateSpaceEnabledChange: (Boolean) -> Unit,
    privateSpacePlacement: String,
    onPrivateSpacePlacementChange: (String) -> Unit,
    isPrivateSpaceConfigured: Boolean,
    privateSpaceAvailable: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val prefs = LocalSettingsPrefs.current
    val scope = rememberCoroutineScope()
    val notConfigured = stringResource(R.string.settings_private_space_not_configured)
    val enabledMessage = stringResource(R.string.settings_private_space_enabled)
    val disabledMessage = stringResource(R.string.settings_private_space_disabled)

    SettingsSectionHeader(stringResource(R.string.private_space))
    SettingToggleItem(
        title = stringResource(R.string.settings_enable_private_space),
        subtitle = stringResource(
            if (isPrivateSpaceConfigured) R.string.settings_private_space_subtitle else R.string.settings_private_space_not_configured
        ),
        icon = { Icon(Icons.Default.Security, contentDescription = null) },
        checked = privateSpaceEnabled && isPrivateSpaceConfigured,
        onCheckedChange = {
            if (!isPrivateSpaceConfigured) {
                scope.launch { snackbarHostState.showSnackbar(notConfigured) }
            } else {
                onPrivateSpaceEnabledChange(it); prefs.privateSpaceEnabled = it
                scope.launch { snackbarHostState.showSnackbar(if (it) enabledMessage else disabledMessage) }
            }
        },
        tooltipText = "Private Space isolates sensitive apps behind a separate authentication layer. Must be configured in system settings first."
    )
    // Placement only matters once the space is configured and turned on.
    if (privateSpaceAvailable && privateSpaceEnabled && isPrivateSpaceConfigured) {
        SettingChoiceItem(
            title = stringResource(R.string.private_space_placement),
            subtitle = stringResource(R.string.private_space_placement_subtitle),
            icon = { Icon(Icons.Default.Security, contentDescription = null) },
            selected = privateSpacePlacement,
            options = listOf(
                Prefs.PrivateSpacePlacement.BOTTOM to stringResource(R.string.private_space_placement_bottom),
                Prefs.PrivateSpacePlacement.SEARCH_BAR to stringResource(R.string.private_space_placement_search_bar)
            ),
            onSelected = {
                onPrivateSpacePlacementChange(it)
                prefs.privateSpacePlacement = it
            }
        )
    }
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

/** A titled row of single-choice chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> SettingChoiceItem(
    title: String,
    icon: @Composable () -> Unit,
    selected: T,
    options: List<Pair<T, String>>,
    subtitle: String = "",
    onSelected: (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = VoidDimens.rowSpacing)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
        ) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                    label = { Text(label) }
                )
            }
        }
    }
}

/** Theme color roles as swatches: each chip shows the color it applies. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingColorItem(
    title: String,
    selected: HomeTextColor,
    onSelected: (HomeTextColor) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Palette, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = VoidDimens.rowSpacing)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
        ) {
            HomeTextColor.entries.forEach { role ->
                FilterChip(
                    selected = selected == role,
                    onClick = { onSelected(role) },
                    label = { Text(stringResource(role.labelRes())) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                                .background(role.resolve(), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                )
            }
        }
    }
}

private fun HomeTextColor.labelRes(): Int = when (this) {
    HomeTextColor.Default -> R.string.home_color_default
    HomeTextColor.Primary -> R.string.home_color_primary
    HomeTextColor.Secondary -> R.string.home_color_secondary
    HomeTextColor.Tertiary -> R.string.home_color_tertiary
    HomeTextColor.Muted -> R.string.home_color_muted
}

@Composable
private fun GesturesSection(
    enableGestures: Boolean,
    onEnableGesturesChange: (Boolean) -> Unit,
    enableSwipeDownNotifications: Boolean,
    onEnableSwipeDownNotificationsChange: (Boolean) -> Unit,
    enableSummary: Boolean,
    enableWidgets: Boolean,
    enableNotes: Boolean,
    leftSwipeAction: String,
    onLeftSwipeActionChange: (String) -> Unit,
    rightSwipeAction: String,
    onRightSwipeActionChange: (String) -> Unit,
    animationSpeed: String,
    onAnimationSpeedChange: (String) -> Unit,
    contentSwipeToBack: Boolean,
    onContentSwipeToBackChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val prefs = LocalSettingsPrefs.current
    val scope = rememberCoroutineScope()
    val grantNotificationAccess = stringResource(R.string.grant_notification_access)
    val couldNotOpenNotificationSettings = stringResource(R.string.could_not_open_notification_settings)

    SettingsSectionHeader(stringResource(R.string.gestures))
    SettingChoiceItem(
        title = stringResource(R.string.animation_speed),
        subtitle = stringResource(R.string.animation_speed_subtitle),
        icon = { Icon(Icons.Default.Swipe, contentDescription = null) },
        selected = animationSpeed,
        options = listOf(
            Prefs.AnimationSpeed.FAST to stringResource(R.string.animation_speed_fast),
            Prefs.AnimationSpeed.STANDARD to stringResource(R.string.animation_speed_standard),
            Prefs.AnimationSpeed.SLOW to stringResource(R.string.animation_speed_slow)
        ),
        onSelected = {
            onAnimationSpeedChange(it)
            prefs.animationSpeed = it
        }
    )
    SettingToggleItem(
        title = stringResource(R.string.content_swipe_to_back),
        subtitle = stringResource(R.string.content_swipe_to_back_subtitle),
        icon = { Icon(Icons.Default.Swipe, contentDescription = null) },
        checked = contentSwipeToBack,
        onCheckedChange = { onContentSwipeToBackChange(it); prefs.contentSwipeToBack = it }
    )
    SettingToggleItem(
        title = "Enable Gestures",
        subtitle = "Activate screen swipes",
        icon = { Icon(Icons.Default.Swipe, contentDescription = null) },
        checked = enableGestures,
        onCheckedChange = { onEnableGesturesChange(it); prefs.enableGestures = it }
    )
    SettingToggleItem(
        title = stringResource(R.string.swipe_down_notifications),
        subtitle = stringResource(R.string.swipe_down_notifications_subtitle),
        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
        checked = enableSwipeDownNotifications,
        onCheckedChange = { enabled ->
            if (enabled && !FeatureAvailability.isNotificationListenerEnabled(context)) {
                try {
                    context.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    scope.launch {
                        snackbarHostState.showSnackbar(grantNotificationAccess)
                    }
                } catch (_: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar(couldNotOpenNotificationSettings)
                    }
                }
            }
            onEnableSwipeDownNotificationsChange(enabled)
            prefs.enableSwipeDownNotifications = enabled
        }
    )
    if (enableGestures) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeaturesSection(
    enableSummary: Boolean,
    onEnableSummaryChange: (Boolean) -> Unit,
    enableWidgets: Boolean,
    onEnableWidgetsChange: (Boolean) -> Unit,
    enableNotes: Boolean,
    onEnableNotesChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val prefs = LocalSettingsPrefs.current
    val scope = rememberCoroutineScope()

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
                    } catch (_: Exception) {
                        scope.launch { snackbarHostState.showSnackbar("Error checking notification access") }
                        return@FilterChip
                    }
                }
                onEnableSummaryChange(newState); prefs.enableNotificationSummary = newState
            },
            label = { Text(stringResource(R.string.swipe_action_notification_summary)) },
            leadingIcon = if (enableSummary) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
        )

        FilterChip(
            selected = enableWidgets,
            onClick = {
                val newState = !enableWidgets
                onEnableWidgetsChange(newState); prefs.enableWidgets = newState
            },
            label = { Text(stringResource(R.string.swipe_action_widgets)) },
            leadingIcon = if (enableWidgets) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
        )

        FilterChip(
            selected = enableNotes,
            onClick = {
                val newState = !enableNotes
                onEnableNotesChange(newState); prefs.enableNotes = newState
            },
            label = { Text(stringResource(R.string.swipe_action_notes)) },
            leadingIcon = if (enableNotes) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
        )
    }

    if (enableNotes) {
        var notesTitleAlign by remember { mutableIntStateOf(prefs.notesTitleAlignment) }
        SettingAlignmentItem(
            title = "Notes Title Alignment",
            subtitle = "Left, Center, or Right",
            icon = { Icon(Icons.Default.FormatAlignLeft, contentDescription = null) },
            currentGravity = notesTitleAlign,
            options = listOf(
                Gravity.START to "Left",
                Gravity.CENTER_HORIZONTAL to "Center",
                Gravity.END to "Right"
            ),
            tooltipText = "Align the 'Notes' header on the notes panel.",
            onChanged = {
                notesTitleAlign = it
                prefs.notesTitleAlignment = it
            }
        )
    }
}

@Composable
private fun PermissionsSection() {
    val context = LocalContext.current
    SettingsSectionHeader(stringResource(R.string.permissions))
    Text(
        text = stringResource(R.string.permissions_intro),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    )
    SettingActionItem(
        title = "App Info & System Permissions",
        subtitle = "Open dedicated Android settings to manage all app permissions and defaults",
        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
        onClick = {
            try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_notification_listener),
        subtitle = stringResource(R.string.perm_notification_listener_body),
        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_usage_access),
        subtitle = stringResource(R.string.perm_usage_access_body),
        icon = { Icon(Icons.Default.QueryStats, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_accessibility),
        subtitle = stringResource(R.string.perm_accessibility_body),
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_default_home),
        subtitle = stringResource(R.string.perm_default_home_body),
        icon = { Icon(Icons.Default.Home, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_query_apps),
        subtitle = stringResource(R.string.perm_query_apps_body),
        icon = { Icon(Icons.Default.Apps, contentDescription = null) },
        onClick = {
            try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_widgets),
        subtitle = stringResource(R.string.perm_widgets_body),
        icon = { Icon(Icons.Default.Widgets, contentDescription = null) },
        onClick = {
            try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    )
    SettingActionItem(
        title = stringResource(R.string.perm_private_space),
        subtitle = stringResource(R.string.perm_private_space_body),
        icon = { Icon(Icons.Default.Security, contentDescription = null) },
        onClick = {
            try {
                context.startActivity(Intent("android.settings.PRIVATE_SPACE_SETTINGS").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {
                try {
                    context.startActivity(Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {}
            }
        }
    )
}

@Composable
private fun SettingsSectionHeader(title: String) {
    VoidSectionDivider(
        modifier = Modifier.padding(top = VoidDimens.sectionSpacing, bottom = VoidDimens.rowSpacing)
    )
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = VoidDimens.compactSpacing),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Switch
                )
                .padding(16.dp),
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
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
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
    valueText: (Float) -> String = { String.format(Locale.getDefault(), "%.1f", it) },
    onHoldingChanged: ((Boolean) -> Unit)? = null,
    onChanged: (Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHolding = isDragged || isPressed

    LaunchedEffect(isHolding) {
        onHoldingChanged?.invoke(isHolding)
    }

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
                        text = valueText(value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = value,
                    onValueChange = onChanged,
                    valueRange = range,
                    interactionSource = interactionSource,
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
    if (enableSummary) allActions.add(SwipeAction.NOTIFICATION_SUMMARY to "Notification Summary")
    if (enableWidgets) allActions.add(SwipeAction.WIDGETS to "Widgets")
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
            title = { Text(stringResource(R.string.select_action)) },
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
            Text(stringResource(R.string.select_app), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            androidx.compose.material3.OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(stringResource(R.string.search_apps)) },
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
