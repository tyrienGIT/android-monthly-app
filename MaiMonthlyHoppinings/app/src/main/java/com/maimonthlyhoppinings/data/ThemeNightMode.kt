package com.maimonthlyhoppinings.data

import android.app.UiModeManager
import android.content.Context
import android.os.Build

/**
 * Persist the in-app light/dark choice so the next cold-start splash
 * (API 31+) uses the matching [values] / [values-night] resources.
 */
fun Context.applyAppNightMode(mode: ThemeMode) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val uiModeManager = getSystemService(UiModeManager::class.java) ?: return
    val nightMode = when (mode) {
        ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
        ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
        ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
    }
    uiModeManager.setApplicationNightMode(nightMode)
}
