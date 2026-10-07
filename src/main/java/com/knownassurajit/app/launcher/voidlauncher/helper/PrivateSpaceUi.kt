package com.knownassurajit.app.launcher.voidlauncher.helper

/**
 * What a lock/unlock request achieved. This is the answer to the request only; the lock state the
 * UI shows always comes from re-querying the system, never from this value.
 */
enum class ToggleOutcome {
    /** Quiet mode was turned on: the space is now locked. */
    LOCKED,

    /** Quiet mode was turned off: the space is now unlocked. */
    UNLOCKED,

    /** The system needs the user first (credential prompt or screen-lock setup); nothing changed yet. */
    NEEDS_USER_ACTION,

    /** The request was refused or could not be made (for example VOID is not the foreground default launcher). */
    FAILED
}

/**
 * Pure Private Space UI decisions. No android.* types so the rules stay JVM-testable.
 */
object PrivateSpaceUi {

    /**
     * Maps a quiet-mode request to its outcome. `UserManager.requestQuietModeEnabled` returns false
     * when the change did not happen, which for an unlock (`requestedQuiet == false`) means the
     * system is asking for credentials, so it must not be treated as unlocked.
     */
    fun outcomeFor(requestedQuiet: Boolean, requestReturned: Boolean): ToggleOutcome = when {
        requestReturned && requestedQuiet -> ToggleOutcome.LOCKED
        requestReturned -> ToggleOutcome.UNLOCKED
        requestedQuiet -> ToggleOutcome.FAILED
        else -> ToggleOutcome.NEEDS_USER_ACTION
    }

    /**
     * The Private Space settings action (bottom-header gear and search-bar Shield button) is only
     * offered while the space is unlocked and a settings/add entry exists. It never depends on
     * the search query or on where the controls are placed.
     */
    fun settingsActionVisible(isLocked: Boolean, settingsAvailable: Boolean): Boolean =
        !isLocked && settingsAvailable

    /**
     * Heuristic for the private-profile "Add apps" entry that backs the settings action: a label
     * of Add / Add app(s), or an activity class that mentions PrivateSpace together with Add or
     * Install.
     */
    fun isAddEntryLabel(label: String, activityClassName: String?): Boolean {
        val trimmed = label.trim()
        if (trimmed.equals("Add", ignoreCase = true) ||
            trimmed.equals("Add apps", ignoreCase = true) ||
            trimmed.equals("Add app", ignoreCase = true)
        ) {
            return true
        }
        val cls = activityClassName.orEmpty()
        return cls.contains("PrivateSpace", ignoreCase = true) &&
            (cls.contains("Add", ignoreCase = true) || cls.contains("Install", ignoreCase = true))
    }
}
