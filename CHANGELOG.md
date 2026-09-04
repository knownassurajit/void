# Changelog

## Unreleased

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
