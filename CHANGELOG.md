# Changelog

## Unreleased

### Added
- 30 more launcher fonts and a searchable picker (search, Sans/Serif/Mono/Display filters, Bundled / Google Fonts / System groups): Geist is bundled (unmodified OFL variable font, `docs/licenses/fonts/geist-OFL.txt`) and 29 Google Fonts families (Poppins, Work Sans, Karla, Dongle, Raleway, Inconsolata, Comfortaa, Antic, Actor, Agdasima, Hanken Grotesk, Basic, Cabin, Cantarell, Hind, Jost, Krub, Lato, Nunito, Epilogue, Open Sans, Sintony, Fjalla One, Rethink Sans, Alumni Sans, Sofia Sans, Source Serif 4, Antonio, Roboto) download on demand through Google Play services, gated by `FeatureAvailability.isGoogleFontsProviderAvailable`
- A downloadable font is only applied (and the pref only saved) after its download succeeded, a failure keeps the picker open with a message that fits the cause (connection, Play services, font gone), and the saved font is retried on the next start of the launcher; the picker shows each family's weight names and approximate download size (Dongle is about 8.9 MB)
- The whole app retypesets with the chosen family: per-family line-height scale for tall faces and no positive letter spacing for monospaced ones (`docs/features/fonts.md`)
- Brogman, Volkga, Disttopian, Stanza, Vangura Sans, and Noria are commercial or personal-use fonts and are not offered; `docs/features/fonts.md` lists the reasons and Google Fonts alternatives

### Changed
- Screen transitions use Material 3 standard spring physics (spatial and effects springs scaled by the animation-speed setting) instead of 150/250/400 ms tweens; a panel from the left, right, top or bottom edge slides in with no fade while Home moves the same way by a parallax distance (30% horizontally, 20% vertically) and dims to the scrim opacity; closing plays the same motion in reverse; Settings scales and fades with the screen under it (shared axis Z) and the font picker overlay scales and fades in place
- Every screen except Home sits on an opaque black panel surface, so the screen under a sliding panel no longer shows through (`NavPanel`)
- Navigation matches type-safe routes with `hasRoute` instead of route-name strings, and opening Settings from the drawer no longer stacks a second Settings screen
- Settings and the font picker share one `VoidSearchField`; the picker opens scrolled to the selected font, scales and fades in and out with the animation-speed setting, and is announced and navigable by TalkBack (pane title, headings, one radio group)
- The Launcher Font subtitle says why a saved downloadable font is not in use yet (loading, not downloaded, or needs Google Play services)
- `googleFonts` version in `libs.versions.toml` aligned to 1.11.2, the version that already resolved through the Compose BOM
- Privacy policy discloses that downloadable fonts are fetched by Google Play services

### Fixed
- Dismissing a panel a second time while it is still closing no longer pops Home and blanks the launcher: the springs keep the closing panel on screen for about half a second, so a repeated swipe or back tap is now ignored once only Home is left (`MainActivity.popPanel`)
- Home no longer slides against the incoming panel: the screen under a panel used to exit toward the edge the panel came in from, so the two layers crossed and Home visibly glitched; it now moves the same way as the panel (`docs/architecture/motion.md`)
- Default font now actually loads: the Google Sans assets were HTML placeholder files (the platform font was used silently); VOID now ships a real OFL Google Sans variable subset and unit tests reject non-font files in `res/font` (`docs/bugs/google-sans-html-assets.md`)
- Google Fonts provider certificates (`font_certs.xml`) replaced with the official valid values and pinned by a unit test, so downloadable fonts can verify the Google Play services provider
- Home clock, date, and screen-time text use tabular figures, so Google Sans' proportional digits no longer make the clock change width every minute or second (the bundled font keeps its `tnum` feature)
- Removed the unused legacy `Text*` XML text styles and the window `android:fontFamily` item that referenced the broken assets
- Private Space settings icon (bottom header and search-bar placements) shows only when the space is unlocked and a settings entry is available; it no longer disappears while searching
- Private Space locked/unlocked state now comes from the system's quiet mode instead of the lock/unlock request: an unlock that needs credentials (or is cancelled) no longer shows "unlocked", the state refreshes on the profile broadcasts and on resume, and a refused request shows a message
- Private Space lock/unlock request runs off the main thread and the receiver is only registered (and unregistered) when the feature is available and enabled

## 0.0.0.11 - 2026-09-04

### Fixed
- Home apps no longer collapse to 4 when clock/date/layout prefs emit: `HomeAppsCap` is the only cap resolver (`docs/bugs/home-apps-cap.md`)
- App drawer swipe-up crash from unguarded `FocusRequester` during enter transition
- Home app reorder cascade (package-keyed multi-slot shifts) and compile break in height tracking
- Parent swipe gesture no longer cancels in-progress home reorder

### Changed
- Navigation enter/exit/pop share the same swipe axis (`NavMotion`); Settings fades
- Settings uses one `Prefs` instance (`LocalSettingsPrefs`) and grouped searchable sections
- Root pointer interceptor is limited to the status-bar notification-expand path
- Single-variant OSS build; removed stale integrated/disintegrated CI/docs paths
- `FeatureAvailability` is runtime capability-based (not Play SKU stubs)
- Widgets screen: configure activity flow, resize, reorder, label toggle
- Homescreen settings: clock size scale, section weight, section order, show home apps with reflow

### Added
- Bundled Plus Jakarta Sans, Manrope, and DM Sans; default font is Google Sans
- Private Space placement: bottom list or drawer search bar
- Animation speed and swipe-to-go-back prefs
- Shared Compose components and motion/spacing tokens
- Unit tests for `HomeAppsCap`, `NavMotion`, reorder helper, and feature gates
- Instrumentation smoke tests including clock-toggle home-apps persistence
- Documentation under `docs/` (architecture, features, settings, testing, bugs)
