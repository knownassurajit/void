# Widgets

## Hosting model

Widgets are hosted on a **dedicated Widgets screen** using native `AppWidgetHost` + `AppWidgetHostView` embedded with Compose `AndroidView`.

This is live system widget rendering — not a decorative overlay.

The launcher has one host (`WidgetHostHolder`, id `1024`) that lives as long as the activity and keeps the views it built, the way a home screen keeps its widgets. It listens for updates while the launcher is visible and widgets are pinned, so the kept views stay current. See [Speed](#speed).

## The header is hidden until you open it

The Widgets screen opens as a plain, full-screen grid: there is no top bar. The title, the **Add widget** button and a one-line reminder of how to customize a widget sit in a header that floats over the grid:

- **Open it from the strip at the top**: drag down on the strip under the status bar (a short bar marks it) and the header follows the finger, or tap the strip and it springs open. The strip is the full width of the screen and the grid starts below it, so it never shares a touch with a widget and works the same way every time.
- **A pull on the widgets does not open it.** That was the first design, and on a device it was inconsistent: a widget that handles its own touches (a button, a list, a search bar) keeps the drag from ever reaching the grid, so the header came only when the pull started on an empty spot.
- Releasing settles it on the Material 3 Expressive spatial spring (`MaterialTheme.motionScheme`, which follows the animation-speed setting): open when at least half of it is out, shut otherwise; a fling of 400 dp/s or more decides on its own.
- **Closing**: drag the header up like a sheet, scroll the widgets (the header follows the finger away), tap beside the widgets, or press Back. Customizing a widget and opening the picker put it away too.
- With **no widgets** it stays out (Add widget must be reachable) and comes back when the picker is closed without adding anything.
- For TalkBack the strip is a button, "Show widget options"; the hidden header is not focusable. `HeaderRevealMath`, `RevealHeaderState`, `RevealHeaderConnection` (scroll only hides), `revealHeaderDrag` and `RevealPullZone` are in `ui/components/RevealHeader.kt`.

## Customizing a widget: tap and hold

1. **Tap and hold** a widget (a still finger for the long-press time). It gives a haptic tick and the screen goes into customize mode: the widget rises a little and gets a primary outline, the other widgets dim, the grid cells are outlined, and the **edit bar** slides up from the bottom edge. Nothing is drawn on the widget itself. The bar names the widget ("Customize Weather") and holds every action as a labelled control. A widget under the bar is scrolled clear of it.
2. **Move**: drag the widget, in the same touch as the long press or in a new one. A snapshot of the real widget follows the finger, the cell it will land on (the free one nearest to it) is highlighted, the grid scrolls when the finger is near its top or bottom edge, and the edit bar steps aside while the widget is held.
3. **Resize**: the bar's **Width** and **Height** rows show the size in cells with a − and a + button. A step is one cell; a button is disabled when the widget's app does not allow that direction (`AppWidgetProviderInfo.resizeMode`), at one cell, at the edge of the grid, or when the cells it would take hold another widget (`WidgetGrid.resizeSteps`). A widget that cannot be resized says so instead. The first design used handles on the widget's edges and a dot in its corner; on a device the dot was not understood as a handle, and a handle on the widget competes with the widget's own content for the finger.
4. **Settings** (only when the widget has a configure screen; kept only after it succeeds), **Remove** (asks to confirm) and **Done** are buttons in the bar. Done, a tap beside the widgets, or Back leaves customize mode (Back does not leave the screen while a widget is picked). While a widget is picked, a tap on another one picks that one instead of opening its app.

TalkBack and switch users get the same without gestures: each widget has a **Customize** action; a picked widget adds **Make wider / narrower / taller / shorter** (the same steps as the bar's buttons) and **Move left / right / up / down** (one cell, never onto another widget: `WidgetGrid.moved`); the bar reads each size as "Width, 2 cells".

Other rules:

- Pin / unpin providers; the picker marks already-pinned providers with a check and groups previews by app in a horizontal row (native preview drawable, not text-only).
- Default span uses the **on-screen cell size**, not a fixed 70dp cell.
- Reorder pinned widgets (`Prefs.widgetOrder`).
- Hosted widgets use `updateAppWidgetOptions` / `updateAppWidgetSize` so RemoteViews get the grid cell bounds.
- Bind-denied only when the system grant UI cannot be launched.

## Out of scope

Home-screen grid widget placement is intentionally not part of this epic.

## Grid placement

- The grid has 4 square columns; each pinned widget has a cell (`WIDGET_POSITIONS`, `provider|column|row`) and a span (`WIDGET_SPANS`).
- Add: tap a widget in the picker (first free cell) or hold it and drag it onto the grid. While held, the picker hides, the cells are outlined and the landing cell (the free one nearest the finger) is highlighted.
- Layout rules live in `helper/WidgetGrid.kt` (pure, unit tested). Layouts saved before cells existed are placed in their saved order.

## Speed

Widgets show on the first frame of the screen, already drawn:

- **Kept views**: a widget view is built once and kept while the launcher's activity lives (`WidgetHostHolder`). Each later visit puts the same view back (`obtainView`) instead of asking every provider for its content again, and its size options are sent only when the size changed (`applySize`). The host listens while the launcher is visible, so kept views stay current.
- **Built ahead of time**: 1.5 s after the launcher starts, the pinned widgets' views are built while the main thread is idle, one per idle moment, at the size the grid gives them (`WidgetHostHolder.prewarm`), so even the first visit has nothing to build.
- **Known layout**: the pinned widgets as last read are kept too, so the screen lays the grid out on its first frame and checks with the system in the background (by widget id, `PinnedWidgetQuery`). The all-provider list for the picker loads in the background after the slide-in or when the picker opens, once.
- **A view that has to be built** (a widget just pinned): on Android 12 and later `VoidAppWidgetHost` inflates its `RemoteViews` on a background executor, and it is built at once, one per frame, fading in over a tile of its size. Android 11 and earlier inflate on the main thread, so those wait for the slide-in.
- The kept views are let go when the activity is destroyed, because they hold it.
- Compose's `AndroidView` leaves a released view inside its old holder, so the widget item takes it out in `onRelease`; a kept view is only reused when it has no parent (a Widgets screen that is still closing keeps its own, and the new screen builds a fresh one).

Also:

- Screen positions used while dragging are plain fields, not Compose state, so scrolling and dragging never recompose the widget host views.
- A long press lifts a widget only if the finger stays within the touch slop; a moving finger scrolls the grid or the widget.
- Provider previews are decoded on demand per picker card and cached (`WidgetsViewModel.previewFor`).
- The widget glide, the picker and the edit bar use `MaterialTheme.motionScheme`, which follows the animation-speed setting (`SpeedScaledMotionScheme`).
