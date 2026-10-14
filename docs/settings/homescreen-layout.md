# Homescreen layout settings

## Controls

| Setting | Pref key | Effect |
|---------|----------|--------|
| Show clock / date / screen time | `SHOW_*_WIDGET` | Toggle components |
| Show home apps | `SHOW_HOME_APPS` | Hide/show app list section |
| Clock size | `CLOCK_SIZE_SCALE` | Time font only |
| Date size | `DATE_SIZE_SCALE` | Date font only |
| Screen time size | `SCREEN_TIME_SIZE_SCALE` | Screen-time font only |
| Clock section weight | `CLOCK_SECTION_WEIGHT` | Vertical weight vs apps (clamped 22–48%) |
| Section order | `HOME_SECTION_ORDER` | `clock_first` or `apps_first` |
| App / clock alignment | gravity prefs | H/V gravity |
| App spacing | `APP_SPACING_DP` | Spacing between labels |
| Text scales | home/drawer scales | Typography |
| Max home apps | `MAX_HOME_APPS` (+ legacy `HOME_APPS_NUM`) | Unified by `HomeAppsCap`; missing keys use filled slots then 10 |
| App font | `APP_FONT` | Catalog id (`FontCatalog`): bundled, Google Fonts (downloaded by Play services), or `system`; default `google_sans`. An unknown id or a downloadable family that is unavailable renders with the default and the pref is not rewritten. See `docs/features/fonts.md` |
| Enable gestures, left / right swipe action, swipe-down notifications | `ENABLE_GESTURES`, `LEFT_SWIPE_ACTION`, `RIGHT_SWIPE_ACTION`, `ENABLE_SWIPE_DOWN_NOTIFICATIONS` | Swipes on Home. The finger and the next screen travel together: a swipe that moves right brings the left action in from the left edge, a swipe that moves left the right action from the right edge, swipe up the drawer from the bottom, swipe down the notification list from the top; Home follows the finger while it is down and the action opens on release after 56 dp or a flick of 125 dp/s (moving back under the distance cancels). An action that is an app launches the instant you let go: on Android 11 and earlier it slides in from the same side; on Android 12 and later the system plays its own launch animation (a launcher cannot choose its direction), and Home carries the swipe on, pushed aside and dimmed, until VOID is resumed. A direction with no action, a disabled feature or `Accessibility` (not wired yet) opens nothing and does not move Home; the drag is still Home's, so it does not click or reorder the home app under the finger. Swipes that start at the screen edge work too (see `docs/architecture/motion.md`, Home swipes) |
| Animation speed | `ANIMATION_SPEED` | `fast` / `standard` / `slow` (scales the panel springs, see `docs/architecture/motion.md`) |
| Swipe to go back | `CONTENT_SWIPE_TO_BACK` | Child screens close with a swipe toward the edge they came from (drawer down, notification list up, left/right panels toward their side, Settings down) after 56 dp of travel or a flick of 125 dp/s (a flick back cancels); a child that consumes the drag first (a list, a swipe-to-delete note row) wins, and a note row is deleted with the swipe opposite to the one that closes Notes. Only the swipe inside a screen depends on this setting: the system back gesture (predictive on Android 14+), the Back key and TalkBack always work |
| Private Space placement | `PRIVATE_SPACE_PLACEMENT` | `bottom` or `search_bar` (API 35+) |

## Responsive reflow

When a section is disabled, remaining enabled sections take full height. When both are on, each section is allocated a clamped weight. Overflow is clipped so vertical swipe-up can still open the app library.
