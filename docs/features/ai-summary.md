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

## Screen states

The Summary panel (`NotificationSummaryScreen`) shows one of:

- **No notification access**: `NotificationAccessPrompt` explains why it is empty and opens the system listener page. Access is re-checked on resume (`rememberNotificationAccess`), so the panel fills in after the user comes back.
- **All clear**: access granted and nothing to summarize.
- **Cards**: one `NotificationCard` per app, newest first. While a summary is being made the card says "Summarizing…"; then it lists the points (`SummaryBullets.parse` drops the `•`, `*` or `-` the model or the local fallback puts in front of each line). A card is dismissed by a swipe either way or by TalkBack's Dismiss action, which clears that app's notifications.

When AICore is not available a line under the title says each card shows the notifications as they came in (tier 3). The header's count badge is read as "N notifications".
