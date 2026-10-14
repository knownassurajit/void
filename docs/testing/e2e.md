# E2E / instrumentation

```bash
./gradlew connectedDebugAndroidTest
```

## Critical paths

1. Launch home
2. Swipe up → app drawer (no crash; keyboard on/off); a swipe follows the finger with Home before the panel opens
3. Long-press drag home apps 5→2 cascade
4. Open Widgets: picker shows previews; first pin may show a system “create widgets” prompt; widget appears after allow; pinned widgets show a check; long-press selects without opening the app; drag snaps to the grid
5. Open Notes: swipe a note row to delete it (red arrow; the delete swipe is the opposite of the one that closes Notes, so it goes right when Notes is on the left swipe)
6. Toggle homescreen components and verify reflow
7. Long-press empty home space → picker; remove via chip X
8. Swipe down → in-app notifications (grant listener if empty)
9. Private Space path when OS-configured; otherwise graceful skip

## Back swipe (emulator only)

`ScreenBackSwipeInstrumentedTest` drives the pointer loop of `Modifier.screenBackSwipe` with the Compose test rule: the distance and flick rules, the axis, a child that consumes the drag, a cancelled pointer, a panel sliding under a resting finger, and the Dismiss action. It starts no activity and downloads nothing, but run it on an emulator like the rest:

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*ScreenBackSwipeInstrumentedTest"
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
