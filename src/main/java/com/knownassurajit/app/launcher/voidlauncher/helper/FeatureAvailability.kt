package com.knownassurajit.app.launcher.voidlauncher.helper

import android.app.AppOpsManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Runtime capability gates for the single-variant OSS build.
 * These are not Play Store SKU stubs — they reflect device/OS/permission readiness.
 */
object FeatureAvailability {

    fun isWidgetsAvailable(context: Context): Boolean {
        return try {
            AppWidgetManager.getInstance(context) != null
        } catch (_: Exception) {
            false
        }
    }

    fun isNotificationSummaryAvailable(context: Context): Boolean = true

    fun isNotificationsAvailable(context: Context): Boolean = true

    fun isNotificationListenerEnabled(context: Context): Boolean {
        return try {
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        } catch (_: Exception) {
            false
        }
    }

    fun isPrivateSpaceAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return false
        return try {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS) ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
        } catch (_: Exception) {
            true
        }
    }

    fun isAiCoreLikelyAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.aicore", 0)
            true
        } catch (_: Exception) {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        }
    }

    fun isUsageAccessGranted(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    /** Legacy property accessors kept for gradual migration; prefer context overloads. */
    @Deprecated("Use context-aware overload")
    val isWidgetsAvailable: Boolean
        get() = true

    @Deprecated("Use context-aware overload")
    val isNotificationSummaryAvailable: Boolean
        get() = true

    @Deprecated("Use context-aware overload")
    val isNotificationsAvailable: Boolean
        get() = true

    @Deprecated("Use context-aware overload")
    val isPrivateSpaceAvailable: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}
