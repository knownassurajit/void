package com.voidlauncher.app.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.voidlauncher.app.R

class NoteReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "note_reminders"
        const val EXTRA_NOTE_TEXT = "note_text"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val noteText = intent.getStringExtra(EXTRA_NOTE_TEXT) ?: return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val isSilent = am.ringerMode != AudioManager.RINGER_MODE_NORMAL
        val isDnd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        } else {
            false
        }
        val isScreenOn = pm.isInteractive

        val useSilentChannel = isSilent || isDnd || isScreenOn
        val activeChannelId = if (useSilentChannel) "${CHANNEL_ID}_silent" else CHANNEL_ID

        // Create channels (no-op on repeated calls)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val silentChannel = NotificationChannel(
                "${CHANNEL_ID}_silent",
                "Note Reminders (Silent)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
            }
            nm.createNotificationChannel(channel)
            nm.createNotificationChannel(silentChannel)
        }

        val builder = NotificationCompat.Builder(context, activeChannelId)
            .setSmallIcon(R.drawable.ic_edit)
            .setContentTitle("Note Reminder")
            .setContentText(noteText)
            .setAutoCancel(true)

        if (useSilentChannel) {
            builder.setSilent(true)
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                nm.notify(noteText.hashCode(), builder.build())
            }
        } else {
            nm.notify(noteText.hashCode(), builder.build())
        }

        if (isScreenOn) {
            Toast.makeText(context, "Reminder: $noteText", Toast.LENGTH_LONG).show()
        }
    }
}
