# VOID Launcher Knowledge Base

## Project Overview
VOID Launcher is a minimalist Android launcher focused on reducing distractions and providing a clean, text-based interface.

## Core Features
- **Minimalist Home Screen**: Displays time, date, battery, and a limited set of priority apps.
- **App Drawer**: Searchable list of all installed apps with alphabetical grouping.
- **Notes**: Built-in simple note-taking with reminders and calendar integration.
- **Widgets (Integrated Flavor)**: A dedicated screen for system widgets.
- **Notification Summary (Integrated Flavor)**: On-device AI-powered summarization of notifications using ML Kit GenAI (Gemini Nano).
- **Private Space Integration**: Support for Android 15's Private Space feature.
- **Customization**: Support for different fonts (Inter, Google Sans), text scaling, and layout alignments.

## Tech Stack
- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose (BOM 2025.05.01)
- **Navigation**: Type-safe Jetpack Navigation Compose
- **Architecture**: MVVM with StateFlow and LiveData (legacy interop)
- **AI**: Google ML Kit GenAI (Prompt & Summarization APIs)
- **Persistence**: SharedPreferences (wrapped in `Prefs` class) and JSON file caching for apps.

## Flavor Integration
The project uses two flavors:
1. **Integrated**: Includes all features, including on-device AI and widget hosting.
2. **Disintegrated**: A Play Store-compliant version that removes/stubs features that might violate policies or require high permissions (AI, Widgets, custom Notifications).

### Source Sets
- `src/main`: Shared code and components.
- `src/integrated`: Implementation of AI, Widgets, and actual custom notification screens.
- `src/disintegrated`: Stubs and placeholders for unavailable features.

## Critical Files
- `MainActivity.kt`: Entry point and navigation host.
- `HomeScreen.kt`: Main UI.
- `AppRoutes.kt`: Type-safe route definitions.
- `MainUiViewModel.kt`: UI state management for the home screen.
- `Prefs.kt`: Centralized preference management.
- `AiSummarizer.kt`: Tiered AI engine for notification summaries.

## Known Issues & Ongoing Work
- Resolving crashes during screen transitions.
- Improving flavor integration for better code reuse.
- Optimizing app list loading and caching.
