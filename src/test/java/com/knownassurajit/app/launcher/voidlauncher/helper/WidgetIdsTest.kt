package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetIdsTest {

    @Test
    fun parse_readsProviderAndIdSplitAtTheLastBar() {
        val ids = WidgetIds.parse(listOf("com.example/.Clock|12", "com.example/.Weather|7"))
        assertEquals(mapOf("com.example/.Clock" to 12, "com.example/.Weather" to 7), ids)
    }

    @Test
    fun parse_skipsEntriesWithoutAValidId() {
        val ids = WidgetIds.parse(listOf("broken", "|5", "com.example/.Clock|x", "com.example/.Notes|"))
        assertTrue(ids.isEmpty())
    }

    @Test
    fun parse_keepsTheLastEntryOfAProvider() {
        assertEquals(mapOf("a/.W" to 9), WidgetIds.parse(listOf("a/.W|3", "a/.W|9")))
    }
}
