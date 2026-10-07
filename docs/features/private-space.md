# Private Space

## Behavior

- Requires Android 15+ (API 35 / `VANILLA_ICE_CREAM`).
- Detects the private profile via `LauncherApps.getLauncherUserInfo` + `USER_TYPE_PROFILE_PRIVATE`.
- Quiet mode lock/unlock is requested with `UserManager.requestQuietModeEnabled` (see State source: a request never decides what the UI shows).
- Drawer shows a Private Space section when enabled in Settings and a profile exists.
- Placement pref `PRIVATE_SPACE_PLACEMENT`: `bottom` (default header at the end of the list) or `search_bar` (lock/settings next to search; bottom header hidden).

## Settings action visibility

- The lock toggle is always shown when the section is shown.
- The settings action (Settings gear in the bottom header, Shield button in the search bar) is shown only when the space is unlocked **and** a settings/add entry exists in the private profile's launcher list. Rule: `PrivateSpaceUi.settingsActionVisible(isLocked, settingsAvailable)`.
- Both placements use the same rule. The entry is looked up in the unfiltered private app list, so typing a search query never hides the action.
- Tapping the action launches the settings/add entry (matched by the `PrivateSpaceUi.isAddEntryLabel` heuristic).

## State source

- The system's quiet mode (`UserManager.isQuietModeEnabled` on the private profile) is the only truth for locked/unlocked. The UI never assumes a lock or unlock request succeeded and never writes the lock state from a request result.
- One refresh re-queries quiet mode off the main thread and, when unlocked, reloads the private apps. A newer refresh cancels the older one so overlapping loads cannot interleave, and state is applied only after the last load finishes.
- Refresh runs on: drawer open, `ON_RESUME`, a finished lock/unlock request, and these protected broadcasts: `ACTION_PROFILE_AVAILABLE`, `UNAVAILABLE`, `ACCESSIBLE`, `INACCESSIBLE`, `ADDED`, `REMOVED`. Broadcasts are deferred while VOID is cached, so the `ON_RESUME` re-query is required, not optional.
- The receiver is registered only when API 35+, `FeatureAvailability.isPrivateSpaceAvailable` and the Settings toggle are all true, and it is unregistered inside `runCatching`, so it is never unregistered when it was not registered.
- Unlocking needs credentials. `requestQuietModeEnabled(false, ...)` returns false while the system shows the credential prompt (or the screen-lock setup when the device has none), so the lock icon stays "locked" until authentication succeeds and stays that way if the user cancels. `PrivateSpaceUi.outcomeFor(requestedQuiet, requestReturned)` maps the request to `ToggleOutcome`: `LOCKED`, `UNLOCKED`, `NEEDS_USER_ACTION` (no feedback needed, the system prompt is the feedback) or `FAILED`.
- `FAILED` shows a toast. The main cause is a `SecurityException`: only the foreground default launcher may change quiet mode, so VOID must be the default home app. Logs carry event names only (no package names or profile identifiers).
- Right after an unlock the profile may still be starting, so the private app list can be empty until `ACTION_PROFILE_ACCESSIBLE` triggers the next refresh.

## Failure modes

- Credential prompt cancelled or screen lock missing → the space stays locked and the UI keeps showing locked.
- VOID is not the default home app → the lock/unlock request is refused (toast) and the UI re-reads the real state.
- Missing OS Private Space → settings subtitle explains not configured; feature degrades.
- API/OEM failures → empty private app list; no crash.
- See `PrivateSpaceHelper` for guarded entry points.

## Manual verification (emulator only)

Pixel_8a AVD with a secure screen lock (PIN), VOID set as the home app, and a private space created in system Settings. Never run these against the physical device.

1. Cold-open the drawer while locked: lock toggle only, no settings icon, in both placements.
2. Tap unlock: the credential prompt appears and the icon stays "locked"; cancel leaves it locked; authenticating flips it to unlocked and the private apps load.
3. Tap lock: icon flips to locked and the private rows disappear.
4. Lock or unlock from system UI while the drawer is in the background, then return: state matches the system without reopening the drawer.
5. Set another launcher as home and tap the lock toggle: the "Couldn't change Private Space" toast shows and the UI keeps the real state.
6. Turn the Private Space setting off in VOID Settings and reopen the drawer: no section, no receiver crash on leaving the drawer.
