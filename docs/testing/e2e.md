# E2E / instrumentation

```bash
./gradlew connectedDebugAndroidTest
```

## Critical paths

1. Launch home
2. Swipe up → app drawer (no crash; keyboard on/off)
3. Long-press drag home apps 5→2 cascade
4. Open Widgets / Notes / Notification Summary
5. Toggle homescreen components and verify reflow
6. Private Space path when OS-configured; otherwise graceful skip

CI runs unit + lint always; connected tests when an emulator/device is available.
