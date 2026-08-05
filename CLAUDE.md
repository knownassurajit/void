# CLAUDE.md

## VOID Launcher SOP

Minimalist Android launcher. Kotlin + Compose + Material 3. Single-activity.  
SDK: min 26 · target/compile 36 · JDK 17.  
Package: `com.knownassurajit.app.launcher.voidlauncher` · App ID: `com.voidlauncher.app`.

---

## Operating Rules

- Enforce **small, explicit, reviewable diffs**.
- Prefer **Kotlin-first, Compose-only** for new UI.
- Ship a **single-variant OSS build**; use runtime capability gates (`FeatureAvailability`) instead of Play stub flavors.
- Treat `src/main` as the sole runtime source set; do not ship legacy `app/` code.
- Never commit secrets, generated artifacts, local env files, or debug-only shims.
- Every change must end with:
  - build passes
  - lint passes
  - tests added/updated
  - logs reviewed
  - README updated
  - traceable commit message

---

## Repo Layout

```text
src/main/           shared + full runtime
src/test/           unit tests
src/androidTest/    instrumentation / e2e
.github/workflows/  CI/CD
docs/               architecture / features / settings / testing / bugs
```

Rules:
- Keep feature code co-located.
- Isolate platform capabilities behind `FeatureAvailability` and helpers.
- Prefer abstraction seams over conditionals when behavior diverges by API/permission.

---

## Build / Test / Verify

Run from repo root only.

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease

./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

Single test:
```bash
./gradlew testDebugUnitTest --tests "fully.qualified.TestClass"
```

Before merge:
- build debug + release
- lint
- run affected unit tests
- run e2e/instrumentation when UI paths change
- verify release artifacts
- verify version bump

---

## Architecture

### App Model

- Single Activity architecture.
- Compose Navigation only.
- Type-safe `@Serializable` routes.
- Separate:
  - UI state
  - side effects
  - persistence
  - system interaction

### Data Flow

```text
UI → ViewModel → StateFlow → Repository → System/Data Layer
```

Rules:
- Enforce unidirectional flow.
- Never mutate state in composables.
- Never place business logic in UI.
- Prefer `StateFlow`.
- Use `LiveData` only for legacy integration.

### Persistence

- SharedPreferences access only through `Prefs.kt`.
- Serialize complex types with `kotlinx.serialization`.
- Treat key renames as migrations.
- Validate corrupted/missing values safely.

---

## Capability Policy (single-variant OSS)

VOID ships one open-source build with full features in `src/main`.

Runtime gates live in `FeatureAvailability`:
- Widgets → AppWidgetHost availability
- Private Space → API 35+ / profile presence
- Notification listener → optional permission for live panels
- AICore → soft capability for richer summarization tiers

Degrade gracefully when a capability or permission is missing. Do not reintroduce Play stub flavors unless a Play SKU is explicitly planned.

See `docs/architecture/flavor-decision.md`.

---

## Process / Service Boundaries

- Accessibility service runs in isolated process.
- Do not rely on shared statics.
- Treat reflection/private APIs as unstable.
- Add null-safe fallback behavior.
- Preserve process-safe communication boundaries.

---

## UI / Compose Standards

- Compose-only for all new UI.
- No new Fragments/XML Views.
- One composable = one responsibility.
- Prefer stateless composables.
- Extract reusable primitives.
- Avoid unnecessary recomposition.
- Use:
  - `remember`
  - `derivedStateOf`
  - immutable params
  only when justified.

Never hardcode:
- dimensions
- colors
- typography
- spacing

Use design tokens only.

---

## Kotlin Standards

Prefer:
- explicit types
- `val`
- sealed models
- immutable collections
- extension functions
- expression bodies when concise

Avoid:
- `Any`
- deep nesting
- hidden side effects
- unsafe casts
- global mutable state
- dead code

Rules:
- strict null safety
- coroutine-first async
- never block main thread
- small testable functions
- semantic naming only

---

## Logging / Observability

Use structured logging only.

Log:
- failures
- recoveries
- state transitions
- service/process boundaries
- fallback execution
- migration failures

Never log:
- secrets
- tokens
- identifiers
- personal data

Required coverage:
- app startup
- restore flow
- navigation failures
- settings persistence
- launcher actions
- permission denial
- ML/service fallback paths

---

## QA / Testing

### Unit

Cover:
- ViewModels
- serialization
- prefs
- helpers
- routes
- fallback logic

### Instrumentation / E2E

Cover:
- app launch
- drawer/navigation
- swipe actions
- persistence
- service interaction
- process restore
- restricted-feature fallback

Rules:
- add regression tests for all bug fixes
- prefer focused fakes over excessive mocks
- test behavior, not implementation

Minimum:
- critical path coverage
- no untested modified branch

---

## Lint / Static Analysis

Mandatory:
- ktlint
- detekt
- Android Lint

CI must fail on:
- lint issues
- formatting drift
- test failure
- type/build failure

---

## CI / CD

Pipeline order:

```text
install
→ format
→ lint
→ unit-test
→ instrumentation/E2E
→ build
→ sign
→ release
```

Requirements:
- deterministic workflows
- artifact retention
- signed releases
- rollback-ready artifacts
- version tagging
- reproducible builds

Rules:
- fail fast
- never bypass CI
- verify output naming
- verify signing
- verify flavor parity

---

## Git / Repo Hygiene

### Branches

```text
main
develop
feature/*
fix/*
hotfix/*
release/*
```

### Commit Format

```text
feat:
fix:
refactor:
perf:
test:
docs:
build:
ci:
chore:
```

### .gitignore

Ignore:
```text
/build
/.gradle
/local.properties
.idea/
*.keystore
*.jks
*.apk
*.aab
*.log
/reports
```

### .gitkeep

- Use only for intentionally empty directories.
- Remove stale placeholders.
- Never use as fake structure filler.

### PR Rules

- no direct push to main
- squash merge preferred
- CI required
- review required
- docs updated before merge

---

## Documentation

Treat `README.md` as release-critical.

Update after every:
- feature
- refactor
- architecture change
- dependency change
- CI/CD change
- config/env change
- workflow change

README must include:
- setup
- architecture
- flavor matrix
- build/test commands
- env/config
- troubleshooting
- release flow
- known limitations

Maintain:
```text
README.md
CHANGELOG.md
docs/
```

---

## Security

- Never hardcode credentials.
- Use least-privilege permissions.
- Validate all external input.
- Guard reflection/private APIs.
- Sanitize logs/crashes.
- Fail closed on sensitive operations.

---

## Performance

- Avoid main-thread blocking.
- Minimize startup work.
- Lazy-load heavy paths.
- Reduce recomposition.
- Cache only with invalidation.
- Measure before optimization.

---

## Release Checklist

Before release:
- both flavors build
- tests pass
- lint clean
- docs updated
- changelog updated
- version verified
- signing verified
- fallback paths tested
- artifact names verified
- rollback path documented

---

## Done Definition

Complete only when:
- implementation correct
- tests added
- logs updated
- docs updated
- CI green
- release-safe
- repo hygiene preserved