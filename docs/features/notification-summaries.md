# Local notification summaries

## Scope

Notification summaries are generated deterministically on-device from notification text. No AI model, network connection, or third-party summarization SDK is used.

Notes are not summarized.

## Formatting

- Extract message text and media labels from notification fields.
- For title/body text, retain the title and at most 70 body characters.
- For long text without a title, retain at most 80 characters.
- Render one bullet per non-empty notification.

## Screen states

The Summary panel (`NotificationSummaryScreen`) shows one of:

- **No notification access**: `NotificationAccessPrompt` explains why it is empty and opens the system listener page. Access is re-checked on resume (`rememberNotificationAccess`), so the panel fills in after the user comes back.
- **All clear**: access granted and nothing to summarize.
- **Cards**: one `NotificationCard` per app, newest first. While a summary is being made the card says "Summarizing…"; then it lists the points (`SummaryBullets.parse` drops the `•`, `*` or `-` prefix). A card is dismissed by a swipe either way or by TalkBack's Dismiss action, which clears that app's notifications.

The notice under the title explains that summaries are generated locally from notification text.
