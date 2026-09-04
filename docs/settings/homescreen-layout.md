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
| App font | `APP_FONT` | Bundled families via `VoidAppTheme` (default `google_sans`) |
| Animation speed | `ANIMATION_SPEED` | `fast` / `standard` / `slow` |
| Swipe to go back | `CONTENT_SWIPE_TO_BACK` | Child screens swipe opposite the enter axis |
| Private Space placement | `PRIVATE_SPACE_PLACEMENT` | `bottom` or `search_bar` (API 35+) |

## Responsive reflow

When a section is disabled, remaining enabled sections take full height. When both are on, each section is allocated a clamped weight. Overflow is clipped so vertical swipe-up can still open the app library.
