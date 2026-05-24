package com.knownassurajit.app.launcher.voidlauncher.helper

import android.content.Context

/**
 * Stub for AiSummarizer used in disintegrated flavor.
 * AI features are disabled in this flavor due to policy restrictions.
 */
class AiSummarizer(private val context: Context) {

    suspend fun isAvailable(): Boolean {
        return false
    }

    suspend fun summarize(appName: String, notificationTexts: List<String>): String? {
        // In disintegrated flavor, we don't perform AI summarization.
        // The ViewModel will use its own fallback if this returns null.
        return null
    }
}
