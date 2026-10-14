package com.knownassurajit.app.launcher.voidlauncher.helper

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import android.util.Log
import com.knownassurajit.app.launcher.voidlauncher.data.AppModel
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

object PrivateSpaceHelper {
    private const val TAG = "PrivateSpaceHelper"

    fun isAddEntry(app: AppModel): Boolean =
        PrivateSpaceUi.isAddEntryLabel(app.appLabel, (app as? AppModel.App)?.activityClassName)

    fun isPrivateSpaceSupported(context: Context): Boolean {
        return try {
            FeatureAvailability.isPrivateSpaceAvailable(context) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
                getPrivateSpaceProfile(context) != null
        } catch (e: Exception) {
            Log.w(TAG, "Private Space support check failed", e)
            false
        }
    }

    fun getPrivateSpaceProfile(context: Context): android.os.UserHandle? {
        return try {
            if (!FeatureAvailability.isPrivateSpaceAvailable(context)) return null
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return null
            val um = context.getSystemService(Context.USER_SERVICE) as? UserManager ?: return null
            val la = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return null
            um.userProfiles.firstOrNull { profile ->
                try {
                    val info = la.getLauncherUserInfo(profile)
                    info?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE
                } catch (_: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve Private Space profile", e)
            null
        }
    }

    /**
     * The one place that decides whether Private Space is active for the drawer: the OS supports it,
     * the platform capability gate passes, and the user left the feature enabled in Settings.
     */
    fun isEnabled(context: Context, prefs: Prefs): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
            FeatureAvailability.isPrivateSpaceAvailable(context) &&
            prefs.privateSpaceEnabled

    /**
     * Reads the lock state straight from the system, which is the single source of truth: null when
     * there is no private profile, true while quiet mode is on (locked), false when it is off. A read
     * failure is reported as locked so the UI fails closed. Runs the binder calls off the main thread.
     */
    suspend fun queryLocked(context: Context): Boolean? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return@withContext null
            val profile = getPrivateSpaceProfile(context) ?: return@withContext null
            val um = context.getSystemService(Context.USER_SERVICE) as? UserManager
                ?: return@withContext true
            um.isQuietModeEnabled(profile)
        } catch (e: Exception) {
            Log.w(TAG, "quiet_mode_read_failed reason=${e.javaClass.simpleName}")
            true
        }
    }

    /**
     * Asks the system to flip quiet mode. `requestQuietModeEnabled` is a synchronous system_server
     * call, so it runs on IO. The result only says what the request achieved; it is not the lock
     * state, so callers must re-query with [queryLocked] afterwards.
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend fun togglePrivateSpace(context: Context): ToggleOutcome = withContext(Dispatchers.IO) {
        try {
            val profile = getPrivateSpaceProfile(context) ?: return@withContext ToggleOutcome.FAILED
            val um = context.getSystemService(Context.USER_SERVICE) as? UserManager
                ?: return@withContext ToggleOutcome.FAILED
            val requestedQuiet = !um.isQuietModeEnabled(profile)
            val outcome = PrivateSpaceUi.outcomeFor(
                requestedQuiet = requestedQuiet,
                requestReturned = um.requestQuietModeEnabled(requestedQuiet, profile)
            )
            Log.i(TAG, "quiet_mode_requested requestedQuiet=$requestedQuiet outcome=$outcome")
            outcome
        } catch (_: SecurityException) {
            // Only the foreground default launcher may change quiet mode.
            Log.w(TAG, "quiet_mode_request_denied")
            ToggleOutcome.FAILED
        } catch (e: Exception) {
            Log.w(TAG, "quiet_mode_request_failed reason=${e.javaClass.simpleName}")
            ToggleOutcome.FAILED
        }
    }

    suspend fun loadPrivateSpaceApps(
        context: Context, prefs: Prefs
    ): List<AppModel> = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return@withContext emptyList()
            if (!FeatureAvailability.isPrivateSpaceAvailable(context)) return@withContext emptyList()
            val pApps = mutableListOf<AppModel>()
            val um = context.getSystemService(Context.USER_SERVICE) as? UserManager
                ?: return@withContext emptyList()
            val la = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                ?: return@withContext emptyList()
            val collator = Collator.getInstance()
            for (profile in um.userProfiles) {
                try {
                    val info = la.getLauncherUserInfo(profile)
                    if (info?.userType != UserManager.USER_TYPE_PROFILE_PRIVATE) continue
                    val quiet = um.isQuietModeEnabled(profile)
                    for (app in la.getActivityList(null, profile)) {
                        val label = prefs.getAppRenameLabel(app.applicationInfo.packageName)
                            .ifBlank { app.label.toString() }
                        val model = AppModel.App(
                            appLabel = label,
                            key = collator.getCollationKey(label),
                            appPackage = app.applicationInfo.packageName,
                            activityClassName = app.componentName.className,
                            isNew = false,
                            user = profile
                        )
                        if (!quiet || isAddEntry(model)) {
                            pApps.add(model)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed loading apps for a profile", e)
                }
            }
            pApps.sortWith(compareBy(collator) { it.appLabel })
            pApps
        } catch (e: Exception) {
            Log.w(TAG, "loadPrivateSpaceApps failed", e)
            emptyList()
        }
    }
}
