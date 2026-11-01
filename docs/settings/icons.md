# Icons in Settings and the panels

Rules the Settings rows and the other screens follow, so a new row or screen does not bring the old
problems back (`ui/components/SettingsComponents.kt`).

## Style

- Outlined icons (`Icons.Outlined`, `Icons.AutoMirrored.Outlined`) everywhere. The Settings rows used
  filled icons while the header, the app library and Widgets used outlined ones. The switch thumb's
  check stays filled, it needs the weight.

## One icon, one meaning

- Inside one group no two rows share a glyph. The same setting in two places (clock and apps
  alignment, Usage access in Screen time and in Permissions) uses the same glyph in both.
- Pick a glyph that says what the row does: alignment rows use the format-align and vertical-align
  icons, the status bar row a signal icon, side margins a width icon. Avoid glyphs that read as a
  drawing glitch at 24 dp (`SpaceBar`).

## Where icons sit

- **Text rows** (action, toggle, counter, color): the leading icon and the trailing control are
  centered on the row, however many lines the subtitle wraps to. The list item's own default moves
  them to the title line from three lines on, so icons and switches jumped between neighbouring rows.
- **Control rows** (slider, choice buttons): the leading icon and the value stay on the title line at
  every height and font scale; the icon belongs to the title, not to the middle of the control.
- **Trailing icon of an action row**: a chevron means the row goes deeper inside VOID (a picker, a
  dialog); `external = true` shows an open-in-new icon for a row that leaves VOID for Android's own
  settings (Display, Wallpaper, Usage access, default launcher, every permission).
- **Icon buttons at the end of a row** (the Settings header, the app library's search row): the
  glyph, not the invisible 48 dp target, sits on the content edge. An icon button draws its glyph
  `VoidDimens.iconButtonGlyphInset` (12 dp) inside its target, so the row takes that off its end
  padding.
- **Search fields**: one component (`VoidSearchField`) with the search icon at the start and a clear
  button at the end while there is text; Settings, the app library, the widget picker and the font
  picker all use it.
- A button's icon says what the button does (a gear for a widget's Settings, a trash can for Remove),
  never a different action (the widget Settings button had a pencil).

## Tests

`SettingsRowsInstrumentedTest` (androidTest) measures the rules above: icon and trailing control
centered on a text row of any height, icon on the title line of a slider and a choice row. Run it on
an emulator (`docs/testing/e2e.md`).
