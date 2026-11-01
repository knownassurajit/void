package com.knownassurajit.app.launcher.voidlauncher.helper

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.util.Calendar
import java.util.TimeZone

/**
 * Arithmetic on foreground time, free of Android types so the rules are JVM-testable. The Home
 * total and the per-app figures of the app library are both built from [aggregate], so they always
 * add up.
 */
object ScreenTimeMath {
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MINUTES_PER_HOUR = 60L

    /**
     * Sums the foreground time of every package. A package can appear in several usage buckets, so
     * entries of the same package add up; packages in [exclude] (VOID itself: it is in the foreground
     * whenever Home is shown) and entries without any time are dropped.
     */
    fun aggregate(entries: List<Pair<String, Long>>, exclude: Set<String> = emptySet()): Map<String, Long> =
        entries.asSequence()
            .filter { (packageName, millis) -> packageName !in exclude && millis > 0L }
            .groupingBy { it.first }
            .fold(0L) { total, entry -> total + entry.second }

    /** The first millisecond of the day that contains [nowMillis] in [timeZone]. */
    fun startOfDay(nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long =
        Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** "2h 5m", or "12m" under an hour: whole minutes, rounded down. */
    fun hoursMinutes(millis: Long): String {
        val totalMinutes = (millis.coerceAtLeast(0L)) / MILLIS_PER_MINUTE
        val hours = totalMinutes / MINUTES_PER_HOUR
        val minutes = totalMinutes % MINUTES_PER_HOUR
        return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
    }

    /** The label next to an app: [hoursMinutes], "<1m" for a few seconds, and `null` when there is nothing to show. */
    fun appLabel(millis: Long): String? = when {
        millis <= 0L -> null
        millis < MILLIS_PER_MINUTE -> "<1m"
        else -> hoursMinutes(millis)
    }
}

/** What reading today's foreground time found. */
sealed interface ScreenTimeResult {
    /** Foreground milliseconds per package for today (VOID itself left out). */
    data class Usage(val millisByPackage: Map<String, Long>) : ScreenTimeResult {
        val totalMillis: Long get() = millisByPackage.values.sum()
    }

    /** Usage access is not granted: there is nothing to read until the user allows it. */
    data object PermissionRequired : ScreenTimeResult

    /** The system refused or failed the query. */
    data object Unavailable : ScreenTimeResult
}

/**
 * Reads today's foreground time per app from `UsageStatsManager`. It blocks on a system query, so
 * call it off the main thread. Usage access is a runtime gate (`FeatureAvailability`): without it
 * the result is [ScreenTimeResult.PermissionRequired] and nothing is queried.
 */
class ScreenTimeSource(private val context: Context) {

    fun loadToday(nowMillis: Long = System.currentTimeMillis()): ScreenTimeResult {
        if (!FeatureAvailability.isUsageAccessGranted(context)) {
            Log.i(TAG, "screen_time_skipped reason=usage_access_denied")
            return ScreenTimeResult.PermissionRequired
        }
        return try {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats = manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                ScreenTimeMath.startOfDay(nowMillis),
                nowMillis
            )
            val entries = stats.orEmpty().map { it.packageName to it.totalTimeInForeground }
            ScreenTimeResult.Usage(ScreenTimeMath.aggregate(entries, exclude = setOf(context.packageName)))
        } catch (e: Exception) {
            Log.w(TAG, "screen_time_failed reason=${e.javaClass.simpleName}")
            ScreenTimeResult.Unavailable
        }
    }

    private companion object {
        const val TAG = "ScreenTimeSource"
    }
}
