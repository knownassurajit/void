# Data flow

```text
UI (Compose) → ViewModel → StateFlow → Repository/Prefs/Helpers → System
```

## Rules

- Unidirectional flow only.
- Never mutate UI state inside composables (except ephemeral gesture/local UI state).
- Business logic lives in ViewModels/helpers.
- Prefer `StateFlow` for reactive UI.
- SharedPreferences access only through `Prefs`.

## Key types

- `MainUiState` / `MainUiViewModel` — home clock, apps, layout prefs
- `Prefs` / `HomescreenPreferences` — persistence + reactive homescreen stream
- `AppCacheManager` — drawer app list cache, published already ordered by label (the drawer does not sort it again)
- `ScreenTimeSource` / `ScreenTimeMath` — today's foreground time per app from usage access; `MainUiViewModel` sums it for the Home line and `AppDrawerViewModel` hands the per-app figures to the app library, both read off the main thread
- Feature helpers — `PrivateSpaceHelper`, `AiSummarizer`, `VoidAppWidgetHost` (an `AppWidgetHost`) in `WidgetsViewModel`
