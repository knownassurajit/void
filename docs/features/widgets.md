# Widgets

## Hosting model

Widgets are hosted on a **dedicated Widgets screen** using native `AppWidgetHost` + `AppWidgetHostView` embedded with Compose `AndroidView`.

This is live system widget rendering — not a decorative overlay.

Android keeps a single listener registration per `AppWidgetHost` id (`1024`), while each Widgets screen builds its own host and the closing screen stays composed until its exit animation ends. Listening is therefore reference counted (`ListeningLease`): every Widgets screen starts it, and only the last one to leave stops it, so reopening Widgets while it is still closing never silences the new screen. See `docs/architecture/motion.md`.

## The header is hidden until you pull

The Widgets screen opens as a plain, full-screen grid: there is no top bar. The title, the **Add widget** button and a one-line reminder of how to customize a widget sit in a header that floats over the grid and stays out of sight until it is asked for:

- **Pull down** anywhere on the grid (the finger moving down) and the header follows the finger; **tap or drag the short bar** under the status bar and it opens as well. Moving the finger up pushes it away again, the way a Material "enter always" app bar behaves, but it never takes scroll from the list: it floats over it, so the grid answers the finger at once and no widget moves when the header appears.
- Releasing settles it on a spring: open when at least half of it is out, closed otherwise, and a fling of 400 dp/s or more decides on its own. The spring is `MaterialTheme.motionScheme.defaultSpatialSpec()` (Material 3 Expressive) and follows the animation-speed setting. While the header is still coming out, the part of a pull the list could not use is taken by the header, so the list does not stretch as well; once it is fully out the rest of the pull stretches the list as usual.
- A tap beside the widgets, customizing a widget and opening the picker put the header away. With **no widgets** it stays out (there is nothing to give the room to, and Add widget has to be reachable) and it comes back when the picker is closed without adding anything.
- For TalkBack the bar is a button, "Show widget options"; the hidden header is not focusable. The pure rules (clamp, fraction, settle target) are `HeaderRevealMath`; `RevealHeaderState`, `RevealHeaderConnection` and `RevealHandle` are in `ui/components/RevealHeader.kt`.

## Customizing a widget: tap and hold

1. **Tap and hold** a widget (a still finger for the long-press time). It gives a haptic tick and the screen goes into customize mode: the widget rises a little and gets a primary outline, the other widgets dim, the grid cells are outlined, and the **edit bar** slides up from the bottom edge. The bar names the widget ("Customize Weather"), says in one line what to do ("Drag the widget to move it · drag a handle to resize") and has plain, labelled buttons: **Settings** (only when the provider has a configure activity; persisted only after it succeeds), **Remove** (asks to confirm) and **Done**. A widget under the bar is scrolled clear of it.
2. **Move**: drag the widget, in the same touch as the long press or in a new one. A snapshot of the real widget follows the finger, the cell it will land on (the free one nearest to it) is highlighted, the grid scrolls when the finger is near its top or bottom edge, and the edit bar steps aside while the widget is held.
3. **Resize**: drag a handle. A widget that can grow both ways has a handle on its bottom-right corner and, once it is at least two cells tall or wide, one in the middle of its right and of its bottom edge; a widget that grows one way has the handle on that edge only (`AppWidgetProviderInfo.resizeMode`). The span snaps to whole cells and shrinks to the largest size that does not cover another widget.
4. **Done**, a tap beside the widgets, or Back leaves customize mode (Back no longer leaves the screen while a widget is picked).

TalkBack and switch users get the same without gestures: each widget has a **Customize** action; a picked widget adds **Make wider / narrower / taller / shorter** (only the directions the provider allows) and **Move left / right / up / down** (one cell, never onto another widget: `WidgetGrid.moved`), and the edit bar's buttons are ordinary buttons. The handle geometry is `resizeHandlesFor` and `isOnResizeHandle` (`WidgetEditChrome.kt`, unit tested), shared by the handles and by the widget's own long-press, which leaves a touch that starts on a handle alone.

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

## Performance and interaction

Opening Widgets used to stall the slide-in, because three things landed on the main thread while the panel moved. They are now kept out of it:

- **Provider list**: the pinned widgets are read by widget id (`AppWidgetManager.getAppWidgetInfo`, a few milliseconds), so the first widget no longer waits for every provider on the device and its label to load. That heavy list (`WidgetsViewModel.loadProviders`) loads in the background once the panel has arrived, or when the picker opens, and only once.
- **Widget views**: `VoidAppWidgetHost` makes the host views apply the provider's `RemoteViews` on a background executor on Android 12 and later (`AppWidgetHostView.setExecutor`, as the platform launcher does), instead of inflating them on the main thread; Android 11 and earlier still inflate on the main thread. The views are also only created after the slide-in has ended (`LocalPanelSettled`), one per frame (`rememberStagedKeys`), with a tile of the widget's size in its place until the provider has drawn.
- **Header**: composed after the panel has arrived (nobody pulls it during the slide-in).

Also:

- A widget's size options are sent to its provider only when its size changes (an `AppWidgetHostView` re-renders on every options update and drops touches meanwhile).
- Screen positions used while dragging are plain fields, not Compose state, so scrolling and dragging never recompose the widget host views.
- A long press lifts a widget only if the finger stays within the touch slop; a moving finger scrolls the grid or the widget.
- Provider previews are decoded on demand per picker card and cached (`WidgetsViewModel.previewFor`).
- The widget glide, the picker and the edit bar use `MaterialTheme.motionScheme`, which follows the animation-speed setting (`SpeedScaledMotionScheme`).
