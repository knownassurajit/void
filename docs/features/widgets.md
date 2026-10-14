# Widgets

## Hosting model

Widgets are hosted on a **dedicated Widgets screen** using native `AppWidgetHost` + `AppWidgetHostView` embedded with Compose `AndroidView`.

This is live system widget rendering — not a decorative overlay.

Android keeps a single listener registration per `AppWidgetHost` id (`1024`), while each Widgets screen builds its own host and the closing screen stays composed until its exit animation ends. Listening is therefore reference counted (`ListeningLease`): every Widgets screen starts it, and only the last one to leave stops it, so reopening Widgets while it is still closing never silences the new screen. See `docs/architecture/motion.md`.

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

## Grid placement

- The grid has 4 square columns; each pinned widget has a cell (`WIDGET_POSITIONS`, `provider|column|row`) and a span (`WIDGET_SPANS`).
- Add: tap a widget in the picker (first free cell) or hold it and drag it onto the grid. While held, the picker hides, the cells are outlined and the landing cell (the free one nearest the finger) is highlighted.
- Move: hold a pinned widget and drag it. Resize: long-press to select, then drag the corner handle; the span snaps to whole cells, follows the provider's `resizeMode` and shrinks to the largest size that does not cover another widget.
- Layout rules live in `helper/WidgetGrid.kt` (pure, unit tested). Layouts saved before cells existed are placed in their saved order.

## Performance and interaction

- A widget's size options are sent to its provider only when its size changes (an `AppWidgetHostView` re-renders on every options update and drops touches meanwhile).
- Screen positions used while dragging are plain fields, not Compose state, so scrolling and dragging never recompose the widget host views.
- A long press lifts a widget only if the finger stays within the touch slop; a moving finger scrolls the grid or the widget.
- Provider previews are decoded on demand per picker card and cached (`WidgetsViewModel.previewFor`); the pinned widgets show as soon as the provider list is read.
