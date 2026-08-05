# Unit tests

```bash
./gradlew testDebugUnitTest
```

## Coverage targets

- `HomeReorderHelper` — multi-slot cascade and `moveItem`
- `AiSummarizer` — tier mapping
- `FeatureAvailability` — private space API gate (robolectric/light)
- Widget id / order parse helpers where extracted

Add a regression test for every bug fix affecting pure logic.
