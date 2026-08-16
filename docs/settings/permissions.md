# Permissions

VOID ships one OSS variant. Features degrade when a grant is missing.

| Permission | Kind | Feature |
|------------|------|---------|
| `QUERY_ALL_PACKAGES` | Install | App library, home picker |
| `REQUEST_DELETE_PACKAGES` | Install | Uninstall from the drawer |
| `SET_ALARM` | Install | Open the system clock |
| `PACKAGE_USAGE_STATS` | Special | Home screen time |
| `POST_NOTIFICATIONS` | Runtime (API 33+) | Note reminders |
| `BIND_APPWIDGET` | Special / grant UI | Pin live widgets |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Special | In-app notification list and summary |
| `BIND_ACCESSIBILITY_SERVICE` | Special | Optional double-tap to lock |
| `BIND_DEVICE_ADMIN` | Special | Lock via device admin fallback |
| `ACCESS_HIDDEN_PROFILES`, `MODIFY_QUIET_MODE`, `INTERACT_ACROSS_PROFILES`, `MANAGE_USERS` | Special (API 35+) | Private Space |

Settings → Permissions deep-links notification listener, usage access, accessibility, and default-home screens.
