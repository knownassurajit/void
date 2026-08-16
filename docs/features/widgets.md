# Widgets

## Hosting model

Widgets are hosted on a **dedicated Widgets screen** using native `AppWidgetHost` + `AppWidgetHostView` embedded with Compose `AndroidView`.

This is live system widget rendering — not a decorative overlay.

## Customization

- Pin / unpin providers
- Launch configure activity when the provider declares one (persist only after success)
- **Long-press** a pinned widget to select it (host long-click is consumed so the widget app does not open). Drag anywhere on the overlay to snap width/height to the 4-column grid (`Prefs.widgetSpans`). Tap the overlay (no drag) to deselect.
- Remove a selected widget with the close control on the top-right
- Picker marks already-pinned providers with a check
- Default span uses the **on-screen cell size**, not a fixed 70dp cell
- Reorder pinned widgets (`Prefs.widgetOrder`)
- Picker groups **previews by app** in a horizontal row (native preview drawable, not text-only)
- Hosted widgets use `updateAppWidgetOptions` / `updateAppWidgetSize` so RemoteViews get the grid cell bounds
- Bind-denied only when the system grant UI cannot be launched.

There is no W/H stepper chrome. Resize follows available screen width so cells stay even.

## Out of scope

Home-screen grid widget placement is intentionally not part of this epic.
