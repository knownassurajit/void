package com.knownassurajit.app.launcher.voidlauncher.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class NoteRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("void_notes", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "NoteRepository"
        private const val KEY_NOTES = "notes_json"
    }

    fun getAllNotes(): List<NoteItem> {
        val json = prefs.getString(KEY_NOTES, "[]") ?: "[]"
        return parseNotes(json).sortedWith(
            compareBy<NoteItem> { it.isCompleted }
                .thenByDescending { it.isStarred }
                .thenByDescending { it.priority }
                .thenByDescending { it.createdAt }
        )
    }

    fun addNote(text: String): NoteItem {
        val notes = getAllNotesRaw().toMutableList()
        val highestPriority = notes.maxOfOrNull { it.priority } ?: 0
        val note = NoteItem(
            text = text,
            priority = highestPriority + 1
        )
        notes.add(note)
        saveNotes(notes)
        return note
    }

    fun toggleComplete(id: Long) {
        val notes = getAllNotesRaw().toMutableList()
        val index = notes.indexOfFirst { it.id == id }
        if (index != -1) {
            notes[index] = notes[index].copy(isCompleted = !notes[index].isCompleted)
            saveNotes(notes)
        }
    }

    fun toggleStarred(id: Long) {
        val notes = getAllNotesRaw().toMutableList()
        val index = notes.indexOfFirst { it.id == id }
        if (index != -1) {
            notes[index] = notes[index].copy(isStarred = !notes[index].isStarred)
            saveNotes(notes)
        }
    }

    /** Puts back a note removed by [deleteNote] (undo), keeping its id, flags and reminder. */
    fun restoreNote(note: NoteItem) {
        val notes = getAllNotesRaw().filterNot { it.id == note.id } + note
        saveNotes(notes)
    }

    fun deleteNote(id: Long) {
        val notes = getAllNotesRaw().toMutableList()
        notes.removeAll { it.id == id }
        saveNotes(notes)
    }

    fun updateNoteText(id: Long, newText: String) {
        val notes = getAllNotesRaw().toMutableList()
        val index = notes.indexOfFirst { it.id == id }
        if (index != -1) {
            notes[index] = notes[index].copy(text = newText)
            saveNotes(notes)
        }
    }

    fun updateNoteReminder(id: Long, triggerTime: Long?) {
        val notes = getAllNotesRaw().toMutableList()
        val index = notes.indexOfFirst { it.id == id }
        if (index != -1) {
            notes[index] = notes[index].copy(reminderTime = triggerTime)
            saveNotes(notes)
        }
    }

    private fun getAllNotesRaw(): List<NoteItem> {
        val json = prefs.getString(KEY_NOTES, "[]") ?: "[]"
        return parseNotes(json)
    }

    private fun saveNotes(notes: List<NoteItem>) {
        val arr = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject()
            obj.put("id", note.id)
            obj.put("text", note.text)
            obj.put("isCompleted", note.isCompleted)
            obj.put("isStarred", note.isStarred)
            obj.put("priority", note.priority)
            obj.put("createdAt", note.createdAt)
            if (note.reminderTime != null) {
                obj.put("reminderTime", note.reminderTime)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_NOTES, arr.toString()).apply()
    }

    /**
     * Parses each entry on its own: one corrupted entry is skipped instead of dropping every note
     * after it, which the next save would then have made permanent.
     */
    private fun parseNotes(json: String): List<NoteItem> {
        val arr = try {
            JSONArray(json)
        } catch (_: Exception) {
            Log.w(TAG, "notes_parse_failed scope=array")
            return emptyList()
        }
        val list = mutableListOf<NoteItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i)
            val id = obj?.optLong("id", 0L) ?: 0L
            val text = obj?.optString("text").orEmpty()
            if (obj == null || id == 0L || text.isBlank()) {
                Log.w(TAG, "notes_parse_skipped index=$i")
                continue
            }
            list.add(
                NoteItem(
                    id = id,
                    text = text,
                    isCompleted = obj.optBoolean("isCompleted", false),
                    isStarred = obj.optBoolean("isStarred", false),
                    priority = obj.optInt("priority", 0),
                    createdAt = obj.optLong("createdAt", id),
                    reminderTime = if (obj.has("reminderTime")) obj.optLong("reminderTime") else null
                )
            )
        }
        return list
    }
}
