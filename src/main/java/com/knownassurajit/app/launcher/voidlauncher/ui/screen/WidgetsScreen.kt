package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.app.Application
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSearchField
import androidx.core.view.drawToBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateIntOffsetAsState
import android.view.View
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.WidgetInfo
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.ListenerCount
import com.knownassurajit.app.launcher.voidlauncher.helper.ListeningLease
import com.knownassurajit.app.launcher.voidlauncher.helper.VoidAppWidgetHost
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetBindHelper
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetGrid
import com.knownassurajit.app.launcher.voidlauncher.helper.WidgetLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import com.knownassurajit.app.launcher.voidlauncher.ui.components.RevealHandle
import com.knownassurajit.app.launcher.voidlauncher.ui.components.RevealHeaderConnection
import com.knownassurajit.app.launcher.voidlauncher.ui.components.RevealHeaderState
import com.knownassurajit.app.launcher.voidlauncher.ui.components.revealHeader
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalPanelSettled
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.stoppingAtOnePixel
import kotlinx.coroutines.flow.first

private const val WIDGET_HOST_ID = 1024
private const val PREVIEW_CACHE_SIZE = 64
private const val TAG = "WidgetsViewModel"

/** Shared by every [WidgetsViewModel]: the host id is registered once per process. */
private val hostListeners = ListenerCount()

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

    val appWidgetHost: AppWidgetHost = VoidAppWidgetHost(ctx, WIDGET_HOST_ID)

    // Every Widgets screen builds its own host, but Android keeps one registration per host id.
    // Screens overlap (the closing one stays until its exit animation ends), so a screen must not
    // stop what a newer one started.
    private val hostListening = ListeningLease(
        count = hostListeners,
        start = {
            Log.d(TAG, "widget_host_listening state=start")
            appWidgetHost.startListening()
        },
        stop = {
            Log.d(TAG, "widget_host_listening state=stop")
            appWidgetHost.stopListening()
        }
    )

    private val _allWidgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val allWidgets: StateFlow<List<WidgetInfo>> = _allWidgets.asStateFlow()

    private val _pinnedWidgets = MutableStateFlow<List<WidgetInfo>>(emptyList())
    val pinnedWidgets: StateFlow<List<WidgetInfo>> = _pinnedWidgets.asStateFlow()

    private val _widgetIds = MutableStateFlow<Map<String, Int>>(emptyMap())
    val widgetIds: StateFlow<Map<String, Int>> = _widgetIds.asStateFlow()

    private val _widgetSpans = MutableStateFlow<Map<String, WidgetLayoutHelper.WidgetSpan>>(emptyMap())
    val widgetSpans: StateFlow<Map<String, WidgetLayoutHelper.WidgetSpan>> = _widgetSpans.asStateFlow()

    private val _widgetPositions = MutableStateFlow<Map<String, WidgetGrid.Cell>>(emptyMap())
    val widgetPositions: StateFlow<Map<String, WidgetGrid.Cell>> = _widgetPositions.asStateFlow()

    // Where a widget dropped from the picker goes once the bind/configure round trip finishes.
    private val pendingCells = mutableMapOf<String, WidgetGrid.Cell>()

    // The provider of every pinned widget and, once the picker needs them, of every widget on the device.
    private val known = LinkedHashMap<String, WidgetInfo>()
    private var providersJob: Job? = null

    init { loadPinnedWidgets() }

    fun startListening() = hostListening.acquire()

    fun stopListening() = hostListening.release()

    override fun onCleared() {
        super.onCleared()
        hostListening.release()
    }

    // The pinned widgets come first and alone. Asking the system for the provider of each widget that is
    // bound takes a few milliseconds; listing every provider on the device and loading its label took long
    // enough to put the first widget on screen in the middle of the slide-in of the screen.
    private fun loadPinnedWidgets() = viewModelScope.launch {
        val pinned = withContext(Dispatchers.IO) { queryPinnedWidgets() }
        pinned.forEach { known[it.key] = it }
        refreshPinnedAndIds()
        _isLoading.value = false
    }

    /** Lists every provider for the picker. It is heavy (each label is loaded), so it runs once, off the main thread; later calls do nothing. */
    fun loadProviders() {
        if (providersJob != null) return
        providersJob = viewModelScope.launch {
            _isLoadingProviders.value = true
            val all = withContext(Dispatchers.IO) { queryWidgets() }
            // A pinned widget keeps the provider it was bound with.
            all.forEach { known.putIfAbsent(it.key, it) }
            _allWidgets.value = all
            _isLoadingProviders.value = false
        }
    }

    /** True until the pinned widgets are known. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** True while the provider list for the picker loads. */
    private val _isLoadingProviders = MutableStateFlow(false)
    val isLoadingProviders: StateFlow<Boolean> = _isLoadingProviders.asStateFlow()

    private val previews = android.util.LruCache<String, Drawable>(PREVIEW_CACHE_SIZE)

    /** The preview the picker shows for [widget]: decoded off the main thread once, then cached. */
    suspend fun previewFor(widget: WidgetInfo): Drawable? {
        previews.get(widget.key)?.let { return it }
        val loaded = withContext(Dispatchers.IO) {
            runCatching { widget.provider.loadPreviewImage(ctx, ctx.resources.displayMetrics.densityDpi) }.getOrNull()
                ?: runCatching { ctx.packageManager.getApplicationIcon(widget.provider.provider.packageName) }.getOrNull()
        }
        loaded?.let { previews.put(widget.key, it) }
        return loaded
    }

    fun cachedPreview(widget: WidgetInfo): Drawable? = previews.get(widget.key)

    private fun appNameOf(packageName: String, appNames: MutableMap<String, String>): String =
        appNames.getOrPut(packageName) {
            val pm = ctx.packageManager
            runCatching { pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString() }.getOrDefault(packageName)
        }

    /** The provider of every widget that is pinned and bound, asked of the system by widget id. */
    private fun queryPinnedWidgets(): List<WidgetInfo> {
        return try {
            val manager = AppWidgetManager.getInstance(ctx)
            val pm = ctx.packageManager
            val appNames = HashMap<String, String>()
            val started = android.os.SystemClock.elapsedRealtime()
            val pinned = prefs.pinnedWidgets
            val widgets = buildWidgetIdMap().mapNotNull { (key, id) ->
                if (key !in pinned) return@mapNotNull null
                val info = try { manager.getAppWidgetInfo(id) } catch (_: Exception) { null }
                // A widget id the system no longer knows, or one now bound to another provider, is not shown.
                if (info == null || info.provider.flattenToString() != key) return@mapNotNull null
                WidgetInfo(provider = info, label = info.loadLabel(pm), appName = appNameOf(info.provider.packageName, appNames))
            }
            Log.d(TAG, "widget_pinned_loaded count=${widgets.size} ms=${android.os.SystemClock.elapsedRealtime() - started}")
            widgets
        } catch (e: Exception) {
            Log.w(TAG, "widget_pinned_failed reason=${e.javaClass.simpleName}")
            emptyList()
        }
    }

    private fun queryWidgets(): List<WidgetInfo> {
        return try {
            val manager = AppWidgetManager.getInstance(ctx)
            val pm = ctx.packageManager
            val appNames = HashMap<String, String>()
            val started = android.os.SystemClock.elapsedRealtime()
            val all = manager.installedProviders.mapNotNull { info ->
                try {
                    WidgetInfo(provider = info, label = info.loadLabel(pm), appName = appNameOf(info.provider.packageName, appNames))
                } catch (_: Exception) { null }
            }.sortedBy { it.appName }
            Log.d(TAG, "widget_providers_loaded count=${all.size} ms=${android.os.SystemClock.elapsedRealtime() - started}")
            all
        } catch (e: Exception) {
            Log.w(TAG, "widget_providers_failed reason=${e.javaClass.simpleName}")
            emptyList()
        }
    }

    private fun refreshPinnedAndIds() {
        var pinned = prefs.pinnedWidgets
        val idMap = buildWidgetIdMap()

        // Drop pinned entries that have no allocated ID (e.g., migrating from old version)
        val orphans = pinned.filter { key -> !idMap.containsKey(key) }
        if (orphans.isNotEmpty()) {
            pinned = pinned.toMutableSet().also { it.removeAll(orphans.toSet()) }
            prefs.pinnedWidgets = pinned
        }

        val orderedKeys = prefs.widgetOrder
        _pinnedWidgets.value = known.values.filter { widget ->
            idMap.containsKey(widget.key) && pinned.contains(widget.key)
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
        _widgetPositions.value = prefs.widgetPositions.mapNotNull(WidgetGrid::parseCell).toMap()
            .filterKeys { it in pinnedKeys }
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

    /** Starts adding [widget]; [cell] is where it was dropped on the grid, `null` for the first free cell. */
    fun beginPinWidget(widget: WidgetInfo, cell: WidgetGrid.Cell? = null): PinWidgetResult {
        val key = widget.provider.provider.flattenToString()
        if (prefs.pinnedWidgets.contains(key)) return PinWidgetResult.Added
        if (cell != null) pendingCells[key] = cell else pendingCells.remove(key)

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

    fun releaseAllocatedId(appWidgetId: Int, widget: WidgetInfo? = null) {
        widget?.let { pendingCells.remove(it.key) }
        try { appWidgetHost.deleteAppWidgetId(appWidgetId) } catch (_: Exception) {}
    }

    fun commitPendingPin(binding: PendingWidgetBinding) {
        commitPin(binding.widget, binding.appWidgetId)
    }

    fun cancelPendingPin(binding: PendingWidgetBinding) {
        pendingCells.remove(binding.widget.key)
        try { appWidgetHost.deleteAppWidgetId(binding.appWidgetId) } catch (_: Exception) {}
    }

    private fun commitPin(widget: WidgetInfo, appWidgetId: Int) {
        val key = widget.key
        known[key] = widget
        val ids = prefs.widgetAllocatedIds.toMutableSet()
        ids.removeAll { it.startsWith("$key|") }
        ids.add("$key|$appWidgetId")
        prefs.widgetAllocatedIds = ids

        val pins = prefs.pinnedWidgets.toMutableSet()
        pins.add(key)
        prefs.pinnedWidgets = pins
        prefs.widgetOrder = (prefs.widgetOrder + key).distinct()
        // The grid screen re-checks the cell against the current layout and moves it to a free one if needed.
        pendingCells.remove(key)?.let { cell -> persistPosition(key, cell) }
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
        prefs.widgetPositions = prefs.widgetPositions.filterNot { WidgetGrid.parseCell(it)?.first == key }.toSet()
        prefs.widgetOrder = prefs.widgetOrder - key
        refreshPinnedAndIds()
    }

    /** Saves where widgets sit, after a move or after the grid resolved missing or colliding cells. */
    fun savePlacements(placements: List<WidgetGrid.Placement>) {
        val cells = placements.associate { it.key to it.cell }
        if (cells == _widgetPositions.value) return
        val others = prefs.widgetPositions.filterNot { entry ->
            WidgetGrid.parseCell(entry)?.first in cells.keys
        }
        prefs.widgetPositions = others.toSet() + cells.map { (key, cell) -> WidgetGrid.encodeCell(key, cell) }
        _widgetPositions.value = _widgetPositions.value + cells
    }

    private fun persistPosition(key: String, cell: WidgetGrid.Cell) {
        prefs.widgetPositions = prefs.widgetPositions.filterNot { WidgetGrid.parseCell(it)?.first == key }.toSet() +
            WidgetGrid.encodeCell(key, cell)
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

}

// ── Screen ──

/**
 * A widget held under the finger: one from the picker ([isNew], not pinned yet) or a pinned one being
 * moved. [grab] is where the finger holds it, from its top-left corner, in px; [snapshot] is what a
 * pinned widget looked like when it was lifted.
 */
private data class WidgetDrag(
    val widget: WidgetInfo,
    val span: WidgetLayoutHelper.WidgetSpan,
    val isNew: Boolean,
    val pointerInRoot: Offset,
    val grab: Offset,
    val snapshot: ImageBitmap? = null
)

/** Grid geometry for the current width: square cells of [cellDp] with [spacingDp] gaps. */
internal data class GridMetrics(val cellDp: Int, val spacingDp: Int) {
    val stepDp: Float get() = (cellDp + spacingDp).toFloat()
}

/**
 * Where things are on screen, read while a widget is held. Plain fields, not state: they change on
 * every scroll frame, and as state they recomposed every widget host view each frame.
 */
private class WidgetLayoutRefs {
    var screenOrigin: Offset = Offset.Zero
    var gridOrigin: Offset = Offset.Zero
    var viewport: Rect = Rect.Zero

    /** Where each pinned widget is, by key; read only when a widget is picked, to scroll it clear of the edit bar. */
    val coordinates: MutableMap<String, LayoutCoordinates> = HashMap()
}

private val AUTO_SCROLL_EDGE = 72.dp
private const val AUTO_SCROLL_STEP_PX = 18f
private const val AUTO_SCROLL_FRAME_MS = 16L
private const val GHOST_ALPHA = 0.92f
private const val LIFTED_ALPHA = 0.3f
private const val GHOST_SCALE = 1.04f

/** A picked widget rises a little; the others are dimmed. */
private const val SELECTED_SCALE = 1.02f
private const val DIMMED_ALPHA = 0.55f
private val OUTLINE_WIDTH = 2.dp

/** How long the tile that holds a widget's place stays after the view is built, while the provider draws. */
private const val PLACEHOLDER_HOLD_MS = 200L

/** Release speed of a finger, per second, that opens or closes the header whatever distance it was pulled. */
private val HEADER_FLING_VELOCITY = 400.dp

/** Space kept under the grid and above the screen edge for the edit bar. */
private val EDIT_BAR_RESERVE = 176.dp

/** How far the pull handle reaches up from the content area, into the status bar's space. */
private val HANDLE_LIFT = 28.dp

/** How long to wait for the grid to grow under the edit bar before a picked widget is scrolled clear. */
private const val EDIT_SCROLL_WAIT_MS = 400L

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val widgetPositions by viewModel.widgetPositions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingProviders by viewModel.isLoadingProviders.collectAsState()
    var showPicker by remember { mutableStateOf(false) }
    var widgetToRemove by remember { mutableStateOf<WidgetInfo?>(null) }
    var selectedWidgetKey by remember { mutableStateOf<String?>(null) }
    var pendingGrant by remember { mutableStateOf<WidgetGrant?>(null) }
    var followUpIntent by remember { mutableStateOf<Intent?>(null) }
    var drag by remember { mutableStateOf<WidgetDrag?>(null) }
    // Bumped after each auto-scroll step, so the landing cell is recomputed while the finger rests.
    var scrollTick by remember { mutableIntStateOf(0) }
    val refs = remember { WidgetLayoutRefs() }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    // The grid spans the screen width minus its side padding: four square cells and three gaps.
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val metrics = remember(screenWidthDp) {
        val spacing = WidgetLayoutHelper.GRID_SPACING_DP
        val columns = WidgetLayoutHelper.GRID_COLUMNS
        val cell = ((screenWidthDp - VoidDimens.screenPadding.value * 2 - spacing * (columns - 1)) / columns)
            .toInt()
            .coerceAtLeast(WidgetLayoutHelper.MIN_HEIGHT_DP)
        GridMetrics(cell, spacing)
    }

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
                    viewModel.releaseAllocatedId(grant.appWidgetId, grant.widget)
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
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    fun pin(widget: WidgetInfo, cell: WidgetGrid.Cell?) {
        when (val result = viewModel.beginPinWidget(widget, cell)) {
            PinWidgetResult.Added -> Unit
            is PinWidgetResult.NeedsBind -> {
                pendingGrant = WidgetGrant.Bind(result.widget, result.appWidgetId)
                activityLauncher.launch(result.bindIntent)
            }
            is PinWidgetResult.NeedsConfiguration -> {
                pendingGrant = WidgetGrant.Configure(result.binding)
                activityLauncher.launch(result.binding.configureIntent)
            }
            PinWidgetResult.Denied -> Toast.makeText(context, widgetBindDenied, Toast.LENGTH_LONG).show()
        }
    }

    val spans = pinnedWidgets.associate { widget ->
        widget.key to (widgetSpans[widget.key] ?: viewModel.defaultSpan(widget, metrics.cellDp))
    }
    val placements = remember(pinnedWidgets, spans, widgetPositions) {
        WidgetGrid.resolve(pinnedWidgets.map { it.key }, widgetPositions, spans)
    }
    // Widgets without a stored cell or span (new, older layouts, collisions) get theirs saved once.
    LaunchedEffect(placements) {
        viewModel.savePlacements(placements)
        pinnedWidgets.forEach { widget ->
            if (widgetSpans[widget.key] == null) spans[widget.key]?.let { viewModel.setWidgetSpan(widget, it) }
        }
    }

    // Where the held widget lands: the free cell nearest to the one under it.
    val dropCell: WidgetGrid.Cell? = drag?.let { current ->
        scrollTick // Read so an auto-scroll step recomputes the cell.
        val topLeft = current.pointerInRoot - current.grab - refs.gridOrigin
        val wanted = WidgetGrid.cellAt(
            xDp = with(density) { topLeft.x.toDp().value },
            yDp = with(density) { topLeft.y.toDp().value },
            stepDp = metrics.stepDp,
            span = current.span
        )
        val others = placements.filterNot { it.key == current.widget.key }
        WidgetGrid.nearestFreeCell(others, current.widget.key, current.span, wanted)
    }

    fun endDrag(commit: Boolean) {
        val current = drag ?: return
        val cell = dropCell
        drag = null
        if (!commit || cell == null) return
        haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
        if (current.isNew) {
            showPicker = false
            pin(current.widget, cell)
        } else {
            viewModel.savePlacements(placements.map { if (it.key == current.widget.key) it.copy(cell = cell) else it })
        }
    }

    // While a widget is held near the top or bottom of the grid, scroll the grid toward that edge.
    LaunchedEffect(drag != null) {
        while (drag != null) {
            val y = drag?.pointerInRoot?.y ?: break
            val edge = with(density) { AUTO_SCROLL_EDGE.toPx() }
            val step = when {
                y > refs.viewport.bottom - edge -> AUTO_SCROLL_STEP_PX
                y < refs.viewport.top + edge -> -AUTO_SCROLL_STEP_PX
                else -> 0f
            }
            if (step != 0f && scrollState.scrollBy(step) != 0f) scrollTick++
            delay(AUTO_SCROLL_FRAME_MS)
        }
    }

    val editing = drag != null || selectedWidgetKey != null
    val motion = MaterialTheme.motionScheme
    val scope = rememberCoroutineScope()
    val panelSettled by LocalPanelSettled.current
    val selectedWidget = pinnedWidgets.firstOrNull { it.key == selectedWidgetKey }
    // The edit bar slides out with the widget it showed, so it keeps that widget until it has left.
    val barWidget = remember { arrayOfNulls<WidgetInfo>(1) }
    if (selectedWidget != null) barWidget[0] = selectedWidget

    // Nothing to hide a header from when there is no widget: the header stays out and offers Add widget.
    val headerPinnedOpen = !isLoading && pinnedWidgets.isEmpty()
    val headerState = remember { RevealHeaderState() }
    val settleSpec = motion.defaultSpatialSpec<Float>()
    val settleSpecLatest = rememberUpdatedState(settleSpec)
    val headerEnabled by rememberUpdatedState(!editing && !showPicker && !headerPinnedOpen)
    val flingVelocityPx = with(density) { HEADER_FLING_VELOCITY.toPx() }
    val revealConnection = remember(headerState, flingVelocityPx) {
        RevealHeaderConnection(
            state = headerState,
            flingVelocityPx = flingVelocityPx,
            settleSpec = { settleSpecLatest.value },
            isEnabled = { headerEnabled }
        )
    }
    // It is out while the screen is empty and nothing else is going on, away while a widget is being
    // customized or added, and otherwise (a screen with widgets) left to the user's pull.
    val headerForcedOpen = headerPinnedOpen && !editing && !showPicker
    LaunchedEffect(headerForcedOpen, editing, showPicker) {
        if (headerForcedOpen) headerState.show(settleSpec) else headerState.hide(settleSpec)
    }
    // Leaving customize mode with the system Back puts the widget down instead of leaving the screen.
    BackHandler(enabled = selectedWidgetKey != null && drag == null) { selectedWidgetKey = null }
    // The picker's provider list is the heavy part of this screen. It loads in the background once the
    // panel has arrived, so it is ready by the time Add widget is tapped.
    LaunchedEffect(panelSettled, showPicker) {
        if (panelSettled || showPicker) viewModel.loadProviders()
    }
    // The widget views are built after the slide-in, one per frame (see [rememberStagedKeys]).
    val builtKeys = rememberStagedKeys(
        keys = placements.map { it.key },
        enabled = panelSettled && !isLoading
    )
    // A widget picked for customizing may sit under the bar: bring it into the clear part of the screen.
    LaunchedEffect(selectedWidgetKey) {
        val key = selectedWidgetKey ?: return@LaunchedEffect
        val bounds = refs.coordinates[key]?.takeIf { it.isAttached }?.boundsInRoot() ?: return@LaunchedEffect
        val margin = with(density) { VoidDimens.sectionSpacing.toPx() }
        val clearTop = refs.viewport.top + margin
        val clearBottom = refs.viewport.bottom - with(density) { EDIT_BAR_RESERVE.toPx() }
        val delta = when {
            // Never past the widget's own top: one taller than the clear space stays where it is.
            bounds.bottom > clearBottom -> minOf(bounds.bottom - clearBottom, bounds.top - clearTop).coerceAtLeast(0f)
            bounds.top < clearTop -> bounds.top - clearTop
            else -> 0f
        }
        // The room under the last widget appears with the next layout: scroll once the range has it.
        if (delta > 0f) {
            withTimeoutOrNull(EDIT_SCROLL_WAIT_MS) { snapshotFlow { scrollState.maxValue }.first { it >= scrollState.value + delta } }
        }
        if (delta != 0f) scrollState.animateScrollBy(delta, motion.defaultSpatialSpec())
    }

    val showOptionsLabel = stringResource(R.string.widget_header_show)
    val statusBarHeight = LocalFixedStatusBarHeight.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { refs.screenOrigin = it.positionInRoot() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarHeight)
                .navigationBarsPadding()
                .screenBackSwipe(onBack, enabled = LocalContentSwipeToBack.current && !editing)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { refs.viewport = it.boundsInRoot() }
                    .nestedScroll(revealConnection)
            ) {
                val visibleRows = ((maxHeight - VoidDimens.sectionSpacing * 2).value / metrics.stepDp).toInt()
                val rows = maxOf(
                    WidgetGrid.rowCount(placements) + if (editing) 1 else 0,
                    dropCell?.let { it.row + (drag?.span?.rows ?: 1) } ?: 0,
                    if (editing) visibleRows else 0
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState, enabled = drag == null)
                ) {
                    WidgetGridCanvas(
                        rows = rows,
                        metrics = metrics,
                        showCells = editing,
                        dropCell = dropCell,
                        dropSpan = drag?.span,
                        onBackgroundTap = {
                            selectedWidgetKey = null
                            // A tap beside the widgets also puts a header that was pulled out away.
                            if (headerEnabled) scope.launch { headerState.hide(settleSpec) }
                        },
                        modifier = Modifier
                            .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.sectionSpacing)
                            .onGloballyPositioned { refs.gridOrigin = it.positionInRoot() }
                    ) {
                        val byKey = pinnedWidgets.associateBy { it.key }
                        placements.forEach { placement ->
                            val widget = byKey[placement.key] ?: return@forEach
                            val widgetId = widgetIds[placement.key] ?: return@forEach
                            key(placement.key) {
                                LiveWidgetItem(
                                    widget = widget,
                                    widgetId = widgetId,
                                    appWidgetHost = viewModel.appWidgetHost,
                                    placement = placement,
                                    metrics = metrics,
                                    viewBuilt = placement.key in builtKeys,
                                    selected = selectedWidgetKey == widget.key,
                                    dimmed = selectedWidgetKey != null && selectedWidgetKey != widget.key,
                                    lifted = drag?.widget?.key == widget.key,
                                    onPositioned = { coordinates -> refs.coordinates[widget.key] = coordinates },
                                    onSelected = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedWidgetKey = widget.key
                                    },
                                    onDeselect = { selectedWidgetKey = null },
                                    onMoveStart = { grab, pointer, snapshot ->
                                        drag = WidgetDrag(widget, placement.span, isNew = false, pointerInRoot = pointer, grab = grab, snapshot = snapshot)
                                    },
                                    onMove = { pointer -> drag = drag?.copy(pointerInRoot = pointer) },
                                    onMoveEnd = { endDrag(commit = true) },
                                    onMoveBy = { columns, rowsDelta ->
                                        val others = placements.filterNot { it.key == widget.key }
                                        WidgetGrid.moved(others, placement, columns, rowsDelta)?.let { cell ->
                                            viewModel.savePlacements(placements.map { if (it.key == widget.key) it.copy(cell = cell) else it })
                                        }
                                    },
                                    onResize = { requested ->
                                        val others = placements.filterNot { it.key == widget.key }
                                        WidgetGrid.fitSpan(others, widget.key, placement.cell, requested)
                                            ?.let { fitted -> viewModel.setWidgetSpan(widget, fitted) }
                                    }
                                )
                            }
                        }
                    }
                    // Room under the last widget for the edit bar, so any widget can be scrolled clear of it.
                    if (selectedWidgetKey != null) Spacer(Modifier.height(EDIT_BAR_RESERVE))
                }

                when {
                    isLoading && pinnedWidgets.isEmpty() -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                    pinnedWidgets.isEmpty() && drag == null -> Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(VoidDimens.screenPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.widget_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(VoidDimens.compactSpacing))
                        Text(
                            stringResource(R.string.widget_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // The header floats over the grid so that showing it never moves a widget. It is composed while
        // it is hidden (it must be measured to be pulled), so it is also kept from a screen reader and
        // from keyboard focus then.
        val headerHidden by remember { derivedStateOf { headerState.isHidden } }
        // Nobody pulls it during the slide-in, so it is composed (two buttons, a surface) once the panel has arrived.
        if (panelSettled) {
            WidgetsHeader(
                onAddWidget = { showPicker = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .revealHeader(headerState)
                    .then(
                        if (headerHidden) Modifier.clearAndSetSemantics {}.focusProperties { canFocus = false } else Modifier
                    )
            )
        }
        // The only mark on a full-screen grid: a short bar under the status bar that says the options
        // are above. It is a bigger target than it looks and fades as the header comes in.
        val handleActive by remember { derivedStateOf { !headerState.isFullyShown } }
        if (!headerPinnedOpen && !editing && !showPicker && handleActive) {
            RevealHandle(
                state = headerState,
                description = showOptionsLabel,
                flingVelocityPx = flingVelocityPx,
                settleSpec = settleSpec,
                onToggle = {
                    scope.launch {
                        if (headerState.isHidden) headerState.show(settleSpec) else headerState.hide(settleSpec)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = statusBarHeight - HANDLE_LIFT)
            )
        }

        AnimatedVisibility(
            visible = selectedWidget != null && drag == null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = VoidDimens.rowSpacing),
            enter = slideInVertically(motion.defaultSpatialSpec<IntOffset>().stoppingAtOnePixel()) { it } +
                fadeIn(motion.defaultEffectsSpec()),
            exit = slideOutVertically(motion.fastSpatialSpec<IntOffset>().stoppingAtOnePixel()) { it } +
                fadeOut(motion.fastEffectsSpec())
        ) {
            val barTarget = barWidget[0]
            if (barTarget != null) {
                val widgetId = widgetIds[barTarget.key]
                WidgetEditBar(
                    title = stringResource(R.string.widget_edit_title, barTarget.label),
                    hasSettings = barTarget.provider.configure != null && widgetId != null,
                    onSettings = {
                        val configure = barTarget.provider.configure
                        if (configure != null && widgetId != null) {
                            activityLauncher.launch(
                                Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                                    .setComponent(configure)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                            )
                        }
                    },
                    onRemove = { widgetToRemove = barTarget },
                    onDone = { selectedWidgetKey = null }
                )
            }
        }

        WidgetPickerPanel(
            visible = showPicker,
            dragging = drag?.isNew == true,
            isLoading = isLoadingProviders,
            allWidgets = allWidgets,
            pinnedKeys = pinnedWidgets.map { it.key }.toSet(),
            loadPreview = viewModel::previewFor,
            cachedPreview = viewModel::cachedPreview,
            ghostSizeFor = { widget ->
                val span = viewModel.defaultSpan(widget, metrics.cellDp)
                with(density) {
                    Size(
                        WidgetGrid.extentDp(span.columns, metrics.cellDp, metrics.spacingDp).dp.toPx(),
                        WidgetGrid.extentDp(span.rows, metrics.cellDp, metrics.spacingDp).dp.toPx()
                    )
                }
            },
            onDismiss = { showPicker = false },
            onAdd = { widget ->
                showPicker = false
                pin(widget, null)
            },
            onDragStart = { widget, pointer, ghost ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedWidgetKey = null
                drag = WidgetDrag(
                    widget = widget,
                    span = viewModel.defaultSpan(widget, metrics.cellDp),
                    isNew = true,
                    pointerInRoot = pointer,
                    grab = Offset(ghost.width / 2f, ghost.height / 2f)
                )
            },
            onDrag = { pointer -> drag = drag?.copy(pointerInRoot = pointer) },
            onDragEnd = { endDrag(commit = true) },
            onDragCancel = { endDrag(commit = false) }
        )

        drag?.let { current ->
            WidgetDragGhost(
                widget = current.widget,
                snapshot = current.snapshot,
                preview = viewModel.cachedPreview(current.widget),
                topLeft = current.pointerInRoot - current.grab - refs.screenOrigin,
                widthDp = WidgetGrid.extentDp(current.span.columns, metrics.cellDp, metrics.spacingDp),
                heightDp = WidgetGrid.extentDp(current.span.rows, metrics.cellDp, metrics.spacingDp)
            )
        }
    }

    widgetToRemove?.let { widget ->
        AlertDialog(
            onDismissRequest = { widgetToRemove = null },
            title = { Text(stringResource(R.string.widget_remove_title)) },
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

/**
 * The grid area: [rows] rows of square cells. While editing ([showCells]) every cell is outlined and
 * the cell a held widget would land on ([dropCell], [dropSpan]) is highlighted. A tap on an empty
 * part leaves edit mode.
 */
@Composable
private fun WidgetGridCanvas(
    rows: Int,
    metrics: GridMetrics,
    showCells: Boolean,
    dropCell: WidgetGrid.Cell?,
    dropSpan: WidgetLayoutHelper.WidgetSpan?,
    onBackgroundTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val cellColor = MaterialTheme.colorScheme.outlineVariant
    val dropColor = MaterialTheme.colorScheme.primary
    val onBackgroundTapLatest = rememberUpdatedState(onBackgroundTap)
    val heightDp = if (rows > 0) WidgetGrid.extentDp(rows, metrics.cellDp, metrics.spacingDp) else 0
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .pointerInput(Unit) { detectTapGestures { onBackgroundTapLatest.value() } }
            .drawBehind {
                val step = metrics.stepDp.dp.toPx()
                val cell = metrics.cellDp.dp.toPx()
                val radius = CornerRadius(VoidDimens.sectionSpacing.toPx())
                if (showCells) {
                    for (row in 0 until rows) {
                        for (column in 0 until WidgetLayoutHelper.GRID_COLUMNS) {
                            drawRoundRect(
                                color = cellColor,
                                topLeft = Offset(column * step, row * step),
                                size = Size(cell, cell),
                                cornerRadius = radius,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                }
                if (dropCell != null && dropSpan != null) {
                    val width = WidgetGrid.extentDp(dropSpan.columns, metrics.cellDp, metrics.spacingDp).dp.toPx()
                    val height = WidgetGrid.extentDp(dropSpan.rows, metrics.cellDp, metrics.spacingDp).dp.toPx()
                    val topLeft = Offset(dropCell.column * step, dropCell.row * step)
                    drawRoundRect(color = dropColor.copy(alpha = 0.16f), topLeft = topLeft, size = Size(width, height), cornerRadius = radius)
                    drawRoundRect(color = dropColor, topLeft = topLeft, size = Size(width, height), cornerRadius = radius, style = Stroke(2.dp.toPx()))
                }
            }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiveWidgetItem(
    widget: WidgetInfo,
    widgetId: Int,
    appWidgetHost: AppWidgetHost,
    placement: WidgetGrid.Placement,
    metrics: GridMetrics,
    viewBuilt: Boolean,
    selected: Boolean,
    dimmed: Boolean,
    lifted: Boolean,
    onPositioned: (LayoutCoordinates) -> Unit,
    onSelected: () -> Unit,
    onDeselect: () -> Unit,
    onMoveStart: (grab: Offset, pointerInRoot: Offset, snapshot: ImageBitmap?) -> Unit,
    onMove: (pointerInRoot: Offset) -> Unit,
    onMoveEnd: () -> Unit,
    onMoveBy: (columns: Int, rows: Int) -> Unit,
    onResize: (WidgetLayoutHelper.WidgetSpan) -> Unit
) {
    val span = placement.span
    val widthDp = WidgetGrid.extentDp(span.columns, metrics.cellDp, metrics.spacingDp)
    val heightDp = WidgetGrid.extentDp(span.rows, metrics.cellDp, metrics.spacingDp)
    val density = LocalDensity.current
    val motion = MaterialTheme.motionScheme
    val shape = MaterialTheme.shapes.largeIncreased
    val outlineColor = MaterialTheme.colorScheme.primary
    val spanLatest = rememberUpdatedState(span)
    val selectedLatest = rememberUpdatedState(selected)
    val onPositionedLatest = rememberUpdatedState(onPositioned)
    val onSelectedLatest = rememberUpdatedState(onSelected)
    val onDeselectLatest = rememberUpdatedState(onDeselect)
    val onMoveStartLatest = rememberUpdatedState(onMoveStart)
    val onMoveLatest = rememberUpdatedState(onMove)
    val onMoveEndLatest = rememberUpdatedState(onMoveEnd)
    val onMoveByLatest = rememberUpdatedState(onMoveBy)
    val onResizeLatest = rememberUpdatedState(onResize)
    // The item stays in place while held (a snapshot follows the finger), so its coordinates stay valid.
    val coordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val hostView = remember { arrayOfNulls<View>(1) }
    // The size last sent to the provider: options go out only when it changes, never per recomposition.
    val appliedSize = remember { intArrayOf(0, 0) }
    val resizeMode = widget.provider.resizeMode
    val resizesHorizontally = resizeMode and AppWidgetProviderInfo.RESIZE_HORIZONTAL != 0
    val resizesVertically = resizeMode and AppWidgetProviderInfo.RESIZE_VERTICAL != 0
    val handles = resizeHandlesFor(resizesHorizontally, resizesVertically, span)
    val handlesLatest = rememberUpdatedState(handles)
    val target = with(density) {
        IntOffset(
            (placement.cell.column * metrics.stepDp).dp.roundToPx(),
            (placement.cell.row * metrics.stepDp).dp.roundToPx()
        )
    }
    // A widget that changes cell glides there on the theme's spatial spring instead of jumping.
    val offset by animateIntOffsetAsState(
        target,
        motion.defaultSpatialSpec<IntOffset>().stoppingAtOnePixel(),
        label = "widget_cell"
    )
    // Picking a widget lifts it a little and quiets the others, so the one being customized is obvious.
    val lift by animateFloatAsState(if (selected) SELECTED_SCALE else 1f, motion.defaultSpatialSpec(), label = "widget_lift")
    val quiet by animateFloatAsState(if (dimmed) DIMMED_ALPHA else 1f, motion.defaultEffectsSpec(), label = "widget_quiet")
    val outline by animateFloatAsState(if (selected) 1f else 0f, motion.defaultEffectsSpec(), label = "widget_outline")
    // The widget view is built a little after the screen has arrived; until then (and for the moment it
    // takes the provider to draw) a tile of the right size holds the place, so nothing jumps.
    val viewAlpha by animateFloatAsState(if (viewBuilt) 1f else 0f, motion.defaultEffectsSpec(), label = "widget_view")
    var placeholderGone by remember { mutableStateOf(false) }
    LaunchedEffect(viewBuilt) {
        if (viewBuilt) {
            delay(PLACEHOLDER_HOLD_MS)
            placeholderGone = true
        }
    }
    val placeholderAlpha by animateFloatAsState(if (placeholderGone) 0f else 1f, motion.defaultEffectsSpec(), label = "widget_placeholder")

    val customizeLabel = stringResource(R.string.widget_action_customize)
    val widerLabel = stringResource(R.string.widget_action_wider)
    val narrowerLabel = stringResource(R.string.widget_action_narrower)
    val tallerLabel = stringResource(R.string.widget_action_taller)
    val shorterLabel = stringResource(R.string.widget_action_shorter)
    val leftLabel = stringResource(R.string.widget_action_move_left)
    val rightLabel = stringResource(R.string.widget_action_move_right)
    val upLabel = stringResource(R.string.widget_action_move_up)
    val downLabel = stringResource(R.string.widget_action_move_down)

    Box(
        modifier = Modifier
            .offset { offset }
            .size(widthDp.dp, heightDp.dp)
            .zIndex(if (selected) 1f else 0f)
            .graphicsLayer {
                alpha = (if (lifted) LIFTED_ALPHA else 1f) * quiet
                scaleX = lift
                scaleY = lift
            }
            .onGloballyPositioned {
                coordinates[0] = it
                onPositionedLatest.value(it)
            }
            .drawWithContent {
                drawContent()
                if (outline > 0f) {
                    drawOutline(
                        outline = shape.createOutline(size, layoutDirection, this),
                        color = outlineColor.copy(alpha = outline),
                        style = Stroke(width = OUTLINE_WIDTH.toPx())
                    )
                }
            }
            // A screen reader has no long press and no drag: the same customizing is offered as actions.
            .semantics {
                val actions = mutableListOf<CustomAccessibilityAction>()
                if (!selected) {
                    actions += CustomAccessibilityAction(customizeLabel) { onSelectedLatest.value(); true }
                } else {
                    if (resizesHorizontally) {
                        actions += CustomAccessibilityAction(widerLabel) { onResizeLatest.value(WidgetLayoutHelper.bumpSpan(spanLatest.value, 1, 0)); true }
                        actions += CustomAccessibilityAction(narrowerLabel) { onResizeLatest.value(WidgetLayoutHelper.bumpSpan(spanLatest.value, -1, 0)); true }
                    }
                    if (resizesVertically) {
                        actions += CustomAccessibilityAction(tallerLabel) { onResizeLatest.value(WidgetLayoutHelper.bumpSpan(spanLatest.value, 0, 1)); true }
                        actions += CustomAccessibilityAction(shorterLabel) { onResizeLatest.value(WidgetLayoutHelper.bumpSpan(spanLatest.value, 0, -1)); true }
                    }
                    actions += CustomAccessibilityAction(leftLabel) { onMoveByLatest.value(-1, 0); true }
                    actions += CustomAccessibilityAction(rightLabel) { onMoveByLatest.value(1, 0); true }
                    actions += CustomAccessibilityAction(upLabel) { onMoveByLatest.value(0, -1); true }
                    actions += CustomAccessibilityAction(downLabel) { onMoveByLatest.value(0, 1); true }
                }
                customActions = actions
            }
            .pointerInput(widget.key) {
                val handleTarget = RESIZE_HANDLE_TARGET.toPx()
                val handleLength = RESIZE_HANDLE_EDGE_LENGTH.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                    val wasSelected = selectedLatest.value
                    // The resize handles of a selected widget handle their own touches.
                    if (wasSelected && isOnResizeHandle(down.position, size.width.toFloat(), size.height.toFloat(), handlesLatest.value, handleTarget, handleLength)) {
                        return@awaitEachGesture
                    }
                    if (!wasSelected) {
                        // Only a still finger held for the long-press time lifts the widget: a tap belongs to the
                        // widget, and a finger that moves first is scrolling the grid or the widget itself.
                        val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            var gaveUp = false
                            while (!gaveUp) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id }
                                gaveUp = change == null || !change.pressed ||
                                    (change.position - down.position).getDistance() > viewConfiguration.touchSlop
                            }
                            true
                        }
                        if (released != null) return@awaitEachGesture
                        onSelectedLatest.value()
                    }
                    currentEvent.changes.forEach { it.consume() }
                    val toRoot: (Offset) -> Offset = { local -> coordinates[0]?.localToRoot(local) ?: local }
                    var moving = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        event.changes.forEach { it.consume() }
                        if (!change.pressed) break
                        if (!moving && (change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                            moving = true
                            val snapshot = runCatching { hostView[0]?.drawToBitmap()?.asImageBitmap() }.getOrNull()
                            onMoveStartLatest.value(down.position, toRoot(change.position), snapshot)
                        }
                        if (moving) onMoveLatest.value(toRoot(change.position))
                    }
                    when {
                        moving -> onMoveEndLatest.value()
                        wasSelected -> onDeselectLatest.value()
                    }
                }
            }
    ) {
        if (placeholderAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = placeholderAlpha }
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }
        if (viewBuilt) {
            AndroidView(
                factory = { ctx ->
                    try {
                        appWidgetHost.createView(ctx, widgetId, widget.provider).also { view ->
                            WidgetLayoutHelper.applyHostSize(view, widgetId, widthDp, heightDp)
                            appliedSize[0] = widthDp
                            appliedSize[1] = heightDp
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "widget_view_failed reason=${e.javaClass.simpleName}")
                        android.widget.FrameLayout(ctx)
                    }.also { hostView[0] = it }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = viewAlpha }
                    .clip(shape),
                update = { view ->
                    val host = view as? AppWidgetHostView ?: return@AndroidView
                    if (appliedSize[0] != widthDp || appliedSize[1] != heightDp) {
                        WidgetLayoutHelper.applyHostSize(host, widgetId, widthDp, heightDp)
                        appliedSize[0] = widthDp
                        appliedSize[1] = heightDp
                    }
                }
            )
        }
        if (dimmed) {
            // While another widget is being customized a tap picks this one; it must not open the app behind it.
            // Drags still pass through, so the grid scrolls over it.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(widget.key) { detectTapGestures { onSelectedLatest.value() } }
            )
        }
        if (selected) {
            val widthLabel = stringResource(R.string.widget_resize_width)
            val heightLabel = stringResource(R.string.widget_resize_height)
            val resizeLabel = stringResource(R.string.widget_resize_handle)
            val cornerAxis = ResizeAxis.Both
            if (handles.corner) {
                WidgetResizeHandle(cornerAxis, HandleSide.Corner, resizeLabel, { spanLatest.value }, metrics, onResize)
            }
            if (handles.rightEdge) {
                val axis = ResizeAxis.Width
                WidgetResizeHandle(axis, HandleSide.Right, widthLabel, { spanLatest.value }, metrics, onResize)
            }
            if (handles.bottomEdge) {
                val axis = ResizeAxis.Height
                WidgetResizeHandle(axis, HandleSide.Bottom, heightLabel, { spanLatest.value }, metrics, onResize)
            }
        }
    }
}

/**
 * The keys of [keys] that may build their widget view yet. Nothing before [enabled] (the screen has
 * not arrived), then one more per frame, in order: a view is built on the main thread and asks the
 * provider for its content, so building them all in one frame would stall it by the sum.
 */
@Composable
private fun rememberStagedKeys(keys: List<String>, enabled: Boolean): Set<String> {
    var released by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(enabled, keys) {
        if (!enabled) return@LaunchedEffect
        for (key in keys) {
            if (key in released) continue
            released = released + key
            withFrameNanos { }
        }
    }
    return released
}

/**
 * What follows the finger while a widget is held: a snapshot of the widget itself when it is a pinned
 * one, else its preview, at the size it will take on the grid.
 */
@Composable
private fun WidgetDragGhost(
    widget: WidgetInfo,
    snapshot: ImageBitmap?,
    preview: Drawable?,
    topLeft: Offset,
    widthDp: Int,
    heightDp: Int
) {
    Surface(
        shape = MaterialTheme.shapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = VoidDimens.rowSpacing,
        modifier = Modifier
            .offset { IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()) }
            .size(widthDp.dp, heightDp.dp)
            .graphicsLayer {
                alpha = GHOST_ALPHA
                scaleX = GHOST_SCALE
                scaleY = GHOST_SCALE
            }
            .zIndex(10f)
    ) {
        if (snapshot != null) {
            Image(bitmap = snapshot, contentDescription = widget.label, modifier = Modifier.fillMaxSize())
        } else {
            Column(modifier = Modifier.padding(VoidDimens.rowSpacing)) {
                WidgetPreviewThumb(
                    drawable = preview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Text(
                    text = widget.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = VoidDimens.compactSpacing)
                )
            }
        }
    }
}

/**
 * The widget picker, drawn in this window (not a dialog window) so a card held here can be dragged
 * onto the grid: while one is held ([dragging]) the panel turns invisible but stays composed, so the
 * gesture keeps reaching the card. A tap adds the widget to the first free cell.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WidgetPickerPanel(
    visible: Boolean,
    dragging: Boolean,
    isLoading: Boolean,
    allWidgets: List<WidgetInfo>,
    pinnedKeys: Set<String>,
    loadPreview: suspend (WidgetInfo) -> Drawable?,
    cachedPreview: (WidgetInfo) -> Drawable?,
    ghostSizeFor: (WidgetInfo) -> Size,
    onDismiss: () -> Unit,
    onAdd: (WidgetInfo) -> Unit,
    onDragStart: (WidgetInfo, Offset, Size) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    BackHandler(enabled = visible && !dragging, onBack = onDismiss)
    var query by remember { mutableStateOf("") }
    val groupedWidgets = remember(allWidgets, query) {
        val needle = query.trim()
        allWidgets
            .filter { needle.isEmpty() || it.label.contains(needle, ignoreCase = true) || it.appName.contains(needle, ignoreCase = true) }
            .groupBy { it.appName }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible && !dragging,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 1f - VoidAlpha.coveredByPanel))
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>().stoppingAtOnePixel()) { it } +
                fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec<IntOffset>().stoppingAtOnePixel()) { it } +
                fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLargeIncreased.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .graphicsLayer { alpha = if (dragging) 0f else 1f }
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = VoidDimens.rowSpacing + VoidDimens.compactSpacing)
                            .size(width = 32.dp, height = 4.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                    )
                    Text(
                        stringResource(R.string.widget_picker_title),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = VoidDimens.screenPadding, end = VoidDimens.screenPadding, top = VoidDimens.sectionSpacing)
                    )
                    Text(
                        stringResource(R.string.widget_picker_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = VoidDimens.screenPadding)
                    )
                    VoidSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        hint = stringResource(R.string.widget_search_hint),
                        modifier = Modifier.padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing),
                        clearDescription = stringResource(R.string.font_search_clear)
                    )
                    if (isLoading && allWidgets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            LoadingIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = VoidDimens.sectionSpacing)
                        ) {
                            groupedWidgets.forEach { (appName, widgets) ->
                                item(key = "group_$appName") {
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = VoidDimens.rowSpacing)) {
                                        Text(
                                            text = appName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = VoidDimens.screenPadding, bottom = VoidDimens.rowSpacing)
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(VoidDimens.sectionSpacing - VoidDimens.compactSpacing),
                                            contentPadding = PaddingValues(horizontal = VoidDimens.screenPadding)
                                        ) {
                                            items(items = widgets, key = { it.key }) { widget ->
                                                WidgetPickerCard(
                                                    widget = widget,
                                                    pinned = widget.key in pinnedKeys,
                                                    loadPreview = loadPreview,
                                                    cachedPreview = cachedPreview,
                                                    ghostSize = { ghostSizeFor(widget) },
                                                    onAdd = { onAdd(widget) },
                                                    onDragStart = { pointer, ghost -> onDragStart(widget, pointer, ghost) },
                                                    onDrag = onDrag,
                                                    onDragEnd = onDragEnd,
                                                    onDragCancel = onDragCancel
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPickerCard(
    widget: WidgetInfo,
    pinned: Boolean,
    loadPreview: suspend (WidgetInfo) -> Drawable?,
    cachedPreview: (WidgetInfo) -> Drawable?,
    ghostSize: () -> Size,
    onAdd: () -> Unit,
    onDragStart: (Offset, Size) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val cardWidth = WidgetLayoutHelper.previewCardWidthDp(
        minWidthDp = widget.provider.minWidth,
        minHeightDp = widget.provider.minHeight
    )
    val preview by produceState(cachedPreview(widget), widget.key) { value = loadPreview(widget) }
    // The card stays where it is during the drag (the panel only turns invisible), so these stay valid.
    val coordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val onDragStartLatest = rememberUpdatedState(onDragStart)
    val onDragLatest = rememberUpdatedState(onDrag)
    val onDragEndLatest = rememberUpdatedState(onDragEnd)
    val onDragCancelLatest = rememberUpdatedState(onDragCancel)
    val toRoot: (Offset) -> Offset = { local -> coordinates[0]?.localToRoot(local) ?: local }
    Column(
        modifier = Modifier
            .width(cardWidth.dp)
            .onGloballyPositioned { coordinates[0] = it }
            .clip(MaterialTheme.shapes.large)
            .clickable(enabled = !pinned, onClick = onAdd)
            .then(
                if (pinned) {
                    Modifier
                } else {
                    Modifier.pointerInput(widget.key) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { start -> onDragStartLatest.value(toRoot(start), ghostSize()) },
                            onDrag = { change, _ ->
                                change.consume()
                                onDragLatest.value(toRoot(change.position))
                            },
                            onDragEnd = { onDragEndLatest.value() },
                            onDragCancel = { onDragCancelLatest.value() }
                        )
                    }
                }
            )
            .padding(VoidDimens.compactSpacing)
    ) {
        Box {
            WidgetPreviewThumb(
                drawable = preview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            )
            if (pinned) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.widget_already_added),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(VoidDimens.compactSpacing)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(VoidDimens.compactSpacing / 2)
                        .size(FilterChipDefaults.IconSize)
                )
            }
        }
        Text(
            text = widget.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (pinned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = VoidDimens.rowSpacing - VoidDimens.compactSpacing / 2)
        )
    }
}

@Composable
private fun WidgetPreviewThumb(
    drawable: Drawable?,
    modifier: Modifier = Modifier
) {
    val thumbModifier = modifier
        .clip(MaterialTheme.shapes.medium)
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
