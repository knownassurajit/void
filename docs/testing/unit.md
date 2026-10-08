# Unit tests

```bash
./gradlew testDebugUnitTest
```

## Coverage targets

- `HomeAppsCap` — missing `MAX_HOME_APPS` plus filled slots must not collapse to 4
- `NavTravelTest` — for every `PanelEdge` the panel enters from that edge and the covered screen moves the same physical way as the panel during a push (regression for Home sliding against the incoming panel), the four offsets `NavMotion` slides with (`enterStart`, `exitEnd`, `popEnterStart`, `popExitEnd`), a pop is the push played backwards, parallax fractions, `None` has no translation
- `NavMotionTest` — `PanelOrigin.resolve` (drawer Bottom, notifications Top, Settings None, left/right assigned panels Left/Right, a panel on both sides takes Left, an unknown route has no edge), swipe action to route, the parts and the spring of each transition for every edge and animation speed, the predictive pop pair (scale, slide and fade parts and their tweens per speed, no springs), `NavRouteKind.settingsClosed`, `shouldPopPanel` (only the top panel pops, never Home)
- `VoidMotionTest` — Material 3 standard spring tokens, animation-speed factor and `stiffness / f^2` scaling with damping unchanged, visibility thresholds
- `ScreenBackSwipeTest` — the close direction of a panel is the finger moving toward its origin edge (Settings and the drawer: down); the decision to close, driven through `BackSwipeRecognizer` (distance, flick, flick back after a long drag, wrong way, the other axis, a consumed or cancelled pointer, the travel before the slop, a panel sliding under a resting finger never counts as a swipe)
- `NotesScreenTest` — a note is deleted with the swipe opposite to the one that closes Notes, for every edge, in LTR and RTL
- `SwipeMathTest` — axis lock: cone edge at a dominance of 1.5, diagonals lock to the longer axis at 3 slops and are never rejected, an exact diagonal goes vertical, the axis never changes once locked (`AxisLock`), the drag is measured from the touch-down point; rubber band: slope 1 at 0, below the limit, monotonic, mirrored; commit by 56 dp of travel or a 125 dp/s flick past the lock distance, a short flick is a twitch, a flick back at 125 dp/s cancels even after the distance while a slower return does not, travel the wrong way never commits, dp conversion at 1.5x, 2x and 3.5x
- `PredictiveBackMathTest` — edge sign (left `+1`, right `-1`, none `0`), the `W / 20 - 8 dp` shift cap and its floor at 0, the sideways-only `shiftOffset`, scales 0.9 and 1.1, duration per animation speed, the 35% fade split, the easing control points (0.1, 0.1, 0, 1) and two of its values
- `ListeningLeaseTest` — the widget host listener: the closing screen never stops the newest one, every acquire starts, only the last release stops, double acquire and double release are harmless, independent counts
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
