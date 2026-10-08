# Changelog

## Unreleased

### Added
- 30 more launcher fonts and a searchable picker (search, Sans/Serif/Mono/Display filters, Bundled / Google Fonts / System groups): Geist is bundled (unmodified OFL variable font, `docs/licenses/fonts/geist-OFL.txt`) and 29 Google Fonts families (Poppins, Work Sans, Karla, Dongle, Raleway, Inconsolata, Comfortaa, Antic, Actor, Agdasima, Hanken Grotesk, Basic, Cabin, Cantarell, Hind, Jost, Krub, Lato, Nunito, Epilogue, Open Sans, Sintony, Fjalla One, Rethink Sans, Alumni Sans, Sofia Sans, Source Serif 4, Antonio, Roboto) download on demand through Google Play services, gated by `FeatureAvailability.isGoogleFontsProviderAvailable`
- A downloadable font is only applied (and the pref only saved) after its download succeeded, a failure keeps the picker open with a message that fits the cause (connection, Play services, font gone), and the saved font is retried on the next start of the launcher; the picker shows each family's weight names and approximate download size (Dongle is about 8.9 MB)
- The whole app retypesets with the chosen family: per-family line-height scale for tall faces and no positive letter spacing for monospaced ones (`docs/features/fonts.md`)
- Brogman, Volkga, Disttopian, Stanza, Vangura Sans, and Noria are commercial or personal-use fonts and are not offered; `docs/features/fonts.md` lists the reasons and Google Fonts alternatives
- Home swipes follow the finger: while a swipe toward a configured action is in progress, Home moves with the finger (a rubber band, never more than 64 dp) in all four directions, and the swipe is decided on release (56 dp of travel or a 125 dp/s flick, the Material 3 swipe-to-dismiss limits), so moving the finger back under the distance cancels it; Home then springs back with the speed of the finger, or, when the action runs, settles on the same spring the panel transition moves it on (spatial slow), so the two add up to one movement and Home never pulls back against the finger; a new touch while Home is still springing back picks the spring up where it is instead of snapping it to rest. A sideways swipe that opens the notification list does not pull Home sideways, because the list drops from the top (`HomeSwipeRecognizer`, `Modifier.homeSwipe` in `ui/gesture`, `docs/architecture/motion.md`)
- Haptic cues on a Home swipe: a tick when the travel reaches the commit distance (re-armed only after it drops under 80%), a confirm when the action runs and a reject when it cannot (an app that no longer launches)
- Swipes that start at a screen edge: while Home is the screen on top, two full-height strips along the left and right edges (only for an edge that has an action) ask the system to leave them to the app (`systemGestureExclusion`, width `max(system gesture inset, 30 dp) + 8 dp`), so the edge swipe is an ordinary swipe; the system Back gesture is untouched on every other screen

### Changed
- Screen transitions use Material 3 standard spring physics (spatial and effects springs scaled by the animation-speed setting) instead of 150/250/400 ms tweens; a panel from the left, right, top or bottom edge slides in with no fade while Home moves the same way by a parallax distance (30% horizontally, 20% vertically) and dims to the scrim opacity; closing plays the same motion in reverse; Settings scales and fades with the screen under it (shared axis Z) and the font picker overlay scales and fades in place
- The system back gesture (Android 14+) now plays the Material predictive-back animation on every screen: the leaving screen shrinks to 90% and moves a little the way the finger moves while the screen under it grows from 110%; no screen registers its own back handler to leave itself any more (`ChildScreenBackHandler` is gone), so `NavHost`'s seekable pop receives the gesture. On Android 14+ a Back key press that lasts longer than a frame starts the same predictive back, because the platform does; Home still refreshes after Settings on every way back (a route change observed in `MainActivity`)
- The swipe inside a screen that closes it is measured in dp and uses velocity: it commits after 56 dp or a 125 dp/s flick (the Material 3 swipe-to-dismiss limits), a flick back at that speed cancels it, it locks its axis as soon as the drag is long enough, counts the travel before the touch slop and does nothing when cancelled; Notes on the left swipe now closes with a swipe left like any left panel (a note row still wins the drag), so on that side a note is deleted with a swipe right instead of left and closing Notes can never delete a note; TalkBack gets a Dismiss action on every panel
- Every screen except Home sits on an opaque black panel surface, so the screen under a sliding panel no longer shows through (`NavPanel`)
- Navigation matches type-safe routes with `hasRoute` instead of route-name strings, and opening Settings from the drawer no longer stacks a second Settings screen
- Home swipes are measured in dp and decided on release instead of a 120 px distance chosen by the longer axis at lift-off: the axis locks as soon as the drag is long enough (counting the travel before the touch slop), a diagonal goes to its longer axis, a long press still belongs to the reorder and the app picker, and a swipe never starts while Home is not the current screen or a transition still moves it. A direction with no action, a disabled feature or the not yet wired `Accessibility` action moves nothing and opens nothing, but the drag is still consumed (as it was), so it neither clicks nor starts a reorder on the home app under the finger, and the same goes for a drag while a transition still moves Home
- An app set as a swipe action is launched through `MainViewModel` (`LauncherApps.startMainActivity`, with the stale-component fallbacks and a message when the app is gone) instead of `startActivity` in `HomeScreen`, and on Android 11 and earlier its window animation follows the swipe: it slides in from the edge the swipe brings content in from (left swipe action from the left, right from the right) while Home leaves the same way by a shorter distance and dimmed; the `res/anim` slides are retuned to 300 ms on the Material 3 emphasized-decelerate curve with Home's 30% / 20% parallax and 68% dimming (the system plays them, so the animation-speed setting does not scale them). Android 12 and later ignore a custom animation for a launch that opens another app's task, so there the system default plays and no options are passed (`LaunchAnimation.playsOn`); the app opens in its primary launcher activity, as before. `launchApp` takes a `PanelEdge` instead of the old finger-direction strings, whose labels were the inverse of the edge naming
- Settings and the font picker share one `VoidSearchField`; the picker opens scrolled to the selected font, scales and fades in and out with the animation-speed setting, and is announced and navigable by TalkBack (pane title, headings, one radio group)
- The Launcher Font subtitle says why a saved downloadable font is not in use yet (loading, not downloaded, or needs Google Play services)
- `googleFonts` version in `libs.versions.toml` aligned to 1.11.2, the version that already resolved through the Compose BOM
- Privacy policy discloses that downloadable fonts are fetched by Google Play services

### Fixed
- Horizontal swipes that start within 24 dp of a screen edge are no longer dropped: the old guard ignored them in every navigation mode, so a swipe that literally starts at the edge (the natural way to swipe from the right edge) never opened anything; in gesture navigation the edge strips now ask the system for them
- On Android 11 and earlier, opening an app with a swipe no longer plays Android's default launch animation: it follows the direction of the swipe like the panels do. On Android 12 and later the system still plays its own (see above)
- Opening Widgets again while it was still closing no longer silences the widgets: the closing screen's `AppWidgetHost.stopListening` ran after the new screen had started listening on the same host id; listening is now reference counted (`ListeningLease`)
- Dismissing a panel a second time while it is still closing no longer pops Home and blanks the launcher, or the screen under it (Settings over the drawer went on to Home): the springs keep the closing panel on screen for about half a second, so a dismiss from a panel that is no longer on top is now ignored (`MainActivity.popPanel`, `shouldPopPanel`)
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
