package com.knownassurajit.app.launcher.voidlauncher.helper

import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.knownassurajit.app.launcher.voidlauncher.R

class PinItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set window to be transparent
        window.setBackgroundDrawable(null)

        val launcherApps = getSystemService(LauncherApps::class.java)
        val pinItemRequest = launcherApps.getPinItemRequest(intent)

        when (pinItemRequest != null) {
            true -> handleRequestType(pinItemRequest)
            false -> showToast(R.string.pin_invalid_request)
        }

        finish()
    }

    private fun handleRequestType(pinItemRequest: LauncherApps.PinItemRequest) {
        when (pinItemRequest.requestType) {
            LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT ->
                handleShortcutRequest(pinItemRequest)

            LauncherApps.PinItemRequest.REQUEST_TYPE_APPWIDGET ->
                handleWidgetRequest(pinItemRequest)

            else -> showToast(R.string.pin_unsupported)
        }
    }

    private fun handleShortcutRequest(pinItemRequest: LauncherApps.PinItemRequest) {
        val shortcutInfo = pinItemRequest.shortcutInfo
        if (shortcutInfo != null) {
            showToast(if (pinItemRequest.accept()) R.string.pin_shortcut_added else R.string.pin_shortcut_failed)
        } else {
            showToast(R.string.pin_invalid_request)
        }
    }

    private fun handleWidgetRequest(pinItemRequest: LauncherApps.PinItemRequest) {
        showToast(if (pinItemRequest.accept()) R.string.pin_widget_added else R.string.pin_widget_failed)
    }
}