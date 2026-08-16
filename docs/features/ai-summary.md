# AI notification summary

## Scope

On-device summarization of **notifications** via ML Kit GenAI (`AiSummarizer`).

Notes are **not** AI-summarized in this release.

## Tiers

1. Prompt API when available
2. Summarization API fallback
3. Deterministic local fallback text

## Gates

- Notification listener improves live data quality
- AICore presence is a soft capability; UI remains reachable with fallbacks
