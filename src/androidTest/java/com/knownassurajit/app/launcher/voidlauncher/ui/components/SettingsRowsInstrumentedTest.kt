package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Where the icons of Settings rows sit. A row that is only text keeps its leading icon and its
 * trailing control centered on the row however many lines the subtitle wraps to (the list item's own
 * default moves them to the title line at three lines, so they jumped between rows of one group);
 * a row with a control under its title keeps the icon on the title line.
 *
 * Run it on an emulator (`./gradlew connectedDebugAndroidTest`), not on the physical device.
 */
@RunWith(AndroidJUnit4::class)
class SettingsRowsInstrumentedTest {

    @get:Rule
    val rule = createComposeRule()

    private val shortSubtitle = "Short"
    private val longSubtitle = "A longer explanation that wraps onto several lines of the row. ".repeat(6)

    private fun setRows(vararg rows: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            VoidAppTheme(dynamicColor = false) {
                SettingsSection(title = null, items = rows.toList())
            }
        }
    }

    private fun icon(tag: String): @androidx.compose.runtime.Composable () -> Unit = {
        Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.testTag(tag))
    }

    private fun row(tag: String, content: @androidx.compose.runtime.Composable () -> Unit): @androidx.compose.runtime.Composable () -> Unit = {
        Box(modifier = Modifier.testTag(tag)) { content() }
    }

    private fun top(tag: String): Dp = rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot().top

    private fun height(tag: String): Dp = rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot().height

    private fun centerY(tag: String): Dp =
        rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot().let { (it.top + it.bottom) / 2 }

    private fun assertCentered(row: String, vararg parts: String) {
        parts.forEach { part ->
            assertEquals("$part of $row", centerY(row).value, centerY(part).value, TOLERANCE.value)
        }
    }

    @Test
    fun actionRowKeepsIconAndTrailingCenteredWhateverTheSubtitleLength() {
        setRows(
            row("short") {
                SettingActionItem(
                    title = "Title", subtitle = shortSubtitle, icon = icon("short-icon"), onClick = {},
                    trailing = icon("short-end")
                )
            },
            row("long") {
                SettingActionItem(
                    title = "Title", subtitle = longSubtitle, icon = icon("long-icon"), onClick = {},
                    trailing = icon("long-end")
                )
            }
        )

        assertTrue("the long row must be taller or the test proves nothing", height("long") > height("short"))
        assertCentered("short", "short-icon", "short-end")
        assertCentered("long", "long-icon", "long-end")
    }

    @Test
    fun toggleRowKeepsIconCenteredWhateverTheSubtitleLength() {
        setRows(
            row("short") {
                SettingToggleItem(
                    title = "Title", subtitle = shortSubtitle, icon = icon("short-icon"),
                    checked = true, onCheckedChange = {}
                )
            },
            row("long") {
                SettingToggleItem(
                    title = "Title", subtitle = longSubtitle, icon = icon("long-icon"),
                    checked = true, onCheckedChange = {}
                )
            }
        )

        assertTrue("the long row must be taller or the test proves nothing", height("long") > height("short"))
        assertCentered("short", "short-icon")
        assertCentered("long", "long-icon")
    }

    @Test
    fun controlRowsKeepTheIconOnTheTitleLine() {
        setRows(
            row("slider") {
                SettingSliderItem(
                    title = "Title", subtitle = "", icon = icon("slider-icon"),
                    value = 0.5f, range = 0f..1f, valueText = { "50%" }, onChanged = {}
                )
            },
            row("choice") {
                SettingChoiceItem(
                    title = "Title", icon = icon("choice-icon"), selected = 1,
                    options = listOf(1 to "One", 2 to "Two"), onSelected = {}
                )
            }
        )

        listOf("slider", "choice").forEach { id ->
            assertTrue("$id is a tall row", height(id) > TALL_ROW)
            val iconOffset = centerY("$id-icon") - top(id)
            assertTrue("$id icon is $iconOffset below the row top, on the title line", iconOffset < TITLE_LINE)
        }
    }

    private companion object {
        val TOLERANCE = 1.5.dp

        /** A row with a control under its title is taller than this. */
        val TALL_ROW = 80.dp

        /** The icon of such a row is within this far of its top: the title line, not the middle of the control. */
        val TITLE_LINE = 36.dp
    }
}
