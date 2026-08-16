package com.knownassurajit.app.launcher.voidlauncher.helper

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.UserHandle

/**
 * Builds the system widget-bind grant intent.
 * Third-party launchers cannot bind until the user accepts ACTION_APPWIDGET_BIND.
 */
object WidgetBindHelper {
    fun createBindIntent(
        appWidgetId: Int,
        provider: ComponentName,
        profile: UserHandle? = null
    ): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
            if (profile != null) {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, profile)
            }
        }
    }
}
