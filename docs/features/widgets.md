# Widgets

## Hosting model

Widgets are hosted on a **dedicated Widgets screen** using native `AppWidgetHost` + `AppWidgetHostView` embedded with Compose `AndroidView`.

This is live system widget rendering — not a decorative overlay.

## Customization

- Pin / unpin providers
- Launch configure activity when the provider declares one (persist only after success)
- Resize height in edit mode (persisted in `Prefs.widgetHeights`)
- Reorder pinned widgets (`Prefs.widgetOrder`)
- Toggle labels (`Prefs.showWidgetLabels`)
- Bind-denied path shows guidance toast

## Out of scope

Home-screen grid widget placement is intentionally not part of this epic.
