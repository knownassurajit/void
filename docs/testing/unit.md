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
- `PrivateSpaceUi` — settings action visibility, add-entry heuristic, `outcomeFor` (an unlock request that returns false is never `UNLOCKED`)
- `BundledFontFilesTest` — every `res/font` `.ttf`/`.otf`/`.ttc` has a valid sfnt header (would have caught the Google Sans HTML placeholders); the default Google Sans asset exists, exposes only the pinned-down `wght` axis covering the weights `GoogleSansFamily` declares, and keeps its `tnum` (tabular figures) feature
- `GoogleSansFamilyTest` — `GoogleSansFamily` declares Normal/Medium/Bold from `google_sans_variable`, each with only its own `wght` axis value (guards Medium/Bold rendering as Regular)
- `TabularFiguresTest` — `withTabularFigures()` requests `tnum` and keeps the rest of the style
- `FontCertsTest` — `font_certs.xml` arrays are strict base64, parse as X.509, and match the pinned dev/prod SHA-256 fingerprints
- Widget id / order parse helpers where extracted

Add a regression test for every bug fix affecting pure logic.
