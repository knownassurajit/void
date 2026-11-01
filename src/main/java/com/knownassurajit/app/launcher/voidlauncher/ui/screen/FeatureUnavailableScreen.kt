package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.ui.components.PanelMessage
import com.knownassurajit.app.launcher.voidlauncher.ui.components.screenBackSwipe

/** Shown in place of a panel whose capability this device lacks: the panel's [title], why it is missing, and a way back. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureUnavailableScreen(
    title: String,
    message: String,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.screenBackSwipe(onBack),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                }
            )
        }
    ) { padding ->
        PanelMessage(
            title = stringResource(R.string.feature_unavailable),
            body = message,
            modifier = Modifier.padding(padding)
        )
    }
}
