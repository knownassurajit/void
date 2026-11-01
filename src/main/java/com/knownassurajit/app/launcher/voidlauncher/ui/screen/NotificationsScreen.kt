package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.NotificationGroup
import com.knownassurajit.app.launcher.voidlauncher.helper.NotificationService
import com.knownassurajit.app.launcher.voidlauncher.ui.components.DismissibleCard
import com.knownassurajit.app.launcher.voidlauncher.ui.components.NotificationAccessPrompt
import com.knownassurajit.app.launcher.voidlauncher.ui.components.NotificationCard
import com.knownassurajit.app.launcher.voidlauncher.ui.components.PanelMessage
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidPanelTitle
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.components.panelTitlePadding
import com.knownassurajit.app.launcher.voidlauncher.ui.components.rememberNotificationAccess
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens

@Composable
fun NotificationsScreen(
    notifications: List<NotificationGroup> = emptyList(),
    onBack: () -> Unit
) {
    val hasAccess = rememberNotificationAccess()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            .screenBackSwipe(onBack)
    ) {
        VoidPanelTitle(
            text = stringResource(R.string.notifications),
            modifier = Modifier.panelTitlePadding()
        )
        VoidSectionDivider(modifier = Modifier.padding(horizontal = VoidDimens.screenPadding))

        val contentModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        when {
            !hasAccess -> NotificationAccessPrompt(modifier = contentModifier)
            notifications.isEmpty() -> PanelMessage(body = stringResource(R.string.no_notifications), modifier = contentModifier)
            else -> {
                NotificationList(notifications = notifications, modifier = contentModifier)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VoidDimens.rowSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { NotificationService.dismissAll() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(text = stringResource(R.string.clear_all), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationList(notifications: List<NotificationGroup>, modifier: Modifier = Modifier) {
    val dismissLabel = stringResource(R.string.dismiss)
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing),
        verticalArrangement = Arrangement.spacedBy(VoidDimens.cardGap)
    ) {
        items(items = notifications, key = { it.groupKey }) { group ->
            DismissibleCard(
                dismissLabel = dismissLabel,
                onDismiss = { NotificationService.dismissNotificationsForPackage(group.packageName) },
                modifier = Modifier.animateItem()
            ) {
                NotificationGroupCard(group)
            }
        }
    }
}

/**
 * One app's notifications. Collapsed it shows the latest one; when the app has more, a tap on the
 * header shows them all, each with more of its text.
 */
@Composable
private fun NotificationGroupCard(group: NotificationGroup) {
    var expanded by rememberSaveable(group.groupKey) { mutableStateOf(false) }
    val relativeTime = remember(group.latestTimestamp) { relativeTimeOf(group.latestTimestamp) }
    val lines = remember(group.notifications) { group.notifications.map(NotificationLine::of) }
    val expandable = lines.size > 1
    val showAll = expanded && expandable

    NotificationCard(
        appLabel = group.appLabel,
        count = group.childCount,
        time = relativeTime,
        expanded = showAll,
        onToggle = if (expandable) ({ expanded = !expanded }) else null
    ) {
        val visible = if (showAll) lines else lines.take(1)
        val bodyLines = if (showAll || !expandable) EXPANDED_BODY_LINES else COLLAPSED_BODY_LINES
        visible.forEach { line -> NotificationLineText(line, bodyLines) }
    }
}

@Composable
private fun NotificationLineText(line: NotificationLine, bodyLines: Int) {
    Column {
        if (line.title.isNotBlank()) {
            Text(
                text = line.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (line.text.isNotBlank()) {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = bodyLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** The title and text a notification shows, read once from its extras. */
@Immutable
private data class NotificationLine(val title: String, val text: String) {
    companion object {
        fun of(sbn: StatusBarNotification): NotificationLine {
            val extras = sbn.notification.extras
            return NotificationLine(
                title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
                text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            )
        }
    }
}

/** "5 min. ago"-style time of a notification, in the device's language. */
internal fun relativeTimeOf(timestamp: Long): String =
    DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()

private const val COLLAPSED_BODY_LINES = 2
private const val EXPANDED_BODY_LINES = 4
