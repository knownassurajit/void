package com.knownassurajit.app.launcher.voidlauncher.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Schedules and cancels the alarm behind a note reminder. Exact alarms need a user-granted special
 * access on Android 12+ (denied by default for new installs on 14+); without it the reminder falls
 * back to an inexact alarm instead of silently never firing.
 */
object NoteReminderScheduler {
    private const val TAG = "NoteReminderScheduler"

    /**
     * Request code and notification id for [noteId]. Note ids are epoch milliseconds, so a plain
     * `toInt()` keeps only the low 32 bits; folding the high half in keeps distinct ids apart.
     */
    fun requestCodeFor(noteId: Long): Int = (noteId xor (noteId ushr 32)).toInt()

    /** Exact alarms are free below Android 12; from then on they need the special access. */
    fun usesExactAlarm(sdkInt: Int, canScheduleExactAlarms: Boolean): Boolean =
        sdkInt < Build.VERSION_CODES.S || canScheduleExactAlarms

    fun schedule(context: Context, noteId: Long, noteText: String, triggerAtMillis: Long): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        val pending = PendingIntent.getBroadcast(
            context,
            requestCodeFor(noteId),
            reminderIntent(context, noteId).putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val exact = usesExactAlarm(
            Build.VERSION.SDK_INT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()
        )
        return try {
            if (exact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            } else {
                Log.i(TAG, "reminder_schedule mode=inexact reason=exact_alarm_not_granted")
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "reminder_schedule failed reason=${e.javaClass.simpleName}")
            false
        }
    }

    /** Cancels a pending reminder so a deleted or cleared note never fires with stale text. */
    fun cancel(context: Context, noteId: Long) {
        val pending = PendingIntent.getBroadcast(
            context,
            requestCodeFor(noteId),
            reminderIntent(context, noteId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java)?.cancel(pending)
        pending.cancel()
    }

    private fun reminderIntent(context: Context, noteId: Long): Intent =
        Intent(context, NoteReminderReceiver::class.java)
            .putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId)
}
