package com.knownassurajit.app.launcher.voidlauncher.data

import android.appwidget.AppWidgetProviderInfo

/**
 * A widget provider as the Widgets screen lists it. The preview image is not part of it: decoding
 * every provider's preview made the screen wait seconds before showing the pinned widgets, so
 * previews load lazily, per card (`WidgetsViewModel.previewFor`).
 */
data class WidgetInfo(
    val provider: AppWidgetProviderInfo,
    val label: String,
    val appName: String,
    val isPinned: Boolean = false
) {
    val key: String
        get() = provider.provider.flattenToString()
}
