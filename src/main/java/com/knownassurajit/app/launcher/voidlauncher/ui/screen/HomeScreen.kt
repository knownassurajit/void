package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.provider.CalendarContract
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.BuildConfig
import com.knownassurajit.app.launcher.voidlauncher.HomeApp
import com.knownassurajit.app.launcher.voidlauncher.MainUiState
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.AppModel
import com.knownassurajit.app.launcher.voidlauncher.data.HomeAppSlot
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeReorderHelper
import com.knownassurajit.app.launcher.voidlauncher.helper.getAppsList
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.activity.compose.BackHandler

// ── Alignment helpers ──

private fun gravityToAlignment(gravity: Int): Alignment.Horizontal = when (gravity) {
    Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> Alignment.CenterHorizontally
    Gravity.END, Gravity.RIGHT -> Alignment.End
    else -> Alignment.Start
}

fun gravityToVerticalArrangement(gravity: Int): Arrangement.Vertical {
    return when (gravity) {
        Gravity.TOP -> Arrangement.Top
        Gravity.BOTTOM -> Arrangement.Bottom
        else -> Arrangement.Center
    }
}

private fun gravityToVerticalContentAlignment(gravity: Int): Alignment.Vertical {
    return when (gravity) {
        Gravity.TOP -> Alignment.Top
        Gravity.BOTTOM -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }
}

private fun gravityToTextAlign(gravity: Int): TextAlign = when (gravity) {
    Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> TextAlign.Center
    Gravity.END, Gravity.RIGHT -> TextAlign.End
    else -> TextAlign.Start
}

private fun openScreenTimeDestination(context: android.content.Context) {
    val packageManager = context.packageManager

    val usageAccessIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val candidateIntents = listOf(
        Intent().apply {
            setClassName(
                com.knownassurajit.app.launcher.voidlauncher.data.Constants.DIGITAL_WELLBEING_PACKAGE_NAME,
                "com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent().apply {
            setClassName(
                com.knownassurajit.app.launcher.voidlauncher.data.Constants.DIGITAL_WELLBEING_PACKAGE_NAME,
                "com.google.android.apps.wellbeing.home.TopLevelSettingsActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent("com.google.android.apps.wellbeing.VIEW_APP_USAGE").apply {
            setPackage(com.knownassurajit.app.launcher.voidlauncher.data.Constants.DIGITAL_WELLBEING_PACKAGE_NAME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent("android.settings.DIGITAL_WELLBEING_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        Intent().apply {
            setClassName(
                com.knownassurajit.app.launcher.voidlauncher.data.Constants.DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME,
                com.knownassurajit.app.launcher.voidlauncher.data.Constants.DIGITAL_WELLBEING_SAMSUNG_ACTIVITY
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        context.packageManager
            .getLaunchIntentForPackage(com.knownassurajit.app.launcher.voidlauncher.data.Constants.DIGITAL_WELLBEING_PACKAGE_NAME)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
        usageAccessIntent,
        appDetailsIntent
    ).filterNotNull()

    val resolvedIntent = candidateIntents.firstOrNull { intent ->
        packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    if (resolvedIntent != null) {
        val launchedFallback = resolvedIntent.action == Settings.ACTION_USAGE_ACCESS_SETTINGS ||
            resolvedIntent.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        if (launchedFallback) {
            Toast.makeText(
                context,
                "Digital Wellbeing not found. Opening a settings fallback.",
                Toast.LENGTH_SHORT
            ).show()
        }
        context.startActivity(resolvedIntent)
        return
    }

    Toast.makeText(
        context,
        "No screen-time destination available on this device.",
        Toast.LENGTH_SHORT
    ).show()
}

// ── Main Home Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: MainUiState,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenNotificationSummary: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenNotes: () -> Unit,
    onAppClick: (HomeApp) -> Unit,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
    onHomeAppsChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val haptic = LocalHapticFeedback.current
    val clockAlign = remember(state.clockHorizontalAlignment) { gravityToAlignment(state.clockHorizontalAlignment) }
    val appAlign = remember(state.appHorizontalAlignment) { gravityToAlignment(state.appHorizontalAlignment) }
    val clockVertical = remember(state.clockVerticalAlignment) { gravityToVerticalArrangement(state.clockVerticalAlignment) }
    val appVertical = remember(state.appVerticalAlignment) { gravityToVerticalContentAlignment(state.appVerticalAlignment) }

    val clockTextAlign = remember(state.clockHorizontalAlignment) { gravityToTextAlign(state.clockHorizontalAlignment) }
    val appTextAlign = remember(state.appHorizontalAlignment) { gravityToTextAlign(state.appHorizontalAlignment) }

    // ── Swipe gesture state ──
    val swipeThreshold = 120f
    var showAppPicker by remember { mutableStateOf(false) }

    var isDraggingGlobal by remember { mutableStateOf(false) }
    val isDraggingGlobalLatest = rememberUpdatedState(isDraggingGlobal)
    val swipeDownEnabledLatest = rememberUpdatedState(state.enableSwipeDownNotifications)

    // Consume back press on home screen — prevents re-transition to self
    BackHandler { /* Do nothing — home screen is the root */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            // Stable keys — do NOT key on isDraggingGlobal (restarts cancel child reorder).
            // Yield to long-press (reorder / picker) instead of consuming the pointer immediately.
            .pointerInput(
                state.leftSwipeAction,
                state.rightSwipeAction,
                state.enableGestures,
                state.enableSwipeDownNotifications
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (isDraggingGlobalLatest.value) {
                        return@awaitEachGesture
                    }
                    val slop = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis.toLong()) {
                        awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                    }
                    if (slop == null || isDraggingGlobalLatest.value) {
                        return@awaitEachGesture
                    }
                    var total = slop.positionChange()
                    drag(slop.id) { change ->
                        if (isDraggingGlobalLatest.value) return@drag
                        total += change.positionChange()
                        change.consume()
                    }
                    if (isDraggingGlobalLatest.value) return@awaitEachGesture
                    val absX = abs(total.x)
                    val absY = abs(total.y)
                    if (absX > swipeThreshold || absY > swipeThreshold) {
                        if (absX > absY) {
                            if (state.enableGestures) {
                                if (total.x > 0) {
                                    dispatchSwipeAction("left", state.leftSwipeAction, context,
                                        onOpenNotificationSummary, onOpenWidgets, onOpenNotes, onOpenNotifications)
                                } else {
                                    dispatchSwipeAction("right", state.rightSwipeAction, context,
                                        onOpenNotificationSummary, onOpenWidgets, onOpenNotes, onOpenNotifications)
                                }
                            }
                        } else {
                            if (total.y > 0) {
                                if (swipeDownEnabledLatest.value) {
                                    onOpenNotifications()
                                }
                            } else {
                                onOpenApps()
                            }
                        }
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val clockEnabled = state.showClock || state.showDate ||
                (state.showScreenTime && state.screenTime.isNotBlank())
            val appsEnabled = state.showHomeApps
            val bothEnabled = clockEnabled && appsEnabled
            val weights = HomeLayoutHelper.sectionWeights(
                clockEnabled = clockEnabled,
                appsEnabled = appsEnabled,
                requestedClockWeight = state.clockSectionWeight
            )
            val clockWeight = weights.clock
            val appsWeight = weights.apps

            @Composable
            fun Clock() {
                HomeClockSection(
                    state = state,
                    clockAlign = clockAlign,
                    clockVertical = clockVertical,
                    clockTextAlign = clockTextAlign,
                    weight = clockWeight,
                    onClockClick = onClockClick,
                    onDateClick = onDateClick,
                    onShowAppPicker = { showAppPicker = true }
                )
            }

            @Composable
            fun Apps() {
                HomeAppsSection(
                    state = state,
                    appAlign = appAlign,
                    appVertical = appVertical,
                    appTextAlign = appTextAlign,
                    weight = appsWeight,
                    onAppClick = onAppClick,
                    onHomeAppsChanged = onHomeAppsChanged,
                    prefs = prefs,
                    haptic = haptic,
                    onDraggingGlobalChanged = { isDraggingGlobal = it },
                    onShowAppPicker = { showAppPicker = true }
                )
            }

            @Composable
            fun Divider() {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = VoidDimens.screenPadding),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                )
            }

            if (state.homeSectionOrder == "apps_first") {
                if (appsEnabled) Apps()
                if (bothEnabled) Divider()
                if (clockEnabled) Clock()
            } else {
                if (clockEnabled) Clock()
                if (bothEnabled) Divider()
                if (appsEnabled) Apps()
            }
        }
    }

    // ── App picker sheet ──
    if (showAppPicker) {
        HomeAppPickerSheet(
            currentApps = state.homeApps,
            maxApps = state.homeAppsCount.coerceIn(1, 10),
            onDismiss = { showAppPicker = false },
            onHomeAppsChanged = onHomeAppsChanged
        )
    }
}

@Composable
private fun ColumnScope.HomeClockSection(
    state: MainUiState,
    clockAlign: Alignment.Horizontal,
    clockVertical: Arrangement.Vertical,
    clockTextAlign: TextAlign,
    weight: Float,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
    onShowAppPicker: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onShowAppPicker() }
                    )
                }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(
                    when (clockVertical) {
                        Arrangement.Top -> Alignment.TopCenter
                        Arrangement.Bottom -> Alignment.BottomCenter
                        else -> Alignment.Center
                    }
                )
                .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.sectionSpacing),
            horizontalAlignment = clockAlign,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ── Time ──
            if (state.showClock) {
                Text(
                    text = state.currentTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.displayLarge.fontSize *
                            state.clockSizeScale *
                            (if (state.showSeconds) 0.7f else 1.0f),
                        letterSpacing = (-1.5).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = clockTextAlign,
                    modifier = Modifier.clickable {
                            try {
                                context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
                            } catch (_: Exception) {
                                onClockClick()
                            }
                        }
                )
            }

            // ── Date ──
            if (state.showDate) {
                Text(
                    text = state.currentDate.uppercase(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize * state.dateSizeScale
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.87f),
                    textAlign = clockTextAlign,
                    modifier = Modifier.clickable {
                            try {
                                val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                                context.startActivity(Intent(Intent.ACTION_VIEW).setData(builder.build()))
                            } catch (_: Exception) {
                                onDateClick()
                            }
                        }
                )
            }

            // ── Screen Time ──
            if (state.showScreenTime && state.screenTime.isNotBlank()) {
                Text(
                    text = state.screenTime.uppercase(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.5.sp,
                        fontSize = 11.sp * state.screenTimeSizeScale
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    textAlign = clockTextAlign,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { openScreenTimeDestination(context) }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.HomeAppsSection(
    state: MainUiState,
    appAlign: Alignment.Horizontal,
    appVertical: Alignment.Vertical,
    appTextAlign: TextAlign,
    weight: Float,
    onAppClick: (HomeApp) -> Unit,
    onHomeAppsChanged: () -> Unit,
    prefs: Prefs,
    haptic: HapticFeedback,
    onDraggingGlobalChanged: (Boolean) -> Unit,
    onShowAppPicker: () -> Unit
) {
    // ── Dynamic app spacing based on screen real-estate ──
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val displayedCount = state.homeApps.size.coerceAtLeast(1)
    val dynamicBaseSpacing = ((screenHeightDp * 0.75f) / (displayedCount + 3)).coerceIn(4f, 32f)
    val spacingMultiplier = if (state.appSpacingDp <= 0f) 0f else state.appSpacingDp / 24f
    val computedSpacing = (dynamicBaseSpacing * spacingMultiplier).coerceIn(0f, 64f)

    // ── Drag-to-reorder state ──
    val reorderList = remember { mutableStateListOf<HomeApp>() }
    var isDragging by remember { mutableStateOf(false) }
    var draggedKey by remember { mutableStateOf<HomeReorderHelper.ReorderKey?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val itemHeights = remember { FloatArray(11) }
    val density = LocalDensity.current
    val spacingPx = with(density) { computedSpacing.dp.toPx() }

    fun homeAppKey(app: HomeApp): HomeReorderHelper.ReorderKey =
        HomeReorderHelper.ReorderKey(
            packageName = app.packageName,
            userString = app.userString,
            isShortcut = app.isShortcut,
            shortcutId = app.shortcutId
        )

    // Non-snapshot holders so onDrag in the same frame as onDragStart sees the index.
    val liveDragIndex = remember { intArrayOf(-1) }
    val liveDragList = remember { mutableListOf<HomeApp>() }
    val liveDragKey = remember { arrayOfNulls<HomeReorderHelper.ReorderKey>(1) }

    fun resolveDraggedIndex(): Int {
        val key = liveDragKey[0] ?: draggedKey ?: return -1
        val list = if (liveDragList.isNotEmpty()) liveDragList else reorderList
        return list.indexOfFirst { homeAppKey(it) == key }
    }

    LaunchedEffect(isDragging) {
        onDraggingGlobalChanged(isDragging)
    }

    LaunchedEffect(state.homeApps) {
        if (!isDragging) {
            reorderList.clear()
            reorderList.addAll(state.homeApps)
        }
    }

    fun persistHomeAppOrder(ordered: List<HomeApp>) {
        prefs.replaceHomeApps(
            apps = ordered.map { app ->
                HomeAppSlot(
                    label = app.label,
                    packageName = app.packageName,
                    activityClassName = app.activityClassName,
                    userString = app.userString,
                    isShortcut = app.isShortcut,
                    shortcutId = app.shortcutId
                )
            },
            maxSlots = prefs.maxHomeApps.coerceIn(1, 10)
        )
        onHomeAppsChanged()
    }

    fun commitReorder() {
        if (reorderList.isEmpty()) return
        persistHomeAppOrder(reorderList.toList())
    }

    fun handleDragDelta(deltaY: Float) {
        val fromKey = resolveDraggedIndex()
        val index = when {
            fromKey >= 0 -> fromKey
            liveDragIndex[0] >= 0 -> liveDragIndex[0]
            else -> -1
        }
        if (index < 0) return
        val previousIndex = index
        val workingList = if (liveDragList.isNotEmpty()) liveDragList.toList() else reorderList.toList()
        val (newList, newIndex, remaining) = HomeReorderHelper.applyDragCascade(
            list = workingList,
            draggedIndex = index,
            dragY = dragY + deltaY,
            itemHeights = itemHeights,
            keyOf = ::homeAppKey
        )
        if (newIndex != previousIndex || newList != workingList) {
            reorderList.clear()
            reorderList.addAll(newList)
            liveDragList.clear()
            liveDragList.addAll(newList)
            liveDragIndex[0] = newIndex
            if (newIndex in newList.indices) {
                val movedKey = homeAppKey(newList[newIndex])
                draggedKey = movedKey
                liveDragKey[0] = movedKey
            }
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        dragY = remaining
    }

    val onDragDelta = rememberUpdatedState<(Float) -> Unit>(::handleDragDelta)
    val onDragEndLatest = rememberUpdatedState<() -> Unit> {
        commitReorder()
        isDragging = false
        draggedKey = null
        liveDragIndex[0] = -1
        liveDragKey[0] = null
        liveDragList.clear()
        dragY = 0f
        onDraggingGlobalChanged(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight)
            .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                if (isDragging) return@detectTapGestures
                                onShowAppPicker()
                            }
                        )
                    }
            )
            val displayApps = if (isDragging) reorderList else state.homeApps

            val appArrangement = remember(computedSpacing, appVertical) {
                Arrangement.spacedBy(computedSpacing.dp, appVertical)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalAlignment = appAlign,
                verticalArrangement = appArrangement
            ) {
                displayApps.forEachIndexed { index, app ->
                    val itemKey = homeAppKey(app)
                    key(itemKey) {
                    val isThisDragged = isDragging && draggedKey == itemKey

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isThisDragged) 10f else 0f)
                            .onGloballyPositioned { coords ->
                                if (index in itemHeights.indices) {
                                    itemHeights[index] = coords.size.height.toFloat() + spacingPx
                                }
                            }
                            .graphicsLayer {
                                if (isThisDragged) {
                                    translationY = dragY
                                    scaleX = 1.05f
                                    scaleY = 1.05f
                                    alpha = 0.90f
                                }
                            }
                    ) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize * state.homeTextSizeScale,
                                letterSpacing = (-0.5).sp
                            ),
                            color = when {
                                isThisDragged -> MaterialTheme.colorScheme.primary
                                isDragging -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                                else -> MaterialTheme.colorScheme.onBackground
                            },
                            textAlign = appTextAlign,
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(itemKey) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            onDraggingGlobalChanged(true)
                                            isDragging = true
                                            reorderList.clear()
                                            reorderList.addAll(state.homeApps)
                                            liveDragList.clear()
                                            liveDragList.addAll(state.homeApps)
                                            draggedKey = itemKey
                                            liveDragKey[0] = itemKey
                                            liveDragIndex[0] = liveDragList.indexOfFirst {
                                                homeAppKey(it) == itemKey
                                            }.let { resolved -> if (resolved >= 0) resolved else index }
                                            dragY = 0f
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            onDragDelta.value(amount.y)
                                        },
                                        onDragEnd = {
                                            onDragEndLatest.value()
                                        },
                                        onDragCancel = {
                                            if (reorderList.isNotEmpty() &&
                                                reorderList.toList() != state.homeApps
                                            ) {
                                                commitReorder()
                                            }
                                            isDragging = false
                                            draggedKey = null
                                            liveDragIndex[0] = -1
                                            liveDragKey[0] = null
                                            liveDragList.clear()
                                            dragY = 0f
                                            onDraggingGlobalChanged(false)
                                        }
                                    )
                                }
                                .then(
                                    if (!isDragging) {
                                        Modifier.clickable { onAppClick(app) }
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(vertical = 16.dp)
                        )
                    }
                    }
                }
            }

            if (isDragging) {
                Text(
                    text = "Release to confirm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }

        HomeFooterBlock(
            alignment = appAlign,
            appTextAlign = appTextAlign,
            batteryLevel = state.batteryLevel
        )
    }
}

// ── Sub-composables ──

@Composable
private fun HomeFooterBlock(
    alignment: Alignment.Horizontal,
    appTextAlign: TextAlign,
    batteryLevel: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = alignment
    ) {
        Text(
            text = "$batteryLevel%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
            textAlign = appTextAlign,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HomeAppPickerSheet(
    currentApps: List<HomeApp>,
    maxApps: Int,
    onDismiss: () -> Unit,
    onHomeAppsChanged: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allApps = remember { mutableStateListOf<AppModel>() }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            allApps.addAll(getAppsList(context, prefs))
        }
    }

    val selectedApps = remember { mutableStateListOf<HomeApp>() }
    LaunchedEffect(currentApps) {
        selectedApps.clear()
        selectedApps.addAll(currentApps)
    }
    val filtered = remember(search, allApps.toList()) {
        if (search.isBlank()) allApps.toList()
        else allApps.filter { it.appLabel.contains(search, ignoreCase = true) }
    }

    fun persistDense(ordered: List<HomeApp>) {
        val next = ordered.take(maxApps.coerceIn(1, 10))
        selectedApps.clear()
        selectedApps.addAll(next)
        next.forEachIndexed { idx, app ->
            prefs.setAppAtLocation(
                idx + 1,
                app.label,
                app.packageName,
                app.activityClassName,
                app.userString,
                app.isShortcut,
                app.shortcutId
            )
        }
        for (slot in (next.size + 1)..maxApps.coerceIn(1, 10)) {
            prefs.setAppAtLocation(slot, "", "", null, "", false, "")
        }
        onHomeAppsChanged()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = VoidDimens.screenPadding)
        ) {
            Text(
                stringResource(R.string.home_apps_picker_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${selectedApps.size} / $maxApps selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = VoidDimens.compactSpacing, bottom = VoidDimens.rowSpacing)
            )
            if (selectedApps.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing),
                    verticalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
                ) {
                    selectedApps.forEach { app ->
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 140.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val remaining = selectedApps.filterNot {
                                            it.packageName == app.packageName &&
                                                it.userString == app.userString
                                        }
                                        persistDense(remaining)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.remove),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            VoidSectionDivider(modifier = Modifier.padding(vertical = VoidDimens.rowSpacing))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(stringResource(R.string.search_apps_to_add)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(VoidDimens.rowSpacing))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(
                    items = filtered,
                    key = { index, app -> "lib_${app.id}_$index" }
                ) { _, app ->
                    val isOnHome = selectedApps.any { it.packageName == app.appPackage }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isOnHome && selectedApps.size < maxApps) {
                                    val a = app as? AppModel.App ?: return@clickable
                                    persistDense(
                                        selectedApps.toList() + HomeApp(
                                            position = selectedApps.size + 1,
                                            label = a.appLabel,
                                            packageName = a.appPackage,
                                            activityClassName = a.activityClassName,
                                            userString = a.user.toString(),
                                            isShortcut = false,
                                            shortcutId = ""
                                        )
                                    )
                                }
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.appLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOnHome) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isOnHome) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (selectedApps.size < maxApps) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.add),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item(key = "picker_bottom") {
                    Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing))
                }
            }
        }
    }
}

private fun dispatchSwipeAction(
    direction: String,
    action: String,
    context: android.content.Context,
    onSummary: () -> Unit,
    onWidgets: () -> Unit,
    onNotes: () -> Unit,
    onNotifications: () -> Unit
) {
    val prefs = com.knownassurajit.app.launcher.voidlauncher.data.Prefs(context)
    when (action) {
        com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction.NOTIFICATION_SUMMARY -> {
            if (prefs.enableNotificationSummary) onSummary()
        }
        com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction.WIDGETS -> {
            if (prefs.enableWidgets) onWidgets()
        }
        com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction.NOTES -> {
            if (prefs.enableNotes) onNotes()
        }
        com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction.NOTIFICATIONS -> onNotifications()
        com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction.APP -> {
            val pkg = if (direction == "left") prefs.leftSwipeAppPackage else prefs.rightSwipeAppPackage
            if (pkg.isNotEmpty()) {
                try {
                    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
