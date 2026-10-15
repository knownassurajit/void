# Home apps cap collapse

## Symptom

Pinned home apps dropped from 10 (or any count above 4) down to 4 after toggling clock/date or otherwise emitting homescreen preferences.

## Root cause

Two defaults for the same SharedPreferences key:

- `maxHomeApps` getter used `getInt(MAX_HOME_APPS, 10)`
- `readHomescreenPreferences().maxApps` used `getInt(MAX_HOME_APPS, getInt(HOME_APPS_NUM, 4))`

When both keys were missing (common on devices that never wrote the cap), a clock/date emit reloaded the list with `maxApps = 4`. Densifying the truncated picker list could then wipe slots 5–10.

## Fix

`HomeAppsCap.resolve` is the only resolver: explicit `MAX_HOME_APPS`, else `max(legacy, filled slots)`, else `max(filled, 10)`. Prefs init persists the resolved value to both keys. Homescreen preference collection reloads apps only when the cap changes.
