# Motion

How Home and its panels (drawer, notification list, summary, widgets, notes, Settings) move. The code is in `ui/theme`: `Motion.kt` (spring tokens), `NavTravel.kt` (pure travel geometry), `NavMotion.kt` (the `NavHost` transitions and `NavPanel`).

## What the user sees

A panel moves the way you swipe. Swipe so the finger moves right and the panel arrives from the left edge; move it left and the panel arrives from the right edge; swipe up and the drawer rises from the bottom; swipe down and the notification list drops from the top. **Home moves with the panel**, a shorter way (parallax) and dimmed, so the two layers travel together instead of crossing. Going back plays the same motion in reverse: the panel leaves toward the edge it came from and Home returns.

This is the reading of the request "the next screen follows the gesture direction and Home must not glitch": it is symmetric for both edges and the panel never moves against the finger. Settings has no edge and uses a Material shared-axis Z transition.

### Why Home glitched

`NavMotion.exit` for the horizontal panels had the wrong sign: the panel slid in from one side while Home slid out toward that same side, so the layers crossed. Nothing hid the overlap, because no screen paints an opaque background (only the black window sits underneath), and every transition was a plain 150/250/400 ms tween. `NavMotionTest` only checked axis names, never directions, so it stayed green.

## Travel

`PanelEdge` is the **physical** screen edge a panel enters from (`Left`, `Right`, `Top`, `Bottom`, `None`). It is never mirrored for right-to-left layouts: the finger does not mirror either. `PanelOrigin.resolve(route, leftRoute, rightRoute)` decides the edge:

| Destination | Edge | Opens with | Closes with |
|---|---|---|---|
| App drawer | `Bottom` | swipe up | swipe down |
| Notification list | `Top` | swipe down | swipe up |
| Panel assigned to the left swipe action | `Left` | finger moves right | finger moves left |
| Panel assigned to the right swipe action | `Right` | finger moves left | finger moves right |
| Settings | `None` | the settings icon in the drawer header | swipe down |
| Home, unassigned panels | `None` | | |

A panel assigned to both swipe actions takes the left edge (the left action wins). The notification list always enters from the top, even when a horizontal swipe opens it.

Two panels close differently from the table:

- Notes (`conflictSafe`) assigned to the left swipe closes with a swipe **up**, not left, because a horizontal swipe on a note row deletes it. The panel still leaves toward the left edge. Notes on the right swipe closes with a swipe right.
- Settings is reachable only from the drawer today (`HomeScreen.onOpenSettings` is declared and wired but never called), so the screen under Settings is always the drawer, never Home.

`NavTravel.path(edge, layer, direction, size)` gives the start and end offset of a layer (`Panel` or `Covered`) in pixels, and `NavMotion` passes exactly those values to `slideIn` and `slideOut` through `enterStart` (panel, push, start), `exitEnd` (covered, push, end), `popEnterStart` (covered, pop, start) and `popExitEnd` (panel, pop, end):

| | Panel (the screen opening or closing) | Covered screen |
|---|---|---|
| Push | slides from its edge (`Left` starts at `-width`) to rest, **no fade** | moves the same way as the panel by 30% of the width (`Left`/`Right`) or 20% of the height (`Top`/`Bottom`), fading to 68% opacity (1 minus the Material 3 scrim of 0.32) |
| Pop | the push path reversed | the push path reversed |

Because the covered screen moves the way the panel moves, the sign of its travel equals the panel's on every edge; `NavTravelTest` asserts this for each edge and for pops.

### Settings and other panels without an edge (shared axis Z)

`PanelEdge.None` panels do not translate. Push: the panel scales from 0.8 to 1 and fades in; the screen under it scales from 1 to 1.1 and fades out. Pop is the reverse (the panel scales 1 to 0.8 and fades out, the screen under it returns from 1.1 to 1). The Settings font picker overlay uses only the panel half of the pair (`enter` and `popExit` with `PanelEdge.None`): it scales from 0.8 to 1 and fades over a Settings screen that stays put, because the Settings content under it has no covered-screen transition of its own. It is an in-place overlay, not a full shared-axis Z.

### Opaque panels

`NavPanel(edge) { ... }` wraps every destination except Home in a full-size black surface and provides `LocalPanelEdge` for that destination. Without it the screen under a sliding panel shows through, because no screen paints a background. Home stays transparent over the black window. `LocalPanelEdge` is per destination, so it does not flip while a panel is closing (the old global value was derived from the current back-stack entry).

### NavHost wiring

All four transitions are keyed on the **panel**. In `NavHost` 2.10.0 the transition of the screen opening comes from `targetState`, the one of the screen leaving from `initialState`; on a pop it is the other way round:

| Lambda | Plays on | Edge of |
|---|---|---|
| `enterTransition` | panel opening | `targetState` |
| `exitTransition` | screen under it | `targetState` |
| `popEnterTransition` | screen under it, returning | `initialState` |
| `popExitTransition` | panel closing | `initialState` |

Routes are matched with `NavDestination.hasRoute<T>()` against the type-safe `@Serializable` routes, not by route-name strings. Opening Settings from the drawer uses `launchSingleTop`.

## Spring tokens

`VoidMotion` holds the Material 3 **standard** spring tokens (dampingRatio, stiffness) from material3 1.4.0 `StandardMotionTokens`. `MotionScheme` is internal in every released material3 (1.3.2 on our classpath has none), so the numbers are copied and `VoidMotionScheme` mirrors the `MotionScheme` function names (`fastSpatialSpec`, `defaultSpatialSpec`, `slowSpatialSpec`, `fastEffectsSpec`, `defaultEffectsSpec`, `slowEffectsSpec`) so adopting it later is mechanical.

| Token | Damping | Stiffness | Used for |
|---|---|---|---|
| spatial fast | 0.9 | 1400 | small components |
| spatial default | 0.9 | 700 | partial-screen movement |
| spatial slow | 0.9 | 300 | full-screen panels (slide, scale) |
| effects fast | 1.0 | 3800 | covered-screen fade next to a shared-axis panel |
| effects default | 1.0 | 1600 | shared-axis panel fade |
| effects slow | 1.0 | 800 | covered-screen dim next to a sliding panel |

Standard, not Expressive: damping 0.9 overshoots by about 0.15% (under 2 px on a 1080 px screen), so a full-screen panel never exposes the black window edge. Expressive springs overshoot visibly and are not used for anything full-screen.

Springs on `IntOffset` or `IntSize` carry `IntOffset.VisibilityThreshold` / `IntSize.VisibilityThreshold`, so they stop at one pixel instead of running on. Springs on a scale carry `VoidMotion.scaleVisibilityThreshold` (0.002, via `slowScaleSpec()`): Compose's default of 0.01 for a `Float` ends the spring with up to 1% of scale left and snaps the screen by about 5 px at each edge of a 1080 px display. Fades keep the default, because a 1% alpha step is invisible.

### Animation speed

The `Animation speed` setting maps to a duration factor `f`: fast 0.6, standard 1.0, slow 1.6 (the old 150, 250 and 400 ms ratios). Every VOID spring uses `stiffness / f^2` with the damping unchanged (`VoidMotion.stiffnessFor`), because a spring's settle time scales with `1 / sqrt(stiffness)`. An unknown value plays at standard speed. Compose multiplies the system animator scale on top, so Android's "Remove animations" still snaps every transition. Material 3 built-ins (switches, sheets, dialogs) ignore the setting.

At standard speed a panel settles to one pixel in about 0.45 s, and the covered screen stays composed until then.

## Limits

- Forward swipes are not finger-tracked: `NavHost` cannot seek a forward transition, so a panel starts when the swipe is recognised and then springs in. A finger-tracked open needs Home permanently composed under an overlay panel host; that is a separate change.
- System back still runs through each screen's own back handler (`ChildScreenBackHandler`), so the plain pop (the push reversed) plays, not the predictive-back preview.
- Reversing a push in flight (open the drawer, then dismiss it at once) plays the screen under the panel back on Compose's default springs, not on the `popEnter` spring: `EnterExitTransition` applies an enter spec only to the PreEnter to Visible segment and an exit spec only to Visible to PostExit, and the reversal is PostExit to Visible, which uses the defaults. There is no public API to change that segment. The panel leaves on its own `popExit` spring, so Home settles a little before the panel has gone.
- A dismiss that arrives while the panel is already closing is ignored (`MainActivity.popPanel` pops only while a screen is left under the panel). The closing panel stays on screen and touchable until its exit finishes, about 0.45 s at standard speed and about 0.8 s at slow speed, and popping again would remove Home and leave the `NavHost` blank.
- Two swipe-opened destinations do not follow the swipe direction yet. The notification list opened by a horizontal swipe (the `NOTIFICATIONS` swipe action) still drops from the top, because the edge is chosen from the destination alone and a swipe down or the status-bar pull open the same destination. A swipe action that launches an external app (`APP`) has no directional motion at all (`HomeScreen.dispatchSwipeAction` calls a plain `startActivity`). Both need the trigger direction, for example a one-shot edge hint set by Home's open callbacks and read by `edgeOf`, which belongs with the Home gesture changes. When `APP` gets directional motion, derive the slide pair from the physical finger direction (finger moves right, content enters from the left); `MainViewModel.launchApp`'s `swipeDirection` strings are the inverse of that and must not be wired straight through.
- Gesture thresholds, edge-start swipes and haptics are separate work.

## Tests

`NavTravelTest` (direction of every layer for every edge, the four offsets `NavMotion` hands to `slideIn` and `slideOut` (`enterStart`, `exitEnd`, `popEnterStart`, `popExitEnd`), a pop is the push played backwards, parallax, `None` has no translation), `NavMotionTest` (`PanelOrigin.resolve` table including a panel on both sides and an unknown route, the parts and the spring of every transition for every edge and speed), `VoidMotionTest` (token values, speed scaling, thresholds), `ScreenBackSwipeTest` (close direction per edge).

Not covered by a JVM test, because the project has no Robolectric or navigation-testing: the `hasRoute` mapping in `MainActivity.routeKind()`, the choice of `targetState` or `initialState` in the four `NavHost` lambdas, the `NavPanel` provision of `LocalPanelEdge`, and `popPanel`. They are exercised by `docs/testing/manual-checklist.md` on a device; an instrumented test of the real graph needs `navigation-testing` and the lambdas extracted into one function, and is a follow-up.
