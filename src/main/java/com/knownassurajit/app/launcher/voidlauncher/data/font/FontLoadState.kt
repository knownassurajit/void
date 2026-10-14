package com.knownassurajit.app.launcher.voidlauncher.data.font

import kotlinx.coroutines.TimeoutCancellationException

/** How much of a downloadable family a load covers. */
enum class FontCoverage {
    /** The Regular weight only: enough to draw the family name in its own face in the picker. */
    Sample,

    /** Every declared weight: required before the family drives the app theme. */
    Full
}

data class FontLoadKey(val id: String, val coverage: FontCoverage)

/** Why a download failed, mapped from the provider's `FAIL_REASON_*` codes (kept free of Android types). */
enum class FontFailure {
    ProviderNotFound,
    WrongCertificates,
    FontLoadError,
    SecurityViolation,
    FontNotFound,
    FontUnavailable,
    MalformedQuery,
    Timeout,
    Unknown
}

sealed interface FontLoadState {
    data object Idle : FontLoadState

    data object Loading : FontLoadState

    data object Ready : FontLoadState

    data class Failed(val reason: FontFailure) : FontLoadState
}

typealias FontLoadStates = Map<FontLoadKey, FontLoadState>

/** What a picker row shows for a family, independent of the Compose layer. */
enum class FontRowState { Available, Downloading, Failed, Downloaded }

/** A family that finished its full load also has its Regular weight, so [FontCoverage.Full] covers [FontCoverage.Sample]. */
fun FontLoadStates.stateOf(id: String, coverage: FontCoverage): FontLoadState {
    val own = this[FontLoadKey(id, coverage)] ?: FontLoadState.Idle
    val full = this[FontLoadKey(id, FontCoverage.Full)]
    return if (coverage == FontCoverage.Sample && full == FontLoadState.Ready) FontLoadState.Ready else own
}

fun FontLoadStates.isReady(id: String, coverage: FontCoverage): Boolean =
    stateOf(id, coverage) == FontLoadState.Ready

/**
 * Row state for a downloadable family, from its full load only. The [FontCoverage.Sample] prefetch is passive (the
 * user never asked for it), so its progress or failure must not look like a download or an error on the row.
 */
fun FontLoadStates.rowState(id: String): FontRowState = when (stateOf(id, FontCoverage.Full)) {
    FontLoadState.Loading -> FontRowState.Downloading
    is FontLoadState.Failed -> FontRowState.Failed
    FontLoadState.Ready -> FontRowState.Downloaded
    FontLoadState.Idle -> FontRowState.Available
}

/** What the user can usefully be told about a failed download; the copy lives in string resources. */
enum class FontFailureAdvice {
    /** Probably the network: try again later. */
    CheckConnection,

    /** The font provider is absent, not Google Play services, or rejected VOID; retrying will not help. */
    ProviderUnavailable,

    /** The provider does not know the family (any more). */
    FontMissing
}

fun FontFailure.advice(): FontFailureAdvice = when (this) {
    FontFailure.ProviderNotFound,
    FontFailure.WrongCertificates,
    FontFailure.SecurityViolation -> FontFailureAdvice.ProviderUnavailable
    FontFailure.FontNotFound,
    FontFailure.MalformedQuery -> FontFailureAdvice.FontMissing
    FontFailure.FontLoadError,
    FontFailure.FontUnavailable,
    FontFailure.Timeout,
    FontFailure.Unknown -> FontFailureAdvice.CheckConnection
}

/** How the Settings subtitle qualifies the saved family's name. */
enum class FontAvailability {
    /** Nothing to add: the saved family is in use, or the saved id is unknown and the default is named instead. */
    Available,

    /** A downloadable family that is still being loaded in this process. */
    Loading,

    /** A downloadable family on a device without the Google Play services font provider. */
    NeedsPlayServices,

    /** A downloadable family whose last load failed. */
    NotDownloaded
}

/** [fullState] is the full-coverage load state of the saved family, see [stateOf]. */
fun fontAvailability(reason: FontResolutionReason, fullState: FontLoadState): FontAvailability = when (reason) {
    FontResolutionReason.Exact,
    FontResolutionReason.UnknownId -> FontAvailability.Available
    FontResolutionReason.GmsUnavailable -> FontAvailability.NeedsPlayServices
    FontResolutionReason.NotReady ->
        if (fullState is FontLoadState.Failed) FontAvailability.NotDownloaded else FontAvailability.Loading
}

private const val MAX_CAUSE_DEPTH = 8
private val REASON_PATTERN = Regex("""reason=(-?\d+)""")

// FontsContractCompat.FontRequestCallback.FAIL_REASON_* values; the Compose loader only reports them in a message.
private val FAILURE_BY_REASON_CODE: Map<Int, FontFailure> = mapOf(
    -1 to FontFailure.ProviderNotFound,
    -2 to FontFailure.WrongCertificates,
    -3 to FontFailure.FontLoadError,
    -4 to FontFailure.SecurityViolation,
    1 to FontFailure.FontNotFound,
    2 to FontFailure.FontUnavailable,
    3 to FontFailure.MalformedQuery
)

/**
 * Maps an exception thrown by `FontFamily.Resolver.preload` to a [FontFailure]. Compose wraps the provider error
 * (and its own timeout) in an `IllegalStateException`, so the whole cause chain is searched.
 */
fun classifyFontFailure(error: Throwable): FontFailure {
    var current: Throwable? = error
    var depth = 0
    while (current != null && depth < MAX_CAUSE_DEPTH) {
        if (current is TimeoutCancellationException) return FontFailure.Timeout
        val code = current.message?.let(REASON_PATTERN::find)?.groupValues?.get(1)?.toIntOrNull()
        code?.let(FAILURE_BY_REASON_CODE::get)?.let { return it }
        current = current.cause
        depth++
    }
    return FontFailure.Unknown
}
