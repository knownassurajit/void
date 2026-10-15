package com.knownassurajit.app.launcher.voidlauncher.data.font

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FontCatalogTest {

    private val googleFonts: List<FontSpec> = FontCatalog.all.filter { it.source == FontSource.GoogleFonts }

    @Test
    fun ids_areUniqueLowerSnakeCase() {
        val ids = FontCatalog.all.map { it.id }
        assertEquals("duplicate ids in $ids", ids.size, ids.toSet().size)
        val pattern = Regex("^[a-z0-9_]+$")
        ids.forEach { assertTrue("$it is not lower snake case", pattern.matches(it)) }
    }

    @Test
    fun displayNames_areUnique() {
        val names = FontCatalog.all.map { it.displayName.lowercase() }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun gmsNames_areUniqueAndOnlyOnDownloadableFonts() {
        val gmsNames = googleFonts.map { it.gmsName }
        assertEquals(gmsNames.size, gmsNames.toSet().size)
        FontCatalog.all.filter { it.source != FontSource.GoogleFonts }.forEach {
            assertNull("${it.id} must not carry a Google Fonts name", it.gmsName)
            assertEquals("${it.id} downloads nothing", 0, it.approxKb)
        }
    }

    @Test
    fun defaultFont_existsAndIsBundled() {
        val default = FontCatalog.find(FontCatalog.DEFAULT_ID)
        assertNotNull(default)
        assertSame(default, FontCatalog.default)
        assertEquals(FontSource.Bundled, FontCatalog.default.source)
        assertEquals("google_sans", FontCatalog.DEFAULT_ID)
    }

    @Test
    fun legacyIds_areFrozenWithUnchangedSource() {
        val legacy = mapOf(
            "google_sans" to FontSource.Bundled,
            "inter" to FontSource.Bundled,
            "plus_jakarta" to FontSource.Bundled,
            "manrope" to FontSource.Bundled,
            "dm_sans" to FontSource.Bundled,
            "system" to FontSource.System
        )
        legacy.forEach { (id, source) ->
            assertEquals("source of $id", source, FontCatalog.find(id)?.source)
        }
    }

    @Test
    fun everyDownloadableFont_hasGmsNameAndRealWeights() {
        assertTrue(googleFonts.isNotEmpty())
        googleFonts.forEach { spec ->
            assertFalse("${spec.id} needs a Google Fonts name", spec.gmsName.isNullOrBlank())
            assertTrue("${spec.id} must ship Regular", 400 in spec.weights)
            assertTrue("${spec.id} declares ${spec.weights}", FontCatalog.SUPPORTED_WEIGHTS.containsAll(spec.weights))
            assertTrue("${spec.id} needs a download size", spec.approxKb > 0)
        }
    }

    @Test
    fun everyFont_declaresAscendingDistinctSupportedWeights() {
        FontCatalog.all.forEach { spec ->
            assertTrue("${spec.id} has no weights", spec.weights.isNotEmpty())
            assertEquals("${spec.id} weights must be ascending and distinct", spec.weights.sorted().distinct(), spec.weights)
            assertTrue(FontCatalog.SUPPORTED_WEIGHTS.containsAll(spec.weights))
        }
    }

    @Test
    fun requestedFamilies_areAllSelectable() {
        val requested = listOf(
            "Manrope", "Poppins", "Work Sans", "Karla", "Dongle", "Raleway", "Inconsolata", "Comfortaa", "Antic",
            "Actor", "Agdasima", "Hanken Grotesk", "Basic", "Cabin", "Cantarell", "Inter", "Hind", "Jost", "Krub",
            "Lato", "Nunito", "Epilogue", "Open Sans", "Sintony", "Fjalla One", "Rethink Sans", "Alumni Sans",
            "Geist", "Sofia Sans", "Source Serif 4", "Antonio", "Roboto"
        )
        val available = FontCatalog.all.map { it.displayName }
        requested.forEach { assertTrue("$it is missing from the catalog", it in available) }
    }

    @Test
    fun geist_isBundled_andRobotoIsADistinctDownloadFromSystem() {
        assertEquals(FontSource.Bundled, FontCatalog.find("geist")?.source)
        assertEquals(FontSource.GoogleFonts, FontCatalog.find("roboto")?.source)
        assertEquals(FontSource.System, FontCatalog.find("system")?.source)
    }

    @Test
    fun unlicensedFamilies_areNotInTheCatalog() {
        val unavailable = listOf("brogman", "volkga", "disttopian", "stanza", "vangura sans", "noria")
        val names = FontCatalog.all.flatMap { listOf(it.id, it.displayName.lowercase()) }
        unavailable.forEach { name ->
            assertFalse("$name is not a Google Font and must not be shipped", name in names)
            assertFalse(name.replace(' ', '_') in names)
        }
    }

    @Test
    fun lineHeightScale_isClampedToPolicyRange() {
        FontCatalog.all.forEach { spec ->
            assertTrue("${spec.id} scale ${spec.lineHeightScale}", spec.lineHeightScale in 1.0f..1.15f)
        }
    }

    @Test
    fun lineHeightScale_followsTheNaturalLineHeight() {
        assertEquals(1.0f, FontCatalog.default.lineHeightScale, 0f)
        assertEquals(1.15f, requireNotNull(FontCatalog.find("hind")).lineHeightScale, 0f)
        assertEquals(1.15f, requireNotNull(FontCatalog.find("poppins")).lineHeightScale, 0f)
        assertEquals(1.45f / 1.30f, requireNotNull(FontCatalog.find("jost")).lineHeightScale, 0.0001f)
    }

    @Test
    fun letterSpacing_dropsPositiveTrackingOnlyForMono() {
        val mono = requireNotNull(FontCatalog.find("inconsolata"))
        assertEquals(FontKind.Mono, mono.kind)
        assertEquals(0f, mono.letterSpacing(0.5f), 0f)
        assertEquals(-0.25f, mono.letterSpacing(-0.25f), 0f)
        assertEquals(0.5f, FontCatalog.default.letterSpacing(0.5f), 0f)
    }

    @Test
    fun onlyDongle_isALargeDownload() {
        assertEquals(listOf("dongle"), FontCatalog.all.filter { it.isLargeDownload }.map { it.id })
    }

    @Test
    fun resolve_unknownOrBlankId_usesDefaultWithUnknownIdReason() {
        listOf("no_such_font", "", "  ", null).forEach { raw ->
            val resolution = FontCatalog.resolve(raw, gmsAvailable = true)
            assertSame(FontCatalog.default, resolution.spec)
            assertEquals(FontResolutionReason.UnknownId, resolution.reason)
        }
    }

    @Test
    fun resolve_downloadableWithoutProvider_usesDefaultWithGmsUnavailable() {
        val resolution = FontCatalog.resolve("work_sans", gmsAvailable = false)
        assertSame(FontCatalog.default, resolution.spec)
        assertEquals(FontResolutionReason.GmsUnavailable, resolution.reason)
    }

    @Test
    fun resolve_downloadableThatIsNotReady_usesDefaultWithNotReady() {
        val resolution = FontCatalog.resolve("work_sans", gmsAvailable = true, isReady = { false })
        assertSame(FontCatalog.default, resolution.spec)
        assertEquals(FontResolutionReason.NotReady, resolution.reason)
    }

    @Test
    fun resolve_readyDownloadable_isExact() {
        val resolution = FontCatalog.resolve("work_sans", gmsAvailable = true, isReady = { it.id == "work_sans" })
        assertEquals("work_sans", resolution.spec.id)
        assertEquals(FontResolutionReason.Exact, resolution.reason)
    }

    @Test
    fun resolve_bundledAndSystemFonts_ignoreProviderAndReadiness() {
        listOf("google_sans", "geist", "inter", "system").forEach { id ->
            val resolution = FontCatalog.resolve(id, gmsAvailable = false, isReady = { false })
            assertEquals(id, resolution.spec.id)
            assertEquals(FontResolutionReason.Exact, resolution.reason)
        }
    }

    @Test
    fun needsProvider_isTrueOnlyForDownloadableFamilies() {
        assertTrue(FontCatalog.needsProvider("poppins"))
        listOf("google_sans", "geist", "system", "no_such_font", "", null).forEach {
            assertFalse("$it", FontCatalog.needsProvider(it))
        }
    }

    @Test
    fun displayedId_isTheSavedFamily_orTheDefaultWhenTheIdIsUnknown() {
        assertEquals("poppins", FontCatalog.displayedId("poppins"))
        assertEquals("system", FontCatalog.displayedId("system"))
        assertEquals(FontCatalog.DEFAULT_ID, FontCatalog.displayedId("no_such_font"))
        assertEquals(FontCatalog.DEFAULT_ID, FontCatalog.displayedId(null))
    }

    @Test
    fun loggableId_isBoundedAndIdentifierSafe() {
        assertEquals("work_sans", FontCatalog.loggableId("work_sans"))
        assertEquals("", FontCatalog.loggableId(null))
        assertEquals("a?b?", FontCatalog.loggableId("a.b/"))
        assertEquals(32, FontCatalog.loggableId("x".repeat(500)).length)
    }
}
