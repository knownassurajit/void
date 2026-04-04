## 2024-04-04 - Accessible Screen Time Shortcut
**Learning:** For utility functions like screen time that don't have built-in APIs on all OEM versions, using a fallback intent logic (`Settings.ACTION_USAGE_ACCESS_SETTINGS`) combined with `onClickLabel` improves both functionality and semantics.
**Action:** Always add semantic content descriptions or onClickLabels for touch targets that perform system intents in Compose.
