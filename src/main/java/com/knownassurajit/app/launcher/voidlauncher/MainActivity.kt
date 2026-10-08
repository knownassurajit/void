package com.knownassurajit.app.launcher.voidlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.knownassurajit.app.launcher.voidlauncher.data.AppModel
import com.knownassurajit.app.launcher.voidlauncher.helper.NotificationService
import com.knownassurajit.app.launcher.voidlauncher.helper.getUserHandleFromString
import com.knownassurajit.app.launcher.voidlauncher.helper.openCalendar
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.AppDrawerScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.HomeScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.NotesScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.NotificationSummaryScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.NotificationsScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.SettingsScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.screen.WidgetsScreen
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.LocalContentSwipeToBack
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavMotion
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavPanel
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelOrigin
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAppTheme
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidDimens
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.isAtRest
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.rememberVoidMotionScheme
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.shouldPopPanel

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * Best-effort access to the hidden `StatusBarManager.expandNotificationsPanel` method.
 * The class and method are on Android's non-SDK greylist and may be blocked on
 * `targetSdk` 28+. We cache the result of the first lookup so we don't repeatedly
 * pay the reflection cost — and so a single failure permanently disables the path
 * instead of spamming the log on every gesture.
 */
private object StatusBarPanelOpener {
    private const val TAG = "StatusBarPanelOpener"

    @Volatile private var resolved = false
    @Volatile private var method: java.lang.reflect.Method? = null

    fun expandNotificationsPanel(context: Context) {
        if (!resolved) synchronized(this) {
            if (!resolved) {
                method = try {
                    @Suppress("PrivateApi")
                    Class.forName("android.app.StatusBarManager")
                        .getMethod("expandNotificationsPanel")
                } catch (t: Throwable) {
                    Log.w(TAG, "StatusBarManager.expandNotificationsPanel unavailable: ${t.javaClass.simpleName}")
                    null
                }
                resolved = true
            }
        }
        val m = method ?: return
        try {
            m.invoke(context.getSystemService("statusbar"))
        } catch (t: Throwable) {
            Log.w(TAG, "expandNotificationsPanel invoke failed: ${t.javaClass.simpleName}")
        }
    }
}

/** Physical status bar height, available to all screens regardless of bar visibility. */
val LocalFixedStatusBarHeight = compositionLocalOf<Dp> { 24.dp }

class MainActivity : ComponentActivity() {
    private val appReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let { ctx ->
                lifecycleScope.launch { com.knownassurajit.app.launcher.voidlauncher.helper.AppCacheManager.syncCache(ctx) }
            }
        }
    }

    private val viewModel: MainUiViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            com.knownassurajit.app.launcher.voidlauncher.helper.AppCacheManager.initializeCache(this@MainActivity)
        }

        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(appReceiver, pkgFilter)
        androidx.core.content.ContextCompat.registerReceiver(this, appReceiver, pkgFilter, androidx.core.content.ContextCompat.RECEIVER_EXPORTED)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            VoidAppTheme(appFont = uiState.appFont) {
                // Capture the physical status bar height — this returns the
                // hardware inset even before the bar is programmatically hidden.
                val density = LocalDensity.current
                val statusBarHeightDp = with(density) {
                    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                    val heightPx = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
                    if (heightPx > 0) (heightPx / density.density).dp else 24.dp
                }

                CompositionLocalProvider(LocalFixedStatusBarHeight provides statusBarHeightDp) {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val notifications by NotificationService.notificationsState
                    .collectAsStateWithLifecycle(initialValue = emptyList())
                val currentRoute = currentBackStackEntry.routeKind()
                val allowTopEdgeNotificationExpansion =
                    uiState.showStatusBar && (currentRoute == NavRouteKind.Home)

                LaunchedEffect(uiState.showStatusBar) {
                    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                    if (uiState.showStatusBar) {
                        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
                        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                    } else {
                        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
                        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }

                val motion = rememberVoidMotionScheme(uiState.animationSpeed)
                val predictiveBackEdgeMarginPx = with(density) { VoidDimens.predictiveBackEdgeMargin.toPx() }

                // Settings writes its preferences straight to Prefs, so Home reads them again whenever
                // Settings is left: by its own back arrow, the Back key, TalkBack, a swipe or the
                // system back gesture (which no longer passes through a callback of the screen).
                LaunchedEffect(Unit) {
                    var previousRoute: NavRouteKind? = null
                    snapshotFlow { currentBackStackEntry.routeKind() }.collect { route ->
                        if (NavRouteKind.settingsClosed(previousRoute, route)) viewModel.refreshFromPrefs()
                        previousRoute = route
                    }
                }
                val leftRoute = NavRouteKind.forSwipeAction(uiState.leftSwipeAction)
                val rightRoute = NavRouteKind.forSwipeAction(uiState.rightSwipeAction)
                fun edgeOf(route: NavRouteKind?) = PanelOrigin.resolve(route, leftRoute, rightRoute)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (allowTopEdgeNotificationExpansion) {
                                Modifier.pointerInput(statusBarHeightDp) {
                                    val topEdgePx = statusBarHeightDp.toPx() * 1.5f
                                    awaitEachGesture {
                                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                        if (down.position.y >= topEdgePx) {
                                            return@awaitEachGesture
                                        }
                                        var totalY = 0f
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val change = event.changes.firstOrNull() ?: break
                                            totalY += change.position.y - change.previousPosition.y
                                            if (totalY > 120f) {
                                                StatusBarPanelOpener.expandNotificationsPanel(this@MainActivity)
                                                break
                                            }
                                            if (!change.pressed) break
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    CompositionLocalProvider(
                        LocalContentSwipeToBack provides uiState.contentSwipeToBack
                    ) {
                    // All four transitions are keyed on the PANEL, the screen opening or closing:
                    // targetState on a push, initialState on a pop. The screen under the panel
                    // plays exit/popEnter with the panel's edge so both layers move together.
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        enterTransition = { NavMotion.enter(edgeOf(targetState.routeKind()), motion) },
                        exitTransition = { NavMotion.exit(edgeOf(targetState.routeKind()), motion) },
                        popEnterTransition = { NavMotion.popEnter(edgeOf(initialState.routeKind()), motion) },
                        popExitTransition = { NavMotion.popExit(edgeOf(initialState.routeKind()), motion) },
                        // The system back gesture (Android 14+) plays the Material predictive-back
                        // animation for every route, and so does a Back key press that lasts longer
                        // than a frame (the platform starts the same predictive back for it, with
                        // the left edge, and the lambdas cannot tell them apart). API 26-33 and
                        // in-app pops (the back arrow, a swipe in a panel, TalkBack's Dismiss) play
                        // the pop pair above.
                        predictivePopEnterTransition = { NavMotion.predictivePopEnter(motion) },
                        predictivePopExitTransition = { swipeEdge ->
                            NavMotion.predictivePopExit(swipeEdge, motion, predictiveBackEdgeMarginPx)
                        }
                    ) {
                        composable<HomeRoute> {
                            // Home is at rest once no transition moves it any more: a swipe that starts
                            // while a panel is still leaving would stack a second panel on top.
                            val atRest = isAtRest(transition.currentState, transition.targetState)
                            HomeScreen(
                                state = uiState,
                                isCurrentDestination = currentRoute == NavRouteKind.Home,
                                isSettled = atRest,
                                onOpenApps = { navController.navigate(AppDrawerRoute) { launchSingleTop = true } },
                                onOpenSettings = { navController.navigate(SettingsRoute) { launchSingleTop = true } },
                                onOpenNotifications = {
                                    navController.navigate(NotificationPanelRoute) { launchSingleTop = true }
                                },
                                onOpenNotificationSummary = { navController.navigate(NotificationSummaryRoute) { launchSingleTop = true } },
                                onOpenWidgets = { navController.navigate(WidgetsRoute) { launchSingleTop = true } },
                                onOpenNotes = { navController.navigate(NotesRoute) { launchSingleTop = true } },
                                onLaunchSwipeApp = { packageName, enterFrom ->
                                    mainViewModel.launchSwipeApp(packageName, enterFrom)
                                },
                                onAppClick = { app -> launchHomeApp(app) },
                                onClockClick = { mainViewModel.setDefaultClockApp() },
                                onDateClick = { openCalendar(this@MainActivity) },
                                onHomeAppsChanged = { viewModel.refreshFromPrefs() }
                            )
                        }
                    composable<AppDrawerRoute> { backStackEntry ->
                        NavPanel(edgeOf(NavRouteKind.AppDrawer)) {
                            AppDrawerScreen(
                                onBack = { navController.popPanel(backStackEntry) },
                                onAppClick = { app ->
                                    mainViewModel.selectedApp(app, com.knownassurajit.app.launcher.voidlauncher.data.Constants.FLAG_LAUNCH_APP)
                                },
                                onOpenSettings = { navController.navigate(SettingsRoute) { launchSingleTop = true } },
                                privateSpacePlacement = uiState.privateSpacePlacement
                            )
                        }
                    }
                    composable<SettingsRoute> { backStackEntry ->
                        NavPanel(edgeOf(NavRouteKind.Settings)) {
                            SettingsScreen(onBack = { navController.popPanel(backStackEntry) })
                        }
                    }
                    composable<NotificationPanelRoute> { backStackEntry ->
                        NavPanel(edgeOf(NavRouteKind.NotificationPanel)) {
                            NotificationsScreen(
                                notifications = notifications,
                                onBack = { navController.popPanel(backStackEntry) }
                            )
                        }
                    }
                    composable<NotificationSummaryRoute> { backStackEntry ->
                        NavPanel(edgeOf(NavRouteKind.NotificationSummary)) {
                            NotificationSummaryScreen(
                                onBack = { navController.popPanel(backStackEntry) }
                            )
                        }
                    }
                    composable<WidgetsRoute> { backStackEntry ->
                        NavPanel(edgeOf(NavRouteKind.Widgets)) {
                            WidgetsScreen(
                                onBack = { navController.popPanel(backStackEntry) }
                            )
                        }
                    }
                    composable<NotesRoute> { backStackEntry ->
                        NavPanel(edgeOf(NavRouteKind.Notes)) {
                            NotesScreen(onBack = { navController.popPanel(backStackEntry) })
                        }
                    }
                    }
                    }
                }
            } // end CompositionLocalProvider
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFromPrefs()
        lifecycleScope.launch { com.knownassurajit.app.launcher.voidlauncher.helper.AppCacheManager.syncCache(this@MainActivity) }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(appReceiver)
    }

    private fun launchHomeApp(app: HomeApp) {
        val userHandle = getUserHandleFromString(this, app.userString)
        val appModel = if (app.isShortcut) {
            AppModel.PinnedShortcut(
                appLabel = app.label,
                key = null,
                appPackage = app.packageName,
                shortcutId = app.shortcutId,
                user = userHandle
            )
        } else {
            AppModel.App(
                appLabel = app.label,
                key = null,
                appPackage = app.packageName,
                activityClassName = app.activityClassName,
                user = userHandle
            )
        }
        mainViewModel.selectedApp(appModel, com.knownassurajit.app.launcher.voidlauncher.data.Constants.FLAG_LAUNCH_APP)
    }

}

/**
 * Closes [panel], the screen the dismiss came from. The panel stays on screen and touchable while
 * its exit animation plays, so a second dismiss can arrive after the screen below it is already
 * the top entry; popping then would remove that screen too (or Home, leaving the `NavHost`
 * empty). It pops only while [panel] is the top entry and a screen is left under it.
 */
private fun NavController.popPanel(panel: NavBackStackEntry) {
    if (shouldPopPanel(panelIsTop = currentBackStackEntry == panel, hasEntryBelow = previousBackStackEntry != null)) {
        popBackStack()
    }
}

private fun NavBackStackEntry?.routeKind(): NavRouteKind? {
    val destination = this?.destination ?: return null
    return when {
        destination.hasRoute<HomeRoute>() -> NavRouteKind.Home
        destination.hasRoute<AppDrawerRoute>() -> NavRouteKind.AppDrawer
        destination.hasRoute<SettingsRoute>() -> NavRouteKind.Settings
        destination.hasRoute<NotificationPanelRoute>() -> NavRouteKind.NotificationPanel
        destination.hasRoute<NotificationSummaryRoute>() -> NavRouteKind.NotificationSummary
        destination.hasRoute<WidgetsRoute>() -> NavRouteKind.Widgets
        destination.hasRoute<NotesRoute>() -> NavRouteKind.Notes
        else -> null
    }
}
