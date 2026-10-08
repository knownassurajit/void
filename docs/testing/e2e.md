# E2E / instrumentation

```bash
./gradlew connectedDebugAndroidTest
```

## Critical paths

1. Launch home
2. Swipe up → app drawer (no crash; keyboard on/off)
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

## Font delivery (emulator only)

`FontProviderInstrumentedTest` checks the bundled Geist face, the provider certificate check (`GoogleFont.Provider.isAvailableOnDevice`), and a real download of Work Sans. Run it on an emulator image with Google Play (it needs a network) and never on a physical device, because the download test downloads fonts. That test is skipped unless you opt in with the `allowFontDownload` instrumentation argument:

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest \
  --tests "*FontProviderInstrumentedTest" \
  -Pandroid.testInstrumentationRunnerArguments.allowFontDownload=true
```

The provider tests skip themselves on images without Play services. Airplane-mode, offline-reuse, Bold-text, and no-Play-services behaviour are manual checks in `manual-checklist.md`.

CI runs unit + lint always; connected tests when an emulator/device is available.
