# Motion

How Home and its panels (drawer, notification list, summary, widgets, notes, Settings) move. The code is in `ui/theme`: `Motion.kt` (spring tokens), `NavTravel.kt` (pure travel geometry), `NavMotion.kt` (the `NavHost` transitions and `NavPanel`), `PredictiveBackMath.kt` (the system back animation); and in `ui/gesture` and `ui/components`: `SwipeMath.kt` (pure swipe arithmetic), `HomeSwipe.kt`, `HomeSwipeDecision.kt`, `HomeSwipeGate.kt` and `HomeSwipeDeparture.kt` (the swipes on Home: pointer loop, state, and pure decisions), `LaunchAnimation.kt` (the window animation of an app a swipe launches) and `ScreenBackSwipe.kt` (the in-content back swipe).

## What the user sees

A panel moves the way you swipe. Swipe so the finger moves right and the panel arrives from the left edge; move it left and the panel arrives from the right edge; swipe up and the drawer rises from the bottom; swipe down and the notification list drops from the top. **Home moves with the panel**, a shorter way (parallax) and dimmed, so the two layers travel together instead of crossing. Going back plays the same motion in reverse: the panel leaves toward the edge it came from and Home returns. Home also follows the finger while the swipe is in progress (see [Home swipes](#home-swipes)), and an app opened by a swipe slides in from the same side on Android 11 and earlier; on Android 12 and later the system owns that animation, so Home itself carries on in the swipe direction while the app opens (see [Launching an app by swipe](#launching-an-app-by-swipe)).

This is the reading of the request "the next screen follows the gesture direction and Home must not glitch": it is symmetric for both edges and the panel never moves against the finger. Settings has no edge and uses a Material shared-axis Z transition.

### Why Home glitched

`NavMotion.exit` for the horizontal panels had the wrong sign: the panel slid in from one side while Home slid out toward that same side, so the layers crossed. Nothing hid the overlap, because no screen paints an opaque background (only the black window sits underneath), and every transition was a plain 150/250/400 ms tween. `NavMotionTest` only checked axis names, never directions, so it stayed green.

## Travel

`PanelEdge` is the **physical** screen edge a panel enters from (`Left`, `Right`, `Top`, `Bottom`, `None`). It is never mirrored for right-to-left layouts: the finger does not mirror either. `PanelOrigin.resolve(route, leftRoute, rightRoute)` decides the edge:

| Destination | Edge | Opens with | Closes with |
|---|---|---|---|
| App drawer | `Bottom` | swipe up | swipe down |
| Notification list | `Top` | swipe down | swipe up |
| Panel assigned to **Swipe left** | `Right` | finger moves left | finger moves right |
| Panel assigned to **Swipe right** | `Left` | finger moves right | finger moves left |
| Settings | `None` | the settings icon in the drawer header | swipe down |
| Home, unassigned panels | `None` | | |

Swipe sides are named after the finger, as on every Android surface: the content follows the finger in, and the screen closes by swiping back toward the edge it came from. An app opened by a swipe (window animation on Android 11 and earlier, Home's departure on 12 and later) uses the same edge. Preferences saved under the old edge-based naming are migrated once (`SwipeSideMigration`), so every gesture keeps its action. A panel assigned to both swipes takes the right edge (Swipe left wins). The notification list always enters from the top, even when a horizontal swipe opens it.

Settings is reachable only from the drawer today (`HomeScreen.onOpenSettings` is declared and wired but never called), so the screen under Settings is always the drawer, never Home. Every other panel closes as the table says; Notes has no exception: it closes like any panel from its edge. What changed is the note row. A row is a swipe-to-dismiss box that deletes the note for good and consumes the drag first, so a swipe that closes Notes with a finger over a note would delete it. The delete swipe therefore goes the other way from the close swipe (`noteDeleteFromStartToEnd` in `NotesScreen.kt`): Notes on the left swipe closes with a swipe left, so there a note is deleted with a swipe right; on the right swipe it closes with a swipe right and a note is deleted with a swipe left. Start and end flip in right-to-left layouts, the close swipe does not, and the function takes that into account.

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

## Going back

There are several ways to leave a panel, and they take different paths:

| Way | Plays | Owned by |
|---|---|---|
| System back gesture (swipe from a screen edge, Android 14+) | the **predictive back** animation, following the finger | `NavHost` (`predictivePopEnterTransition`, `predictivePopExitTransition`) |
| Back key or 3-button navigation, Android 14+ | the **predictive back** animation too, not following anything: see below | `NavHost` |
| Back key or 3-button navigation, API 26-33 | the plain pop: the push played backwards (`popEnter`, `popExit`) | `NavHost` |
| The back arrow of a screen, a swipe inside the panel, TalkBack's Dismiss action | the plain pop, started at once (the swipe: once it is recognised) | `popPanel`, `screenBackSwipe`, `dismissAction` in `ScreenBackSwipe.kt` |
| TalkBack's back action | the same as the Back key when it injects a key press that lasts longer than a frame; the plain pop when the key down and up arrive in the same frame | `NavHost` |

**The Back key is not a plain pop on Android 14 and later.** The platform treats a Back key press like a gesture for an app that registered an `OnBackAnimationCallback` (the manifest sets `enableOnBackInvokedCallback`, and `navigationevent` 1.1.2 registers one on API 34+, `OnBackInvokedInput`): on key down it calls `onBackStarted` with an event that has no progress and the **left** edge (`ViewRootImpl.doOnBackKeyEvent`, in the Android 14 and 16 sources; Android 16 reports `EDGE_NONE` only where its `predictiveBackSwipeEdgeNone` flag is on), and on key up `onBackInvoked`. `NavHost` takes that for a predictive back, so a press that lasts longer than a frame (a hardware key, an on-screen Back button) plays the predictive pair, and the leaving screen shifts right by up to `W / 20 - 8 dp` whichever edge the panel came from. Only a key whose down and up arrive in one frame (`adb shell input keyevent KEYCODE_BACK`, often TalkBack's injected back) skips the preview and plays the plain pop. The `predictivePop*` lambdas receive only the swipe edge, and a key press carries no touch position and no progress, so they cannot tell a press from a swipe from the left edge. If the plain reverse for the Back key were a product requirement, it cannot be had with this `NavHost` configuration.

API 26-33 have no predictive preview at all (every way back plays the plain pop).

**No screen registers a back handler of its own to leave itself.** The old `ChildScreenBackHandler` was composed after `NavHost`, so it was asked first (back handlers are last in, first out) and swallowed every back before `NavHost`'s seekable predictive pop could see it: the edge gesture showed no preview at all. A screen may still register a handler for its own local UI that has to go first (the font picker in Settings does, and closes only the picker); Home keeps an unconditional one that does nothing, so a back on the root never leaves the launcher.

Leaving Settings by any path (its arrow, the Back key, TalkBack, a swipe, the system gesture) makes Home read the preferences again (`NavRouteKind.settingsClosed`, observed in `MainActivity`). That used to run only in Settings' own back callback, which the system gesture no longer goes through. It runs when the route changes, that is at the start of the pop, as the old callback did. `MainUiViewModel` also follows preference changes live (`Prefs.homescreenPreferences`: the font, the layout and the Private Space placement among them), so the screen shown under Settings, in the predictive preview too, is current; the refresh on leaving re-reads what that flow does not (the home app slots).

### Predictive back

The numbers are the Android predictive-back design guidance for going back to another screen of the same app, the same for every route (a panel without an edge included):

| | Leaving screen | Screen under it |
|---|---|---|
| Scale | 100% to 90% (`exitScale`) | 110% to 100% (`enterScale`) |
| Shift | sideways by at most `W / 20 - 8 dp`, the way the finger moves | none |
| Fade | gone after 35% of the gesture | starts at 35% of the gesture |
| Curve | cubic Bezier (0.1, 0.1, 0, 1) for scale and shift, linear for the fades | the same |

`PredictiveBackMath.shiftSign` gives the direction from the edge the swipe started at: left edge `+x` (right), right edge `-x` (left), no edge `0` (`shiftOffset` turns it into the sideways offset of the screen). A Back key press reports the left edge (see above), so it shifts right. The direction is the physical one and is not mirrored for right-to-left layouts. The 8 dp margin is `VoidDimens.predictiveBackEdgeMargin`.

`NavHost` seeks these transitions **by time**: the gesture progress becomes a fraction of the total duration, so every part is a tween (not a spring) of one shared duration, 300 ms times the animation-speed factor (180, 300 or 480 ms), and the two fades are split at 35% of it (105 ms out; 195 ms in after a 105 ms delay at standard speed). Committing the gesture continues from the current fraction and cancelling plays it back to the start; both are `NavHost`'s own and cannot be tuned from here.

### The swipe inside a panel

`screenBackSwipe` (on the root of every panel, gated by the "Swipe to go back" setting) closes the panel with a swipe toward the edge it came from. It follows the Material 3 `SwipeToDismissBox` conventions, in dp, and decides on release:

| Rule | Value | Source |
|---|---|---|
| Commit by distance | travelled 56 dp along the dismiss direction | `SwipeToDismissBoxDefaults.positionalThreshold` (material3 1.3.2) as `VoidDimens.swipeCommitDistance` |
| Commit by flick | released at 125 dp/s or faster along the dismiss direction, after at least the axis lock distance | `DismissVelocityThreshold` (material3 1.3.2) as `VoidDimens.swipeFlickVelocity` |
| Cancel by flick back | released at 125 dp/s or faster the other way: the panel stays, however far the finger went. A slower return is decided by the distance | `AnchoredDraggableState` settles on the anchor the velocity points to once it reaches the threshold, and only then looks at the distance |
| Axis lock | a drag within about 34 degrees of an axis (dominance 1.5) locks it at 1.5 touch slops (12 dp); a diagonal locks to its longer axis at 3 touch slops (24 dp); the axis never changes during the drag | `SwipeMath.lockAxis`, `AxisLock` |
| Drag measured from | the touch-down point, so the travel before the touch slop counts. It is the sum of what the finger moved in each event (position minus previous position, both read through the panel's transform of that moment), not a difference from the touch-down position: a panel that is still sliding in moves under a resting finger, and that motion must not count as a swipe | `BackSwipeRecognizer` in `ScreenBackSwipe.kt` |
| Not committed | anything shorter or slower, a drag on the other axis, a drag that a child consumed first, or a pointer the system cancelled | |

All distances are compared in dp (`px / density`), so a swipe needs the same finger travel on every display. Velocity comes from a `VelocityTracker` fed with the travel, not the position, so the panel's own motion is not in it. A flick back at 125 dp/s or faster cancels (see the table), and a negative travel never commits. Thresholds do not depend on the animation speed.

Settings has no edge and closes with a swipe **down**, as before. TalkBack's Dismiss action on the panel root does the same as the swipe (`dismissAction`); with the Settings font picker open, it closes the picker instead.

Because a child that consumes the drag first wins, a vertical swipe starts the dismiss only on regions that consume nothing (headers, the search bar, gaps): the lists of the drawer, the notification list and Settings scroll instead, and a note row slides to delete. The system back gesture, the Back key and TalkBack are the reliable ways out of those screens. A swipe that starts in the strip Android claims for its own back gesture is cancelled by the system, which then plays the predictive animation, so it does nothing here.

## Home swipes

`homeSwipe` (`ui/gesture/HomeSwipe.kt`) is the pointer loop on Home. Its decisions are pure and unit tested: `HomeSwipeRecognizer` (axis lock, direction, commit, haptic latch), `HomeSwipeSettings.targetFor` and `resolveSwipe` (what each direction opens), `GestureEdgeStrip` (the edge strips) in `HomeSwipeDecision.kt`; `homeSwipeGate` (may a swipe start) in `HomeSwipeGate.kt`; `homeAfterCommit`, `departureOffsetPx` and `HomeDeparture` (what Home does once the swipe is committed) in `HomeSwipeDeparture.kt`.

### Direction model

The next screen travels **with the finger**, symmetric for both edges, and Home follows the finger too:

| Finger moves | Content comes in from | Opens | Home follows |
|---|---|---|---|
| right | the left edge | the left swipe action (`leftSwipeAction`) | right |
| left | the right edge | the right swipe action (`rightSwipeAction`) | left |
| up | the bottom | the app drawer | up |
| down | the top | the notification list (`onOpenNotifications`) | down |

`resolveSwipe(action, edge, flags, appPackage)` turns an action into a `SwipeTarget` (`Panel(route, edge)`, `App(package, edge)`, `Notifications` or `None`) with the same gating as before: Notes, Widgets and Summary need their feature switches, `APP` needs a chosen app, the horizontal swipes need "Enable gestures", swipe down needs its own switch and swipe up is not gated. `SwipeAction.ACCESSIBILITY` and unknown values are `None`: the picker offers `ACCESSIBILITY`, but nothing runs it yet (the system notification shade work wires it). A thin dispatcher (`HomeScreen.dispatchSwipeTarget`) calls the open callbacks and says whether the action could run.

### Following the finger and committing

| Rule | Value |
|---|---|
| Pointer | one pointer, `VelocityTracker` fed with the travel from the touch-down point and clamped to the platform's maximum fling velocity |
| Axis lock | `SwipeMath.lockAxis` (1.5 touch slops, 3 for a diagonal), counting the travel before the slop; the direction (which edge) is chosen once, from the sign of the travel on the locked axis |
| Which drags move Home | only a drag toward a **configured** target. A finger that goes back past where it started moves nothing. A sideways swipe that opens the notification list does not move Home either (the list drops from the top), but it still gets the haptic cue and opens on release |
| Drags Home owns without moving | from the touch slop on, every drag is **consumed** until the finger lifts, even one toward a direction with no target (`HomeSwipeStep.Ignored`) or one that starts while Home is not swipeable (`enabled` false: a transition still moves it, or a panel is on top). The children then see neither a tap when the finger lifts (a home app row would launch its app) nor a long press when it waits (reorder, app picker). Only a drag that a child or the system took first (a consumed change, a long press) is left alone (`HomeSwipeStep.GiveUp`), and so is anything while a reorder drag runs. Under the touch slop nothing is consumed, so taps and long presses are untouched |
| What moves | Home's content (the `Column` inside the padded box, not the box that watches the pointer) is translated along the axis by `rubberBand` (limit 64 dp, `VoidDimens.swipePeekLimit`) in a `graphicsLayer` block reading state: no recomposition per frame. The travel up to the axis lock (12 dp for a straight drag) is not followed, so Home does not jump when the axis locks. The offset is the pull of the finger plus what is left of the spring of the swipe before (`HomeSwipeState`), so a new touch on the same axis takes that motion up where it is |
| Commit | decided **on release**: 56 dp of travel toward the target, or a flick of 125 dp/s after at least the lock distance (`SwipeMath.shouldCommit`, the Material 3 `SwipeToDismissBox` limits). Reversing the finger back under the distance, or a flick back at 125 dp/s, cancels |
| On commit | the open callback runs first, so the `NavHost` transition (or the app launch) starts at once, and then `homeAfterCommit` decides what Home's own offset does. **A panel or the notification list**: Home settles to rest on spatial slow (0.9/300), the spring `NavMotion.exit` moves Home on, so the offset and the transition add up to one smooth movement (on spatial fast the peek would pull Home back against the finger for a moment on a long drag). **An app on Android 12 and later**: Home does not spring back, it carries on in the swipe direction to where a panel parks the screen under it (30% of the width) and dims to 68% on spatial default (0.9/700), starting at the speed of the finger, and stays there until VOID is resumed (see [Launching an app by swipe](#launching-an-app-by-swipe)). **An app on Android 11 and earlier**: the system's launch animation moves the Home window, so Home settles quickly on spatial fast (0.9/1400) and is not moved twice. **An action that could not run** (an app that no longer launches): the reject haptic and a spring back on spatial default. The offset is inside the screen that `NavHost` slides, so the two add up and nothing jumps (`afterCommitSpec`) |
| On cancel | Home springs back to rest with spatial default (0.9/700) starting at the speed of the finger (slowed by the rubber band's slope) |
| Long press | a finger that has not moved a touch slop by the long-press timeout is a long press and belongs to the children (reorder, app picker); so does anything while a reorder drag runs. The timeout ends at the first slop crossing, not at the axis lock |
| Not while moving | no swipe starts unless the gate is open (`HomeSwipeGate`, see [Keeping the swipe reliable](#keeping-the-swipe-reliable)): Home must be the top entry of the back stack and resumed, which the navigation controller only allows once no transition involves it any more and while the activity is resumed; nor while Home has departed for an app. Such a drag is swallowed (see above) and logged once as `swipe_blocked` with its reason. A swipe that is following the finger when the gate closes stops and Home springs back. The pointer loop is keyed on its state object only and reads everything else through the latest values, so a screen change in the middle of a swipe never restarts it |

Springs use the animation-speed preference like every other VOID spring, and Android's "Remove animations" snaps them.

Haptics, with Compose's `HapticFeedbackType` (API 34+ constants, an automatic fallback below): `GestureThresholdActivate` once when the travel reaches 56 dp (it re-arms only after the travel drops under 80% of that), `GestureEnd` when the action ran, `Reject` when it could not (an app that no longer launches). A cancel is silent.

### Keeping the swipe reliable

A swipe on Home is gated, and Home is put back in its place, by what the navigation controller says, not by the internals of the `NavHost` animation.

- **The gate** (`HomeSwipeGate`, `homeSwipeGate(isOnTop, isResumed)`): `Open` when the Home entry is the top entry of the back stack (`currentBackStackEntry`) and its lifecycle is `RESUMED`; `Busy` when Home is on top but not resumed (the controller keeps the top entry `STARTED` while a transition involves it, for example a panel that is still leaving, and while the activity is paused); `NotOnTop` when another screen is on top (and then Home does not take the back press). It replaces the old pair of `isCurrentDestination` (a value captured from the outer composition by the `NavGraph` builder lambda) and `isAtRest` (the `currentState` and `targetState` of the `AnimatedContent` child transition of the Home destination). The two facts of the new gate are read in `composable<HomeRoute>`, inside the destination, from `currentBackStackEntryFlow` and the entry's `lifecycle.currentStateAsState()`, and the pointer loop of `homeSwipe` takes the latest value.
- **Reset** (`HomeSwipeRestore`, `HomeSwipeState.reset()`): each time the Home screen is resumed (the Home entry reaches `RESUMED`: Home becomes the current screen again once a panel has left, and VOID comes back from another app), whatever a swipe left behind is undone at once, without an animation: a pull of a finger that is gone, a spring that was cut short, Home standing off for an app. Resuming is a lifecycle event, not a recomposition, so it cannot be missed. `reset()` says whether anything was out of place (logged as `swipe_state_reset reason=resume`).
- **Recall**: a launch that does not take VOID off the screen (a window the system opens next to it, an app that fails to start after `startMainActivity` returned) is followed by no resume. If Home is still departed and the gate is still open 1.5 s after the commit (`DEPARTURE_RECALL_MILLIS`) it springs back on spatial default (`swipe_depart_recalled`); a paused VOID is covered and is reset when it comes back.
- **Diagnostics**: a drag that the gate swallows is logged once per gesture as `swipe_blocked reason=not_on_top|busy|departed`. No identifiers or package names are logged.

Why: a swipe down on Home once did nothing after a system back swipe from the notification list and about a minute without touching the phone, and an immediate retry worked. It was not reproduced, so the cause is not proven (see `docs/bugs/home-swipe-gate.md`); the old gate depended on two values that can lag or stay behind, and both are gone.

### Edge-origin swipes

The old code dropped every horizontal swipe that began within 24 dp of an edge, in every navigation mode, which meant a swipe that really starts at the edge could never work. That guard is gone. What happens at the edge depends on who claims it:

| Navigation mode | Who claims the edge strip | What VOID does |
|---|---|---|
| Gesture navigation (API 29+) | System UI takes a strip of about 30 dp times the user's back-sensitivity scale on each side for the Back gesture and cancels the app's pointer stream there | While Home is the current destination and at rest, two full-height strips along the left and right edges call `Modifier.systemGestureExclusion()` (`HomeEdgeExclusionStrips`), each only if gestures are on and that edge has a target. Width: `max(WindowInsets.systemGestures inset of that side, 30 dp) + 8 dp` (`GestureEdgeStrip.exclusionWidthPx`). The edge swipe then reaches `homeSwipe` like any other |
| 3-button navigation, API 26-28 | Nothing is claimed ([not verified on a device](#limits)) | No guard and no special case; the strips do no harm (the exclusion call is a no-op below API 29) |

The exclusion is a request, not a guarantee. The platform caps the excluded height at 200 dp per edge, but not for the home activity (`MainActivity` is the HOME activity, and only while VOID is the default launcher); the system may also choose to relax it and an OEM may ignore it. A swipe the system still takes arrives as a cancel and does nothing; swipes that start further in work as always. The strips exist only while Home is the current destination, so the system Back gesture works as usual on every panel (it does nothing on Home anyway). VOID does **not** send synthetic back events or use a `PredictiveBackHandler` on Home to open panels. If the emulator checks show an OEM or API level that ignores the exclusion, the fallback is a bridge from `NavigationEvent` (swipe edge, touch position), which would also draw the system back arrow over a forward gesture: it is not built.

### Launching an app by swipe

The swipe action `APP` used to call a plain `startActivity`, so Android played its default launch animation. It now goes through `MainViewModel.launchSwipeApp(package, edge)` (the same `LauncherApps.startMainActivity` path and stale-component fallbacks as a tap, in the current user, opening the package's primary launcher activity as `getLaunchIntentForPackage` returns it, as the swipe did before; the last launcher entry that a hidden app gets applies only when the package manager has no launch intent) and, **on Android 11 and earlier only** (`LaunchAnimation.playsOn`), passes an `ActivityOptions.makeCustomAnimation` pair chosen from the **physical edge** the swipe brings content in from (`PanelEdge.launchAnimation()`):

| Swipe | Edge | Animation | App window | Home window |
|---|---|---|---|---|
| left swipe action (finger right) | `Left` | `FromLeft` | `slide_in_left` | `slide_out_right` |
| right swipe action (finger left) | `Right` | `FromRight` | `slide_in_right` | `slide_out_left` |
| swipe down (finger down) | `Top` | `FromTop` | `slide_in_top` | `slide_out_bottom` |
| swipe up (finger up) | `Bottom` | `FromBottom` | `slide_in_bottom` | `slide_out_top` |

(Only the two horizontal swipes can launch an app today; the vertical rows document the pairs.) The names are the edge the app comes in from, **not** the direction of the finger: the old `swipeDirection` strings of `launchApp` ("left" slid the app in from the right) were the inverse of the edge naming of `dispatchSwipeAction("left")`, which is why they were not wired through. `launchApp` now takes a `PanelEdge` (`selectedApp` lost its unused direction parameter); a tap passes none and keeps the default window animation (`fade_enter` / `fade_exit`).

The app window travels its whole width or height; Home leaves the same way by the parallax of the panel transition (`NavTravel`: 30% of the width for `Left`/`Right`, 20% of the height for `Top`/`Bottom`) and dims to 68% (`VoidAlpha.coveredByPanel`). All eight files run 300 ms on the Material 3 emphasized-decelerate curve (`res/interpolator/emphasized_decelerate.xml`, cubic Bezier 0.05, 0.7, 0.1, 1.0), and `LaunchAnimationTest` ties the files to those constants. Here Home's own content settles quickly (spatial fast) as soon as the finger is up, because the system moves the Home window itself, and the content must not be moved twice.

**On Android 12 and later (API 31+) the pair is not passed, because the system would ignore it.** A launch of another app from Home opens a new task, and for a task change the shell transition handler plays a custom animation only when the options carry `overrideTaskTransition` (`DefaultTransitionHandler.loadAnimation`: `overrideType == ANIM_CUSTOM && (!isTask || options.getOverrideTaskTransition())`); the public `makeCustomAnimation` never sets it, and `makeCustomTaskAnimation` is hidden and needs `START_TASKS_FROM_RECENTS`. The Android 12 legacy path has the same rule (`persist.wm.disable_custom_task_animation`, default on). Checked with `javap -public android.app.ActivityOptions` on the SDK 35, 36 and 37 `android.jar`: only `makeCustomAnimation` (two overloads) and `makeClipRevealAnimation` are public; `setOverrideTaskTransition` and `makeCustomTaskAnimation` are not. **So on Android 12 and later the app's own entrance animation is system-owned and its direction cannot be forced by a third-party launcher.** The `LaunchAnimation` enum, the `res/anim` files and `PanelEdge.launchAnimation()` stay for API 26 to 30, where the system honours the pair.

What a launcher can still do there, and VOID does (`homeAfterCommit` returns `Depart`): **start the launch at the instant the swipe commits, and let its own Home layer carry the swipe on.** An emulator run (Pixel 8a AVD, Android 17, gesture navigation) showed Home springing back to rest and then idling about 250 to 300 ms before the system's launch animation began, so the two motions looked disconnected. Now Home keeps travelling in the direction of the finger from where the rubber band left it, starting at the speed of the finger, to the place a panel parks the screen under it (`departureOffsetPx`, which is `NavTravel.exitEnd`: 30% of the width) while it dims to 68% (`HomeDeparture`, `VoidAlpha.coveredByPanel`), on spatial default (0.9/700), so by the time the system's window arrives Home is already pushed aside and dimmed. It stays there, behind the system's own animation, until VOID is resumed: `HomeSwipeRestore` resets it without an animation (see [Keeping the swipe reliable](#keeping-the-swipe-reliable)). If the app cannot be launched, `launchSwipeApp` has already shown the message, the reject haptic plays and Home springs back on spatial default. No curtain, relay activity or screenshot is involved, and the launch is never delayed for Home's motion. `ActivityOptions.makeClipRevealAnimation` from a thin rectangle on the swiped edge would be a wipe, not a slide, and is not built.

**This animation is played by the system, not by Compose**: it ignores the in-app animation-speed preference and is scaled only by the system animation scale (Android's "Remove animations" turns it off). It starts when the other app's window is ready, so a cold start of a slow app delays it; nothing in VOID can change that.

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

- The panel itself does not follow the finger: `NavHost` cannot seek a forward transition, so Home follows the finger (with a rubber band) while the swipe is in progress and the panel starts when the swipe is released past the commit rule, then springs in. A panel that is dragged in under the finger needs Home permanently composed under an overlay panel host; that is a separate change.
- The swipe inside a panel is not finger-tracked: nothing moves until it is recognised and released, then the plain pop plays. Only the system back gesture follows the finger, and only on Android 14 and later. Finger-tracking the swipe would need synthetic back events fed to `NavHost`, which can wedge the system's own back input if one is left unfinished, so it is not done.
- The predictive preview is `NavHost`'s: it has no vertical shift for the drawer and the notification list (the edge it is given is horizontal only), the corners stay square while it scales, and the cancel is a linear tween. A panel that left toward the left edge may first shift right when the swipe started at the left edge, and a Back key press always reports the left edge, so any panel shifts right.
- A vertical in-panel dismiss only starts on regions that consume no drag (see above). Fixing that needs a nested-scroll hand-off from the lists.
- A `backProgress` mapping (travel over the commit distance) is deliberately not built: it would only be needed to drive `NavHost` with synthetic back events, which is rejected above.
- The predictive pair follows the written guidance, so around 35% of the gesture the leaving screen has faded out and the screen under it has not started to: only the black window shows for that moment, in a held gesture and on a Back key press. The checklist asks to look at it on an emulator. If it reads as Home vanishing, start the fade-in of the screen under it at 0% from a non-zero alpha (keeping the 110% to 100% scale and the 35% fade-out) and change `PredictiveBackMath`, its test and the table above together.
- Reversing a push in flight (open the drawer, then dismiss it at once) plays the screen under the panel back on Compose's default springs, not on the `popEnter` spring: `EnterExitTransition` applies an enter spec only to the PreEnter to Visible segment and an exit spec only to Visible to PostExit, and the reversal is PostExit to Visible, which uses the defaults. There is no public API to change that segment. The panel leaves on its own `popExit` spring, so Home settles a little before the panel has gone.
- A dismiss that arrives while the panel is already closing is ignored (`MainActivity.popPanel(panel)` pops only while that panel is the top entry and a screen is left under it: `shouldPopPanel`). The closing panel stays on screen and touchable until its exit finishes, about 0.45 s at standard speed and about 0.8 s at slow speed, and popping again would remove the screen under it as well (Settings over the drawer would go on to Home), or Home itself and leave the `NavHost` blank.
- The notification list opened by a horizontal swipe (the `NOTIFICATIONS` swipe action) still drops from the top, because the edge is chosen from the destination alone and a swipe down or the status-bar pull open the same destination. Home stays put during that swipe (it only gives the haptic cue and opens on release), so it is not pulled sideways against the panel; the list does not come from the side of the finger. Making it do so needs the trigger direction, for example a one-shot edge hint set by Home's open callbacks and read by `edgeOf`, and belongs with the work on the system notification shade, which replaces that action.
- A panel assigned to both swipe actions enters from the left (`PanelOrigin.resolve`), so a swipe to the left would peek Home to the left while the panel arrives from the left. The Settings picker hides a panel that the other side already uses, so this can only come from preferences restored from elsewhere; `SwipeTarget.Panel.edge` is the edge the finger brings it in from, not the edge the `NavHost` transition uses.
- **On Android 12 and later the app's own entrance animation is system-owned and its direction cannot be forced by a third-party launcher.** An app opened by a swipe uses the system's launch animation, not the directional slide: the public `ActivityOptions` of SDK 35 to 37 has no `setOverrideTaskTransition` or `makeCustomTaskAnimation` (checked with `javap`), and the shell ignores `makeCustomAnimation` for a launch in a new task. All VOID can do is start the launch at once and let Home carry the swipe on, pushed aside and dimmed, until the app's window takes over (see [Launching an app by swipe](#launching-an-app-by-swipe)); it cannot make the app slide in from the side of the swipe, and the first part of the system animation still starts when the app's window is ready, which a cold start delays. After the launch Home stays pushed aside and dimmed behind the system animation until VOID is resumed, when it is put back without an animation; if the system opens the app in a window beside VOID (VOID is not paused) Home springs back after 1.5 s. `docs/testing/manual-checklist.md` asks for the check on an API 30 and an API 31+ emulator.
- Edge-origin swipes depend on the system honouring `systemGestureExclusion` (see above). It is not verified on a device or an emulator yet, in gesture navigation at low and high back sensitivity or in 3-button navigation, nor when VOID is not the default launcher (the 200 dp cap then applies); `docs/testing/manual-checklist.md` lists the checks.
- A swipe that starts while Home is not resumed on top (a navigation transition still involves it, see [Keeping the swipe reliable](#keeping-the-swipe-reliable)), while Home stands off for an app, or while the Home reorder is dragging, is ignored. After a cancelled swipe Home springs back in about a third of a second; a new touch in that time on the same axis takes the spring up where it is (the pull of the finger adds to what is left, so nothing snaps), while a swipe on the other axis drops what is left of the first because one offset cannot carry both axes.
- The intermittent miss of a swipe down on Home (once after a system back swipe from the notification list and about a minute without touching the phone; an immediate retry worked) was seen twice in about eleven attempts on a headless emulator. Reading the Navigation and Compose animation sources did not reproduce it, so the cause is not proven. The gate, the reset and the `swipe_blocked` log in [Keeping the swipe reliable](#keeping-the-swipe-reliable) remove the suspects that the sources show and make a repeat visible in the logcat of the emulator (tag `HomeSwipe`); if it happens again with a `swipe_blocked reason=` line, the reason says which fact the gate saw. `docs/bugs/home-swipe-gate.md` has the investigation and the manual soak.

## Tests

`NavTravelTest` (direction of every layer for every edge, the four offsets `NavMotion` hands to `slideIn` and `slideOut` (`enterStart`, `exitEnd`, `popEnterStart`, `popExitEnd`), a pop is the push played backwards, parallax, `None` has no translation), `NavMotionTest` (`PanelOrigin.resolve` table including a panel on both sides and an unknown route, the parts and the spring of every transition for every edge and speed, the predictive pair with its tweens written out from the guidance curve, `settingsClosed`, `shouldPopPanel`), `VoidMotionTest` (token values, speed scaling, thresholds), `ScreenBackSwipeTest` (dismiss direction per edge and the decision to close, driven through `BackSwipeRecognizer`: distance, flick, flick back, the other axis, a child that consumed the drag, a cancel, the travel before the slop, a panel sliding under a resting finger), `HomeSwipeRecognizerTest` (axis lock into each of the four directions, an unconfigured direction is ignored but takes the pointer and a wrong-way finger moves nothing, a drag past the slop takes the pointer while one under it and one a child or the system took do not, Home follows the finger on every edge and never past the rubber band limit, a sideways notification-list swipe does not pull Home, commit by 56 dp and by a flick, a flick back and a reversal under the distance cancel, the haptic latch and its 80% re-arm, a long press and a consumed pointer give up, the release velocity), `HomeSwipeStateTest` (the offset of Home driven by a manual frame clock: following, settling to exactly rest, a new touch on the same axis picks the running spring up without a jump, the other axis drops it; `reset()` while the finger is down, while a spring runs and while Home is departed puts Home back at rest at once and says whether anything was out of place, an interrupted lock and a gesture that ends after a reset leave nothing behind; `depart()` carries Home on to the parked place with no jump at the start and dims it to the covered opacity, stays departed until reset, and `settle()` brings a departed Home back opaque; and the spring a commit moves Home on per outcome: spatial slow for a panel, fast for an app moved by the system, default for a departure or a failure, scaled by the animation speed), `HomeSwipeGateTest` (the gate from the two facts of the navigation controller, which combination opens it, who keeps the back press, the log reasons), `HomeSwipeDepartureTest` (`homeAfterCommit` for every target, outcome and Android version, `departureOffsetPx` equal to where a panel parks the screen under it on every edge, the dimming of `HomeDeparture` in both directions and past its ends), `HomeSwipeDecisionTest` (`resolveSwipe` for every action on every edge including disabled features, `HomeSwipeSettings.targetFor` per direction, finger direction and edge, the threshold latch, the edge strip width), `HomeSwipeDispatchTest` (which callback each target runs and whether the action could run, and `homeSwipeSettings` putting each side's action, app and the feature switches in their places), `LaunchAnimationTest` (edge to animation: the left swipe action comes from the left, the right one from the right, swipe up from the bottom; the custom animation only up to Android 11; the resource pairs; the XML durations, curve, travel, parallax and dimming against `NavTravel` and `VoidAlpha`), `SwipeMathTest` (axis lock cones, diagonals and that it never changes, rubber band slope and limit, commit by distance and by flick, a short flick, a flick or drag the wrong way, a flick back after the distance, dp conversion at several densities), `PredictiveBackMathTest` (edge signs, the shift cap and the sideways-only offset, the 35% fade split, durations per speed, the easing control points and two of its values), `NotesScreenTest` (the delete swipe of a note is never the close swipe, in LTR and RTL), and `ListeningLeaseTest` (see below).

`HomeSwipeInstrumentedTest` (`src/androidTest`) runs the pointer loop of `homeSwipe` and the follow-the-finger offset on the real Compose input pipeline: each direction opens the target of its edge, a flick commits, Home is off its place by the rubber band of the pull while the finger is down (checked against the content's bounds, tagged after the layer so they include its translation) and is back at rest after a short swipe, a reversal under the distance, a commit, a commit whose action cannot run, and a swipe that is switched off in the middle; a direction without a target, a swipe that does not commit and a disabled swipe move nothing and are not a click on the clickable child under the finger, while a plain tap still is. On Android 12 and later (the tests skip on 11 and earlier) an app commit also leaves Home 30% of the width off its place in the swipe direction, dimmed and departed, a reset puts it back at once, a swipe while it is departed is swallowed, and an app that cannot be launched springs Home back. Like the next one it compiles with the build but has not been run.

`ScreenBackSwipeInstrumentedTest` (`src/androidTest`) runs the pointer loop of `screenBackSwipe` on the real Compose input pipeline: a slow drag past 56 dp closes, a short slow one does not, a short fast flick closes, the wrong way and the other axis do nothing, a child that consumes the drag wins, a cancelled pointer does nothing, a tap does nothing, a panel sliding under a resting finger does not close, and with the setting off the swipe does nothing while Dismiss still closes. It compiles with the build but has not been run: it needs an emulator, and the physical device is not used for instrumented tests.

Not covered by a JVM or instrumented test, because the project has no Robolectric or navigation-testing: the `hasRoute` mapping in `MainActivity.routeKind()`, the choice of `targetState` or `initialState` in the four `NavHost` lambdas and the two predictive ones, the `NavPanel` provision of `LocalPanelEdge`, the `currentBackStackEntry == panel` lookup in `popPanel` (its decision is `shouldPopPanel`), and the Settings refresh effect. They are exercised by `docs/testing/manual-checklist.md` on a device; an instrumented test of the real graph needs `navigation-testing` and the lambdas extracted into one function, and is a follow-up.

## Widgets and the widget host

`AppWidgetHost` registers one listener per host id for the whole process, and each Widgets screen builds its own host. The screen that is closing stays composed until its exit animation ends, so opening Widgets again in that time started a second host, and the first screen's `stopListening` then silenced it: the widgets stopped updating. `WidgetsViewModel` now takes a `ListeningLease` on a process-wide `ListenerCount`: every screen starts listening (so the newest one receives the updates), and only the last one to leave stops it (`ListeningLeaseTest`). Releasing twice, from the disposed effect and from `onCleared`, is harmless.
