# E2E / instrumentation

```bash
./gradlew connectedDebugAndroidTest
```

## Critical paths

1. Launch home
2. Swipe up → app drawer (no crash; keyboard on/off)
3. Long-press drag home apps 5→2 cascade
4. Open Widgets: picker shows previews; first pin may show a system “create widgets” prompt; widget appears after allow; pinned widgets show a check; long-press selects without opening the app; drag snaps to the grid
5. Open Notes: swipe left to delete (red arrow)
6. Toggle homescreen components and verify reflow
7. Long-press empty home space → picker; remove via chip X
8. Swipe down → in-app notifications (grant listener if empty)
9. Private Space path when OS-configured; otherwise graceful skip

CI runs unit + lint always; connected tests when an emulator/device is available.
