package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.resolve
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceUi
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens

/**
 * The Private Space controls, identical wherever they are placed (search bar or bottom header):
 * a tonal pill holding the lock toggle and, while unlocked, the Private Space settings action. The
 * pill and the shield-badged settings icon keep it visibly apart from the plain launcher-settings
 * gear, and the settings action slides in inside the pill instead of shifting the row around it.
 */
@Composable
fun PrivateSpaceControls(
    isLocked: Boolean,
    hasSettings: Boolean,
    onToggle: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: HomeColor = HomeColor.Default
) {
    val lockState = stringResource(if (isLocked) R.string.private_space_locked else R.string.private_space_unlocked)
    val colors = privateSpaceColors(iconColor)
    Surface(
        shape = CircleShape,
        color = colors.container,
        contentColor = colors.content,
        modifier = modifier.semantics { stateDescription = lockState }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                    contentDescription = stringResource(
                        if (isLocked) R.string.unlock_private_space else R.string.lock_private_space
                    )
                )
            }
            AnimatedVisibility(
                visible = PrivateSpaceUi.settingsActionVisible(isLocked, hasSettings),
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Outlined.AdminPanelSettings,
                        contentDescription = stringResource(R.string.private_space_settings)
                    )
                }
            }
        }
    }
}

/**
 * The Private Space section header in the app list. With [controls] it carries the
 * [PrivateSpaceControls] (bottom placement); without, it only labels the private apps below it
 * (search-bar placement, where the controls live next to the search field).
 */
@Composable
fun PrivateSpaceHeader(
    modifier: Modifier = Modifier,
    iconColor: HomeColor = HomeColor.Default,
    controls: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = VoidDimens.rowSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = privateSpaceColors(iconColor).accent
        )
        Spacer(modifier = Modifier.width(VoidDimens.rowSpacing))
        Text(
            text = stringResource(R.string.private_space),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        controls?.invoke()
    }
}

/** The colors of the Private Space controls for the chosen icon [color]. */
@Immutable
data class PrivateSpaceColors(val container: Color, val content: Color, val accent: Color)

/**
 * The theme default keeps the tertiary tonal pair; any other choice tints the icons with it and
 * draws the pill in a translucent wash of the same color, so it stays apart from the plain gear.
 */
@Composable
fun privateSpaceColors(color: HomeColor): PrivateSpaceColors {
    if (color == HomeColor.Default) {
        return PrivateSpaceColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            accent = MaterialTheme.colorScheme.tertiary
        )
    }
    val tint = color.resolve()
    return PrivateSpaceColors(container = tint.copy(alpha = PILL_WASH_ALPHA), content = tint, accent = tint)
}

private const val PILL_WASH_ALPHA = 0.18f
