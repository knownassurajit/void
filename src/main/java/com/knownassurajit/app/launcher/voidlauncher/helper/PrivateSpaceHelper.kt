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

    fun isAddEntry(app: AppModel): Boolean {
        val label = app.appLabel.trim()
        if (label.equals("Add", ignoreCase = true) ||
            label.equals("Add apps", ignoreCase = true) ||
            label.equals("Add app", ignoreCase = true)
        ) {
            return true
        }
        val cls = (app as? AppModel.App)?.activityClassName.orEmpty()
        return cls.contains("PrivateSpace", ignoreCase = true) &&
            (cls.contains("Add", ignoreCase = true) || cls.contains("Install", ignoreCase = true))
    }

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

    fun isQuietModeEnabled(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return true
            val profile = getPrivateSpaceProfile(context) ?: return true
            val um = context.getSystemService(Context.USER_SERVICE) as? UserManager ?: return true
            um.isQuietModeEnabled(profile)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read quiet mode", e)
            true
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun togglePrivateSpace(context: Context) {
        try {
            val profile = getPrivateSpaceProfile(context) ?: return
            val um = context.getSystemService(Context.USER_SERVICE) as? UserManager ?: return
            val locked = um.isQuietModeEnabled(profile)
            um.requestQuietModeEnabled(!locked, profile)
            Log.i(TAG, "Private Space quiet mode requested locked=${!locked}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle Private Space", e)
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
