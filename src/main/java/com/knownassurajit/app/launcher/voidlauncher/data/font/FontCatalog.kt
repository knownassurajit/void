package com.knownassurajit.app.launcher.voidlauncher.data.font

/** Where a family's glyphs come from. Declaration order is the order the picker lists the groups in. */
enum class FontSource {
    /** Shipped in the APK (`res/font`); works offline and never flashes a fallback face. */
    Bundled,

    /** Downloaded on demand by Google Play services through the downloadable-fonts provider. */
    GoogleFonts,

    /** The platform default family (`FontFamily.Default`). */
    System
}

/** Coarse classification used by the picker's filter chips. */
enum class FontKind { Sans, Serif, Mono, Display }

/**
 * One selectable launcher font. Pure data: Compose font objects live in `ui/theme/font/FontFamilies.kt`.
 *
 * @property id Stable pref value. Legacy ids are frozen; new ids are the lower snake case of the family name.
 * @property weights Declared weights, ascending, a subset of [FontCatalog.SUPPORTED_WEIGHTS] that the family really ships.
 * @property gmsName Family name sent to the Google Fonts provider; only set for [FontSource.GoogleFonts].
 * @property approxKb Approximate total download for [weights]; zero when nothing is downloaded.
 * @property naturalLineHeight Font line height as a ratio of the font size, see [lineHeightScale].
 */
data class FontSpec(
    val id: String,
    val displayName: String,
    val source: FontSource,
    val kind: FontKind,
    val weights: List<Int>,
    val gmsName: String? = null,
    val approxKb: Int = 0,
    val naturalLineHeight: Float = FontCatalog.BASELINE_LINE_HEIGHT
) {
    /** Multiplier for the Material line-height tokens so tall families are not clipped or cramped. */
    val lineHeightScale: Float
        get() = (naturalLineHeight / FontCatalog.BASELINE_LINE_HEIGHT)
            .coerceIn(FontCatalog.MIN_LINE_HEIGHT_SCALE, FontCatalog.MAX_LINE_HEIGHT_SCALE)

    /** True when selecting the family needs a download first. */
    val needsDownload: Boolean
        get() = source == FontSource.GoogleFonts

    /** Large downloads are never started by scrolling past the row; the user has to tap them. */
    val isLargeDownload: Boolean
        get() = needsDownload && approxKb > FontCatalog.LARGE_DOWNLOAD_KB

    /** Monospaced families keep stable advances, so positive tracking from the type tokens is dropped. */
    fun letterSpacing(tokenValue: Float): Float =
        if (kind == FontKind.Mono && tokenValue > 0f) 0f else tokenValue

    fun matches(query: String): Boolean {
        val needle = query.trim()
        return needle.isEmpty() || displayName.contains(needle, ignoreCase = true)
    }
}

enum class FontResolutionReason {
    /** The saved id is known and usable as is. */
    Exact,

    /** The saved id is not in the catalog (restored from a newer build, corrupted); the default is used. */
    UnknownId,

    /** The family is downloadable but the Google Fonts provider is missing on this device. */
    GmsUnavailable,

    /** The family is downloadable and has not finished downloading in this process yet. */
    NotReady
}

/** The family to render with and why. Resolution never rewrites the saved pref. */
data class FontResolution(val spec: FontSpec, val reason: FontResolutionReason)

/** One picker section. [needsProvider] marks the Google Fonts section collapsed because the provider is missing. */
data class FontGroup(
    val source: FontSource,
    val fonts: List<FontSpec>,
    val needsProvider: Boolean = false
) {
    /** List items the picker draws for the group: its header, then one row per family or the single info row. */
    val itemCount: Int
        get() = GROUP_HEADER_ITEMS + if (needsProvider) 1 else fonts.size
}

private const val GROUP_HEADER_ITEMS = 1

/** Index of [fontId]'s row in the picker list built from these groups, or null when it is not listed. */
fun List<FontGroup>.listIndexOf(fontId: String): Int? {
    var firstItem = 0
    for (group in this) {
        val position = group.fonts.indexOfFirst { it.id == fontId }
        if (position >= 0) return firstItem + GROUP_HEADER_ITEMS + position
        firstItem += group.itemCount
    }
    return null
}

object FontCatalog {
    const val DEFAULT_ID = "google_sans"

    /** The only weights the UI ever asks for (Regular, Medium, Bold); nothing else is declared or downloaded. */
    val SUPPORTED_WEIGHTS: List<Int> = listOf(400, 500, 700)

    /** Line-height ratio of the Material tokens in `Type.kt` (1.25 to 1.33); taller families are scaled up. */
    const val BASELINE_LINE_HEIGHT = 1.30f
    const val MIN_LINE_HEIGHT_SCALE = 1.0f
    const val MAX_LINE_HEIGHT_SCALE = 1.15f
    const val LARGE_DOWNLOAD_KB = 1024

    private const val MAX_LOGGED_ID_LENGTH = 32
    private const val UNKNOWN_ID_CHAR = '?'

    private val ALL_WEIGHTS: List<Int> = SUPPORTED_WEIGHTS
    private val REGULAR_BOLD: List<Int> = listOf(400, 700)
    private val REGULAR_ONLY: List<Int> = listOf(400)

    /**
     * Bundled families record the hhea ratio measured on the shipped file. Downloadable families record the
     * `lineHeight` of the Regular file in the Google Fonts metadata (docs/features/fonts.md) only when they are
     * clearly taller than [BASELINE_LINE_HEIGHT] (1.35 and up); the rest are treated as baseline.
     */
    val all: List<FontSpec> = listOf(
        bundled("google_sans", "Google Sans", naturalLineHeight = 1.25f),
        bundled("dm_sans", "DM Sans"),
        bundled("geist", "Geist"),
        bundled("inter", "Inter", naturalLineHeight = 1.21f),
        bundled("manrope", "Manrope", naturalLineHeight = 1.37f),
        bundled("plus_jakarta", "Plus Jakarta Sans", naturalLineHeight = 1.26f),
        google("actor", "Actor", FontKind.Sans, REGULAR_ONLY, 32),
        google("agdasima", "Agdasima", FontKind.Display, REGULAR_BOLD, 44),
        google("alumni_sans", "Alumni Sans", FontKind.Display, ALL_WEIGHTS, 190),
        google("antic", "Antic", FontKind.Sans, REGULAR_ONLY, 37),
        google("antonio", "Antonio", FontKind.Display, ALL_WEIGHTS, 108),
        google("basic", "Basic", FontKind.Sans, REGULAR_ONLY, 41),
        google("cabin", "Cabin", FontKind.Sans, ALL_WEIGHTS, 218),
        google("cantarell", "Cantarell", FontKind.Sans, REGULAR_BOLD, 84, naturalLineHeight = 1.42f),
        google("comfortaa", "Comfortaa", FontKind.Display, ALL_WEIGHTS, 322),
        google("dongle", "Dongle", FontKind.Display, REGULAR_BOLD, 8721, naturalLineHeight = 1.45f),
        google("epilogue", "Epilogue", FontKind.Sans, ALL_WEIGHTS, 205),
        google("fjalla_one", "Fjalla One", FontKind.Display, REGULAR_ONLY, 175),
        google("hanken_grotesk", "Hanken Grotesk", FontKind.Sans, ALL_WEIGHTS, 168),
        google("hind", "Hind", FontKind.Sans, ALL_WEIGHTS, 429, naturalLineHeight = 1.60f),
        google("inconsolata", "Inconsolata", FontKind.Mono, ALL_WEIGHTS, 215),
        google("jost", "Jost", FontKind.Sans, ALL_WEIGHTS, 172, naturalLineHeight = 1.45f),
        google("karla", "Karla", FontKind.Sans, ALL_WEIGHTS, 122),
        google("krub", "Krub", FontKind.Sans, ALL_WEIGHTS, 237),
        google("lato", "Lato", FontKind.Sans, REGULAR_BOLD, 66),
        google("nunito", "Nunito", FontKind.Sans, ALL_WEIGHTS, 368, naturalLineHeight = 1.36f),
        google("open_sans", "Open Sans", FontKind.Sans, ALL_WEIGHTS, 289, naturalLineHeight = 1.36f),
        google("poppins", "Poppins", FontKind.Sans, ALL_WEIGHTS, 447, naturalLineHeight = 1.50f),
        google("raleway", "Raleway", FontKind.Sans, ALL_WEIGHTS, 344),
        google("rethink_sans", "Rethink Sans", FontKind.Sans, ALL_WEIGHTS, 173),
        google("roboto", "Roboto", FontKind.Sans, ALL_WEIGHTS, 241),
        google("sintony", "Sintony", FontKind.Display, REGULAR_BOLD, 46),
        google("sofia_sans", "Sofia Sans", FontKind.Sans, ALL_WEIGHTS, 324),
        google("source_serif_4", "Source Serif 4", FontKind.Serif, ALL_WEIGHTS, 570, naturalLineHeight = 1.37f),
        google("work_sans", "Work Sans", FontKind.Sans, ALL_WEIGHTS, 336),
        FontSpec(
            id = "system",
            displayName = "System",
            source = FontSource.System,
            kind = FontKind.Sans,
            weights = ALL_WEIGHTS
        )
    )

    private val byId: Map<String, FontSpec> = all.associateBy { it.id }

    val default: FontSpec = byId.getValue(DEFAULT_ID)

    /** Family names for Settings search, so a font can be found from the main Settings screen. */
    val searchTerms: List<String> = all.map { it.displayName }

    fun find(id: String?): FontSpec? = id?.let(byId::get)

    /** True when resolving [rawId] depends on the Google Fonts provider; every other id works without it. */
    fun needsProvider(rawId: String?): Boolean = find(rawId)?.needsDownload == true

    /** The id the picker marks as selected: an id the catalog does not know renders as the default family. */
    fun displayedId(rawId: String?): String = find(rawId)?.id ?: DEFAULT_ID

    /**
     * Picks the family to render with. Unknown ids and unavailable downloads fall back to [default]; the saved
     * pref is left alone so the choice survives a downgrade, a restore, or a provider that comes back later.
     */
    fun resolve(
        rawId: String?,
        gmsAvailable: Boolean,
        isReady: (FontSpec) -> Boolean = { true }
    ): FontResolution {
        val spec = find(rawId) ?: return FontResolution(default, FontResolutionReason.UnknownId)
        return when {
            !spec.needsDownload -> FontResolution(spec, FontResolutionReason.Exact)
            !gmsAvailable -> FontResolution(default, FontResolutionReason.GmsUnavailable)
            !isReady(spec) -> FontResolution(default, FontResolutionReason.NotReady)
            else -> FontResolution(spec, FontResolutionReason.Exact)
        }
    }

    /**
     * Picker sections for [query] and [kind]. Groups with no match are omitted. Without the provider the Google
     * Fonts section keeps no rows and is flagged [FontGroup.needsProvider], but only when something would match.
     */
    fun groups(query: String, kind: FontKind?, gmsAvailable: Boolean): List<FontGroup> =
        FontSource.entries.mapNotNull { source ->
            val matching = all.filter { it.source == source && (kind == null || it.kind == kind) && it.matches(query) }
            when {
                matching.isEmpty() -> null
                source == FontSource.GoogleFonts && !gmsAvailable ->
                    FontGroup(source, fonts = emptyList(), needsProvider = true)
                else -> FontGroup(source, matching)
            }
        }

    /** Bounded, identifier-safe form of a saved id for logs: prefs can be restored from a backup. */
    fun loggableId(rawId: String?): String =
        rawId.orEmpty().take(MAX_LOGGED_ID_LENGTH).map { char ->
            if (char in 'a'..'z' || char in '0'..'9' || char == '_') char else UNKNOWN_ID_CHAR
        }.joinToString(separator = "")

    private fun bundled(
        id: String,
        displayName: String,
        naturalLineHeight: Float = BASELINE_LINE_HEIGHT
    ): FontSpec = FontSpec(
        id = id,
        displayName = displayName,
        source = FontSource.Bundled,
        kind = FontKind.Sans,
        weights = ALL_WEIGHTS,
        naturalLineHeight = naturalLineHeight
    )

    private fun google(
        id: String,
        displayName: String,
        kind: FontKind,
        weights: List<Int>,
        approxKb: Int,
        naturalLineHeight: Float = BASELINE_LINE_HEIGHT
    ): FontSpec = FontSpec(
        id = id,
        displayName = displayName,
        source = FontSource.GoogleFonts,
        kind = kind,
        weights = weights,
        gmsName = displayName,
        approxKb = approxKb,
        naturalLineHeight = naturalLineHeight
    )
}
