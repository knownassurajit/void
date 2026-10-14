# A swipe down on Home sometimes did nothing (intermittent, not reproduced)

## Symptom

On a headless emulator (Pixel 8a AVD, Android 17, gesture navigation) a swipe down on Home did not open the notification list twice in about eleven attempts. Every time the sequence was: swipe down (the list opens), system back swipe from the left edge (the list closes with the predictive-back preview), about 45 to 60 seconds without touching the phone, swipe down. An immediate retry worked. The drag did nothing visible: no list, no error.

## What was checked

The swipe is swallowed (consumed, nothing opens) when the gate that `Modifier.homeSwipe` reads is closed, so the investigation looked for a way for the gate to stay closed, or for the swipe state to stay out of place. From reading the sources (Navigation Compose 2.10.0, Navigation runtime 2.10.0, Compose animation 1.10.5):

1. **`isCurrentDestination` was a captured value.** `MainActivity` passed `currentRoute == NavRouteKind.Home`, computed in the outer composition, into the Home destination. The `NavGraph` builder lambda that captures it is rebuilt when the route changes, and `NavController.setGraph` only swaps the new destinations into the existing entries; nothing guarantees that a destination that is already composed is invoked again with the new lambda. The predictive-back preview composes Home when the gesture starts, while the panel is still the current entry, so Home could be composed with `false`. Whether and when it was refreshed depended on `AnimatedContent` rebuilding its content map at the end of the transition.
2. **`isAtRest` read the child transition of `AnimatedContent`.** `currentState` and `targetState` of that transition are only brought together by `Transition.onTransitionEnd` of the root transition (`SeekableTransitionState.animateTo` and `snapTo` call it), while the target of the child is updated by a later composition (`Transition.updateTarget`). Reading `snapTo`, which `NavHost` uses to finish a cancelled predictive back, the child can be left with a current state that is behind its target until something else moves it. No path that does this to Home itself was found (Home's composition is removed when the pop is cancelled and created again for the next one), but the signal depends on the order of two mechanisms that are internal to the animation library.
3. **`HomeSwipeState`** cannot stay locked: every path out of the pointer loop settles Home, and the state is created with the composition, which is recreated after each pop. A thrown action was the one path that skipped it.
4. **The edge strips and the `pointerInput` key** do not gate anything for a swipe in the middle of the screen, and the loop is keyed on the state object only.

None of this was reproduced; the cause is not proven.

## Fix (hardening)

- `HomeSwipeGate` (`Open`, `Busy`, `NotOnTop`) is derived from what the navigation controller keeps (the top entry of the back stack and the lifecycle of the Home entry, which the controller only resumes once no transition involves the entry and while the activity is resumed), read inside `composable<HomeRoute>`. `isCurrentDestination` and `isAtRest` are gone.
- `HomeSwipeState.reset()` runs each time the Home entry is resumed (a lifecycle event, so it cannot be skipped by a missed recomposition), and the pointer loop marks a gesture as released only after its outcome ran, so an action that throws still settles Home.
- A drag that the gate swallows is logged once per gesture: `swipe_blocked reason=not_on_top|busy|departed` (tag `HomeSwipe`, no identifiers).

## Regression

Unit (JVM, no emulator): `HomeSwipeGateTest` (the gate from the two facts), `HomeSwipeStateTest` (reset while the finger is down, while a spring runs and while Home is departed; an interrupted lock; the end of a gesture after a reset; a cancel).

Manual, emulator only (never the physical device), see `docs/testing/manual-checklist.md`: repeat the sequence swipe down, system back swipe from the left edge, about 60 seconds idle, swipe down, and a 20-cycle soak of it; open the logcat of the emulator (tag `HomeSwipe`) and keep any `swipe_blocked` line: its reason says which fact the gate saw.
