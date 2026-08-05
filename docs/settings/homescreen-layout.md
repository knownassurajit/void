# Homescreen layout settings

## Controls

| Setting | Pref key | Effect |
|---------|----------|--------|
| Show clock / date / screen time | `SHOW_*_WIDGET` | Toggle components |
| Show home apps | `SHOW_HOME_APPS` | Hide/show app list section |
| Clock size | `CLOCK_SIZE_SCALE` | Font scale for clock |
| Clock section weight | `CLOCK_SECTION_WEIGHT` | Vertical weight vs apps |
| Section order | `HOME_SECTION_ORDER` | `clock_first` or `apps_first` |
| App / clock alignment | gravity prefs | H/V gravity |
| App spacing | `APP_SPACING_DP` | Spacing between labels |
| Text scales | home/drawer scales | Typography |

## Responsive reflow

When a section is disabled, remaining enabled sections redistribute weight so the layout does not leave empty voids.
