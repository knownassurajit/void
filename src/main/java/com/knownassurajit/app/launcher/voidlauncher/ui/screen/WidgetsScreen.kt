package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.app.Application
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.WidgetInfo
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetBindHelper
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.components.ChildScreenBackHandler
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val WIDGET_HOST_ID = 1024

data class PendingWidgetBinding(
    val widget: WidgetInfo,
    val appWidgetId: Int,
    val configureIntent: Intent
)

sealed interface PinWidgetResult {
    data object Added : PinWidgetResult
    data class NeedsBind(val widget: WidgetInfo, val appWidgetId: Int, val bindIntent: Intent) : PinWidgetResult
    data class NeedsConfiguration(val binding: PendingWidgetBinding) : PinWidgetResult
    data object Denied : PinWidgetResult
}

private sealed interface WidgetGrant {
    data class Bind(val widget: WidgetInfo, val appWidgetId: Int) : WidgetGrant
    data class Configure(val binding: PendingWidgetBinding) : WidgetGrant
}

// ── ViewModel (Integrated Only Stub/Mock if needed, but here we keep it) ──

class WidgetsViewModel(application: Application) : AndroidViewModel(application) {
    private val ctx = application.applicationContext
    private val prefs = Prefs.get(ctx)

    val appWidgetHost = AppWidgetHost(ctx, WIDGET_HOST_ID)

    private val _allWidgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val allWidgets: StateFlow<List<WidgetInfo>> = _allWidgets.asStateFlow()

    private val _pinnedWidgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val pinnedWidgets: StateFlow<List<WidgetInfo>> = _pinnedWidgets.asStateFlow()

    private val _widgetIds = MutableStateFlow<Map<String, Int>>(emptyMap())
    val widgetIds: StateFlow<Map<String, Int>> = _widgetIds.asStateFlow()

    private val _widgetSpans = MutableStateFlow<Map<String, WidgetLayoutHelper.WidgetSpan>>(emptyMap())
    val widgetSpans: StateFlow<Map<String, WidgetLayoutHelper.WidgetSpan>> = _widgetSpans.asStateFlow()

    init { loadWidgets() }

    override fun onCleared() {
        super.onCleared()
        appWidgetHost.stopListening()
    }

    private fun loadWidgets() {
        try {
            val manager = AppWidgetManager.getInstance(ctx)
            val pm = ctx.packageManager
            val all = manager.installedProviders.mapNotNull { info ->
                try {
                    val label = info.loadLabel(pm)
                    val appName = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(info.provider.packageName, 0)).toString()
                    } catch (_: Exception) { info.provider.packageName }
                    val preview = try {
                        info.loadPreviewImage(ctx, ctx.resources.displayMetrics.densityDpi)
                    } catch (_: Exception) {
                        null
                    } ?: try {
                        pm.getApplicationIcon(info.provider.packageName)
                    } catch (_: Exception) {
                        null
                    }
                    WidgetInfo(provider = info, label = label, previewImage = preview, appName = appName)
                } catch (_: Exception) { null }
            }.sortedBy { it.appName }
            _allWidgets.value = all
            refreshPinnedAndIds(all)
        } catch (_: Exception) {}
    }

    private fun refreshPinnedAndIds(all: List<WidgetInfo> = _allWidgets.value) {
        var pinned = prefs.pinnedWidgets
        val idMap = buildWidgetIdMap()

        // Drop pinned entries that have no allocated ID (e.g., migrating from old version)
        val orphans = pinned.filter { key -> !idMap.containsKey(key) }
        if (orphans.isNotEmpty()) {
            pinned = pinned.toMutableSet().also { it.removeAll(orphans.toSet()) }
            prefs.pinnedWidgets = pinned
        }

        val orderedKeys = prefs.widgetOrder
        _pinnedWidgets.value = all.filter { widget ->
            val key = widget.provider.provider.flattenToString()
            idMap.containsKey(key) && pinned.contains(key)
        }.sortedBy { widget ->
            val index = orderedKeys.indexOf(widget.key)
            if (index < 0) Int.MAX_VALUE else index
        }
        _widgetIds.value = idMap
        refreshSpans()
    }

    private fun refreshSpans(pinned: List<WidgetInfo> = _pinnedWidgets.value) {
        val stored = prefs.widgetSpans.mapNotNull(WidgetLayoutHelper::parseSpan).toMap()
        val pinnedKeys = pinned.map { it.key }.toSet()
        _widgetSpans.value = stored.filterKeys { it in pinnedKeys }
    }

    private fun buildWidgetIdMap(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        prefs.widgetAllocatedIds.forEach { entry ->
            val idx = entry.lastIndexOf('|')
            if (idx > 0) {
                val id = entry.substring(idx + 1).toIntOrNull()
                if (id != null) map[entry.substring(0, idx)] = id
            }
        }
        return map
    }

    fun beginPinWidget(widget: WidgetInfo): PinWidgetResult {
        val key = widget.provider.provider.flattenToString()
        if (prefs.pinnedWidgets.contains(key)) return PinWidgetResult.Added

        val manager = AppWidgetManager.getInstance(ctx)
        val appWidgetId = appWidgetHost.allocateAppWidgetId()

        return try {
            val bound = manager.bindAppWidgetIdIfAllowed(appWidgetId, widget.provider.provider)
            if (bound) {
                afterBound(widget, appWidgetId)
            } else {
                val bindIntent = WidgetBindHelper.createBindIntent(
                    appWidgetId = appWidgetId,
                    provider = widget.provider.provider,
                    profile = widget.provider.profile
                )
                val canRequest = bindIntent.resolveActivity(ctx.packageManager) != null
                if (!canRequest) {
                    appWidgetHost.deleteAppWidgetId(appWidgetId)
                    PinWidgetResult.Denied
                } else {
                    PinWidgetResult.NeedsBind(widget, appWidgetId, bindIntent)
                }
            }
        } catch (_: Exception) {
            try { appWidgetHost.deleteAppWidgetId(appWidgetId) } catch (_: Exception) {}
            PinWidgetResult.Denied
        }
    }

    fun afterBound(widget: WidgetInfo, appWidgetId: Int): PinWidgetResult {
        val configure = widget.provider.configure
        return if (configure == null) {
            commitPin(widget, appWidgetId)
            PinWidgetResult.Added
        } else {
            PinWidgetResult.NeedsConfiguration(
                PendingWidgetBinding(
                    widget = widget,
                    appWidgetId = appWidgetId,
                    configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                        .setComponent(configure)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
            )
        }
    }

    fun releaseAllocatedId(appWidgetId: Int) {
        try { appWidgetHost.deleteAppWidgetId(appWidgetId) } catch (_: Exception) {}
    }

    fun commitPendingPin(binding: PendingWidgetBinding) {
        commitPin(binding.widget, binding.appWidgetId)
    }

    fun cancelPendingPin(binding: PendingWidgetBinding) {
        try { appWidgetHost.deleteAppWidgetId(binding.appWidgetId) } catch (_: Exception) {}
    }

    private fun commitPin(widget: WidgetInfo, appWidgetId: Int) {
        val key = widget.key
        val ids = prefs.widgetAllocatedIds.toMutableSet()
        ids.removeAll { it.startsWith("$key|") }
        ids.add("$key|$appWidgetId")
        prefs.widgetAllocatedIds = ids

        val pins = prefs.pinnedWidgets.toMutableSet()
        pins.add(key)
        prefs.pinnedWidgets = pins
        prefs.widgetOrder = (prefs.widgetOrder + key).distinct()
        refreshPinnedAndIds()
    }

    fun unpinWidget(widget: WidgetInfo) {
        val key = widget.provider.provider.flattenToString()
        prefs.widgetAllocatedIds.find { it.startsWith("$key|") }
            ?.substringAfterLast('|')?.toIntOrNull()
            ?.let { id -> try { appWidgetHost.deleteAppWidgetId(id) } catch (_: Exception) {} }

        prefs.widgetAllocatedIds = prefs.widgetAllocatedIds.toMutableSet()
            .also { it.removeAll { e -> e.startsWith("$key|") } }
        prefs.pinnedWidgets = prefs.pinnedWidgets.toMutableSet().also { it.remove(key) }
        prefs.widgetSpans = prefs.widgetSpans.toMutableSet().also { entries ->
            entries.removeAll { WidgetLayoutHelper.parseSpan(it)?.first == key }
        }
        refreshPinnedAndIds()
    }

    fun isPinned(widget: WidgetInfo): Boolean =
        prefs.pinnedWidgets.contains(widget.provider.provider.flattenToString())

    fun defaultSpan(
        widget: WidgetInfo,
        cellDp: Int = WidgetLayoutHelper.CELL_DP
    ): WidgetLayoutHelper.WidgetSpan {
        val targetWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            widget.provider.targetCellWidth
        } else {
            0
        }
        val targetHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            widget.provider.targetCellHeight
        } else {
            0
        }
        return WidgetLayoutHelper.defaultSpan(
            minWidthDp = widget.provider.minWidth,
            minHeightDp = widget.provider.minHeight,
            targetCellWidth = targetWidth,
            targetCellHeight = targetHeight,
            cellDp = cellDp
        )
    }

    fun setWidgetSpan(widget: WidgetInfo, span: WidgetLayoutHelper.WidgetSpan) {
        val next = WidgetLayoutHelper.clampSpan(span)
        val current = _widgetSpans.value[widget.key]
        if (current == next) return
        persistSpan(widget.key, next)
        _widgetSpans.value = _widgetSpans.value + (widget.key to next)
    }

    private fun persistSpan(key: String, span: WidgetLayoutHelper.WidgetSpan) {
        val values = prefs.widgetSpans.toMutableSet()
        values.removeAll { WidgetLayoutHelper.parseSpan(it)?.first == key }
        values.add(WidgetLayoutHelper.encodeSpan(key, span))
        prefs.widgetSpans = values
    }

    fun reorder(widget: WidgetInfo, offset: Int) {
        val current = _pinnedWidgets.value.map(WidgetInfo::key).toMutableList()
        val index = current.indexOf(widget.key)
        val target = index + offset
        if (index < 0 || target !in current.indices) return
        current[index] = current[target].also { current[target] = current[index] }
        prefs.widgetOrder = current
        refreshPinnedAndIds()
    }
}

// ── Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    onBack: () -> Unit,
    viewModel: WidgetsViewModel = viewModel()
) {
    val context = LocalContext.current
    val widgetBindDenied = stringResource(R.string.widget_bind_denied)
    if (!FeatureAvailability.isWidgetsAvailable(context)) {
        FeatureUnavailableScreen(
            stringResource(R.string.widgets),
            stringResource(R.string.widget_unavailable_body),
            onBack
        )
        return
    }

    val pinnedWidgets by viewModel.pinnedWidgets.collectAsState()
    val allWidgets by viewModel.allWidgets.collectAsState()
    val widgetIds by viewModel.widgetIds.collectAsState()
    val widgetSpans by viewModel.widgetSpans.collectAsState()
    var showPicker by remember { mutableStateOf(false) }
    var widgetToRemove by remember { mutableStateOf<WidgetInfo?>(null) }
    var selectedWidgetKey by remember { mutableStateOf<String?>(null) }
    var pendingGrant by remember { mutableStateOf<WidgetGrant?>(null) }
    var followUpIntent by remember { mutableStateOf<Intent?>(null) }
    val activityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (val grant = pendingGrant) {
            is WidgetGrant.Bind -> {
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    when (val next = viewModel.afterBound(grant.widget, grant.appWidgetId)) {
                        is PinWidgetResult.NeedsConfiguration -> {
                            pendingGrant = WidgetGrant.Configure(next.binding)
                            followUpIntent = next.binding.configureIntent
                            return@rememberLauncherForActivityResult
                        }
                        else -> Unit
                    }
                } else {
                    viewModel.releaseAllocatedId(grant.appWidgetId)
                }
                pendingGrant = null
            }
            is WidgetGrant.Configure -> {
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    viewModel.commitPendingPin(grant.binding)
                } else {
                    viewModel.cancelPendingPin(grant.binding)
                }
                pendingGrant = null
            }
            null -> Unit
        }
    }

    LaunchedEffect(followUpIntent) {
        val intent = followUpIntent ?: return@LaunchedEffect
        followUpIntent = null
        activityLauncher.launch(intent)
    }

    DisposableEffect(Unit) {
        viewModel.appWidgetHost.startListening()
        onDispose { viewModel.appWidgetHost.stopListening() }
    }

    ChildScreenBackHandler(onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            .screenBackSwipe(onBack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = VoidDimens.screenPadding,
                    end = VoidDimens.compactSpacing,
                    top = VoidDimens.screenPadding,
                    bottom = VoidDimens.rowSpacing
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.widgets),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = {
                selectedWidgetKey = null
                showPicker = true
            }) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_widget),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.widget_add), color = MaterialTheme.colorScheme.primary)
            }
        }
        VoidSectionDivider()

        if (pinnedWidgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.widget_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.widget_empty_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val spacing = WidgetLayoutHelper.GRID_SPACING_DP.dp
                val gridPadding = VoidDimens.screenPadding
                val cellDp = (
                    (maxWidth - gridPadding * 2 - spacing * (WidgetLayoutHelper.GRID_COLUMNS - 1)) /
                        WidgetLayoutHelper.GRID_COLUMNS
                    ).value.toInt().coerceAtLeast(WidgetLayoutHelper.MIN_HEIGHT_DP)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(WidgetLayoutHelper.GRID_COLUMNS),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = selectedWidgetKey == null,
                    contentPadding = PaddingValues(
                        horizontal = gridPadding,
                        vertical = VoidDimens.sectionSpacing
                    ),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    gridItems(
                        items = pinnedWidgets,
                        key = { it.provider.provider.flattenToString() },
                        span = { widget ->
                            val columns = widgetSpans[widget.key]?.columns
                                ?: viewModel.defaultSpan(widget, cellDp).columns
                            GridItemSpan(columns.coerceIn(1, WidgetLayoutHelper.GRID_COLUMNS))
                        }
                    ) { widget ->
                        val widgetId = widgetIds[widget.provider.provider.flattenToString()]
                        val span = widgetSpans[widget.key] ?: viewModel.defaultSpan(widget, cellDp)
                        LaunchedEffect(widget.key, cellDp, span.columns, span.rows) {
                            if (widgetSpans[widget.key] == null) {
                                viewModel.setWidgetSpan(widget, span)
                            }
                        }
                        if (widgetId != null) {
                            LiveWidgetItem(
                                widget = widget,
                                widgetId = widgetId,
                                appWidgetHost = viewModel.appWidgetHost,
                                span = span,
                                cellDp = cellDp,
                                selected = selectedWidgetKey == widget.key,
                                onSelected = { selectedWidgetKey = widget.key },
                                onDeselect = { selectedWidgetKey = null },
                                onRemoveClick = { widgetToRemove = widget },
                                onConfigureClick = {
                                    widget.provider.configure?.let { configure ->
                                        activityLauncher.launch(
                                            Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                                                .setComponent(configure)
                                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                        )
                                    }
                                },
                                onSpanChange = { next -> viewModel.setWidgetSpan(widget, next) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        WidgetPickerSheet(
            allWidgets = allWidgets,
            pinnedKeys = pinnedWidgets.map { it.key }.toSet(),
            onDismiss = { showPicker = false },
            onPin = { widget ->
                when (val result = viewModel.beginPinWidget(widget)) {
                    PinWidgetResult.Added -> Unit
                    is PinWidgetResult.NeedsBind -> {
                        pendingGrant = WidgetGrant.Bind(result.widget, result.appWidgetId)
                        activityLauncher.launch(result.bindIntent)
                    }
                    is PinWidgetResult.NeedsConfiguration -> {
                        pendingGrant = WidgetGrant.Configure(result.binding)
                        activityLauncher.launch(result.binding.configureIntent)
                    }
                    PinWidgetResult.Denied -> Toast.makeText(
                        context,
                        widgetBindDenied,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    widgetToRemove?.let { widget ->
        AlertDialog(
            onDismissRequest = { widgetToRemove = null },
            title = { Text(stringResource(R.string.widget_remove_title), style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    stringResource(R.string.widget_remove_message, widget.label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unpinWidget(widget)
                    selectedWidgetKey = null
                    widgetToRemove = null
                }) {
                    Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { widgetToRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun LiveWidgetItem(
    widget: WidgetInfo,
    widgetId: Int,
    appWidgetHost: AppWidgetHost,
    span: WidgetLayoutHelper.WidgetSpan,
    cellDp: Int,
    selected: Boolean,
    onSelected: () -> Unit,
    onDeselect: () -> Unit,
    onRemoveClick: () -> Unit,
    onConfigureClick: () -> Unit,
    onSpanChange: (WidgetLayoutHelper.WidgetSpan) -> Unit
) {
    val heightDp = WidgetLayoutHelper.heightForRows(span.rows, cellDp)
    val density = LocalDensity.current
    val shape = RoundedCornerShape(12.dp)
    val spanLatest = rememberUpdatedState(span)
    val onSpanChangeLatest = rememberUpdatedState(onSpanChange)
    val onSelectedLatest = rememberUpdatedState(onSelected)
    val onDeselectLatest = rememberUpdatedState(onDeselect)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                }
            )
            .pointerInput(selected, widget.key) {
                if (selected) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                    val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis.toLong()) {
                        waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    }
                    if (up == null) {
                        val stillDown = currentEvent.changes.any { it.id == down.id && it.pressed }
                        if (stillDown) {
                            onSelectedLatest.value()
                            currentEvent.changes.forEach { it.consume() }
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                                if (event.changes.none { it.pressed }) break
                            }
                        }
                    }
                }
            }
    ) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(1)
        AndroidView(
            factory = { ctx ->
                try {
                    appWidgetHost.createView(ctx, widgetId, widget.provider).apply {
                        setAppWidget(widgetId, widget.provider)
                        isLongClickable = true
                        setOnLongClickListener {
                            onSelectedLatest.value()
                            true
                        }
                        WidgetLayoutHelper.applyHostSize(this, widgetId, widthDp, heightDp)
                    }
                } catch (_: Exception) {
                    android.widget.FrameLayout(ctx)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(shape),
            update = { view ->
                val host = view as? AppWidgetHostView ?: return@AndroidView
                host.isLongClickable = true
                host.setOnLongClickListener {
                    onSelectedLatest.value()
                    true
                }
                WidgetLayoutHelper.applyHostSize(host, widgetId, widthDp, heightDp)
            }
        )
        if (selected) {
            val resizeLabel = stringResource(R.string.widget_resize_handle)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(widget.key, cellDp) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            val origin = spanLatest.value
                            var dragX = 0f
                            var dragY = 0f
                            var started = false
                            drag(down.id) { change ->
                                val delta = change.positionChange()
                                change.consume()
                                dragX += delta.x
                                dragY += delta.y
                                if (!started) {
                                    val slop = viewConfiguration.touchSlop
                                    if (kotlin.math.abs(dragX) <= slop && kotlin.math.abs(dragY) <= slop) {
                                        return@drag
                                    }
                                    started = true
                                }
                                val dxDp = with(density) { dragX.toDp().value }
                                val dyDp = with(density) { dragY.toDp().value }
                                onSpanChangeLatest.value(
                                    WidgetLayoutHelper.snapSpan(
                                        start = origin,
                                        deltaXDp = dxDp,
                                        deltaYDp = dyDp,
                                        cellDp = cellDp
                                    )
                                )
                            }
                            if (!started) onDeselectLatest.value()
                        }
                    }
            )
            if (widget.provider.configure != null) {
                IconButton(
                    onClick = onConfigureClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .zIndex(1f)
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.widget_configure),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.widget_remove),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(1f)
                    .size(48.dp)
                    .semantics { contentDescription = resizeLabel },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetPickerSheet(
    allWidgets: List<WidgetInfo>,
    pinnedKeys: Set<String>,
    onDismiss: () -> Unit,
    onPin: (WidgetInfo) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val groupedWidgets = remember(allWidgets) {
        allWidgets.groupBy { it.appName }.toSortedMap()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(horizontal = VoidDimens.screenPadding - 4.dp)) {
            Text(
                stringResource(R.string.widget_picker_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = VoidDimens.sectionSpacing)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f),
                verticalArrangement = Arrangement.spacedBy(VoidDimens.compactSpacing),
                contentPadding = PaddingValues(bottom = VoidDimens.sectionSpacing)
            ) {
                groupedWidgets.forEach { (appName, widgets) ->
                    item(key = "group_$appName") {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)) {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 8.dp)
                            ) {
                                items(
                                    items = widgets,
                                    key = { it.provider.provider.flattenToString() }
                                ) { widget ->
                                    val pinned = widget.key in pinnedKeys
                                    val cardWidth = WidgetLayoutHelper.previewCardWidthDp(
                                        minWidthDp = widget.provider.minWidth,
                                        minHeightDp = widget.provider.minHeight
                                    )
                                    Column(
                                        modifier = Modifier
                                            .width(cardWidth.dp)
                                            .clickable(enabled = !pinned) { onPin(widget) }
                                    ) {
                                        WidgetPreviewThumb(
                                            drawable = widget.previewImage,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(96.dp)
                                        )
                                        Text(
                                            text = widget.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                        Icon(
                                            if (pinned) Icons.Outlined.Check else Icons.Outlined.Add,
                                            contentDescription = null,
                                            tint = if (pinned) MaterialTheme.colorScheme.primary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WidgetPreviewThumb(
    drawable: Drawable?,
    modifier: Modifier = Modifier
) {
    val thumbModifier = modifier
        .clip(MaterialTheme.shapes.small)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    if (drawable == null) {
        Box(thumbModifier)
        return
    }
    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setImageDrawable(drawable)
            }
        },
        update = { imageView -> imageView.setImageDrawable(drawable) },
        modifier = thumbModifier
    )
}
