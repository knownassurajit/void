package com.knownassurajit.app.launcher.voidlauncher.helper

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.MessageQueue
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.annotation.MainThread
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.data.WidgetInfo
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * The widget host of the launcher. On Android 12 and later its views build the widget's view
 * hierarchy on [WidgetInflateExecutor] instead of the main thread (`AppWidgetHostView.setExecutor`,
 * the way the platform launcher hosts widgets). Before Android 12 there is no such option and the
 * views are applied on the main thread.
 */
class VoidAppWidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId) {

    init {
        Log.d(TAG, "widget_inflate mode=${if (asyncInflation) "async" else "main_thread"} sdk=${Build.VERSION.SDK_INT}")
    }

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        val view = super.onCreateView(context, appWidgetId, appWidget)
        if (asyncInflation) view.setExecutor(WidgetInflateExecutor.instance)
        return view
    }

    private val asyncInflation: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    private companion object {
        const val TAG = "VoidAppWidgetHost"
    }
}

/**
 * Two background threads shared by every widget view. A widget view only needs them for the moment
 * its `RemoteViews` are applied; the threads are daemons and idle the rest of the time.
 */
internal object WidgetInflateExecutor {
    private const val THREADS = 2

    val instance: Executor by lazy {
        Executors.newFixedThreadPool(THREADS) { runnable ->
            Thread(
                {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                    runnable.run()
                },
                "void-widget-inflate"
            ).apply { isDaemon = true }
        }
    }
}

/**
 * The one widget host of the launcher and the widget views it has built, kept while the activity
 * lives, the way a home screen keeps its widgets: a widget view is built once and the Widgets
 * screen puts the same view back each time it opens, already drawn and up to date, instead of asking
 * every provider for its content again. The views are also built ahead of time while Home is idle
 * ([prewarm]), so the first open is instant as well.
 *
 * The host listens for widget updates while the activity is started and widgets are pinned (or the
 * Widgets screen is open, to pin the first one), so the kept views stay current. Main thread only.
 */
@MainThread
object WidgetHostHolder {
    private const val TAG = "WidgetHostHolder"
    const val HOST_ID = 1024

    private var host: VoidAppWidgetHost? = null
    private val views = HashMap<Int, AppWidgetHostView>()
    private val appliedSizes = HashMap<Int, Pair<Int, Int>>()

    /** The pinned widgets as last read, so the Widgets screen can lay them out on its first frame. */
    var lastPinned: List<WidgetInfo>? = null

    private var activityStarted = false
    private var openScreens = 0
    private var hasPinned = false
    private var listening = false

    fun host(context: Context): AppWidgetHost =
        host ?: VoidAppWidgetHost(context.applicationContext, HOST_ID).also { host = it }

    /** Whether a view of [appWidgetId] is ready to be shown in [context] at once. */
    fun isReady(appWidgetId: Int, context: Context): Boolean =
        views[appWidgetId]?.let { it.context === context && it.parent == null } == true

    /**
     * The view of [appWidgetId] for [context]: the kept one when it belongs to this context and is not
     * shown elsewhere (a closing Widgets screen may still hold it), otherwise a new one, which is kept.
     */
    fun obtainView(context: Context, appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView {
        views[appWidgetId]?.let { kept ->
            if (kept.context === context && kept.parent == null && kept.appWidgetInfo?.provider == info.provider) {
                return kept
            }
        }
        val view = host(context).createView(context, appWidgetId, info)
        views[appWidgetId] = view
        appliedSizes.remove(appWidgetId)
        return view
    }

    /** Sends [widthDp] x [heightDp] to the provider only when it differs from what [view] last got (each send re-renders it). */
    fun applySize(view: AppWidgetHostView, appWidgetId: Int, widthDp: Int, heightDp: Int) {
        val size = widthDp to heightDp
        if (appliedSizes[appWidgetId] == size && views[appWidgetId] === view) return
        WidgetLayoutHelper.applyHostSize(view, appWidgetId, widthDp, heightDp)
        if (views[appWidgetId] === view) appliedSizes[appWidgetId] = size
    }

    /** Drops what is kept for a widget that was removed. */
    fun forget(appWidgetId: Int) {
        views.remove(appWidgetId)
        appliedSizes.remove(appWidgetId)
    }

    fun onActivityStarted(context: Context) {
        activityStarted = true
        hasPinned = Prefs.get(context).pinnedWidgets.isNotEmpty()
        updateListening(context)
    }

    fun onActivityStopped(context: Context) {
        activityStarted = false
        updateListening(context)
    }

    fun onScreenOpened(context: Context) {
        openScreens++
        updateListening(context)
    }

    fun onScreenClosed(context: Context) {
        openScreens = (openScreens - 1).coerceAtLeast(0)
        hasPinned = Prefs.get(context).pinnedWidgets.isNotEmpty()
        updateListening(context)
    }

    /** The activity is gone: its widget views hold it, so they are let go. */
    fun onActivityDestroyed(context: Context) {
        activityStarted = false
        openScreens = 0
        updateListening(context)
        views.clear()
        appliedSizes.clear()
    }

    private fun updateListening(context: Context) {
        val want = activityStarted && (hasPinned || openScreens > 0)
        if (want == listening) return
        try {
            if (want) host(context).startListening() else host(context).stopListening()
            listening = want
            Log.d(TAG, "widget_host_listening state=${if (want) "start" else "stop"}")
        } catch (e: Exception) {
            Log.w(TAG, "widget_host_listening_failed reason=${e.javaClass.simpleName}")
        }
    }

    /**
     * Builds the views of the pinned widgets while Home is idle, one per idle moment of the main
     * thread, at the size the grid gives them, so opening Widgets only puts them on screen.
     */
    suspend fun prewarm(activity: Activity) {
        val prefs = Prefs.get(activity)
        if (!prefs.enableWidgets || prefs.pinnedWidgets.isEmpty() || !FeatureAvailability.isWidgetsAvailable(activity)) return
        val started = SystemClock.elapsedRealtime()
        val pinned = withContext(Dispatchers.IO) { PinnedWidgetQuery.query(activity, prefs) }
        lastPinned = pinned.map { it.widget }
        val cellDp = WidgetLayoutHelper.cellDpFor(activity.resources.configuration.screenWidthDp, VoidDimens.screenPadding.value)
        val spans = prefs.widgetSpans.mapNotNull(WidgetLayoutHelper::parseSpan).toMap()
        var built = 0
        for (entry in pinned) {
            awaitMainThreadIdle()
            if (activity.isDestroyed || isReady(entry.appWidgetId, activity)) continue
            val span = spans[entry.widget.key] ?: WidgetLayoutHelper.defaultSpanFor(entry.widget.provider, cellDp)
            try {
                val view = obtainView(activity, entry.appWidgetId, entry.widget.provider)
                applySize(
                    view,
                    entry.appWidgetId,
                    WidgetGrid.extentDp(span.columns, cellDp),
                    WidgetGrid.extentDp(span.rows, cellDp)
                )
                built++
            } catch (e: Exception) {
                Log.w(TAG, "widget_prewarm_failed reason=${e.javaClass.simpleName}")
            }
        }
        Log.d(TAG, "widget_prewarm built=$built ms=${SystemClock.elapsedRealtime() - started}")
    }

}

/** Waits until the main thread has nothing else to do, so pre-built work never takes a frame from the user. */
private suspend fun awaitMainThreadIdle() = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        val queue = Looper.getMainLooper().queue
        val handler = MessageQueue.IdleHandler {
            if (continuation.isActive) continuation.resume(Unit)
            false
        }
        queue.addIdleHandler(handler)
        continuation.invokeOnCancellation { queue.removeIdleHandler(handler) }
    }
}

/** A pinned widget and the id it is bound to. */
data class PinnedWidget(val widget: WidgetInfo, val appWidgetId: Int)

/** Reads the provider of every pinned, bound widget from the system, by widget id. Blocks: call it off the main thread. */
object PinnedWidgetQuery {
    private const val TAG = "PinnedWidgetQuery"

    fun query(context: Context, prefs: Prefs): List<PinnedWidget> {
        return try {
            val manager = AppWidgetManager.getInstance(context)
            val pm = context.packageManager
            val appNames = HashMap<String, String>()
            val started = SystemClock.elapsedRealtime()
            val pinned = prefs.pinnedWidgets
            val widgets = WidgetIds.parse(prefs.widgetAllocatedIds).mapNotNull { (key, id) ->
                if (key !in pinned) return@mapNotNull null
                val info = try { manager.getAppWidgetInfo(id) } catch (_: Exception) { null }
                // A widget id the system no longer knows, or one now bound to another provider, is not shown.
                if (info == null || info.provider.flattenToString() != key) return@mapNotNull null
                val packageName = info.provider.packageName
                val appName = appNames.getOrPut(packageName) {
                    runCatching { pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString() }.getOrDefault(packageName)
                }
                PinnedWidget(WidgetInfo(provider = info, label = info.loadLabel(pm), appName = appName), id)
            }
            Log.d(TAG, "widget_pinned_loaded count=${widgets.size} ms=${SystemClock.elapsedRealtime() - started}")
            widgets
        } catch (e: Exception) {
            Log.w(TAG, "widget_pinned_failed reason=${e.javaClass.simpleName}")
            emptyList()
        }
    }
}

/** The stored `provider|appWidgetId` entries. Pure, so the parsing is JVM-testable. */
object WidgetIds {
    /** Provider key to widget id; entries without a valid id are skipped, and a later entry of a key wins. */
    fun parse(entries: Iterable<String>): Map<String, Int> {
        val map = LinkedHashMap<String, Int>()
        entries.forEach { entry ->
            val separator = entry.lastIndexOf('|')
            if (separator > 0) {
                entry.substring(separator + 1).toIntOrNull()?.let { id -> map[entry.substring(0, separator)] = id }
            }
        }
        return map
    }
}
