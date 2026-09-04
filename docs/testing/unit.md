# Unit tests

```bash
./gradlew testDebugUnitTest
```

## Coverage targets

- `HomeAppsCap` — missing `MAX_HOME_APPS` plus filled slots must not collapse to 4
- `NavMotion` — drawer Up, notifications Down, assigned swipe panels Start/End
- `HomeReorderHelper` — multi-slot cascade and `moveItem`
- `AiSummarizer` — tier mapping
- `FeatureAvailability` — private space API gate (robolectric/light)
- Widget id / order parse helpers where extracted

Add a regression test for every bug fix affecting pure logic.
