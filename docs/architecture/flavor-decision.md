# Flavor decision

## Decision

VOID Launcher ships as a **single-variant open-source build**.

The former `integrated` / `disintegrated` product flavors were removed. All features live in `src/main`.

## Rationale

- The project is intended for GitHub / sideload distribution, not Play Store policy packaging.
- Dual flavors duplicated APIs, broke CI after removal, and confused contributors.
- Capability differences are better expressed as **runtime gates** (API level, permissions, AICore presence) than compile-time stubs.

## Runtime gates

`FeatureAvailability` exposes context-aware checks:

| Capability | Gate |
|------------|------|
| Widgets | AppWidgetHost / AppWidgetManager |
| Private Space | API 35+ and profile detection |
| Notifications UI | Always available; listener optional |
| AI summary | Soft AICore / API readiness; tier fallbacks remain |

`FeatureUnavailableScreen` messaging describes missing capability/permission — not a Play SKU.

## Reintroduction policy

Only reintroduce flavors if a Play Store SKU is explicitly required. Until then, keep one assemble path:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
```
