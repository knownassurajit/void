package com.knownassurajit.app.launcher.voidlauncher.helper

import com.knownassurajit.app.launcher.voidlauncher.BuildConfig

object FeatureAvailability {
    val isWidgetsAvailable: Boolean
        get() = BuildConfig.FLAVOR == "integrated"

    val isNotificationSummaryAvailable: Boolean
        get() = BuildConfig.FLAVOR == "integrated"

    val isNotificationsAvailable: Boolean
        get() = BuildConfig.FLAVOR == "integrated"
        
    val isPrivateSpaceAvailable: Boolean
        get() = BuildConfig.FLAVOR == "integrated"
}
