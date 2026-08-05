# Private Space

## Behavior

- Requires Android 15+ (API 35 / `VANILLA_ICE_CREAM`).
- Detects the private profile via `LauncherApps.getLauncherUserInfo` + `USER_TYPE_PROFILE_PRIVATE`.
- Quiet mode lock/unlock uses `UserManager.requestQuietModeEnabled`.
- Drawer shows a Private Space section when enabled in Settings and a profile exists.

## Failure modes

- Missing OS Private Space → settings subtitle explains not configured; feature degrades.
- API/OEM failures → empty private app list; no crash.
- See `PrivateSpaceHelper` for guarded entry points.
