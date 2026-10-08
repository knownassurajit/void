<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" alt="VOID Launcher" width="128" height="128" style="border-radius: 20%;">
</p>

<h1 align="center">VOID</h1>

<p align="center">
  <em>A radically minimalist, high-performance Android launcher designed to combat digital addiction.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Oreo)-blue?style=flat-square" alt="Min SDK">
  <img src="https://img.shields.io/badge/Target%20SDK-36%20(Android%2016)-blue?style=flat-square" alt="Target SDK">
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=flat-square&logo=kotlin" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-orange?style=flat-square&logo=jetpack-compose" alt="UI">
  <img src="https://img.shields.io/badge/License-GPLv3-red?style=flat-square" alt="License">
</p>

---

## 🌑 Philosophy

**VOID** is built on the principle of intentionality. By stripping away colorful icons, notification badges, and complex grid layouts, VOID eliminates the psychological triggers that lead to mindless scrolling. It provides a hyper-clean, text-based interface where you are in control of your phone, not the other way around.

---

## ✨ Core Features

- **Text-Only Home Screen**: Pin up to 10 of your most essential apps as clean text labels. No icons, no distractions.
- **Smart Notification Grouping**: A dedicated screen that groups system notifications by app with smart summaries, categorized by conversation.
- **On-Device AI Summarization**: Uses Gemini Nano (via ML Kit) to summarize your notifications locally, ensuring privacy and speed.
- **Integrated Quick Notes**: Fast, text-based checklist for capturing thoughts instantly with priority ordering and reminders.
- **Deep Android 15 Integration**: Full support for Private Space, allowing you to access hidden and secure apps directly from the drawer.
- **Digital Wellbeing**: Screen time and unlock counts are integrated directly into the home screen for at-a-glance awareness.
- **Fluid Gestures**: Swipe up for apps, swipe down for the in-app notification list (toggle in Settings), and custom left/right actions. Panels move the way you swipe: swipe right and the panel slides in from the left edge, swipe up and the drawer rises from the bottom, while Home moves the same way underneath. Motion uses Material 3 spring physics that the animation-speed setting scales. The system back gesture (Android 14+) shows the Material predictive-back preview on every screen, and a swipe inside a screen toward its edge also leaves it (56 dp or a quick flick). See [`docs/architecture/motion.md`](docs/architecture/motion.md).
- **Modern UI**: Built with 100% Jetpack Compose and Material 3. Choose from 36 launcher fonts in a searchable picker: six bundled families that work offline (Google Sans default, DM Sans, Geist, Inter, Manrope, Plus Jakarta Sans), 29 Google Fonts families downloaded on demand by Google Play services (Poppins, Work Sans, Karla, Raleway, Inconsolata, Roboto, Source Serif 4, and more), and the system font. See [`docs/features/fonts.md`](docs/features/fonts.md).
- **Searchable settings**: Grouped Look / Home / App library / Gestures & motion / Features / Permissions, with animation speed and content swipe-to-back.

---

## 📱 Screen Flow & Navigation

Navigate your device with a simple, gesture-based mental model:

```mermaid
graph TD
    A[Home Screen] -- Swipe Up --> B[App Drawer]
    A -- Swipe Left --> D[Custom Action: Notes/Widgets]
    A -- Swipe Right --> E[Custom Action: Summary/Notes]
    A -- Double Tap --> F[Lock Screen]
    B -- Settings icon --> C[Settings]
    C -- Back --> B
    B -- Click App --> G[Launch App]
    B -- Back --> A
```

---

## 🛠 Technical Architecture

VOID Launcher leverages the latest Android development stack for maximum performance and a minimal footprint.

- **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) with [Material 3](https://m3.material.io/).
- **Programming Language**: 100% Kotlin.
- **State Management**: Clean Architecture with Fragments/Single-Activity and shared `MainViewModel` using Kotlin Coroutines and Flow.
- **Intelligence**: Integrated with **ML Kit GenAI** for on-device notification processing.
- **Background Tasks**: Powered by `WorkManager` for reliable, low-impact operations like wallpaper updates.
- **Persistence**: Hybrid approach using `SharedPreferences` and JSON for lightweight, fast data access.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Koala** (or newer)
- **JDK 17** or **JDK 21**
- **Android SDK Platform 36**

### Building from Source

```bash
# Clone the repository
git clone https://github.com/knownassurajit/void.git
cd void

# Single-variant open-source build (full features)
./gradlew clean assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
```

Output APKs land under `build/outputs/apk/debug/` or `build/outputs/apk/release/`.

### Testing

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
# with a device/emulator:
./gradlew connectedDebugAndroidTest
```

See [`docs/`](docs/) for architecture, features, settings, testing guides, and bug fix notes.

### Known limitations

- **Downloadable fonts need Google Play services.** The 29 Google Fonts families are fetched by the Play services font provider, not by VOID's own code. Without it, or offline before the first download, those families are unavailable and the picker says so; the six bundled families and the system font always work. A saved downloadable font renders in the default font until its download has finished in the current session.
- **Swipes inside a screen only close it from regions that do not scroll or slide.** A swipe toward a screen's edge closes it from headers, gaps and empty areas, but the drawer, the notification list and Settings lists scroll instead, and a note row slides to delete. The system back gesture (a preview of the screen underneath on Android 14+), the Back key and TalkBack's back or Dismiss action are the reliable ways out of scrolling screens. See [`docs/architecture/motion.md`](docs/architecture/motion.md).
- Six requested names (Brogman, Volkga, Disttopian, Stanza, Vangura Sans, Noria) are commercial or personal-use fonts and are not offered; see [`docs/features/fonts.md`](docs/features/fonts.md) for alternatives.

> **Note:** The former `integrated` / `disintegrated` product flavors were removed. VOID ships as one OSS build with runtime capability gates. See [`docs/architecture/flavor-decision.md`](docs/architecture/flavor-decision.md).

---

## 🔄 CI/CD

Three workflows cover the branch pipeline, each scoped to a single concern:

| File | Trigger | Purpose |
|---|---|---|
| `.github/workflows/ci-cd.yml` | push to `develop`/`master`, PRs targeting `master` | develop/master pipeline (see below) |
| `.github/workflows/stage.yml` | push to `stage` | debug build → GitHub pre-release (unchanged, split out verbatim from the old `build.yml`) |

**`ci-cd.yml` jobs:**
- **`test`** — `testDebugUnitTest` + `lintDebug`, containerized (`eclipse-temurin:17-jdk-jammy`). Runs on every push to `develop`/`master` and on every pull request targeting `master`.
- **`debug-release`** (push to `develop` only, needs `test`) — re-runs tests/lint, then builds and uploads a debug APK as a GitHub pre-release tagged `develop-v$version-$run_number`.
- **`pr-summary`** (pull requests targeting `master`, needs `test`) — re-runs checks and posts a `$GITHUB_STEP_SUMMARY` plus a sticky PR comment with test/lint results and a changelog preview since the last stable release tag.
- **`stable-release`** (push to `master` only, needs `test`) — **the single release path**: unit tests + lint, `assembleRelease` + `bundleRelease`, signs via `r0adkll/sign-android-release@v1` (the only signing step — no unused keystore-decode step), renames to `void-release-v$version.{apk,aab}`, creates/pushes a `release/void/$version` branch for rollback tracking, publishes a GitHub stable release tagged `v$version`, and optionally publishes to the Google Play internal track.

Jobs run inside `container: image: eclipse-temurin:17-jdk-jammy`; each job installs `unzip`/`curl`/`git` via `apt-get` and sets up the Android SDK with `android-actions/setup-android@v3` before invoking Gradle.

**Google Play publishing** is gated on `secrets.PLAY_CONSOLE_JSON` being present. This fixes a bug in the old `play-release.yml`, which gated on `env.PLAY_CONSOLE_JSON != ''` — an env var that was never actually set before that condition was evaluated, making the gate either a permanent no-op or (if evaluated differently) capable of firing an unintended production Play publish on every push to `develop`. The fixed workflow resolves the secret in an earlier `play_console_check` step (a step's own `env:` is visible to its `run:`, just not to its own `if:`) and downstream steps gate on that step's output, so the Play publish step is a safe no-op with no `PLAY_CONSOLE_JSON` secret configured, and only runs on push to `master` when it is.

Required secrets for `stable-release`: `SIGNING_KEY`, `ALIAS`, `KEY_STORE_PASSWORD`, `KEY_PASSWORD` (APK/AAB signing, required), `PLAY_CONSOLE_JSON` (optional — enables Play Console publish).

---

## 🛡 Privacy & Security

VOID is designed with privacy as a first-class citizen:
- **No Ads. No Tracking.**
- **Local AI**: All summarization happens on-device using Gemini Nano. Your data never leaves your phone.
- **Open Source**: The code is fully transparent and open for audit.

---

## 📄 License & Credits

VOID is a heavily restructured and modernized fork of **Olauncher**. We credit the original project for the foundational concept of a minimalist, text-based launcher.

- **License**: This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
- **Original Base**: [Olauncher](https://github.com/tanujnotes/olauncher) by Tanuj.
- **Typography**: all six bundled families are under the SIL Open Font License 1.1, with the copyright notice kept in each font file: Google Sans (Copyright 2025 The Google Sans Project Authors), DM Sans (Copyright 2014 The DM Sans Project Authors, https://github.com/googlefonts/dm-fonts), Geist (Copyright 2024 The Geist Project Authors, https://github.com/vercel/geist-font), Inter (Copyright 2016 The Inter Project Authors), Manrope (Copyright 2019 The Manrope Project Authors, https://github.com/sharanda/manrope), and Plus Jakarta Sans (Copyright 2020 The Plus Jakarta Sans Project Authors, https://github.com/tokotype/PlusJakartaSans). The bundled Google Sans is a Latin/Cyrillic variable subset of the Google Fonts release and Geist is the unmodified upstream variable font (see [`docs/features/fonts.md`](docs/features/fonts.md); licence texts in [`docs/licenses/fonts/`](docs/licenses/fonts/)). The downloadable families come from Google Fonts, are licensed under the OFL by their authors, and are delivered by Google Play services; they are not part of the APK.
- **Icons**: Material Symbols (Google).

---

<p align="center">
  <em>“Are you using your phone, or is your phone using you?”</em>
</p>
