package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.Gravity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.NoteItem
import com.knownassurajit.app.launcher.voidlauncher.data.NoteRepository
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.helper.NoteReminderScheduler
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.components.dismissDirection
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.ui.gesture.SwipeAxis
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalPanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import kotlinx.coroutines.launch

/**
 * Whether a note row is deleted by a swipe from the start side to the end side (`true`), or from
 * the end side to the start side. Deleting is permanent, so it must never be the swipe that closes
 * Notes (toward the edge the panel came from): a row consumes the drag first, so closing Notes with
 * a finger over a note would delete the note. The panel from the left edge closes with a swipe left,
 * so there a note is deleted with a swipe right; everywhere else it is a swipe left (right to left).
 * [rtl] is the layout direction, because "start" and "end" flip with it while the closing swipe is
 * physical (see [dismissDirection]).
 */
internal fun noteDeleteFromStartToEnd(edge: PanelEdge, rtl: Boolean): Boolean {
    val closing = dismissDirection(edge)
    val closesWithASwipeLeft = closing.axis == SwipeAxis.Horizontal && closing.sign < 0
    // A swipe right deletes only when a swipe left closes. Swiping right is start to end in LTR, end to start in RTL.
    return closesWithASwipeLeft != rtl
}

/** What the long-press menu of a note offers. */
internal enum class NoteAction { Edit, ToggleStar, ToggleDone, SetReminder, ClearReminder, Copy, Share, Delete }

/** The menu entries for [note], in display order: a reminder can only be cleared when one is set. */
internal fun noteActionsFor(note: NoteItem): List<NoteAction> = buildList {
    add(NoteAction.Edit)
    add(NoteAction.ToggleStar)
    add(NoteAction.ToggleDone)
    add(NoteAction.SetReminder)
    if (note.hasReminder) add(NoteAction.ClearReminder)
    add(NoteAction.Copy)
    add(NoteAction.Share)
    add(NoteAction.Delete)
}

private val NoteItem.hasReminder: Boolean get() = reminderTime != null && reminderTime > 0L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val deleteFromStartToEnd = noteDeleteFromStartToEnd(
        edge = LocalPanelEdge.current,
        rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    )
    val prefs = remember { Prefs.get(context) }
    var notesTitleAlignment by remember { mutableIntStateOf(prefs.notesTitleAlignment) }
    val repo = remember { NoteRepository(context) }
    val notes = remember { mutableStateListOf<NoteItem>() }
    var newNoteText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var optionsFor by remember { mutableStateOf<NoteItem?>(null) }
    var editing by remember { mutableStateOf<NoteItem?>(null) }
    var reminderFor by remember { mutableStateOf<NoteItem?>(null) }
    val deletedMessage = stringResource(R.string.note_deleted)
    val undoLabel = stringResource(R.string.undo)

    fun refreshNotes() {
        notes.clear()
        notes.addAll(repo.getAllNotes())
    }

    fun addNote() {
        val text = newNoteText.trim()
        if (text.isNotBlank()) {
            repo.addNote(text)
            refreshNotes()
        }
        newNoteText = ""
    }

    // Deleting is reversible for a moment: the note and its reminder come back on Undo.
    fun deleteNote(note: NoteItem) {
        NoteReminderScheduler.cancel(context, note.id)
        repo.deleteNote(note.id)
        refreshNotes()
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(deletedMessage, actionLabel = undoLabel, withDismissAction = true)
            if (result == SnackbarResult.ActionPerformed) {
                repo.restoreNote(note)
                note.reminderTime?.takeIf { it > System.currentTimeMillis() }?.let { time ->
                    NoteReminderScheduler.schedule(context, note.id, note.text, time)
                }
                refreshNotes()
            }
        }
    }

    fun perform(note: NoteItem, action: NoteAction) {
        when (action) {
            NoteAction.Edit -> editing = note
            NoteAction.ToggleStar -> repo.toggleStarred(note.id)
            NoteAction.ToggleDone -> repo.toggleComplete(note.id)
            NoteAction.SetReminder -> reminderFor = note
            NoteAction.ClearReminder -> {
                NoteReminderScheduler.cancel(context, note.id)
                repo.updateNoteReminder(note.id, null)
            }
            NoteAction.Copy -> copyNote(context, note)
            NoteAction.Share -> shareNote(context, note)
            NoteAction.Delete -> {
                deleteNote(note)
                return
            }
        }
        refreshNotes()
    }

    LaunchedEffect(Unit) { refreshNotes() }

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
                onValueChange = { value ->
                    // A pasted or hardware-keyboard newline submits like the IME Done action.
                    if (value.endsWith("\n")) {
                        newNoteText = value
                        addNote()
                    } else {
                        newNoteText = value
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.add_note_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = VoidAlpha.muted)
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
                keyboardActions = KeyboardActions(onDone = { addNote() })
            )

            Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing))

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = VoidAlpha.muted)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items = notes.toList(), key = { it.id }) { note ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value != SwipeToDismissBoxValue.Settled) {
                                    deleteNote(note)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = deleteFromStartToEnd,
                            enableDismissFromEndToStart = !deleteFromStartToEnd,
                            // Only a tint while the row is actually moving: nothing sits behind a resting row.
                            backgroundContent = {
                                if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                                    )
                                }
                            }
                        ) {
                            NoteRow(
                                note = note,
                                onToggle = { perform(note, NoteAction.ToggleDone) },
                                onToggleStar = { perform(note, NoteAction.ToggleStar) },
                                onClick = { editing = note },
                                onLongClick = { optionsFor = note },
                                onDelete = { deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    optionsFor?.let { note ->
        NoteOptionsSheet(
            note = note,
            onDismiss = { optionsFor = null },
            onAction = { action ->
                optionsFor = null
                perform(note, action)
            }
        )
    }

    editing?.let { note ->
        NoteEditDialog(
            note = note,
            onDismiss = { editing = null },
            onSave = { text ->
                repo.updateNoteText(note.id, text)
                if (note.hasReminder) {
                    note.reminderTime?.takeIf { it > System.currentTimeMillis() }?.let { time ->
                        NoteReminderScheduler.schedule(context, note.id, text, time)
                    }
                }
                editing = null
                refreshNotes()
            }
        )
    }

    reminderFor?.let { note ->
        NoteReminderDialogs(
            note = note,
            onDismiss = { reminderFor = null },
            onReminderSet = { triggerTime ->
                repo.updateNoteReminder(note.id, triggerTime)
                NoteReminderScheduler.schedule(context, note.id, note.text, triggerTime)
                launchReminderApp(context, note, triggerTime)
                reminderFor = null
                refreshNotes()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteRow(
    note: NoteItem,
    onToggle: () -> Unit,
    onToggleStar: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val deleteLabel = stringResource(R.string.delete_note)
    val optionsLabel = stringResource(R.string.note_options)
    Column(
        // An opaque row: the swipe background must never show through a resting row.
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClickLabel = optionsLabel,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                )
                // The swipe is not reachable with TalkBack or Switch Access, so delete is also an action.
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(deleteLabel) {
                            onDelete()
                            true
                        }
                    )
                }
                .padding(vertical = VoidDimens.rowSpacing),
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = VoidDimens.rowSpacing)
            ) {
                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (note.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = VoidAlpha.muted)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val reminderTime = note.reminderTime
                if (reminderTime != null && reminderTime > 0L) {
                    val formatted = remember(reminderTime) {
                        java.text.SimpleDateFormat("EEE d MMM · h:mm a", java.util.Locale.getDefault())
                            .format(java.util.Date(reminderTime))
                    }
                    Text(
                        text = formatted.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = VoidAlpha.muted),
                        modifier = Modifier.padding(top = VoidDimens.compactSpacing)
                    )
                }
            }

            IconButton(onClick = onToggleStar) {
                Icon(
                    imageVector = if (note.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(if (note.isStarred) R.string.note_unstar else R.string.note_star),
                    tint = if (note.isStarred) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = VoidAlpha.disabled)
                    }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = VoidAlpha.divider))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteOptionsSheet(
    note: NoteItem,
    onDismiss: () -> Unit,
    onAction: (NoteAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = note.text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing)
        )
        noteActionsFor(note).forEach { action ->
            val (icon, label) = noteActionPresentation(note, action)
            val destructive = action == NoteAction.Delete
            ListItem(
                headlineContent = {
                    Text(
                        text = label,
                        color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.clickable { onAction(action) }
            )
        }
        Spacer(modifier = Modifier.height(VoidDimens.sectionSpacing))
    }
}

@Composable
private fun noteActionPresentation(note: NoteItem, action: NoteAction): Pair<ImageVector, String> = when (action) {
    NoteAction.Edit -> Icons.Outlined.Edit to stringResource(R.string.edit)
    NoteAction.ToggleStar -> if (note.isStarred) {
        Icons.Outlined.StarBorder to stringResource(R.string.note_unstar)
    } else {
        Icons.Filled.Star to stringResource(R.string.note_star)
    }
    NoteAction.ToggleDone -> if (note.isCompleted) {
        Icons.Outlined.CheckBoxOutlineBlank to stringResource(R.string.note_mark_not_done)
    } else {
        Icons.Outlined.CheckBox to stringResource(R.string.note_mark_done)
    }
    NoteAction.SetReminder -> Icons.Outlined.Alarm to stringResource(
        if (note.hasReminder) R.string.note_change_reminder else R.string.set_reminder
    )
    NoteAction.ClearReminder -> Icons.Outlined.AlarmOff to stringResource(R.string.note_clear_reminder)
    NoteAction.Copy -> Icons.Outlined.ContentCopy to stringResource(R.string.copy)
    NoteAction.Share -> Icons.Outlined.Share to stringResource(R.string.share)
    NoteAction.Delete -> Icons.Outlined.DeleteOutline to stringResource(R.string.delete_note)
}

@Composable
private fun NoteEditDialog(
    note: NoteItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember(note.id) { mutableStateOf(note.text) }
    val trimmed = text.trim()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.note_edit_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (trimmed.isNotEmpty()) onSave(trimmed) })
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(trimmed) }, enabled = trimmed.isNotEmpty()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Date first, then time; [onReminderSet] gets the chosen moment in epoch milliseconds. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteReminderDialogs(
    note: NoteItem,
    onDismiss: () -> Unit,
    onReminderSet: (Long) -> Unit
) {
    var selectedDateMillis by remember(note.id) { mutableStateOf<Long?>(null) }
    val pickedDate = selectedDateMillis
    if (pickedDate == null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = note.reminderTime?.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { selectedDateMillis = datePickerState.selectedDateMillis },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text(stringResource(R.string.next)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.set_time)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onReminderSet(reminderTriggerTime(pickedDate, timePickerState.hour, timePickerState.minute))
                }) { Text(stringResource(R.string.set_reminder)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

/**
 * The reminder moment for a date from the Material date picker and a local [hour]:[minute]. The
 * picker returns midnight UTC of the chosen day, so the day is read in UTC and the time is set in
 * the device zone; reading it in the local zone would pick the previous day west of UTC.
 */
internal fun reminderTriggerTime(utcDateMillis: Long, hour: Int, minute: Int): Long {
    val utc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDateMillis }
    return java.util.Calendar.getInstance().apply {
        clear()
        set(
            utc.get(java.util.Calendar.YEAR),
            utc.get(java.util.Calendar.MONTH),
            utc.get(java.util.Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0
        )
    }.timeInMillis
}

private fun copyNote(context: Context, note: NoteItem) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.notes_title), note.text))
}

private fun shareNote(context: Context, note: NoteItem) {
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, note.text)
    try {
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: android.content.ActivityNotFoundException) {
        android.util.Log.w("NotesScreen", "note_share_failed reason=${e.javaClass.simpleName}")
    }
}

private fun launchReminderApp(context: Context, note: NoteItem, triggerTime: Long) {
    var launched = false
    if (note.isStarred) {
        val pkg = "com.google.android.apps.tasks"
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
            } else {
                context.packageManager.getLaunchIntentForPackage(pkg)?.let { launchIntent ->
                    context.startActivity(launchIntent)
                    launched = true
                }
            }
        } catch (_: Exception) {}
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
