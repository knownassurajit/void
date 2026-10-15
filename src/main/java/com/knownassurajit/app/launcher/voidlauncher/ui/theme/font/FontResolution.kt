package com.knownassurajit.app.launcher.voidlauncher.ui.theme.font

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCoverage
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontResolution
import com.knownassurajit.app.launcher.voidlauncher.data.font.isReady
import com.knownassurajit.app.launcher.voidlauncher.helper.FeatureAvailability
import com.knownassurajit.app.launcher.voidlauncher.helper.FontRepository

/**
 * Resolves the saved font id to the family the theme may use right now.
 *
 * A downloadable family is requested here, from the main-process composition (never from `Application`, which
 * also runs in the accessibility process), and is only returned once its full load succeeded. Until then, and
 * whenever the provider is missing, the default family is used and the saved pref is left untouched. The request
 * is repeated on every start of the screen, so a download that failed offline is retried on the next return.
 */
@Composable
fun rememberFontResolution(appFont: String): FontResolution {
    val context = LocalContext.current
    val repository = remember(context) { FontRepository.get(context) }
    // The provider check is a Binder call on the first-frame path: only a downloadable saved family needs it.
    val needsProvider = FontCatalog.needsProvider(appFont)
    val gmsAvailable = remember(context, needsProvider) {
        !needsProvider || FeatureAvailability.isGoogleFontsProviderAvailable(context)
    }
    val downloadable = FontCatalog.find(appFont)?.takeIf { it.needsDownload && gmsAvailable }

    val states by repository.states.collectAsState()
    val ready by remember(downloadable?.id) {
        derivedStateOf { downloadable != null && states.isReady(downloadable.id, FontCoverage.Full) }
    }

    if (downloadable != null) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(downloadable.id, lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.ensureLoaded(downloadable, FontCoverage.Full).await()
            }
        }
    }

    val resolution = FontCatalog.resolve(appFont, gmsAvailable) { ready }
    LaunchedEffect(appFont, resolution.reason) { repository.reportFallback(appFont, resolution) }
    return resolution
}
