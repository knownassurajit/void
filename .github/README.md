# CI/CD (`void/.github`)

```text
.github/
├── dependabot.yml
├── workflows/ci-cd.yml      develop + master
├── workflows/stage.yml      stage branch debug pre-release
└── README.md
```

## Branching

- `develop` — integration
- `stage` — staging debug builds (`stage.yml`)
- `master` — production
- `release/void/<version>` — rollback snapshot

## Jobs (`ci-cd.yml`)

| Job | Trigger | Purpose |
|---|---|---|
| `test` | push to develop/master, PRs into master | unit tests + lint |
| `dependency-review` | PRs | high-severity advisory gate |
| `debug-release` | push to develop | debug APK pre-release |
| `pr-summary` | PRs into master | sticky CI comment |
| `stable-release` | push to master | signed APK/AAB, GitHub release, optional Play internal |

Actions are SHA-pinned. Default workflow permissions are `contents: read`.
