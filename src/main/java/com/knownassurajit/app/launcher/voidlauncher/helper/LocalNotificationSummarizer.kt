package com.knownassurajit.app.launcher.voidlauncher.helper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalNotificationSummarizer {
    suspend fun summarize(notificationTexts: List<String>): String? =
        withContext(Dispatchers.Default) {
            notificationTexts
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .takeIf { it.isNotEmpty() }
                ?.joinToString("\n") { text ->
                    val colonIndex = text.indexOf(':').takeIf { it in 1..50 }
                    val summary = when {
                        colonIndex != null -> {
                            val title = text.substring(0, colonIndex).trim()
                            val body = text.substring(colonIndex + 1).trim()
                            if (body.isBlank()) title else "$title: ${body.take(70)}"
                        }
                        text.length > 80 -> text.take(80) + "…"
                        else -> text
                    }
                    "• $summary"
                }
        }
}
