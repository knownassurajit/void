package com.knownassurajit.app.launcher.voidlauncher.helper

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * The widget host of the Widgets screen. On Android 12 and later its views build the widget's view
 * hierarchy on [WidgetInflateExecutor] instead of the main thread (`AppWidgetHostView.setExecutor`,
 * the way the platform launcher hosts widgets). Inflating a provider's `RemoteViews` takes tens of
 * milliseconds for a simple widget and a few hundred for a heavy one, and it used to happen on the
 * main thread in the middle of the slide-in of the screen, which is the stutter when Widgets opens.
 * Before Android 12 there is no such option and the views are applied on the main thread, as before.
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
