# Homescreen layout settings

## Controls

| Setting | Pref key | Effect |
|---------|----------|--------|
| Show clock / date | `SHOW_CLOCK_WIDGET`, `SHOW_DATE_WIDGET` | Toggle components |
| Screen time: show on Home | `SHOW_SCREEN_TIME_WIDGET` | Today's total under the clock. Turning it on opens Usage Access when it is not granted. While it is off nothing is queried |
| Screen time: show in app library | `SHOW_APP_SCREEN_TIME` (default on) | Today's usage next to each app of the app library, in the form `1h 5m`, `12m` or `<1m`; apps without usage today show nothing. Needs the Home switch on and usage access; the row is disabled while the Home switch is off. Pinned shortcuts and apps of another profile (Private Space, work) show nothing, because their usage is not in this launcher's figures |
| Screen time: usage access | none (system) | Shows whether usage access is granted and opens the system screen; read again each time Settings resumes |
| Show home apps | `SHOW_HOME_APPS` | Hide/show app list section |
| Clock size | `CLOCK_SIZE_SCALE` | Time font only |
| Date size | `DATE_SIZE_SCALE` | Date font only |
| Screen time size | `SCREEN_TIME_SIZE_SCALE` | Screen-time font only (in the Screen time section) |
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

## Text style and margins

| Setting | Key | Range / values | Default |
| --- | --- | --- | --- |
| Clock font weight | `CLOCK_FONT_WEIGHT` | 400, 500, 700 | 700 |
| Clock text color | `CLOCK_COLOR` | default, primary, secondary, tertiary, muted | default |
| Clock opacity | `CLOCK_OPACITY` | 0.2 – 1.0 | 1.0 |
| Home apps font weight | `HOME_APPS_FONT_WEIGHT` | 400, 500, 700 | 400 |
| Home apps text color | `HOME_APPS_COLOR` | as clock | default |
| Home apps opacity | `HOME_APPS_OPACITY` | 0.2 – 1.0 | 1.0 |
| Side margins | `HOME_SIDE_PADDING_DP` | 8 – 48 dp | 20 dp |

Colors are theme roles, so they follow dark mode and dynamic color. Home and the Settings previews draw from the same `HomeStyle` styles and spacing math; the Home screen group starts with a live scaled preview.
