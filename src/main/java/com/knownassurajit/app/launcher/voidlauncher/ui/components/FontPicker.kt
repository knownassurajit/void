package com.knownassurajit.app.launcher.voidlauncher.ui.components

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontFailure
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontFailureAdvice
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontKind
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontPickerSession
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontRowState
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSource
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontSpec
import com.knownassurajit.app.launcher.voidlauncher.data.font.advice
import com.knownassurajit.app.launcher.voidlauncher.data.font.isReady
import com.knownassurajit.app.launcher.voidlauncher.data.font.listIndexOf
import com.knownassurajit.app.launcher.voidlauncher.data.font.rowState
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.FontRepository
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.FontFamilies
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

// A row asks for its sample only after it stayed on screen this long, so a fast fling downloads nothing.
private const val SAMPLE_REQUEST_DELAY_MS = 180L

/**
 * Full-screen font chooser drawn inside the Settings screen (not a window), so other overlays can sit above it.
 *
 * Tapping a row only previews it: the specimen card at the top redraws the clock, app labels and a pangram in that
 * face, so a font is seen before it is chosen. [Apply][R.string.font_apply] commits the previewed family. Bundled and
 * System families commit at once. A Google Fonts family first downloads every declared weight and commits only when
 * that succeeded; on failure the picker stays open, shows a message, and [onFontSelected] is not called. The rules
 * are in [FontPickerSession]; this composable only forwards taps and draws the result.
 */
@Composable
fun FontPicker(
    selectedId: String,
    onFontSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val repository = remember(context) { FontRepository.get(context) }
    val gmsAvailable = remember(context) { FeatureAvailability.isGoogleFontsProviderAvailable(context) }
    val states by repository.states.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentOnFontSelected by rememberUpdatedState(onFontSelected)
    val showFailure: (FontSpec, FontFailure) -> Unit = { spec, reason ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(resources.getString(reason.messageRes(), spec.displayName))
        }
    }
    val currentShowFailure by rememberUpdatedState(showFailure)
    val session = remember(repository, scope) {
        FontPickerSession(
            scope = scope,
            ensureLoaded = repository::ensureLoaded,
            commit = { id -> currentOnFontSelected(id) },
            onFailed = { spec, reason -> currentShowFailure(spec, reason) }
        )
    }

    var query by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf<FontKind?>(null) }
    var previewId by remember { mutableStateOf(selectedId) }
    val previewSpec = FontCatalog.find(previewId) ?: FontCatalog.default
    val groups = remember(query, kind, gmsAvailable) { FontCatalog.groups(query, kind, gmsAvailable) }

    // Open with the selected family in view (the item before it, so a sticky header does not cover it).
    val initialItem = remember { (groups.listIndexOf(selectedId)?.minus(1) ?: 0).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialItem)
    LaunchedEffect(listState) {
        snapshotFlow { query to kind }.drop(1).collect { listState.scrollToItem(0) }
    }

    BackHandler(onBack = onDismiss)

    val title = stringResource(R.string.select_font)
    Surface(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                paneTitle = title
                isTraversalGroup = true
            },
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().imePadding()) {
                FontPickerTopBar(title = title, onDismiss = onDismiss)
                FontSpecimenCard(
                    spec = previewSpec,
                    sampleReady = !previewSpec.needsDownload || states.isReady(previewSpec.id, FontCoverage.Sample),
                    applied = previewSpec.id == selectedId,
                    rowState = states.rowState(previewSpec.id),
                    onSampleWanted = { session.prefetchSample(previewSpec) },
                    onApply = { session.select(previewSpec) }
                )
                VoidSearchField(
                    query = query,
                    onQueryChange = { query = it },
                    hint = stringResource(R.string.font_search_hint),
                    modifier = Modifier.padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing),
                    clearDescription = stringResource(R.string.font_search_clear)
                )
                FontKindChips(selected = kind, onSelected = { kind = it })
                LazyColumn(modifier = Modifier.fillMaxSize().selectableGroup(), state = listState) {
                    if (groups.isEmpty()) {
                        item(key = "no_matches") { FontNoMatches() }
                    }
                    groups.forEach { group ->
                        stickyHeader(key = "header_${group.source}") { FontGroupHeader(group.source) }
                        if (group.needsProvider) {
                            item(key = "provider_unavailable") { FontProviderUnavailableRow() }
                        } else {
                            items(group.fonts, key = { it.id }) { spec ->
                                FontRow(
                                    spec = spec,
                                    applied = spec.id == selectedId,
                                    previewed = spec.id == previewId,
                                    rowState = states.rowState(spec.id),
                                    sampleReady = states.isReady(spec.id, FontCoverage.Sample),
                                    onSampleWanted = { session.prefetchSample(spec) },
                                    onClick = { previewId = spec.id }
                                )
                            }
                        }
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).imePadding()
            )
        }
    }
}

/**
 * The previewed family drawn the way Home uses it: the clock, app labels and a pangram with digits.
 * A downloadable family is drawn in its own face once its sample load finished, until then in the
 * default face with a progress hint.
 */
@Composable
private fun FontSpecimenCard(
    spec: FontSpec,
    sampleReady: Boolean,
    applied: Boolean,
    rowState: FontRowState,
    onSampleWanted: () -> Unit,
    onApply: () -> Unit
) {
    if (!sampleReady) {
        LaunchedEffect(spec.id) { onSampleWanted() }
    }
    val family = if (sampleReady) FontFamilies.familyFor(spec, FontCoverage.Sample) else null
    val downloading = rowState == FontRowState.Downloading
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing)
    ) {
        Column(modifier = Modifier.padding(VoidDimens.sectionSpacing)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = spec.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (!sampleReady) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(VoidDimens.fontStateIconSize),
                        strokeWidth = VoidDimens.fontProgressStrokeWidth
                    )
                }
            }
            Text(
                text = stringResource(R.string.font_specimen_clock),
                style = MaterialTheme.typography.displayMedium,
                fontFamily = family,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.font_specimen_apps),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = family,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.font_specimen_pangram),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = family,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(top = VoidDimens.compactSpacing)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VoidDimens.rowSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fontSupportingText(spec),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onApply, enabled = !applied && !downloading) {
                    if (downloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            strokeWidth = VoidDimens.fontProgressStrokeWidth
                        )
                    } else {
                        Text(stringResource(if (applied) R.string.font_applied else R.string.font_apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun FontPickerTopBar(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = VoidDimens.screenPadding, end = VoidDimens.rowSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.font_picker_close))
        }
    }
}

@Composable
private fun FontKindChips(selected: FontKind?, onSelected: (FontKind?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = VoidDimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(VoidDimens.rowSpacing)
    ) {
        FontKindChip(
            selected = selected == null,
            label = stringResource(R.string.font_kind_all),
            onClick = { onSelected(null) }
        )
        FontKind.entries.forEach { kind ->
            FontKindChip(
                selected = selected == kind,
                label = stringResource(kind.labelRes()),
                onClick = { onSelected(kind) }
            )
        }
    }
}

@Composable
private fun FontKindChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        // The checkmark says "selected" without relying on the tonal fill alone, as Material 3 filter chips do.
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        }
    )
}

@Composable
private fun FontGroupHeader(source: FontSource) {
    Text(
        text = stringResource(source.labelRes()),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = VoidDimens.screenPadding, vertical = VoidDimens.rowSpacing)
            .semantics { heading() }
    )
}

@Composable
private fun FontProviderUnavailableRow() {
    ListItem(
        headlineContent = { Text(stringResource(R.string.font_provider_unavailable)) },
        leadingContent = { Icon(Icons.Outlined.CloudOff, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun FontNoMatches() {
    Text(
        text = stringResource(R.string.font_no_matches),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(VoidDimens.screenPadding)
    )
}

@Composable
private fun FontRow(
    spec: FontSpec,
    applied: Boolean,
    previewed: Boolean,
    rowState: FontRowState,
    sampleReady: Boolean,
    onSampleWanted: () -> Unit,
    onClick: () -> Unit
) {
    // Fonts that need a download show the name in their own face only once it is safe to compose them.
    val showOwnFace = !spec.needsDownload || sampleReady
    if (spec.needsDownload && !sampleReady) {
        LaunchedEffect(spec.id) {
            delay(SAMPLE_REQUEST_DELAY_MS)
            onSampleWanted()
        }
    }
    val stateText = fontStateDescription(spec, applied, rowState)
    val family = if (showOwnFace) FontFamilies.familyFor(spec, FontCoverage.Sample) else null

    ListItem(
        headlineContent = {
            Text(
                text = spec.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = family
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = stringResource(R.string.font_row_sample),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = family,
                    maxLines = 1
                )
                Text(fontSupportingText(spec), style = MaterialTheme.typography.bodySmall)
            }
        },
        trailingContent = { FontRowTrailingIcon(applied, rowState, spec.needsDownload) },
        colors = ListItemDefaults.colors(
            containerColor = if (previewed) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.background
            }
        ),
        modifier = Modifier
            .selectable(selected = previewed, role = Role.RadioButton, onClick = onClick)
            .semantics { stateDescription = stateText }
    )
}

// A load in flight or a failure wins over the checkmark: a selected row that is not applied yet must say so.
@Composable
private fun FontRowTrailingIcon(selected: Boolean, rowState: FontRowState, needsDownload: Boolean) {
    when {
        rowState == FontRowState.Downloading -> CircularProgressIndicator(
            modifier = Modifier.size(VoidDimens.fontStateIconSize),
            strokeWidth = VoidDimens.fontProgressStrokeWidth
        )
        rowState == FontRowState.Failed -> Icon(
            Icons.Default.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        selected -> Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        needsDownload && rowState == FontRowState.Available -> Icon(
            Icons.Outlined.CloudDownload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun fontSupportingText(spec: FontSpec): String {
    val kind = stringResource(spec.kind.labelRes())
    val weightNames = spec.weights.map { stringResource(it.weightLabelRes()) }
    val weights = weightNames.joinToString(separator = stringResource(R.string.font_weights_separator))
    return when (spec.source) {
        FontSource.Bundled -> stringResource(R.string.font_row_bundled, kind, weights)
        FontSource.System -> stringResource(R.string.font_row_system)
        FontSource.GoogleFonts -> stringResource(
            R.string.font_row_download,
            kind,
            weights,
            Formatter.formatShortFileSize(LocalContext.current, spec.approxKb * BYTES_PER_KB)
        )
    }
}

// Spoken by TalkBack in place of its own checked state, so it names both facts: the choice and the download.
@Composable
private fun fontStateDescription(spec: FontSpec, selected: Boolean, rowState: FontRowState): String {
    val selection = stringResource(if (selected) R.string.font_state_selected else R.string.font_state_not_selected)
    val download = when {
        !spec.needsDownload -> null
        rowState == FontRowState.Downloading -> R.string.font_state_downloading
        rowState == FontRowState.Failed -> R.string.font_state_failed
        rowState == FontRowState.Downloaded -> R.string.font_state_downloaded
        else -> R.string.font_state_available
    }
    return if (download == null) {
        selection
    } else {
        stringResource(R.string.font_state_combined, selection, stringResource(download))
    }
}

private const val BYTES_PER_KB = 1024L

@StringRes
internal fun Int.weightLabelRes(): Int = when (this) {
    FontWeight.Medium.weight -> R.string.font_weight_medium
    FontWeight.Bold.weight -> R.string.font_weight_bold
    else -> R.string.font_weight_regular
}

@StringRes
private fun FontFailure.messageRes(): Int = when (advice()) {
    FontFailureAdvice.CheckConnection -> R.string.font_download_failed
    FontFailureAdvice.ProviderUnavailable -> R.string.font_download_failed_provider
    FontFailureAdvice.FontMissing -> R.string.font_download_failed_missing
}

@StringRes
private fun FontKind.labelRes(): Int = when (this) {
    FontKind.Sans -> R.string.font_kind_sans
    FontKind.Serif -> R.string.font_kind_serif
    FontKind.Mono -> R.string.font_kind_mono
    FontKind.Display -> R.string.font_kind_display
}

@StringRes
private fun FontSource.labelRes(): Int = when (this) {
    FontSource.Bundled -> R.string.font_group_bundled
    FontSource.GoogleFonts -> R.string.font_group_google_fonts
    FontSource.System -> R.string.font_group_system
}
