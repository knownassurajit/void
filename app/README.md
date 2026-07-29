# 🌌 Void App Module (`void/app`)

The `app` module provides the core Android application for Void, featuring Glance widget integrations, MLKit smart text recognition fallbacks, and lifecycle sync.

---

## 🏗️ Structure

```text
app/
├── src/main/
│   ├── java/com/knownassurajit/void/
│   │   ├── MainActivity.kt        # Entry Activity
│   │   └── widget/                # Glance widgets & MLKit fallback processors
│   └── res/                       # UI layouts & assets
└── build.gradle.kts
```

---

## ⚙️ Testing & Verification

```bash
./gradlew :app:testDebugUnitTest
```
