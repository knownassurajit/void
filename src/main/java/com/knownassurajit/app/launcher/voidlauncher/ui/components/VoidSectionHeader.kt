package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens

/**
 * The title row of a panel (Notifications, Summary, Notes, Widgets): one size, weight and color for
 * every screen, announced as a heading so a screen reader can jump to it. [trailing] holds the
 * panel's own control (a count, a button). [arrangement] places the title for panels that let the
 * user choose its alignment.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VoidPanelTitle(
    text: String,
    modifier: Modifier = Modifier,
    arrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    titleModifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = VoidDimens.panelTitleMinHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = arrangement
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            // The title gives way to the trailing control: it wraps rather than pushing the control off screen.
            modifier = titleModifier
                .weight(1f, fill = false)
                .semantics { heading() }
        )
        trailing?.invoke(this)
    }
}

/** The hairline between parts of a screen. */
@Composable
fun VoidSectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = VoidAlpha.divider)
    )
}

/** Padding around a [VoidPanelTitle] at the top of a panel. */
fun Modifier.panelTitlePadding(): Modifier = padding(
    start = VoidDimens.screenPadding,
    end = VoidDimens.screenPadding,
    top = VoidDimens.sectionSpacing,
    bottom = VoidDimens.rowSpacing
)
