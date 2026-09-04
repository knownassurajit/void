package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.UserManager
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
import com.knownassurajit.app.launcher.voidlauncher.ui.components.ChildScreenBackHandler
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe
import com.knownassurajit.app.launcher.voidlauncher.helper.getAppsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import kotlin.math.abs
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knownassurajit.app.launcher.voidlauncher.helper.AppCacheManager
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceHelper
import com.knownassurajit.app.launcher.voidlauncher.ui.components.VoidSectionDivider
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability

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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            if (FeatureAvailability.isPrivateSpaceAvailable(context) &&
                prefs.privateSpaceEnabled &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
            ) {
                val profile = PrivateSpaceHelper.getPrivateSpaceProfile(context)
                hasPrivateSpace = profile != null
                if (profile != null) {
                    isPrivateSpaceLocked = PrivateSpaceHelper.isQuietModeEnabled(context)
                    scope.launch {
                        try {
                            val pApps = PrivateSpaceHelper.loadPrivateSpaceApps(context, prefs)
                            privateApps.clear()
                            privateApps.addAll(pApps)
                        } catch (_: Exception) {
                            privateApps.clear()
                        }
                    }
                }
            } else {
                hasPrivateSpace = false
            }
        } catch (_: Exception) {
            hasPrivateSpace = false
            privateApps.clear()
        }
    }

    DisposableEffect(Unit) {
        if (false) {
            onDispose {}
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    if (!prefs.privateSpaceEnabled) return
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        val profile = PrivateSpaceHelper.getPrivateSpaceProfile(context)
                        if (profile != null) {
                            val um = context.getSystemService(Context.USER_SERVICE) as UserManager
                            isPrivateSpaceLocked = um.isQuietModeEnabled(profile)
                            if (!isPrivateSpaceLocked) {
                                scope.launch {
                                    val pApps = PrivateSpaceHelper.loadPrivateSpaceApps(context, prefs)
                                    privateApps.clear()
                                    privateApps.addAll(pApps)
                                }
                            } else {
                                privateApps.clear()
                            }
                        } else {
                            isPrivateSpaceLocked = true
                            privateApps.clear()
                            hasPrivateSpace = false
                        }
                    }
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
                addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                if (Build.VERSION.SDK_INT >= 35) {
                    addAction(Intent.ACTION_PROFILE_ACCESSIBLE)
                    addAction(Intent.ACTION_PROFILE_INACCESSIBLE)
                }
            }
            androidx.core.content.ContextCompat.registerReceiver(context, receiver, filter, androidx.core.content.ContextCompat.RECEIVER_EXPORTED)
            onDispose {
                context.unregisterReceiver(receiver)
            }
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

    ChildScreenBackHandler(onBack)

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
                onTogglePrivateSpace = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        PrivateSpaceHelper.togglePrivateSpace(context)
                        isPrivateSpaceLocked = !isPrivateSpaceLocked
                    }
                },
                onOpenPrivateSpaceSettings = {
                    val addEntry = privateApps.firstOrNull { PrivateSpaceHelper.isAddEntry(it) }
                    if (addEntry != null) onAppClick(addEntry)
                }
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
                privateSpaceEnabled = prefs.privateSpaceEnabled,
                appDrawerTextSizeScale = prefs.appDrawerTextSizeScale,
                showBottomPrivateHeader = !privateInSearchBar,
                onAppClick = onAppClick,
                onTogglePrivateSpace = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        PrivateSpaceHelper.togglePrivateSpace(context)
                        isPrivateSpaceLocked = !isPrivateSpaceLocked
                        if (!isPrivateSpaceLocked) {
                            scope.launch {
                                val pApps = PrivateSpaceHelper.loadPrivateSpaceApps(context, prefs)
                                privateApps.clear()
                                privateApps.addAll(pApps)
                            }
                        } else {
                            privateApps.clear()
                        }
                    }
                }
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
    onOpenPrivateSpaceSettings: () -> Unit
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
                IconButton(onClick = onTogglePrivateSpace) {
                    Icon(
                        imageVector = if (isPrivateSpaceLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        contentDescription = stringResource(
                            if (isPrivateSpaceLocked) R.string.unlock_private_space
                            else R.string.lock_private_space
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onOpenPrivateSpaceSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = stringResource(R.string.private_space_settings),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
    privateSpaceEnabled: Boolean,
    appDrawerTextSizeScale: Float,
    showBottomPrivateHeader: Boolean,
    onAppClick: (AppModel) -> Unit,
    onTogglePrivateSpace: () -> Unit
) {
    val addEntry = filteredPrivateApps.firstOrNull { PrivateSpaceHelper.isAddEntry(it) }
    val privateOnly = filteredPrivateApps.filterNot { PrivateSpaceHelper.isAddEntry(it) }
    val showPrivateSection = privateSpaceEnabled && hasPrivateSpace

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = VoidDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (showPrivateSpaceInSearch) {
            item(key = "private_search_result") {
                AppDrawerPrivateSpaceSearchRow(
                    isLocked = isPrivateSpaceLocked,
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
            if (showBottomPrivateHeader) {
            item(key = "private_divider") {
                VoidSectionDivider()
            }
            item(key = "private_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VoidDimens.rowSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(VoidDimens.rowSpacing))
                    Text(
                        text = stringResource(R.string.private_space),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onTogglePrivateSpace) {
                        Icon(
                            imageVector = if (isPrivateSpaceLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            contentDescription = stringResource(
                                if (isPrivateSpaceLocked) R.string.unlock_private_space
                                else R.string.lock_private_space
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (addEntry != null) {
                        IconButton(onClick = { onAppClick(addEntry) }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.private_space_settings),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                contentDescription = "Private App",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppDrawerPrivateSpaceSearchRow(
    isLocked: Boolean,
    onClick: () -> Unit
) {
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.private_space),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isLocked) "Tap to unlock" else "Tap to lock",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Private Space helpers moved to PrivateSpaceHelper ──
