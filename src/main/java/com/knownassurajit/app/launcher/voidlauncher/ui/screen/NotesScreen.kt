package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.mutableIntStateOf
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.NoteItem
import com.knownassurajit.app.launcher.voidlauncher.data.NoteRepository
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.components.dismissDirection
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalPanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens

/**
 * Whether a note row is deleted by a swipe from the start side to the end side (`true`), or from
 * the end side to the start side. Deleting is permanent, so it must never be the swipe that closes
 * Notes (toward the edge the panel came from): a row consumes the drag first, so closing Notes with
 * a finger over a note would delete the note. The panel from the left edge closes with a swipe left,
 * so there a note is deleted with a swipe right; everywhere else it is a swipe left, as before.
 * [rtl] is the layout direction, because "start" and "end" flip with it while the closing swipe is
 * physical (see [dismissDirection]).
 */
internal fun noteDeleteFromStartToEnd(edge: PanelEdge, rtl: Boolean): Boolean {
    val closing = dismissDirection(edge)
    val closesWithASwipeLeft = closing.axis == SwipeAxis.Horizontal && closing.sign < 0
    // A swipe right deletes only when a swipe left closes. Swiping right is start to end in LTR, end to start in RTL.
    return closesWithASwipeLeft != rtl
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val deleteFromStartToEnd = noteDeleteFromStartToEnd(
        edge = LocalPanelEdge.current,
        rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    )
    val prefs = remember { Prefs.get(context) }
    var notesTitleAlignment by remember { mutableIntStateOf(prefs.notesTitleAlignment) }
    val repo = remember { NoteRepository(context) }
    val notes = remember { mutableStateListOf<NoteItem>() }
    var newNoteText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        notes.clear()
        notes.addAll(repo.getAllNotes())
    }

    fun refreshNotes() {
        notes.clear()
        notes.addAll(repo.getAllNotes())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            .screenBackSwipe(onBack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.sectionSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = when (notesTitleAlignment) {
                    Gravity.CENTER_HORIZONTAL -> Arrangement.Center
                    Gravity.END -> Arrangement.End
                    else -> Arrangement.Start
                }
            ) {
                Text(
                    text = stringResource(R.string.notes_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        val next = when (notesTitleAlignment) {
                            Gravity.START -> Gravity.CENTER_HORIZONTAL
                            Gravity.CENTER_HORIZONTAL -> Gravity.END
                            else -> Gravity.START
                        }
                        notesTitleAlignment = next
                        prefs.notesTitleAlignment = next
                    }
                )
            }
            VoidSectionDivider(modifier = Modifier.padding(vertical = VoidDimens.rowSpacing))
            OutlinedTextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                placeholder = {
                    Text(
                        stringResource(R.string.add_note_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val text = newNoteText.trim()
                        if (text.isNotBlank()) {
                            repo.addNote(text)
                            refreshNotes()
                        }
                        newNoteText = ""
                    }
                )
            )

            LaunchedEffect(newNoteText) {
                if (newNoteText.endsWith("\n")) {
                    val text = newNoteText.trim()
                    if (text.isNotBlank()) {
                        repo.addNote(text)
                        refreshNotes()
                    }
                    newNoteText = ""
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_notes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(items = notes.toList(), key = { it.id }) { note ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value != SwipeToDismissBoxValue.Settled) {
                                    repo.deleteNote(note.id)
                                    refreshNotes()
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = deleteFromStartToEnd,
                            enableDismissFromEndToStart = !deleteFromStartToEnd,
                            backgroundContent = {
                                val isDeleting = dismissState.targetValue != SwipeToDismissBoxValue.Settled
                                val bgColor = if (isDeleting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                                val iconColor = if (isDeleting) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(bgColor, MaterialTheme.shapes.small)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = if (deleteFromStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.swipe_to_delete),
                                        tint = iconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        ) {
                            NoteRow(
                                note = note,
                                onToggle = {
                                    repo.toggleComplete(note.id)
                                    refreshNotes()
                                },
                                onToggleStar = {
                                    repo.toggleStarred(note.id)
                                    refreshNotes()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun NoteRow(
    note: NoteItem,
    onToggle: () -> Unit,
    onToggleStar: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { NoteRepository(context) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // MD3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text(stringResource(R.string.next)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    // MD3 TimePickerDialog (custom since M3 doesn't have a built-in dialog wrapper)
    if (showTimePicker && selectedDateMillis != null) {
        val timePickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = 9, initialMinute = 0, is24Hour = false
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.set_time)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val cal = java.util.Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis!!
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                        set(java.util.Calendar.SECOND, 0)
                    }
                    val triggerTime = cal.timeInMillis
                    repo.updateNoteReminder(note.id, triggerTime)

                    // Schedule alarm
                    try {
                        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                        val alarmIntent = Intent(context, com.knownassurajit.app.launcher.voidlauncher.helper.NoteReminderReceiver::class.java).apply {
                            putExtra("note_id", note.id)
                            putExtra("note_text", note.text)
                        }
                        val pending = android.app.PendingIntent.getBroadcast(
                            context, note.id.toInt(), alarmIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pending)
                    } catch (_: Exception) {}

                    // Use Google Tasks as default reminder if available for starred tasks, calendar as secondary
                    launchReminderApp(context, note, triggerTime)

                    showTimePicker = false
                }) { Text(stringResource(R.string.set_reminder)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = note.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.onSurface,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (note.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (note.reminderTime != null && note.reminderTime > 0L) {
                val formatted = remember(note.reminderTime) {
                    java.text.SimpleDateFormat("EEE d MMM · h:mm a", java.util.Locale.getDefault())
                        .format(java.util.Date(note.reminderTime))
                }
                Text(
                    text = formatted.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        IconButton(
            onClick = onToggleStar,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (note.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (note.isStarred) "Starred" else "Star",
                tint = if (note.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
}

private fun launchReminderApp(context: android.content.Context, note: NoteItem, triggerTime: Long) {
    var launched = false
    if (note.isStarred) {
        val tasksPackages = listOf(
            "com.google.android.apps.tasks",
            "com.google.adnroid.app.tasks"
        )
        for (pkg in tasksPackages) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    setPackage(pkg)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, note.text)
                    putExtra(Intent.EXTRA_TITLE, note.text)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    launched = true
                    break
                }
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    launched = true
                    break
                }
            } catch (_: Exception) {}
        }
    }

    if (!launched) {
        try {
            val calIntent = Intent(Intent.ACTION_INSERT).apply {
                data = android.provider.CalendarContract.Events.CONTENT_URI
                putExtra(android.provider.CalendarContract.Events.TITLE, note.text)
                putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, triggerTime)
                putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, triggerTime + 30 * 60 * 1000)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(calIntent)
        } catch (_: Exception) {}
    }
}
