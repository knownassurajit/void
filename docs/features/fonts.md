# Fonts

VOID retypesets the whole app through `VoidAppTheme(appFont = ...)` (`ui/theme/Type.kt`). The pref `APP_FONT` stores a stable family id; the default id is `google_sans`.

## Bundled families

| Id | Family | Files in `res/font/` | Notes |
|----|--------|----------------------|-------|
| `google_sans` (default) | Google Sans | `google_sans_variable.ttf` | One variable file, weights 400/500/700 (see below) |
| `inter` | Inter | `inter_{regular,medium,bold}.ttf` | Static |
| `plus_jakarta` | Plus Jakarta Sans | `plus_jakarta_sans_{regular,medium,bold}.ttf` | Static |
| `manrope` | Manrope | `manrope_{regular,medium,bold}.ttf` | Static |
| `dm_sans` | DM Sans | `dm_sans_{regular,medium,bold}.ttf` | Static |
| `system` | System | — | `FontFamily.Default` |

Stream F2 extends this document with the full font catalog, weight policy, and picker behavior.

## Google Sans asset

Google Sans is licensed under the SIL Open Font License 1.1 with no Reserved Font Name (`ofl/googlesans/OFL.txt` upstream; `TRADEMARKS.md` only restricts use of the "Google" mark). The licence text ships in `docs/licenses/fonts/google-sans-OFL.txt`. VOID is not affiliated with or endorsed by Google.

`google_sans_variable.ttf` is a modified version of the upstream variable font:

- the `GRAD` axis is pinned to 0 and the `opsz` axis to 18 (the upstream defaults);
- the `wght` axis (400..700) is kept; `Type.kt` declares `FontWeight.Normal/Medium/Bold` as `Font(R.font.google_sans_variable, weight, variationSettings = FontVariation.Settings(FontVariation.weight(w)))` (needs API 26, the project minSdk);
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

- `BundledFontFilesTest` — every file under `res/font` has a valid sfnt header; the default Google Sans asset exists, its `fvar` table has exactly one axis, `wght` (so `GRAD`/`opsz` stay pinned), the axis covers the weights `GoogleSansFamily` declares, and its GSUB table keeps a non-empty `tnum` feature.
- `GoogleSansFamilyTest` — `GoogleSansFamily` declares Normal/Medium/Bold, each backed by `google_sans_variable`, and each sets exactly the `wght` axis to its own weight (without that, Medium and Bold would render as Regular).
- `TabularFiguresTest` — `withTabularFigures()` requests `tnum` and leaves the rest of the style untouched.
- `FontCertsTest` — both certificate arrays decode as strict base64, parse as X.509, and match the fingerprints above.
