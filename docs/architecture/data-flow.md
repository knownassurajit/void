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
- `AppCacheManager` — drawer app list cache
- Feature helpers — `PrivateSpaceHelper`, `AiSummarizer`, `AppWidgetHost` in `WidgetsViewModel`
