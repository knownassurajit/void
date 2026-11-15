package com.knownassurajit.app.launcher.voidlauncher.helper

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalNotificationSummarizerTest {
    private val summarizer = LocalNotificationSummarizer()

    @Test
    fun summarize_emptyOrBlankInput_returnsNull() = runBlocking {
        assertNull(summarizer.summarize(emptyList()))
        assertNull(summarizer.summarize(listOf(" ", "\n")))
    }

    @Test
    fun summarize_titleAndBody_keepsTitleAndBoundsBody() = runBlocking {
        val result = summarizer.summarize(listOf("  Mail: ${"x".repeat(75)}  "))

        assertEquals("• Mail: ${"x".repeat(70)}", result)
    }

    @Test
    fun summarize_longTextWithoutTitle_truncatesAndKeepsBullets() = runBlocking {
        val result = summarizer.summarize(listOf("x".repeat(81), "Second"))

        assertEquals("• ${"x".repeat(80)}…\n• Second", result)
    }
}
