# Bug: Home reorder cascade

## Symptom

Long-press reorder did not shift neighbors correctly (e.g. move 5th → 2nd).

## Root cause

- Adjacent-only index swaps instead of multi-slot cascade
- `pointerInput(index)` unstable identity after swap
- Parent swipe `pointerInput` keyed on drag state restarted and cancelled child drag without commit
- Broken `itemHeights` (`FloatArray` + list APIs) — compile failure

## Fix

- `HomeReorderHelper` package-keyed while-loop cascade
- Stable `pointerInput(ReorderKey)`
- Parent swipe ignores reorder; stable keys
- Dense persist slots `1..n` + clear trailing empties
- Fixed-size `FloatArray(11)` height tracking

## Regression

Unit: `HomeReorderHelperTest`  
E2E: long-press drag last → near-top and verify order
