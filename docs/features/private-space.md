# Private Space

## Behavior

- Requires Android 15+ (API 35 / `VANILLA_ICE_CREAM`).
- Detects the private profile via `LauncherApps.getLauncherUserInfo` + `USER_TYPE_PROFILE_PRIVATE`.
- Quiet mode lock/unlock uses `UserManager.requestQuietModeEnabled`.
- Drawer shows a Private Space section when enabled in Settings and a profile exists.
- Placement pref `PRIVATE_SPACE_PLACEMENT`: `bottom` (default header at the end of the list) or `search_bar` (lock/settings next to search; bottom header hidden).

## Settings action visibility

- The lock toggle is always shown when the section is shown.
- The settings action (Settings gear in the bottom header, Shield button in the search bar) is shown only when the space is unlocked **and** a settings/add entry exists in the private profile's launcher list. Rule: `PrivateSpaceUi.settingsActionVisible(isLocked, settingsAvailable)`.
- Both placements use the same rule. The entry is looked up in the unfiltered private app list, so typing a search query never hides the action.
- Tapping the action launches the settings/add entry (matched by the `PrivateSpaceUi.isAddEntryLabel` heuristic).

## Failure modes

- Missing OS Private Space → settings subtitle explains not configured; feature degrades.
- API/OEM failures → empty private app list; no crash.
- See `PrivateSpaceHelper` for guarded entry points.
