package com.knownassurajit.app.launcher.voidlauncher.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability

private const val TAG = "NotificationAccess"

/**
 * Whether VOID may read notifications. Read again each time the screen resumes, because the usual
 * way back from allowing it is the Back button in system Settings, and the panel should then fill in
 * without being reopened.
 */
@Composable
fun rememberNotificationAccess(): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(FeatureAvailability.isNotificationListenerEnabled(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val now = FeatureAvailability.isNotificationListenerEnabled(context)
        if (now != granted) Log.i(TAG, "notification_access_changed granted=$now")
        granted = now
    }
    return granted
}

/** Explains why a notification panel is empty without notification access, with a button to the system page that grants it. */
@Composable
fun NotificationAccessPrompt(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val openFailed = stringResource(R.string.could_not_open_notification_settings)
    PanelMessage(
        body = stringResource(R.string.notifications_need_access),
        modifier = modifier,
        action = {
            FilledTonalButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    } catch (e: ActivityNotFoundException) {
                        Log.w(TAG, "notification_access_settings_unavailable reason=${e.javaClass.simpleName}")
                        Toast.makeText(context, openFailed, Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        Log.w(TAG, "notification_access_settings_denied reason=${e.javaClass.simpleName}")
                        Toast.makeText(context, openFailed, Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(stringResource(R.string.grant_notification_access))
            }
        }
    )
}
