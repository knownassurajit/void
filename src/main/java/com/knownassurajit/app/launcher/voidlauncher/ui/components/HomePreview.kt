package com.knownassurajit.app.launcher.voidlauncher.ui.components

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.MainUiState
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.helper.HomeLayoutHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeStyle
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeTextColor
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAppTheme
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.resolve
import java.text.SimpleDateFormat
import java.util.Date

/** What a Settings preview draws. */
enum class SettingsPreviewTarget {
    HOME,
    DRAWER
}

private const val PREVIEW_APP_COUNT = 5

/**
 * Home as Home draws it, from the same [HomeStyle] styles, [HomeLayoutHelper] weights and section
 * order, for [state] as Settings currently holds it. Static: no gestures, sample labels and the
 * current time.
 */
@Composable
fun HomePreviewContent(state: MainUiState, modifier: Modifier = Modifier) {
    VoidAppTheme(appFont = state.appFont) {
        val clockEnabled = state.showClock || state.showDate || state.showScreenTime
        val appsEnabled = state.showHomeApps
        val weights = HomeLayoutHelper.sectionWeights(clockEnabled, appsEnabled, state.clockSectionWeight)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val clock: @Composable ColumnScope.() -> Unit = {
                if (clockEnabled) PreviewClockSection(state, weights.clock)
            }
            val apps: @Composable ColumnScope.() -> Unit = {
                if (appsEnabled) PreviewAppsSection(state, weights.apps)
            }
            val divider: @Composable () -> Unit = {
                if (clockEnabled && appsEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = state.homeSidePaddingDp.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                    )
                }
            }
            if (state.homeSectionOrder == "apps_first") {
                apps()
                divider()
                clock()
            } else {
                clock()
                divider()
                apps()
            }
        }
    }
}

@Composable
private fun ColumnScope.PreviewClockSection(state: MainUiState, weight: Float) {
    val clockColor = HomeTextColor.fromKey(state.clockColor)
    val base = clockColor.resolve()
    val textAlign = gravityTextAlign(state.clockHorizontalAlignment)
    // The configuration locale is observable: the samples follow a language change.
    val locale = LocalConfiguration.current.locales[0]
    val time = remember(state.use24HourClock, state.showSeconds, locale) {
        val pattern = when {
            state.use24HourClock && state.showSeconds -> "HH:mm:ss"
            state.use24HourClock -> "HH:mm"
            state.showSeconds -> "h:mm:ss a"
            else -> "h:mm a"
        }
        SimpleDateFormat(pattern, locale).format(Date()).uppercase(locale)
    }
    val date = remember(locale) { SimpleDateFormat("EEE, d MMM", locale).format(Date()).uppercase(locale) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight.coerceAtLeast(0.01f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(gravityBoxAlignment(state.clockVerticalAlignment))
                .padding(horizontal = state.homeSidePaddingDp.dp, vertical = VoidDimens.sectionSpacing),
            horizontalAlignment = gravityHorizontal(state.clockHorizontalAlignment),
            verticalArrangement = Arrangement.spacedBy(VoidDimens.compactSpacing)
        ) {
            if (state.showClock) {
                Text(
                    text = time,
                    style = HomeStyle.clockStyle(MaterialTheme.typography, state.clockSizeScale, state.showSeconds, state.clockFontWeight),
                    color = base.copy(alpha = state.clockOpacity),
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (state.showDate) {
                Text(
                    text = date,
                    style = HomeStyle.dateStyle(MaterialTheme.typography, state.dateSizeScale),
                    color = base.copy(alpha = 0.87f * state.clockOpacity),
                    textAlign = textAlign
                )
            }
            if (state.showScreenTime) {
                Text(
                    text = stringResource(R.string.preview_screen_time).uppercase(locale),
                    style = HomeStyle.screenTimeStyle(MaterialTheme.typography, state.screenTimeSizeScale),
                    color = (if (clockColor == HomeTextColor.Default) MaterialTheme.colorScheme.onSurfaceVariant else base)
                        .copy(alpha = 0.45f * state.clockOpacity),
                    textAlign = textAlign
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.PreviewAppsSection(state: MainUiState, weight: Float) {
    val configuration = LocalConfiguration.current
    val labels = stringPreviewApps()
    val shown = labels.take(state.homeAppsCount.coerceIn(1, PREVIEW_APP_COUNT))
    val spacing = HomeStyle.appSpacingDp(configuration.screenHeightDp, shown.size, state.appSpacingDp)
    val base = HomeTextColor.fromKey(state.homeAppsColor).resolve()
    val textAlign = gravityTextAlign(state.appHorizontalAlignment)
    // Same structure as Home's apps section: the list in the remaining space, the battery line below.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight.coerceAtLeast(0.01f))
            .padding(horizontal = state.homeSidePaddingDp.dp, vertical = VoidDimens.rowSpacing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(gravityBoxAlignment(state.appVerticalAlignment))
                    .padding(top = VoidDimens.rowSpacing, bottom = 12.dp),
                horizontalAlignment = gravityHorizontal(state.appHorizontalAlignment),
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                shown.forEach { label ->
                    Text(
                        text = label,
                        style = HomeStyle.appLabelStyle(MaterialTheme.typography, state.homeTextSizeScale, state.homeAppsFontWeight),
                        color = base.copy(alpha = state.homeAppsOpacity),
                        textAlign = textAlign,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = VoidDimens.sectionSpacing)
                    )
                }
            }
        }
        Text(
            text = "${state.batteryLevel}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = VoidDimens.rowSpacing, bottom = 12.dp)
                .padding(vertical = VoidDimens.compactSpacing)
        )
    }
}

/** The app drawer list as the drawer draws its rows, at [textSizeScale]. */
@Composable
fun DrawerPreviewContent(appFont: String, textSizeScale: Float, modifier: Modifier = Modifier) {
    VoidAppTheme(appFont = appFont) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.sectionSpacing)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Text(
                    text = stringResource(R.string.search),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.sectionSpacing)
                )
            }
            stringPreviewApps().sorted().forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * textSizeScale
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
                    maxLines = 1,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
            }
        }
    }
}

/**
 * Draws [content] laid out for the full screen, shrunk to [scale]: the density is scaled, so sizes,
 * text and spacing keep the proportions they have on the real screen.
 */
@Composable
fun ScaledScreenPreview(scale: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .size((configuration.screenWidthDp * scale).dp, (configuration.screenHeightDp * scale).dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
    ) {
        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
            Box(modifier = Modifier.fillMaxSize()) { content() }
        }
    }
}

/** Full-screen preview shown while a Settings slider is held. */
@Composable
fun SettingsPreviewPane(target: SettingsPreviewTarget, state: MainUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (target) {
            SettingsPreviewTarget.HOME -> HomePreviewContent(state)
            SettingsPreviewTarget.DRAWER -> DrawerPreviewContent(state.appFont, state.appDrawerTextSizeScale)
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = VoidDimens.sectionSpacing)
        ) {
            Text(
                text = stringResource(
                    if (target == SettingsPreviewTarget.HOME) R.string.preview_live_home else R.string.preview_live_drawer
                ),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = VoidDimens.sectionSpacing, vertical = VoidDimens.compactSpacing)
            )
        }
    }
}

@Composable
private fun stringPreviewApps(): List<String> = listOf(
    stringResource(R.string.preview_app_phone),
    stringResource(R.string.preview_app_messages),
    stringResource(R.string.preview_app_browser),
    stringResource(R.string.preview_app_camera),
    stringResource(R.string.preview_app_music)
)

private fun gravityHorizontal(gravity: Int): Alignment.Horizontal = when (gravity) {
    Gravity.CENTER_HORIZONTAL -> Alignment.CenterHorizontally
    Gravity.END -> Alignment.End
    else -> Alignment.Start
}

private fun gravityTextAlign(gravity: Int): TextAlign = when (gravity) {
    Gravity.CENTER_HORIZONTAL -> TextAlign.Center
    Gravity.END -> TextAlign.End
    else -> TextAlign.Start
}

private fun gravityBoxAlignment(gravity: Int): Alignment = when (gravity) {
    Gravity.TOP -> Alignment.TopCenter
    Gravity.CENTER_VERTICAL -> Alignment.Center
    else -> Alignment.BottomCenter
}
