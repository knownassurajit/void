# Notes

Checklist-style notes with optional reminders (`NotesScreen`, `NoteRepository`, WorkManager reminder worker).

- Swipe dismiss / priority ordering
- No AI summarization on notes (AI is for notifications only)
- Reachable via configurable swipe actions from home

## Options and deletion

- Swipe a note toward the start edge (right to left; left to right only when Notes opens from the left edge, where right to left closes the panel) to delete it. Nothing is drawn behind a resting row; a tint shows only while the row moves.
- Every deletion offers **Undo** (snackbar), restoring the note and re-arming a future reminder.
- Long-press a note for its options: Edit, Star/Unstar, Mark done, Set/Change reminder, Clear reminder, Copy, Share, Delete. Tapping a note edits it. TalkBack exposes Delete as a custom action.
- Reminders are scheduled by `NoteReminderScheduler`: exact alarms when the user granted them (Android 12+), otherwise an inexact alarm. The receiver re-reads the note, so a deleted or completed note never fires.
