package com.knownassurajit.app.launcher.voidlauncher.helper

/**
 * Pure Private Space UI decisions. No android.* types so the rules stay JVM-testable.
 */
object PrivateSpaceUi {

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
