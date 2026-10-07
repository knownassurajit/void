# Google Sans never loaded (HTML placeholder assets) and corrupted font-provider certs

## Symptom

- The default app font ("Google Sans") rendered as the platform default font on every device; nothing in the UI, logs, or lint reported a problem.
- Downloadable fonts (Google Fonts through Google Play services) could not have worked: the font-provider certificate arrays were not valid base64 / X.509.

## Root cause

1. `src/main/res/font/google_sans_{regular,medium,bold}.ttf` were GitHub "Page not found" HTML documents (about 300 KB each, committed in `d4b9450`), not font files. About 900 KB of HTML shipped in every APK.
2. Compose catches the font-load failure of a blocking resource font and falls back to the platform default typeface, so the broken asset was masked (most visibly on Pixel devices, whose system font is already Google Sans). No test inspected the files.
3. `src/main/res/values/font_certs.xml` differed from the official Google Play services font-provider certificates (`android/compose-samples`, `Jetchat/app/src/main/res/values-v23/font_certs.xml`): the prod item was not decodable base64 and the dev item was a different, invalid certificate. The font provider rejects the app when the arrays do not parse or match, so every downloadable font would fail.
4. `styles.xml` referenced `@font/google_sans_regular` / `@font/google_sans_medium` nine times (the `AppTheme` window `android:fontFamily` and eight `Text*` / `AppSearchText` styles). None of those text styles had a consumer.

## Fix

- Removed the three HTML assets and `res/font/google_sans.xml`.
- Added one real asset, `res/font/google_sans_variable.ttf` (SIL OFL 1.1): upstream variable Google Sans with `GRAD` and `opsz` pinned and the `wght` axis kept, subset to Latin, Latin Extended-A/B, Cyrillic and common punctuation/currency. `Type.kt` declares weights 400/500/700 from that single file with `FontVariation.weight`. Provenance, hashes, and the exact recipe: `docs/features/fonts.md`. OFL text: `docs/licenses/fonts/google-sans-OFL.txt`.
- Replaced `font_certs.xml` verbatim with the official file (Apache-2.0 header kept).
- Deleted the unused text styles, their now-orphaned `text_colors_default` selector, and the `AppTheme` `android:fontFamily` item instead of repointing them.

## Regression

Unit (JVM, no emulator):

- `BundledFontFilesTest` — every `.ttf`/`.otf`/`.ttc` under `res/font` starts with a valid sfnt magic (this test fails on the old HTML files); `google_sans_variable.ttf` must exist.
- `GoogleSansFamilyTest` — the family declares Normal/Medium/Bold from the variable asset and each weight sets its own `wght` axis value (the stable `Font(resId, weight)` overload passes none, which would render every weight as Regular).
- `BundledFontFilesTest` also checks that the asset keeps its `tnum` feature, which `HomeScreen` uses through `TextStyle.withTabularFigures()` because Google Sans digits are proportional by default.
- `FontCertsTest` — both cert arrays are strict base64, parse as X.509, and match the pinned SHA-256 fingerprints (dev and prod).

Manual: see `docs/testing/manual-checklist.md` (default font renders as Google Sans on a non-Pixel device or emulator).
