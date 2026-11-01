package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens

/**
 * A card that can be swiped away either way. The swipe is not the only way: a screen reader gets the
 * same as a Dismiss action. While the card is swiped, the space it leaves shows what will happen on
 * the side the finger is moving away from; at rest nothing shows behind it.
 */
@Composable
fun DismissibleCard(
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val onDismissLatest by rememberUpdatedState(onDismiss)
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        modifier = modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(dismissLabel) { onDismissLatest(); true })
        },
        onDismiss = { onDismissLatest() },
        backgroundContent = {
            val direction = state.dismissDirection
            val swiping = direction != SwipeToDismissBoxValue.Settled
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .background(if (swiping) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                    .padding(horizontal = VoidDimens.screenPadding),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (swiping) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(dismissLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    ) {
        content()
    }
}

/**
 * One app's notifications as a card: the app's name, how many there are, how long ago the latest
 * came, and [content] under it. An [onToggle] card can be expanded: the whole header row is the
 * target (at least 48 dp tall), a chevron turns with it, and a screen reader hears "Expanded" or
 * "Collapsed" and what a tap does.
 */
@Composable
fun NotificationCard(
    appLabel: String,
    count: Int,
    time: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onToggle: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedState = stringResource(if (expanded) R.string.state_expanded else R.string.state_collapsed)
    val toggleLabel = stringResource(if (expanded) R.string.notification_show_less else R.string.notification_show_all)
    val countDescription = pluralStringResource(R.plurals.notification_count, count, count)
    val chevron by animateFloatAsState(if (expanded) HALF_TURN else 0f, MaterialTheme.motionScheme.fastSpatialSpec(), label = "chevron")
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = VoidDimens.panelTitleMinHeight)
                .then(
                    if (onToggle != null) {
                        Modifier
                            .clickable(onClickLabel = toggleLabel, role = Role.Button, onClick = onToggle)
                            .semantics { stateDescription = expandedState }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = VoidDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (count > 1) {
                Spacer(Modifier.width(VoidDimens.rowSpacing))
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.semantics { contentDescription = countDescription }
                ) {
                    Text("$count", modifier = Modifier.clearAndSetSemantics {})
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = VoidDimens.rowSpacing)
            )
            if (onToggle != null) {
                Icon(
                    Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = VoidDimens.compactSpacing)
                        .rotate(chevron)
                )
            }
        }
        Column(
            modifier = Modifier.padding(
                start = VoidDimens.cardPadding,
                end = VoidDimens.cardPadding,
                bottom = VoidDimens.cardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing),
            content = content
        )
    }
}

private const val HALF_TURN = 180f
