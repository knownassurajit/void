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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.knownassurajit.app.launcher.voidlauncher.helper.getAppsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import kotlin.math.abs
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knownassurajit.app.launcher.voidlauncher.helper.AppCacheManager
import com.knownassurajit.app.launcher.voidlauncher.helper.PrivateSpaceHelper

import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability

@Composable
fun AppDrawerScreen(
    onBack: () -> Unit,
    onAppClick: (AppModel) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
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
                    if (!isPrivateSpaceLocked) {
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

    val filteredApps = remember(searchQuery, allApps) {
        val list = allApps.toList()
        val collator = Collator.getInstance()
        val sorted = list.sortedWith(compareBy(collator) { it.appLabel })
        if (searchQuery.isBlank()) sorted
        else sorted.filter { it.appLabel.contains(searchQuery, ignoreCase = true) }
    }

    val filteredPrivateApps = remember(searchQuery, privateApps.toList()) {
        val list = privateApps.toList()
        if (searchQuery.isBlank()) list
        else list.filter { it.appLabel.contains(searchQuery, ignoreCase = true) }
    }

    val showPrivateSpaceInSearch = remember(searchQuery, hasPrivateSpace) {
        hasPrivateSpace && searchQuery.isNotBlank() &&
                "private space".contains(searchQuery, ignoreCase = true)
    }

    val groupedApps = remember(filteredApps) {
        filteredApps.groupBy { it.appLabel.firstOrNull()?.uppercase() ?: "#" }.toSortedMap()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = LocalFixedStatusBarHeight.current)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppDrawerSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                focusRequester = focusRequester,
                hasPrivateSpace = hasPrivateSpace,
                isPrivateSpaceLocked = isPrivateSpaceLocked,
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
                },
                onOpenSettings = onOpenSettings
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
                filteredPrivateApps = filteredPrivateApps,
                privateSpaceEnabled = prefs.privateSpaceEnabled,
                appDrawerTextSizeScale = prefs.appDrawerTextSizeScale,
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
    hasPrivateSpace: Boolean,
    isPrivateSpaceLocked: Boolean,
    onTogglePrivateSpace: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (hasPrivateSpace) {
                IconButton(onClick = onTogglePrivateSpace) {
                    Icon(
                        imageVector = if (isPrivateSpaceLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        contentDescription = "Private Space",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.AppDrawerAppList(
    groupedApps: Map<String, List<AppModel>>,
    showAlphabetCategories: Boolean,
    showPrivateSpaceInSearch: Boolean,
    isPrivateSpaceLocked: Boolean,
    filteredPrivateApps: List<AppModel>,
    privateSpaceEnabled: Boolean,
    appDrawerTextSizeScale: Float,
    onAppClick: (AppModel) -> Unit,
    onTogglePrivateSpace: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (showPrivateSpaceInSearch) {
            item(key = "private_search_result") {
                AppDrawerPrivateSpaceSearchRow(
                    isLocked = isPrivateSpaceLocked,
                    onClick = onTogglePrivateSpace
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
            items(items = apps, key = { it.id }) { app ->
                AppDrawerItem(
                    app = app,
                    textSizeScale = appDrawerTextSizeScale,
                    onClick = { onAppClick(app) }
                )
            }
        }

        if (privateSpaceEnabled && filteredPrivateApps.isNotEmpty()) {
            item(key = "private_divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
            item(key = "private_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Private Space",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(items = filteredPrivateApps, key = { "private_${it.id}" }) { app ->
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
