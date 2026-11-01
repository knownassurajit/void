# E2E / instrumentation

```bash
./gradlew connectedDebugAndroidTest
```

## Critical paths

1. Launch home
2. Swipe up → app drawer (no crash; keyboard on/off); a swipe follows the finger with Home before the panel opens
3. Long-press drag home apps 5→2 cascade
4. Open Widgets: the grid has no top bar and the strip at the top (drag down or tap) brings in the header (title, Add widget); picker shows previews; first pin may show a system “create widgets” prompt; widget appears after allow; pinned widgets show a check; tap and hold customizes a widget (edit bar with Width/Height buttons) without opening the app; drag snaps to the grid
5. Open Notes: swipe a note row to delete it (red arrow; the delete swipe is the opposite of the one that closes Notes, so it goes right when Notes is on the left swipe)
6. Toggle homescreen components and verify reflow
7. Long-press empty home space → picker; tap a chip to remove the app
8. Swipe down → in-app notifications: without notification access the panel explains why and Grant notification access opens the system page; coming back fills the list without reopening it
9. Private Space path when OS-configured; otherwise graceful skip

## Back swipe (emulator only)

`ScreenBackSwipeInstrumentedTest` drives the pointer loop of `Modifier.screenBackSwipe` with the Compose test rule: the distance and flick rules, the axis, a child that consumes the drag, a cancelled pointer, a panel sliding under a resting finger, and the Dismiss action. It starts no activity and downloads nothing, but run it on an emulator like the rest:

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*ScreenBackSwipeInstrumentedTest"
```

## Widgets header (emulator only)

`RevealHeaderInstrumentedTest` drives the Widgets header with the Compose test rule: a pull on the content never opens it, a drag down or a tap on the strip at the top (`RevealPullZone`) does, and scrolling the content up puts it away. It compiles with the build but has not been run yet: run it on an emulator before relying on it.

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*RevealHeaderInstrumentedTest"
```

## Notification cards (emulator only)

`NotificationCardsInstrumentedTest` drives `DismissibleCard` and `NotificationCard` with the Compose test rule: the Dismiss custom action and a swipe reach the same callback, an expandable header says "Collapsed" or "Expanded" and toggles on a tap and on the click action, the count badge is read as "3 notifications", and a card without a toggle has no click action. It compiles with the build but has not been run yet: run it on an emulator before relying on it.

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*NotificationCardsInstrumentedTest"
```

## Settings rows (emulator only)

`SettingsRowsInstrumentedTest` measures where the icons of Settings rows sit with the Compose test rule: a text row (action, toggle) keeps its leading icon and trailing control centered whatever the subtitle length (a three-line subtitle used to move them to the title line), and a slider or choice row keeps its icon on the title line. Its three tests also run and pass on the JVM under Robolectric with native graphics, and the alignment tests fail when the alignment rule is removed (checked by hand; Robolectric is not a project dependency). It has not been run on an emulator yet: run it before relying on it.

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*SettingsRowsInstrumentedTest"
```

## Home swipes (emulator only)

`HomeSwipeInstrumentedTest` drives the pointer loop of `Modifier.homeSwipe` with the Compose test rule: each direction opens the target of its edge, a quick flick commits, Home is off its place by the rubber band while the finger is down and is back at rest after a short swipe, a reversal under the distance, a commit, a commit whose action cannot run and a swipe that is switched off in the middle; a direction without a target, a swipe that does not commit and a disabled swipe move nothing and are not a click on the clickable child under the finger (a plain tap still is). On Android 12 and later (the tests skip on 11 and earlier) an app commit leaves Home 30% of the width off its place in the swipe direction, dimmed and departed until it is reset, a swipe while it is departed is swallowed, and an app that cannot be launched springs Home back. It starts no activity, so it does not exercise the system edge gestures, the exclusion strips or the app launch animation; those are checks in `manual-checklist.md`. It compiles with the build but has not been run yet: run it on an emulator before relying on it.

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*HomeSwipeInstrumentedTest"
```

## Font delivery (emulator only)

`FontProviderInstrumentedTest` checks the bundled Geist face, the provider certificate check (`GoogleFont.Provider.isAvailableOnDevice`), and a real download of Work Sans. Run it on an emulator image with Google Play (it needs a network) and never on a physical device, because the download test downloads fonts. That test is skipped unless you opt in with the `allowFontDownload` instrumentation argument:

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*FontProviderInstrumentedTest" \
  -Pandroid.testInstrumentationRunnerArguments.allowFontDownload=true
```

The provider tests skip themselves on images without Play services. Airplane-mode, offline-reuse, Bold-text, and no-Play-services behaviour are manual checks in `manual-checklist.md`.

CI runs unit + lint always; connected tests when an emulator/device is available.
