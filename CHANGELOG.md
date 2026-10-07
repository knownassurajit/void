# Changelog

## Unreleased

### Fixed
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
