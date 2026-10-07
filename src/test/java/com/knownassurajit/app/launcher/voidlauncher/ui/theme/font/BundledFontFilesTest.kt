package com.knownassurajit.app.launcher.voidlauncher.ui.theme.font

import com.knownassurajit.app.launcher.voidlauncher.ui.theme.TABULAR_FIGURES_FEATURE
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.googleSansFonts
import java.io.File
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards against placeholder files masquerading as fonts.
 *
 * The original `google_sans_*.ttf` assets were GitHub "Page not found" HTML pages; Compose silently
 * fell back to the platform font so the defect shipped unnoticed.
 */
class BundledFontFilesTest {

    @Test
    fun fontDirectory_containsBundledFonts() {
        assertTrue("expected bundled fonts under res/font", bundledFontFiles().isNotEmpty())
    }

    @Test
    fun everyBundledFontFile_startsWithSfntMagic() {
        val invalid = bundledFontFiles().filterNot { hasSfntMagic(it.readHeader()) }
        assertTrue("not valid sfnt font files: ${invalid.map { it.name }}", invalid.isEmpty())
    }

    @Test
    fun defaultGoogleSansAsset_isBundledAsVariableFont() {
        val file = File(ProjectResources.resDir(), "font/google_sans_variable.ttf")
        assertTrue("google_sans_variable.ttf is missing", file.isFile)
        assertTrue("google_sans_variable.ttf is not an sfnt font", hasSfntMagic(file.readHeader()))
    }

    @Test
    fun defaultGoogleSansAsset_exposesOnlyTheWeightAxisCoveringDeclaredWeights() {
        val file = File(ProjectResources.resDir(), "font/google_sans_variable.ttf")
        val axes = variationAxes(file.readBytes())
        assertEquals("GRAD and opsz must stay pinned", listOf("wght"), axes.map { it.tag })
        val weight = axes.single()
        val declared = googleSansFonts().map { it.weight.weight.toFloat() }
        assertTrue("wght axis must reach ${declared.min()}, was ${weight.min}", weight.min <= declared.min())
        assertTrue("wght axis must reach ${declared.max()}, was ${weight.max}", weight.max >= declared.max())
    }

    @Test
    fun defaultGoogleSansAsset_keepsTabularFigures() {
        val file = File(ProjectResources.resDir(), "font/google_sans_variable.ttf")
        val lookups = gsubFeatureLookupCounts(file.readBytes())[TABULAR_FIGURES_FEATURE] ?: 0
        assertTrue(
            "GSUB '$TABULAR_FIGURES_FEATURE' feature missing or empty; re-subset with --layout-features+=tnum",
            lookups > 0
        )
    }

    @Test
    fun htmlPlaceholder_isRejected() {
        assertFalse(hasSfntMagic("<!DOCTYPE html>".toByteArray(Charsets.UTF_8)))
        assertFalse(hasSfntMagic("<html lang=\"en\">".toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun truncatedAndEmptyHeaders_areRejected() {
        assertFalse(hasSfntMagic(ByteArray(0)))
        assertFalse(hasSfntMagic(byteArrayOf(0x00, 0x01, 0x00)))
    }

    @Test
    fun knownSfntMagics_areAccepted() {
        assertTrue(hasSfntMagic(byteArrayOf(0x00, 0x01, 0x00, 0x00)))
        assertTrue(hasSfntMagic("OTTO".toByteArray(Charsets.US_ASCII)))
        assertTrue(hasSfntMagic("true".toByteArray(Charsets.US_ASCII)))
        assertTrue(hasSfntMagic("ttcf".toByteArray(Charsets.US_ASCII)))
    }

    private fun bundledFontFiles(): List<File> =
        File(ProjectResources.resDir(), "font")
            .listFiles { file -> file.isFile && file.extension.lowercase() in FONT_EXTENSIONS }
            .orEmpty()
            .sortedBy { it.name }

    private fun File.readHeader(): ByteArray = inputStream().use { it.readNBytes(MAGIC_LENGTH) }

    private fun hasSfntMagic(header: ByteArray): Boolean =
        header.size == MAGIC_LENGTH && SFNT_MAGICS.any { it.contentEquals(header) }

    private data class Axis(val tag: String, val min: Float, val max: Float)

    /** Reads the `fvar` axes of an sfnt font; empty when the font is static. */
    private fun variationAxes(font: ByteArray): List<Axis> {
        val buffer = ByteBuffer.wrap(font)
        val fvarOffset = buffer.tableOffset("fvar") ?: return emptyList()
        val axesOffset = fvarOffset + buffer.u16(fvarOffset + FVAR_AXES_ARRAY_OFFSET)
        val axisCount = buffer.u16(fvarOffset + FVAR_AXIS_COUNT)
        val axisSize = buffer.u16(fvarOffset + FVAR_AXIS_SIZE)
        return (0 until axisCount).map { index ->
            val axis = axesOffset + index * axisSize
            Axis(
                tag = buffer.tag(axis),
                min = buffer.getInt(axis + AXIS_MIN_OFFSET) / FIXED_16_16,
                max = buffer.getInt(axis + AXIS_MAX_OFFSET) / FIXED_16_16
            )
        }
    }

    /** Reads the GSUB feature list as tag to total lookup count (a tag repeats once per script/language). */
    private fun gsubFeatureLookupCounts(font: ByteArray): Map<String, Int> {
        val buffer = ByteBuffer.wrap(font)
        val gsubOffset = buffer.tableOffset("GSUB") ?: return emptyMap()
        val featureList = gsubOffset + buffer.u16(gsubOffset + GSUB_FEATURE_LIST_OFFSET)
        val counts = mutableMapOf<String, Int>()
        for (index in 0 until buffer.u16(featureList)) {
            val record = featureList + FEATURE_LIST_HEADER_SIZE + index * FEATURE_RECORD_SIZE
            val feature = featureList + buffer.u16(record + TAG_LENGTH)
            val tag = buffer.tag(record)
            counts[tag] = (counts[tag] ?: 0) + buffer.u16(feature + FEATURE_LOOKUP_COUNT_OFFSET)
        }
        return counts
    }

    private fun ByteBuffer.u16(offset: Int): Int = getShort(offset).toInt() and 0xFFFF

    private fun ByteBuffer.tableOffset(tableTag: String): Int? {
        val tableCount = u16(TABLE_COUNT_OFFSET)
        return (0 until tableCount)
            .map { TABLE_DIRECTORY_OFFSET + it * TABLE_RECORD_SIZE }
            .firstOrNull { tag(it) == tableTag }
            ?.let { getInt(it + TABLE_RECORD_OFFSET_FIELD) }
    }

    private fun ByteBuffer.tag(offset: Int): String =
        String(ByteArray(TAG_LENGTH) { get(offset + it) }, Charsets.US_ASCII)

    private companion object {
        const val MAGIC_LENGTH = 4
        const val TAG_LENGTH = 4
        const val TABLE_COUNT_OFFSET = 4
        const val TABLE_DIRECTORY_OFFSET = 12
        const val TABLE_RECORD_SIZE = 16
        const val TABLE_RECORD_OFFSET_FIELD = 8
        const val FVAR_AXES_ARRAY_OFFSET = 4
        const val FVAR_AXIS_COUNT = 8
        const val FVAR_AXIS_SIZE = 10
        const val AXIS_MIN_OFFSET = 4
        const val AXIS_MAX_OFFSET = 12
        const val GSUB_FEATURE_LIST_OFFSET = 6
        const val FEATURE_LIST_HEADER_SIZE = 2
        const val FEATURE_RECORD_SIZE = 6
        const val FEATURE_LOOKUP_COUNT_OFFSET = 2
        const val FIXED_16_16 = 65536f
        val FONT_EXTENSIONS = setOf("ttf", "otf", "ttc")
        val SFNT_MAGICS: List<ByteArray> = listOf(
            byteArrayOf(0x00, 0x01, 0x00, 0x00),
            "OTTO".toByteArray(Charsets.US_ASCII),
            "true".toByteArray(Charsets.US_ASCII),
            "ttcf".toByteArray(Charsets.US_ASCII)
        )
    }
}
