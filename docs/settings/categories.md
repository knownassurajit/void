# Settings Categories and Organization

VOID Launcher organizes settings into distinct top-level functional categories with dedicated sub-screens and a global real-time search.

## Hierarchy

```text
Settings
├── Global Search (searches across all sections in real-time)
├── Quick Action: Default Launcher
└── Categories (Material 3 Expressive Segmented Items)
    ├── Appearance
    │   ├── Font & style (Launcher font picker, System display, Wallpaper)
    │   ├── System bars (Notification bar visibility)
    │   └── Motion (Animation speed: Fast / Standard / Slow)
    ├── Home screen
    │   ├── Live Home Preview (interactive live scaled preview)
    │   ├── Layout (Show clock, date, home apps, section order, split weight, side margins)
    │   ├── Clock & date (24-hour, seconds, horizontal & vertical alignment, size scaling)
    │   ├── Clock style (Font weight, theme color / hex picker, opacity)
    │   ├── Apps layout (Max apps counter, alignments, app spacing)
    │   ├── App label style (Home text size scale, font weight, color, opacity)
    │   └── Screen time (Show on Home, size scale, usage access permission)
    ├── App drawer
    │   ├── App list (Alphabet categories, auto-show keyboard, drawer text size, app screen time)
    │   └── Private Space (Enablement, placement: bottom / search bar, icon color)
    ├── Gestures & navigation
    │   ├── Swipes (Side swipes toggle, left & right swipe actions, swipe-down notifications)
    │   └── Navigation (Content swipe to back)
    ├── Panels & features
    │   └── Panels (Notification summary, widgets, notes checklist, notes title alignment)
    ├── Permissions
    │   ├── Core launcher (Default home, app info & settings, query apps)
    │   └── Feature access (Notification listener, usage access, accessibility, widgets, private space)
    └── About & system
        ├── System settings (Change default launcher, open Android settings)
        └── About VOID (Developer credits, GitHub repository, version)
```

## Navigation & Motion

- **Top-level Category Hub**: When entering Settings, users see a categorized list of cards with icons, titles, descriptive subtitles, and navigation chevrons.
- **Drill-down**: Tapping any category transitions into its dedicated sub-screen with a top app bar containing a Back button and category title.
- **Back Handling**:
  - Tapping the back button or pressing hardware/predictive back in a category sub-screen returns to the Category Hub.
  - Active search is dismissed first on back.
  - On the Category Hub, back returns to the launcher Home screen.
  - In-screen swipe to back (`screenBackSwipe`) respects category nesting.
- **Global Search**:
  - Typing in the search field activates search mode, evaluating terms across all categories simultaneously.
  - Search results display matching sections grouped by category header.
  - Clearing the search field seamlessly restores the category view.
