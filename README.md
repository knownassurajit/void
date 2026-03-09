<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" alt="VOID Launcher" width="120" height="120" style="border-radius: 50%;">
</p>

<h1 align="center">VOID Launcher</h1>

<p align="center">
  <em>A radically minimalist, text-driven Android launcher conceptualized to combat digital addiction.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Oreo)-blue?style=flat-square" alt="Min SDK">
  <img src="https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-blue?style=flat-square" alt="Target SDK">
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=flat-square&logo=kotlin" alt="Language">
  <img src="https://img.shields.io/badge/License-GPLv3-red?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Size-%3C%202MB-brightgreen?style=flat-square" alt="Size">
</p>

---

## 1. Intent & Philosophy

**VOID** is not just a launcher; it's a physiological tool designed for digital minimalism. Modern smartphone interfaces optimize for "engagement" (screen time) using variable reward schedules—brightly colored iconography, deeply nested notification badges, and infinite-scroll app drawers. These elements hijack the brain's dopamine pathways.

VOID counters this by stripping away visual stimuli. It forces **intentionality**. 
By presenting a hyper-clean, monochrome, text-based interface, your interaction with the device changes from reactive browsing to proactive computing. You open your phone to do exactly what you intended, and then you leave. No ads, no tracking, zero distractions.

---

## 2. Core Design Principles

- **Abstract Icons:** Rasterized icons and colorful logos are entirely banished from the home screen. Apps are represented purely by their typographical names.
- **Monochrome Dominance:** The entire UI acts as a canvas of stark black and white (Adaptive AMOLED Black/Light modes depending on system state).
- **The 30:70 Spatial Split:** The Home Screen enforces a rigid internal geometry. The top 30% is reserved exclusively for utility (Date, Time, live Screen Time), whilst the bottom 70% acts as the App Drawer interaction zone, preventing UI conflict.
- **Directional Spatial Memory:** Every UI transition animates purely based on the physical gesture that invoked it. Swiping right for Notes means the screen inherently slides in from the left, teaching the user's muscle memory exactly where they are in the navigation hierarchy.

---

## 3. Feature Set

### The Home Screen
- **Text-Only Favorites:** Pin up to 10 of your most critical apps to the home screen as clean text labels.
- **Inline Edit Mode:** Long-press any app label to trigger a global edit state. The screen dynamically shrinks to reveal an inline pen (rename/reassign) and reorder (drag) icon instantly without jumping to an entirely new Activity.
- **Live Digital Wellbeing:** Real-time extraction of actual screen time and total device unlock counts overlaid elegantly beneath the clock.

### The App Library (Drawer)
- **Deep Private Space Integration:** Built natively for Android 15+. Access your hidden, secure, or work-profile apps directly from the main drawer, unlocked via Biometric Prompt.
- **Instant Search:** Swipe up to summon a heavily optimized keyboard-ready search bar to filter apps instantly. 

### Swipe Landscapes
- **Notification Grouping (Left Swipe):** Intercepts system notifications and strips away their branding. It aggregates them by application/conversation into monochrome, expandable cards—shielding you from visual noise.
- **Quick Notes (Right Swipe):** A built-in scratchpad for fleeting thoughts. Features priority ordering, click-to-complete, swipe-to-delete, and system alarms for reminders—all without loading a third-party app.

---

## 4. Technical Architecture

VOID Launcher embraces modern Android development practices, ensuring a tiny memory footprint while maintaining 60fps gesture transitions.

### 4.1. Core Stack
- **Language:** 100% Kotlin
- **UI Toolkit:** XML Layouts heavily relying on Material Design 3 guidelines, Android ViewBinding, and standard Canvas APIs (No heavy Compose interop overhead for maximum legacy speed).
- **Architecture Pattern:** Single-Activity, multiple-Fragment design powered by Android's Jetpack Navigation Component. Shared state is maintained via a master `MainViewModel` running Kotlin Coroutines and `LiveData`.

### 4.2. Advanced Intercepts
- **Notification Interceptor:** Implements an `android.service.notification.NotificationListenerService` that lives purely in the background to capture, read, and dismiss Android OS-level broadcast notifications to render the distraction-free Grouping screen.
- **Accessibility Locking:** Binds to `android.accessibilityservice.AccessibilityService` explicitly to allow a double-tap gesture to physically sleep/lock the device display without requiring root access.
- **Usage Stats Extraction:** Queries `android.app.usage.UsageStatsManager` to aggregate daily foreground time and unlock events.

### 4.3. Persistence 
- **Lightweight Storage:** Uses standard `SharedPreferences` for configuration data, avoiding the massive initialization overhead of an SQL/Room database. Notes and tasks are serialized and hydrated from a lightweight JSON structure locally.

### 4.4. Intent Resolution Engine
- **Dynamic Linking:** Standard launchers crash when a user updates an app and the underlying `Activity` package name changes. VOID implements a deep `LauncherApps` Intent resolution engine that actively re-resolves the target application package at launch-time, ensuring dead links are automatically healed.

---

## 5. Building from Source

**Prerequisites:**
- Android Studio Koala (or newer)
- Android SDK API 35
- JDK 17+

```bash
# Clone the repository
git clone https://github.com/knownassurajit/void.git
cd void

# Build the debug APK
./gradlew clean assembleDebug
```

The output APK will be generated at `app/build/outputs/apk/debug/`.

---

## Acknowledgments & License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).

VOID is a heavily restructured, modernized, and refined fork of the original open-source project [Olauncher](https://github.com/knownassurajit/olauncher). Special thanks and credit to the original contributors for laying the foundational concept of a text-only, minimalist interface.
