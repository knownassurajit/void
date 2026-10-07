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
- `BundledFontFilesTest` — every `res/font` `.ttf`/`.otf`/`.ttc` has a valid sfnt header (would have caught the Google Sans HTML placeholders); the default Google Sans asset exists, exposes only the pinned-down `wght` axis covering the weights `GoogleSansFamily` declares, and keeps its `tnum` (tabular figures) feature; every bundled catalog family has its files and the Geist asset is byte-identical to upstream (sha256 pin)
- `GoogleSansFamilyTest` — `GoogleSansFamily` declares Normal/Medium/Bold from `google_sans_variable`, each with only its own `wght` axis value (guards Medium/Bold rendering as Regular)
- `TabularFiguresTest` — `withTabularFigures()` requests `tnum` and keeps the rest of the style
- `FontCertsTest` — `font_certs.xml` arrays are strict base64, parse as X.509, and match the pinned dev/prod SHA-256 fingerprints
- `FontCatalogTest` — catalog invariants (unique snake-case ids and names, frozen legacy ids, only 400/500/700 weights, requested families present, unlicensed families absent), `resolve` fallbacks (unknown id, no provider, not ready), line-height scale and mono tracking policy
- `FontPickerFilterTest` — picker groups: order, case-insensitive search, kind filter, no match, collapsed Google Fonts group without the provider, list index of a row (header per group)
- `FontLoadCoordinatorTest`, `FontLoadStateTest` — font load state machine: single flight, retry after failure, timeout, cancellation (also when the resolver wraps it) is not a failure, `Full` covers `Sample`, sample loads run one at a time and a full load never waits for them, the timeout starts only once a load holds its turn, failure classification from provider reason codes, row state from the full load only, failure advice, Settings subtitle availability
- `FontPickerSessionTest` — a family is committed only after its full load is `Ready`, a failed download never commits, the last selection wins (a superseded one neither commits nor reports), previews are requested once per family and session and never for large downloads or fonts that need no download
- `FontFamiliesTest` — families match the catalog weights, downloadable families have the bundled Google Sans file behind every download, memoisation
- `TypographyPolicyTest` — one family for all 15 roles (the default is the full family the loader preloads), scaled line heights, mono letter spacing, no synthesis override
- `GoogleFontsProviderTest` — only Google Play services may own the font provider; the availability check is false for no provider, a look-alike owner, and a lookup that throws
- Widget id / order parse helpers where extracted

Add a regression test for every bug fix affecting pure logic.
