# Bug: App drawer swipe crash

## Symptom

Swipe up from home to open the app library → “VOID keeps stopping”.

## Root cause

Primary: `AppDrawerScreen` called `FocusRequester.requestFocus()` during enter transition while `autoShowKeyboard` defaulted to `true`, before the requester was attached → `IllegalStateException`.

Secondary: brittle cache JSON `getString("activityClassName")`; Private Space init without local degrade.

## Fix

- Delay + try/catch around focus request after layout settle
- Null-safe cache parse (`optString`)
- Guarded Private Space init with empty fallback

## Regression

Instrumentation: swipe up → drawer stable with keyboard preference on and off.
