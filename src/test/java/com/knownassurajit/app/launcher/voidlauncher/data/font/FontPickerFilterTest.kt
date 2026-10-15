package com.knownassurajit.app.launcher.voidlauncher.data.font

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FontPickerFilterTest {

    private fun groups(query: String = "", kind: FontKind? = null, gmsAvailable: Boolean = true) =
        FontCatalog.groups(query, kind, gmsAvailable)

    @Test
    fun emptyQuery_listsBundledThenGoogleFontsThenSystem() {
        val groups = groups()
        assertEquals(
            listOf(FontSource.Bundled, FontSource.GoogleFonts, FontSource.System),
            groups.map { it.source }
        )
        assertEquals("google_sans", groups.first().fonts.first().id)
        assertEquals(FontCatalog.all.size, groups.sumOf { it.fonts.size })
        assertTrue(groups.none { it.needsProvider })
    }

    @Test
    fun groups_keepCatalogOrderWithinASection() {
        val google = groups().first { it.source == FontSource.GoogleFonts }.fonts
        assertEquals(google.map { it.displayName }.sortedBy { it.lowercase() }, google.map { it.displayName })
    }

    @Test
    fun query_isCaseInsensitiveAndTrimmed() {
        val lower = groups(query = "sans").flatMap { it.fonts }.map { it.id }
        assertEquals(lower, groups(query = "  SANS ").flatMap { it.fonts }.map { it.id })
        assertTrue("work_sans" in lower)
        assertTrue("google_sans" in lower)
        assertTrue("rethink_sans" in lower)
        assertFalse("poppins" in lower)
    }

    @Test
    fun kindFilter_keepsOnlyThatKind() {
        val mono = groups(kind = FontKind.Mono)
        assertEquals(listOf(FontSource.GoogleFonts), mono.map { it.source })
        assertEquals(listOf("inconsolata"), mono.single().fonts.map { it.id })

        val serif = groups(kind = FontKind.Serif).flatMap { it.fonts }
        assertEquals(listOf("source_serif_4"), serif.map { it.id })

        val display = groups(kind = FontKind.Display).flatMap { it.fonts }
        assertTrue(display.isNotEmpty())
        assertTrue(display.all { it.kind == FontKind.Display })
    }

    @Test
    fun queryAndKind_combine() {
        val result = groups(query = "sans", kind = FontKind.Display).flatMap { it.fonts }
        assertEquals(listOf("alumni_sans"), result.map { it.id })
    }

    @Test
    fun noMatch_returnsNoGroups() {
        assertTrue(groups(query = "zzzz-no-font").isEmpty())
        assertTrue(groups(query = "poppins", kind = FontKind.Mono).isEmpty())
    }

    @Test
    fun withoutProvider_googleFontsCollapseToOneInfoGroupKeepingOrder() {
        val groups = groups(gmsAvailable = false)
        assertEquals(
            listOf(FontSource.Bundled, FontSource.GoogleFonts, FontSource.System),
            groups.map { it.source }
        )
        val google = groups.first { it.source == FontSource.GoogleFonts }
        assertTrue(google.needsProvider)
        assertTrue(google.fonts.isEmpty())
        assertTrue(groups.filter { it.source != FontSource.GoogleFonts }.none { it.needsProvider })
    }

    @Test
    fun withoutProvider_searchingForADownloadableFontStillExplainsWhy() {
        val groups = groups(query = "poppins", gmsAvailable = false)
        assertEquals(listOf(FontSource.GoogleFonts), groups.map { it.source })
        assertTrue(groups.single().needsProvider)
    }

    @Test
    fun withoutProvider_searchingForABundledFontShowsNoProviderRow() {
        val groups = groups(query = "inter", gmsAvailable = false)
        assertEquals(listOf(FontSource.Bundled), groups.map { it.source })
        assertEquals(listOf("inter"), groups.single().fonts.map { it.id })
    }

    @Test
    fun listIndexOf_countsOneHeaderPerGroupThenTheRows() {
        val groups = groups()
        val bundled = groups.first().fonts.size

        // Header of Bundled is item 0, so the first row is 1.
        assertEquals(1, groups.listIndexOf("google_sans"))
        assertEquals(bundled, groups.listIndexOf(groups.first().fonts.last().id))
        // The Google Fonts header follows the last bundled row.
        assertEquals(bundled + 2, groups.listIndexOf("actor"))
        assertEquals(groups.sumOf { it.itemCount } - 1, groups.listIndexOf("system"))
        assertNull(groups.listIndexOf("no_such_font"))
    }

    @Test
    fun listIndexOf_followsTheFilteredAndCollapsedLists() {
        assertEquals(1, groups(query = "work").listIndexOf("work_sans"))
        assertNull(groups(query = "inter").listIndexOf("poppins"))

        val collapsed = groups(gmsAvailable = false)
        assertNull(collapsed.listIndexOf("poppins"))
        // Bundled header and rows, the Google Fonts header and its single info row, the System header, then System.
        val bundled = collapsed.first().fonts.size
        assertEquals(1 + bundled + 1 + 1 + 1, collapsed.listIndexOf("system"))
    }
}
