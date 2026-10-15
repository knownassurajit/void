package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.knownassurajit.app.launcher.voidlauncher.LocalFixedStatusBarHeight
import com.knownassurajit.app.launcher.voidlauncher.BuildConfig
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.HomeColor
import com.knownassurajit.app.launcher.voidlauncher.ui.components.privateSpaceColors
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.AppModel
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.helper.getAppsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import kotlin.math.abs
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knownassurajit.app.launcher.voidlauncher.helper.AppCacheManager
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.PrivateSpaceControls
import com.knownassurajit.app.launcher.voidlauncher.ui.components.PrivateSpaceHeader
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.helper.ToggleOutcome
import com.knownassurajit.app.launcher.voidlauncher.helper.showToast

@Composable
fun AppDrawerScreen(
    onBack: () -> Unit,
    onAppClick: (AppModel) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    privateSpacePlacement: String = Prefs.PrivateSpacePlacement.BOTTOM
) {
    val context = LocalContext.current
    val prefs = remember { Prefs.get(context) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val allApps by AppCacheManager.appCacheFlow.collectAsStateWithLifecycle(emptyList())
    val privateApps = remember { mutableStateListOf<AppModel>() }
    var isPrivateSpaceLocked by remember { mutableStateOf(true) }
    var hasPrivateSpace by remember { mutableStateOf(false) }
    val showAlphabetCategories = remember { prefs.showAlphabetCategories }
    val privateIconColor = remember { HomeColor.parse(prefs.privateSpaceIconColor) }
    val focusRequester = remember { FocusRequester() }

    val privateSpaceActive = PrivateSpaceHelper.isEnabled(context, prefs)
    var refreshJob by remember { mutableStateOf<Job?>(null) }
    var toggleJob by remember { mutableStateOf<Job?>(null) }

    // The system's quiet mode is the only source of truth for the lock state: every trigger (open,
    // resume, broadcast, finished toggle) funnels through here and nothing else writes it. A newer
    // refresh cancels the older one, and state is written only after the last suspension point, so
    // a cancelled or overlapping load can never leave a stale or half-applied result behind.
    val refreshPrivateSpace: () -> Unit = remember {
        {
            refreshJob?.cancel()
            refreshJob = scope.launch {
                val locked = if (PrivateSpaceHelper.isEnabled(context, prefs)) {
                    PrivateSpaceHelper.queryLocked(context)
                } else {
                    null
                }
                val apps = if (locked == false) {
                    PrivateSpaceHelper.loadPrivateSpaceApps(context, prefs)
                } else {
                    emptyList()
                }
                hasPrivateSpace = locked != null
                isPrivateSpaceLocked = locked ?: true
                privateApps.clear()
                privateApps.addAll(apps)
            }
        }
    }

    // A lock/unlock request only asks the system; it never decides what the UI shows. An unlock
    // that needs credentials returns without changing anything (the system prompt is the feedback),
    // so the outcome is used only to report a refused request, then the real state is re-read.
    val onTogglePrivateSpace: () -> Unit = remember {
        {
            if (toggleJob?.isActive != true) {
                toggleJob = scope.launch {
                    val outcome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        PrivateSpaceHelper.togglePrivateSpace(context)
                    } else {
                        ToggleOutcome.FAILED
                    }
                    if (outcome == ToggleOutcome.FAILED) {
                        context.showToast(R.string.private_space_toggle_failed)
                    }
                    refreshPrivateSpace()
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshPrivateSpace() }

    // Profile broadcasts are deferred while the process is cached, so also re-query on resume.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refreshPrivateSpace() }

    DisposableEffect(privateSpaceActive) {
        // Nothing is registered below API 35 or when the feature is off, so there is nothing to
        // unregister either.
        if (!privateSpaceActive || Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return@DisposableEffect onDispose {}
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                refreshPrivateSpace()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            addAction(Intent.ACTION_PROFILE_ACCESSIBLE)
            addAction(Intent.ACTION_PROFILE_INACCESSIBLE)
            addAction(Intent.ACTION_PROFILE_ADDED)
            addAction(Intent.ACTION_PROFILE_REMOVED)
        }
        // Profile broadcasts come from the system, which still reaches a non-exported receiver.
        androidx.core.content.ContextCompat.registerReceiver(context, receiver, filter, androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val privateInSearchBar = privateSpacePlacement == Prefs.PrivateSpacePlacement.SEARCH_BAR
    val filteredApps by remember {
        derivedStateOf {
            val list = allApps.toList().filterNot { PrivateSpaceHelper.isAddEntry(it) }
            val collator = Collator.getInstance()
            val sorted = list.sortedWith(compareBy(collator) { it.appLabel })
            if (searchQuery.isBlank()) sorted
            else sorted.filter { it.appLabel.contains(searchQuery, ignoreCase = true) }
        }
    }
    val filteredPrivateApps by remember {
        derivedStateOf {
            val list = privateApps.toList()
            if (searchQuery.isBlank()) list
            else list.filter { it.appLabel.contains(searchQuery, ignoreCase = true) }
        }
    }
    // Derived from the unfiltered list so a search query never hides the settings action.
    val privateSettingsEntry by remember {
        derivedStateOf { privateApps.firstOrNull { PrivateSpaceHelper.isAddEntry(it) } }
    }
    val showPrivateSpaceInSearch by remember {
        derivedStateOf {
            hasPrivateSpace && searchQuery.isNotBlank() &&
                "private space".contains(searchQuery, ignoreCase = true)
        }
    }
    val groupedApps by remember {
        derivedStateOf {
            filteredApps.groupBy { it.appLabel.firstOrNull()?.uppercase() ?: "#" }.toSortedMap()
        }
    }

    val hasPrivateSpaceSettings by remember {
        derivedStateOf { privateSettingsEntry != null || PrivateSpaceHelper.isPrivateSpaceSupported(context) }
    }
    val onOpenPrivateSpaceSettings: () -> Unit = remember {
        {
            val entry = privateSettingsEntry
            if (entry != null) {
                onAppClick(entry)
            } else {
                try {
                    context.startActivity(Intent("android.settings.PRIVATE_SPACE_SETTINGS").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {
                    try {
                        context.startActivity(Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } catch (_: Exception) {}
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            .imePadding()
            .screenBackSwipe(onBack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppDrawerSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                focusRequester = focusRequester,
                onOpenSettings = onOpenSettings,
                showPrivateSpaceControls = privateInSearchBar && hasPrivateSpace && prefs.privateSpaceEnabled,
                isPrivateSpaceLocked = isPrivateSpaceLocked,
                onTogglePrivateSpace = onTogglePrivateSpace,
                hasPrivateSpaceSettings = hasPrivateSpaceSettings,
                onOpenPrivateSpaceSettings = onOpenPrivateSpaceSettings,
                privateIconColor = privateIconColor
            )

            LaunchedEffect(Unit) {
                if (!prefs.autoShowKeyboard) return@LaunchedEffect
                // Wait until the search field FocusRequester is attached to avoid
                // IllegalStateException during the drawer enter transition.
                kotlinx.coroutines.delay(320)
                try {
                    focusRequester.requestFocus()
                } catch (_: IllegalStateException) {
                    // FocusRequester not ready — degrade silently
                } catch (_: Exception) {
                    // Ignore soft-keyboard focus failures
                }
            }

            AppDrawerAppList(
                groupedApps = groupedApps,
                showAlphabetCategories = showAlphabetCategories,
                showPrivateSpaceInSearch = showPrivateSpaceInSearch,
                isPrivateSpaceLocked = isPrivateSpaceLocked,
                hasPrivateSpace = hasPrivateSpace,
                filteredPrivateApps = filteredPrivateApps,
                settingsEntry = privateSettingsEntry,
                hasPrivateSpaceSettings = hasPrivateSpaceSettings,
                privateSpaceEnabled = prefs.privateSpaceEnabled,
                appDrawerTextSizeScale = prefs.appDrawerTextSizeScale,
                showBottomPrivateHeader = !privateInSearchBar,
                onAppClick = onAppClick,
                onTogglePrivateSpace = onTogglePrivateSpace,
                onOpenPrivateSpaceSettings = onOpenPrivateSpaceSettings,
                privateIconColor = privateIconColor,
                onCloseByPull = onBack
            )
        }
    }
}

@Composable
private fun AppDrawerSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onOpenSettings: () -> Unit,
    showPrivateSpaceControls: Boolean,
    isPrivateSpaceLocked: Boolean,
    onTogglePrivateSpace: () -> Unit,
    hasPrivateSpaceSettings: Boolean,
    onOpenPrivateSpaceSettings: () -> Unit,
    privateIconColor: HomeColor
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VoidDimens.screenPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VoidDimens.sectionSpacing)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        stringResource(R.string.search),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (showPrivateSpaceControls) {
                PrivateSpaceControls(
                    isLocked = isPrivateSpaceLocked,
                    hasSettings = hasPrivateSpaceSettings,
                    onToggle = onTogglePrivateSpace,
                    onOpenSettings = onOpenPrivateSpaceSettings,
                    iconColor = privateIconColor
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        VoidSectionDivider()
    }
}

@Composable
private fun ColumnScope.AppDrawerAppList(
    groupedApps: Map<String, List<AppModel>>,
    showAlphabetCategories: Boolean,
    showPrivateSpaceInSearch: Boolean,
    isPrivateSpaceLocked: Boolean,
    hasPrivateSpace: Boolean,
    filteredPrivateApps: List<AppModel>,
    settingsEntry: AppModel?,
    hasPrivateSpaceSettings: Boolean,
    privateSpaceEnabled: Boolean,
    appDrawerTextSizeScale: Float,
    showBottomPrivateHeader: Boolean,
    onAppClick: (AppModel) -> Unit,
    onTogglePrivateSpace: () -> Unit,
    onOpenPrivateSpaceSettings: () -> Unit,
    privateIconColor: HomeColor,
    onCloseByPull: () -> Unit
) {
    val privateOnly = filteredPrivateApps.filterNot { PrivateSpaceHelper.isAddEntry(it) }
    val showPrivateSection = privateSpaceEnabled && hasPrivateSpace
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val closeLatest = rememberUpdatedState(onCloseByPull)
    val scrollConnection = remember(density) {
        DrawerScrollConnection(
            closePullPx = with(density) { VoidDimens.drawerClosePull.toPx() },
            closeVelocityPx = with(density) { VoidDimens.drawerCloseVelocity.toPx() },
            onUserScroll = {
                // Scrolling means browsing, not typing: the keyboard goes away even when it opened by itself.
                keyboard?.hide()
                focusManager.clearFocus()
            },
            onClose = { closeLatest.value() }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .nestedScroll(scrollConnection)
            .padding(horizontal = VoidDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (showPrivateSpaceInSearch) {
            item(key = "private_search_result") {
                AppDrawerPrivateSpaceSearchRow(
                    isLocked = isPrivateSpaceLocked,
                    iconColor = privateIconColor,
                    onClick = onTogglePrivateSpace
                )
                VoidSectionDivider()
            }
        }

        groupedApps.forEach { (letter, apps) ->
            if (showAlphabetCategories) {
                item(key = "header_$letter") {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                        modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
                    )
                }
            }
            itemsIndexed(
                items = apps,
                key = { index, app ->
                    "drawer_${letter}_${app.appPackage}_${app.user}_${app.id}_$index"
                }
            ) { _, app ->
                AppDrawerItem(
                    app = app,
                    textSizeScale = appDrawerTextSizeScale,
                    onClick = { onAppClick(app) }
                )
            }
        }

        if (showPrivateSection) {
            // Bottom placement: the header carries the controls. Search-bar placement: the controls sit
            // next to the search field and the header only labels the private apps while they show.
            if (showBottomPrivateHeader || !isPrivateSpaceLocked) {
                item(key = "private_divider") {
                    VoidSectionDivider()
                }
                item(key = "private_header") {
                    PrivateSpaceHeader(
                        iconColor = privateIconColor,
                        controls = if (showBottomPrivateHeader) {
                            {
                                PrivateSpaceControls(
                                    isLocked = isPrivateSpaceLocked,
                                    hasSettings = hasPrivateSpaceSettings,
                                    onToggle = onTogglePrivateSpace,
                                    onOpenSettings = onOpenPrivateSpaceSettings,
                                    iconColor = privateIconColor
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
            if (!isPrivateSpaceLocked) {
                itemsIndexed(
                    items = privateOnly,
                    key = { index, app -> "private_${app.id}#$index" }
                ) { _, app ->
                    AppDrawerItem(
                        app = app,
                        textSizeScale = appDrawerTextSizeScale,
                        isPrivate = true,
                        privateIconColor = privateIconColor,
                        onClick = { onAppClick(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppDrawerItem(
    app: AppModel,
    textSizeScale: Float,
    isPrivate: Boolean = false,
    privateIconColor: HomeColor = HomeColor.Default,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = app.appLabel,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * textSizeScale
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isPrivate) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = stringResource(R.string.private_app),
                tint = privateSpaceColors(privateIconColor).accent
            )
        }
    }
}

@Composable
private fun AppDrawerPrivateSpaceSearchRow(
    isLocked: Boolean,
    iconColor: HomeColor,
    onClick: () -> Unit
) {
    val accent = privateSpaceColors(iconColor).accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = accent
        )
        Spacer(modifier = Modifier.width(VoidDimens.rowSpacing))
        Column {
            Text(
                text = stringResource(R.string.private_space),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(if (isLocked) R.string.private_space_tap_to_unlock else R.string.private_space_tap_to_lock),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
            contentDescription = null,
            tint = accent
        )
    }
}

// ── Private Space helpers moved to PrivateSpaceHelper ──

/**
 * Hides the keyboard as soon as the user scrolls the list, and closes the drawer when the user pulls
 * down past the top of the list: farther than [closePullPx], or released fast downward after pulling
 * at the top. A fling that only reaches the top on its own never closes it, because only a drag at
 * the top counts as a pull.
 */
internal class DrawerScrollConnection(
    private val closePullPx: Float,
    private val closeVelocityPx: Float,
    private val onUserScroll: () -> Unit,
    private val onClose: () -> Unit
) : NestedScrollConnection {
    private var pullPx = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source == NestedScrollSource.UserInput && available.y != 0f) onUserScroll()
        return Offset.Zero
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        // Down movement the list could not use: the list is at its top and the finger keeps pulling.
        if (source == NestedScrollSource.UserInput) {
            pullPx = if (available.y > 0f) pullPx + available.y else 0f
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val pulled = pullPx
        pullPx = 0f
        if (shouldClose(pulled, available.y, closePullPx, closeVelocityPx)) onClose()
        return Velocity.Zero
    }

    companion object {
        fun shouldClose(pulledPx: Float, releaseVelocityY: Float, closePullPx: Float, closeVelocityPx: Float): Boolean =
            pulledPx >= closePullPx || (pulledPx > 0f && releaseVelocityY >= closeVelocityPx)
    }
}
