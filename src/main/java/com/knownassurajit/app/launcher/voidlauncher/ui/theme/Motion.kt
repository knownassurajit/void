package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.animation.core.tween

object VoidMotion {
    const val fastMs = 150
    const val standardMs = 250
    const val slowMs = 400

    fun <T> standard() = tween<T>(durationMillis = standardMs)
}
