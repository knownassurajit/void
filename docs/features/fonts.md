# Fonts

VOID retypesets the whole app through `VoidAppTheme(appFont = ...)`. The pref `APP_FONT` stores a stable family id (default `google_sans`); `data/font/FontCatalog.kt` is the single source of truth for every selectable family, and `ui/theme/font/FontFamilies.kt` turns a catalog entry into Compose font objects. One family drives all 15 Material 3 type roles, so choosing a font changes Home, the drawer, Settings, and Notes together.

## Catalog

36 selectable families: 6 bundled, 29 downloadable, plus the platform font.

### Bundled (work offline, no flash of another face)

| Id | Family | Files in `res/font/` | Notes |
|----|--------|----------------------|-------|
| `google_sans` (default) | Google Sans | `google_sans_variable.ttf` | One variable file, weights 400/500/700 (see below) |
| `dm_sans` | DM Sans | `dm_sans_{regular,medium,bold}.ttf` | Static Latin subsets (pre-existing; upstream revision not recorded) |
| `geist` | Geist | `geist_variable.ttf` | One unmodified variable file, see "Geist asset" |
| `inter` | Inter | `inter_{regular,medium,bold}.ttf` | Static Latin subsets (pre-existing; upstream revision not recorded) |
| `manrope` | Manrope | `manrope_{regular,medium,bold}.ttf` | Static Latin subsets (pre-existing; upstream revision not recorded) |
| `plus_jakarta` | Plus Jakarta Sans | `plus_jakarta_sans_{regular,medium,bold}.ttf` | Static Latin subsets (pre-existing; upstream revision not recorded) |

### Downloadable through Google Play services

All are SIL OFL 1.1 families from Google Fonts with no Reserved Font Name. Sizes are the summed `Content-Length` of the declared weights from Google's font host on 2026-10-06, in KiB (1024 bytes); `approxKb` in `FontCatalog` holds them. The picker prints them with Android's `Formatter`, which uses SI units (1 kB = 1000 bytes), so the row of a ~8.5 MB entry reads about 8.9 MB and Actor reads about 33 kB. None of them is stored in the APK.

| Id | Family | Kind | Weights | Download | Notes |
|----|--------|------|---------|----------|-------|
| `actor` | Actor | Sans | 400 | ~32 KB |  |
| `agdasima` | Agdasima | Display | 400 / 700 | ~44 KB |  |
| `alumni_sans` | Alumni Sans | Display | 400 / 500 / 700 | ~190 KB |  |
| `antic` | Antic | Sans | 400 | ~37 KB |  |
| `antonio` | Antonio | Display | 400 / 500 / 700 | ~108 KB |  |
| `basic` | Basic | Sans | 400 | ~41 KB |  |
| `cabin` | Cabin | Sans | 400 / 500 / 700 | ~218 KB |  |
| `cantarell` | Cantarell | Sans | 400 / 700 | ~84 KB | Tall line height, scaled |
| `comfortaa` | Comfortaa | Display | 400 / 500 / 700 | ~322 KB |  |
| `dongle` | Dongle | Display | 400 / 700 | ~8.5 MB (row: about 8.9 MB) | Two ~4.3 MB files (Hangul coverage); the row never downloads on scroll, only on tap |
| `epilogue` | Epilogue | Sans | 400 / 500 / 700 | ~205 KB |  |
| `fjalla_one` | Fjalla One | Display | 400 | ~175 KB | Condensed headline face, classed Display by hand |
| `hanken_grotesk` | Hanken Grotesk | Sans | 400 / 500 / 700 | ~168 KB |  |
| `hind` | Hind | Sans | 400 / 500 / 700 | ~429 KB | Tall line height, scaled (1.60) |
| `inconsolata` | Inconsolata | Mono | 400 / 500 / 700 | ~215 KB | Monospaced: positive letter spacing is dropped |
| `jost` | Jost | Sans | 400 / 500 / 700 | ~172 KB | Tall line height, scaled (1.45) |
| `karla` | Karla | Sans | 400 / 500 / 700 | ~122 KB |  |
| `krub` | Krub | Sans | 400 / 500 / 700 | ~237 KB |  |
| `lato` | Lato | Sans | 400 / 700 | ~66 KB | Play services serves Lato 1.x, not the 2.0 release in the google/fonts repository |
| `nunito` | Nunito | Sans | 400 / 500 / 700 | ~368 KB | Natural line height 1.36, scaled |
| `open_sans` | Open Sans | Sans | 400 / 500 / 700 | ~289 KB | Natural line height 1.36, scaled |
| `poppins` | Poppins | Sans | 400 / 500 / 700 | ~447 KB | Tall line height, scaled (1.50) |
| `raleway` | Raleway | Sans | 400 / 500 / 700 | ~344 KB |  |
| `rethink_sans` | Rethink Sans | Sans | 400 / 500 / 700 | ~173 KB | Requested as "rething sans"; the family is Rethink Sans |
| `roboto` | Roboto | Sans | 400 / 500 / 700 | ~241 KB | A download, distinct from `system`: OEM system fonts differ from Roboto |
| `sintony` | Sintony | Display | 400 / 700 | ~46 KB |  |
| `sofia_sans` | Sofia Sans | Sans | 400 / 500 / 700 | ~324 KB |  |
| `source_serif_4` | Source Serif 4 | Serif | 400 / 500 / 700 | ~570 KB | Optical size stays at its default (downloads are static instances) |
| `work_sans` | Work Sans | Sans | 400 / 500 / 700 | ~336 KB |  |

### System

| Id | Family | Notes |
|----|--------|-------|
| `system` | System | `FontFamily.Default`; follows the device font |

Legacy ids (`google_sans`, `inter`, `plus_jakarta`, `manrope`, `dm_sans`, `system`) are frozen because they are stored in user prefs (and restored from backups). New ids are the lower snake case of the family name. An id that is not in the catalog (a downgrade, a restore, a corrupted value) renders with the default family and is **never rewritten** in the pref, so the choice survives until a build that knows it comes back.

## Unavailable families

Six requested names are not in the Google Fonts catalog or in Android's `directory.xml` (checked on 2026-10-07), and the families they appear to refer to are commercial, demo, or personal-use fonts. They are therefore not shipped (VOID is GPLv3 and cannot redistribute them without a licence) and nothing was invented in their place:

| Requested | What was found during research (not re-verified) |
|-----------|--------------------------------------------------|
| Brogman | Commercial family (Masyafi Studio) |
| Volkga | Personal-use family (Pandeka Foundry) |
| Disttopian | Probably Distopia (Alessandro Comotti) or TypeType Dystopian; both commercial |
| Stanza | Commercial or shareware (URW or Pinisiart) |
| Vangura Sans | No trace of a public release found |
| Noria | Demo or personal-use family (Flawless & Co) |

Design-alikes that do exist in both the Google Fonts catalog and `directory.xml`, if any of them should be added: Outfit, Urbanist, Red Hat Display, Syne, Space Grotesk, Sora (also Lexend, Figtree, Albert Sans, Instrument Sans). Adding one is one `FontCatalog` line plus the verify recipe below. If a licensed, redistributable file for any of the six turns up, bundle it like Geist (see "Geist asset").

## Delivery mechanism

Hybrid, chosen so that the APK stays small and the default experience never depends on the network:

1. **Bundled** families are `res/font` files loaded through `Font(resId, ...)`. Geist and Google Sans are single variable files (weight set through `FontVariation.weight`, API 26+).
2. **Google Fonts** families are Google Play services *downloadable fonts* through `androidx.compose.ui.text.googlefonts`. The provider is `com.google.android.gms.fonts` / package `com.google.android.gms`; its signing certificates are in `res/values/font_certs.xml` (see "Font-provider certificates"). VOID's own code opens no connection for fonts and needs no permission for them: Google Play services performs the download in its own process, and VOID's request carries only the family name, weight, and style. (The merged manifest does contain `INTERNET`, contributed by other dependencies; font loading does not use it.) Per weight the family chain is `[download, bundled Google Sans]`, so a face is always available.
3. **System** is `FontFamily.Default`.

The gate is `FeatureAvailability.isGoogleFontsProviderAvailable(context)`: the content provider for the authority must resolve and be owned by `com.google.android.gms`, inside a try/catch that answers false. It reads package visibility through `QUERY_ALL_PACKAGES`, which the manifest already declares. Signature mismatches (a ROM that ships a look-alike provider) are only detected when a font is requested and surface as a failed load. When the gate is false the picker collapses the Google Fonts group into one explanatory row and the theme falls back to the default family; bundled families are unaffected.

### Load states and the preload-first invariant

Compose caches a failure (or the 15 s timeout) of an async font that happens *during composition* as permanent for the whole process, and a launcher process lives for a long time. So a downloadable family only enters composition (theme, picker row, preview) after it has been loaded successfully in the same process:

- `FontLoadCoordinator` (pure Kotlin, `data/font/`) tracks `FontLoadState` per `(id, coverage)`: `Idle`, `Loading`, `Ready`, `Failed(reason)`. `Sample` covers the Regular weight only (enough to draw a row in its own face); `Full` covers every declared weight and is what the theme needs. A finished `Full` load also satisfies `Sample`.
- `FontRepository` (`helper/`) runs the loads in an application-lifetime scope through `FontFamily.Resolver.preload`. `preload` throws on failure and caches nothing for it, so a failed attempt stays retryable; a success is cached permanently and shared with the composition resolver. Callers only *await*: a row scrolling away or a screen closing cannot cancel a download, and a cancellation (also one the resolver wraps in another exception) is never reported as `Failed`. Concurrent requests for one family share a single load; a guard of 15 s (Compose's own per-font limit) ends a hung request as `Failed(Timeout)`.
- **One request queue.** androidx.core serves every downloadable-font request of the process from a single thread with an unbounded FIFO queue that cannot be cancelled, and Compose's per-font 15 s timer (and the guard above) start when a request is *submitted*, not when it reaches the front. A burst of passive picker previews would therefore make the font the user tapped wait behind them and time out on a healthy network. So `FontLoadCoordinator` runs `Sample` loads one at a time (`SAMPLE_PARALLELISM`), starts their timeout only once a load holds its turn, and lets `Full` loads bypass that queue: a tap waits behind at most one preview request that is already in flight. A `Full` load of Dongle (two ~4.3 MB files) still delays any other request, including the theme's saved-font load, while it runs; that is the library's queue, not ours.
- `rememberFontResolution` (used by `VoidAppTheme` and by the Settings subtitle) asks for the saved family on every start of the screen and returns the default family until the load is `Ready` (`NotReady`), when the id is unknown (`UnknownId`), or when the provider is missing (`GmsUnavailable`). The provider check (a Binder call) only runs when the saved family is downloadable, so it stays off the first-frame path for every other font. The request therefore runs from the main-process composition and never from `VOIDApp.onCreate`, which also runs in the accessibility process. Because it repeats on each start, a font that failed offline is retried on the next return to the launcher.
- Failures are classified from the provider's `FAIL_REASON_*` codes (`FontFailure`: provider not found, wrong certificates, load error, security violation, not found, unavailable, malformed query) or `Timeout`.

Cold start with a saved downloadable font shows the default family until the load finishes, then the theme swaps once; it is a single reflow per process. Whether Google Play services serves a repeat request offline from its own cache is **not documented** by Google and not verified on a device; the manual checklist lists it as a test.

## Weight policy

The UI asks for Regular (400), Medium (500), Bold (700), and a few `SemiBold` (600) call sites. Each family declares only the weights it really ships *intersected with* 400/500/700, so no family triggers more than three downloads and none ever requests 100/200/300/800/900. A family without Medium (or with 400 only) renders the nearest declared weight; Compose synthesizes bold only when 600+ is requested and the font is lighter. `FontSynthesis` is left at its default on purpose, so the system **Bold text** setting (which adds 300 to every requested weight on API 31+) keeps working for every family. Static downloads cannot reach variable axes (`wdth`, `opsz`); Compose requests name, weight and italic only, which is observed in the `ui-text-google-fonts` source.

## Typography policy

`getTypography(spec)` (`Type.kt`) builds all 15 roles from the same token table and applies two per-family adjustments:

- **Line height**: `lineHeightScale = clamp(naturalLineHeight / 1.30, 1.0, 1.15)` multiplies each token line height, so tall families (Hind 1.60, Poppins 1.50, Jost and Dongle 1.45, Cantarell 1.42, Source Serif 4, Manrope, Open Sans, Nunito, about 1.36) are not clipped. The natural height is the `hhea` ratio of the shipped file (bundled) or the `lineHeight` of the Regular file in the Google Fonts metadata; only families of 1.35 and above record a value, the rest are treated as 1.30 (scale 1.0). The scale is data-derived and should be reviewed on a device: fixed-height rows (home app list, Settings rows) can still clip the tallest families.
- **Letter spacing**: monospaced families (`FontKind.Mono`, currently Inconsolata) drop positive tracking tokens so digits keep stable advances.

## Picker

*Settings > Aesthetics > Launcher Font* opens `FontPicker` (`ui/components/FontPicker.kt`), a full-screen overlay drawn inside the Settings screen rather than a dialog or navigation destination. It stays in the same composition so a preview can later be drawn above it; the Compose Navigation rule in `CLAUDE.md` is applied to screens, and this overlay is part of the Settings screen's own state. While it is open the screen's back-swipe is disabled and the system Back dismisses the picker first.

- Search field and kind chips (All, Sans, Serif, Mono, Display). Matching is a case-insensitive substring of the family name; the same names feed the Settings search.
- Sticky groups: Bundled, Google Fonts, System. Without the provider the Google Fonts group is one info row ("Downloadable fonts need Google Play services...") and is only shown when something in it matches.
- Each row shows the family name in its own face, supporting text (kind, weight names, and either "Works offline", "Follows your device font", or "About N kB download") and a trailing state: progress, refresh (failed, select to retry), check (selected), cloud-download (not downloaded yet). A load in flight or a failure wins over the check, so a selected family that is not applied yet says so. The state comes from the family's *full* load only: the passive preview request never shows progress or an error. Rows are `selectable` with the radio-button role inside one `selectableGroup`, and their state description names both facts ("Selected, Downloaded", "Not selected, Not downloaded yet"). The pane has a title and the top bar and group headers are headings. While the picker is open the Settings content below it is hidden from accessibility and its ancestor focus properties set `canFocus = false` (focus targets nested inside another focus target are not reached by that setting, so keyboard navigation of those is unverified).
- A downloadable row asks for its sample only after staying on screen for 180 ms (a fast fling downloads nothing), only if it is not a large download (over 1 MB; today Dongle), which is requested by tapping, and at most once per picker session, so a failed or slow preview is not retried while scrolling; reopening the picker tries again. Previews are fetched one at a time (see the load-state notes). Bundled and System rows show their own face immediately.
- Tapping a Bundled or System row commits immediately. Tapping a Google Fonts row downloads the full family first and commits `APP_FONT` only when the load is `Ready`; on failure the picker stays open, a Snackbar names the family with advice that fits the cause (connection, Google Play services cannot provide fonts, or the font is no longer available; `FontFailure.advice()`), and the pref is unchanged. Retrying is selecting the row again. The last tap wins if several rows are tapped in a row. These rules live in `FontPickerSession` (`data/font/`, pure Kotlin), not in the composable.
- The list opens scrolled so the selected family is in view and returns to the top when the query or a kind chip changes. The overlay fades in and out with `NavMotion` and the animation-speed setting. The kind chips show a checkmark when selected. The search field is the shared `VoidSearchField`, also used by Settings.

Strings are in `res/values/strings.xml` (`font_*`), dimension tokens in `VoidDimens`.

## Verify the catalog

The Google Fonts and Play services data move independently of this repository (Android's `directory.xml` lags the catalog by months: it lacks Geist, Google Sans, Google Sans Flex, and Google Sans Code). Re-run this from the repository root (the downloads go to a temporary directory, so nothing lands in the working tree) before adding or changing a downloadable family, and whenever the Play services behaviour is in doubt. It checks that every downloadable family exists in the Google Fonts catalog and that both the catalog and `directory.xml` ship exactly the weights the entry declares (`directory.xml` also lists real 600 weights for many families, which the policy deliberately ignores):

```bash
TMP=$(mktemp -d)
curl -sSL -o "$TMP/metadata.json" https://fonts.google.com/metadata/fonts
curl -sSL -o "$TMP/directory.xml" https://fonts.gstatic.com/s/a/directory.xml
TMP="$TMP" python3 - <<'PY'
import json, os, re

TMP = os.environ["TMP"]
CATALOG = "src/main/java/com/knownassurajit/app/launcher/voidlauncher/data/font/FontCatalog.kt"
WEIGHTS = {"ALL_WEIGHTS": [400, 500, 700], "REGULAR_BOLD": [400, 700], "REGULAR_ONLY": [400]}

rows = re.findall(r'google\("([a-z0-9_]+)", "([^"]+)", FontKind\.(\w+), (\w+), (\d+)', open(CATALOG).read())
families = {f["family"]: f for f in json.load(open(f"{TMP}/metadata.json", encoding="utf-8"))["familyMetadataList"]}
directory = open(f"{TMP}/directory.xml", encoding="utf-8").read()

problems = []
for font_id, name, kind, weight_set, _kb in rows:
    declared = WEIGHTS[weight_set]
    family = families.get(name)
    if family is None:
        problems.append(f"{name}: not in the Google Fonts catalog")
        continue
    real = sorted(int(w) for w in family["fonts"] if w.isdigit())
    if [w for w in real if w in (400, 500, 700)] != declared:
        problems.append(f"{name}: catalog declares {declared}, Google Fonts ships {real}")
    block = re.search(rf"<family name='{re.escape(name)}'.*?</family>", directory, re.S)
    if block is None:
        problems.append(f"{name}: missing from directory.xml, Play services cannot serve it")
    else:
        served = sorted({int(w) for w, italic in re.findall(r"weight='(\d+)'.*?italic='([\d.]+)'", block.group(0)) if italic == "0.0"})
        if [w for w in served if w in (400, 500, 700)] != declared:
            problems.append(f"{name}: directory.xml serves {served}, catalog declares {declared}")

print(f"checked {len(rows)} downloadable families")
print("\n".join(problems) or "OK")
PY
rm -rf "$TMP"
```

Last run: 2026-10-07, 29 families, `OK` (catalog and `directory.xml` v28, last modified 2026-03-03). Sizes are not in either file; re-measure them with `curl -sI <font url from directory.xml>` and sum `Content-Length` over the declared weights. `kind` follows the Google Fonts `category` (`Serif`, `Monospace`) and `classifications` (`Display`: Comfortaa, Dongle, Agdasima, Sintony, Alumni Sans, Antonio), plus Fjalla One by hand.

## Known limitations

- A downloadable family that was never downloaded is unavailable offline, and on devices without Google Play services; the first use needs a connection. Whether Play services serves a repeat request offline from its own cache is unverified (manual checklist).
- The 15 s guard is Compose's own per-font timeout, counted from when a request is submitted and not configurable (`Font.MaximumAsyncTimeoutMillis`), so a longer guard in `FontLoadCoordinator` would not extend it; Dongle (two ~4.3 MB files, 2.3 Mbit/s or more) can time out on a slow connection and then shows the failed state until retried. While a full Dongle load runs, other font requests wait behind it in the library's single-threaded queue.
- Google Play services serves static instances of each weight: variable axes such as `wdth` and `opsz` (Inconsolata, Cabin, Open Sans, Roboto, Source Serif 4, Inter) are not reachable, so Source Serif 4 keeps its default optical size on large text.
- Lato from Play services is the 1.x release, not the 2.0 one in the google/fonts repository.
- Android's `directory.xml` is a proxy for what Play services serves; any family can still fail on a given device. The failed state, the retry on the next start, and the default-family fallback contain it.
- The line-height scale is derived from font metrics and has not been reviewed on a device; fixed-height rows can still clip the tallest families (Hind, Poppins, Dongle).
- The OFL texts of the four older bundled static families (Inter, Manrope, Plus Jakarta Sans, DM Sans) are not in `docs/licenses/fonts/` yet; only Google Sans and Geist have theirs. Their copyright notices are kept in each font file's `name` table and credited in the README.

## Geist asset

Geist is not served by Play services (it is in the Google Fonts catalog but missing from `directory.xml`), so it is bundled as one unmodified variable file, `src/main/res/font/geist_variable.ttf`. The `name` table keeps the copyright and licence records (IDs 0, 13, 14); the licence text is in `docs/licenses/fonts/geist-OFL.txt`. No Reserved Font Name is declared.

| Item | Value |
|------|-------|
| Upstream repository | `https://github.com/google/fonts` |
| Upstream path | `ofl/geist/Geist[wght].ttf` |
| Upstream commit used | `718e1db4deb9e4d9d85a0ead1b9f5fde2761ccfd` (2026-04-27) |
| Upstream git blob | `f63f0afc6390323715972b6645115485f37cc9f4` (169,056 bytes) |
| sha256 (identical upstream and shipped) | `73894e0448cae90a92b6c2f8732b7bb9acb7b94c418bff559dad4a18e1de9659` |
| Source project | `https://github.com/vercel/geist-font`, commit `a6d260e6cbc07eafdfad438f33601fe3c38b1e6f`, version 1.800 |
| Copyright | Copyright 2024 The Geist Project Authors (https://github.com/vercel/geist-font) |
| Axis | `wght` 100..900 |

```bash
SHA=718e1db4deb9e4d9d85a0ead1b9f5fde2761ccfd
curl -sSL -o geist_variable.ttf "https://raw.githubusercontent.com/google/fonts/$SHA/ofl/geist/Geist%5Bwght%5D.ttf"
shasum -a 256 geist_variable.ttf   # 73894e04...e1de9659
```

`BundledFontFilesTest` pins the sha256, so the file cannot be edited or replaced by accident; update the table and the test together when taking a new upstream revision. Known limitation: the system **Bold text** setting adds to the variation value as well as to the requested weight (read from the Compose sources, not device-verified), so Geist's 100..900 axis can render heavier than Bold with that setting on; Google Sans is capped at 700 and is not affected.

## Google Sans asset

Google Sans is licensed under the SIL Open Font License 1.1 with no Reserved Font Name (`ofl/googlesans/OFL.txt` upstream; `TRADEMARKS.md` only restricts use of the "Google" mark). The licence text ships in `docs/licenses/fonts/google-sans-OFL.txt`. VOID is not affiliated with or endorsed by Google.

`google_sans_variable.ttf` is a modified version of the upstream variable font:

- the `GRAD` axis is pinned to 0 and the `opsz` axis to 18 (the upstream defaults);
- the `wght` axis (400..700) is kept; `FontFamilies.kt` declares `FontWeight.Normal/Medium/Bold` as `Font(R.font.google_sans_variable, weight, variationSettings = FontVariation.Settings(FontVariation.weight(w)))` (needs API 26, the project minSdk);
- glyphs are subset to Latin, Latin Extended-A/B, IPA/spacing letters, Cyrillic, Latin Extended Additional, general punctuation, currency symbols, letterlike symbols, four arrows, and the minus/division-slash signs (`U+0000-00FF,0100-02AF,0400-04FF,1E00-1EFF,2000-206F,20A0-20CF,2100-2122,2190-2193,2212,2215`);
- the default layout features plus `tnum` are kept (`kern`, `liga`, `calt`, `locl`, `frac`, `tnum`, ...); stylistic sets, small caps, and similar are dropped to keep the asset small (241 KB versus about 380 KB with all features). `tnum` is kept on purpose: Google Sans digits are proportional by default (the `1` is about 1/3 narrower than the `0`), so the home clock, date, and screen-time text request fixed-width digits through `TextStyle.withTabularFigures()` (see Tabular figures below);
- the `name` table (copyright and licence records) is kept so the OFL notice stays in the binary.

### Provenance

| Item | Value |
|------|-------|
| Upstream repository | `https://github.com/google/fonts` |
| Upstream path | `ofl/googlesans/GoogleSans[GRAD,opsz,wght].ttf` |
| Upstream commit used | `a0e3dbcdc3a3ecfafff3f071159ae0221628922d` (2026-09-24) |
| Upstream git blob | `a5e76cc713206f1cfe9a7fc31cc1b6f035034d81` (4,974,940 bytes) |
| Upstream sha256 | `d0a87d835a944b8b40d0e82a5651bb59ab97b936a2aeed5946eb57e7b2a3a90a` |
| Upstream OFL copyright | Copyright 2025 The Google Sans Project Authors (https://github.com/googlefonts/googlesans) |
| Shipped asset | `src/main/res/font/google_sans_variable.ttf` (240,872 bytes) |
| Shipped sha256 | `5bd395456fb10cc18368630d486b79e3c1bd944767903f027d569b269052c2fc` |
| Tooling | fontTools 4.66.1 (brotli 1.2.0 installed alongside, unused for TTF output) |

### Recipe (reproducible)

Run from any scratch directory (not inside the repo). Setting `SOURCE_DATE_EPOCH` fixes the `head.modified` timestamp so the output hash is stable.

```bash
python3 -m venv venv
./venv/bin/pip install fonttools==4.66.1 brotli==1.2.0

SHA=a0e3dbcdc3a3ecfafff3f071159ae0221628922d
curl -sSL -o GoogleSans-var.ttf \
  "https://raw.githubusercontent.com/google/fonts/$SHA/ofl/googlesans/GoogleSans%5BGRAD%2Copsz%2Cwght%5D.ttf"
shasum -a 256 GoogleSans-var.ttf   # d0a87d83...a3a90a

export SOURCE_DATE_EPOCH=1789131940   # 2026-09-11T13:05:40Z, date of the upstream commit that last changed the font file (5412d702)

./venv/bin/fonttools varLib.instancer GoogleSans-var.ttf GRAD=0 opsz=18 -o pinned.ttf

./venv/bin/pyftsubset pinned.ttf \
  --unicodes='U+0000-00FF,U+0100-02AF,U+0400-04FF,U+1E00-1EFF,U+2000-206F,U+20A0-20CF,U+2100-2122,U+2190-2193,U+2212,U+2215' \
  --layout-features+=tnum \
  --name-IDs='*' --name-legacy --notdef-outline \
  --output-file=google_sans_variable.ttf

shasum -a 256 google_sans_variable.ttf   # 5bd39545...9052c2fc
```

Re-verify the licence before updating the asset: `ofl/googlesans/OFL.txt` must still carry no "Reserved Font Name" declaration after the copyright line.

### Tabular figures

Default Google Sans digits are proportional (at `wght` 400 the advances run from 430 for `1` to 643 for `0`, out of 1000 units per em), so a clock such as `11:11` is visibly narrower than `00:00`. The subset keeps the font's `tnum` feature (at every weight each digit is 600 units wide, and `:` `.` `,` and the space are 300), and `withTabularFigures()` in `Type.kt` sets `fontFeatureSettings = "tnum"` on the text that changes while it is visible: the home clock, date, and screen-time lines in `HomeScreen.kt`. Other bundled families apply the request when they have a `tnum` feature and ignore it otherwise. Any new live-updating numeric `Text` should use the same helper.

### Behavior notes

- The wght axis maximum is 700, so the system "Bold text" accessibility setting (which adds to the requested weight on API 31+) should clamp at Bold rather than render Black. This follows from reading the Compose sources and is not yet device-verified.
- The previous `google_sans_{regular,medium,bold}.ttf` files were HTML placeholders, so the default font had never loaded; see `docs/bugs/google-sans-html-assets.md`.

## Font-provider certificates

`src/main/res/values/font_certs.xml` holds the Google Play services font-provider signing certificates used by downloadable fonts (`GoogleFont.Provider`, `com.google.android.gms.fonts`). It is copied verbatim (Apache-2.0 header included) from `android/compose-samples`, `Jetchat/app/src/main/res/values-v23/font_certs.xml` (last upstream change: commit `7e44d8739bebb833a92e9d422fd32dd38bd371cc`, 2022-05-03). Shipped sha256: `b0f4def17a8e9a915548633353ab495bc6e85347dda05d13c9365827c10d0067`.

| Array | Certificate SHA-256 |
|-------|---------------------|
| `com_google_android_gms_fonts_certs_dev` | `1975B2F17177BC89A5DFF31F9E64A6CAE281A53DC1D1D59B1D147FE1C82AFA00` |
| `com_google_android_gms_fonts_certs_prod` | `F0FD6C5B410F25CB25C3B53346C8972FAE30F8EE7411DF910480AD6B2D60DB83` |

Do not edit these values by hand; replace the file wholesale from the upstream source and update the fingerprints in `FontCertsTest`.

## Tests

- `BundledFontFilesTest` — every file under `res/font` has a valid sfnt header; every bundled catalog family is backed by existing files; the default Google Sans asset exists, its `fvar` table has exactly one axis, `wght` (so `GRAD`/`opsz` stay pinned), the axis covers the weights `GoogleSansFamily` declares, and its GSUB table keeps a non-empty `tnum` feature; the Geist asset is byte-identical to upstream (sha256 pin) with a single `wght` axis covering 400..700.
- `GoogleSansFamilyTest` — `GoogleSansFamily` declares Normal/Medium/Bold, each backed by `google_sans_variable`, and each sets exactly the `wght` axis to its own weight (without that, Medium and Bold would render as Regular).
- `TabularFiguresTest` — `withTabularFigures()` requests `tnum` and leaves the rest of the style untouched.
- `FontCertsTest` — both certificate arrays decode as strict base64, parse as X.509, and match the fingerprints above.
- `FontCatalogTest` — unique snake-case ids and names, the default is bundled, legacy ids and sources are frozen, every requested family is present and the six unlicensed names are absent, downloadable entries have a Google Fonts name and only 400/500/700 weights including 400, `lineHeightScale` stays within 1.0..1.15, mono drops positive tracking, `resolve` falls back to the default for unknown ids, a missing provider, and a font that is not ready (never rewriting the pref), `needsProvider` (only downloadable ids), and `displayedId`.
- `FontPickerFilterTest` — group order, case-insensitive trimmed search, kind filter, empty results, the collapsed Google Fonts group without the provider, and the list index of a row.
- `FontLoadCoordinatorTest` / `FontLoadStateTest` — single flight, `Loading` to `Ready`, failure classification and retry, timeout, cancellation (also one wrapped by the resolver) never recorded as a failure, `Full` covering `Sample`, bundled fonts needing no load, samples running one at a time while a full load does not wait for them and the timeout counting only from a load's turn, the row state derived from the full load only, failure advice, and the Settings subtitle availability.
- `FontPickerSessionTest` — commit only after `Ready`, a failure never commits, last selection wins, and once-per-session previews that skip large and non-downloadable fonts.
- `FontFamiliesTest` — bundled families match the catalog weights, downloadable families declare only catalog weights with the bundled Google Sans file behind every download and a Regular-only sample, and families are memoised (the preloaded fonts are the composed fonts).
- `TypographyPolicyTest` — one family for all 15 roles, the default family being the full family the loader preloads, baseline line heights unchanged, tall families scaled, mono tracking, no font synthesis override.
- `GoogleFontsProviderTest` — only Google Play services may own the font provider authority, and the availability check is false for no provider, a look-alike owner, and a throwing lookup.

Emulator-only checks (never on a physical device) are in `docs/testing/manual-checklist.md`: the provider cert check with `GoogleFont.Provider.isAvailableOnDevice`, first-use failure in airplane mode, offline repeat use after one download, the no-Play-services path, and Bold text.
