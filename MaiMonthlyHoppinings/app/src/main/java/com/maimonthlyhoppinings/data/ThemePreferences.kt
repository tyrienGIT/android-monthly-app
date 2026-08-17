package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_preferences",
)

class ThemePreferences(
    private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val activeColorThemeKey = stringPreferencesKey("active_color_theme")
    // Legacy key from earlier builds.
    private val legacyColorThemeKey = stringPreferencesKey("color_theme")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[themeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.default
    }

    val activeColorTheme: Flow<ActiveColorTheme> = context.themeDataStore.data.map { prefs ->
        val encoded = prefs[activeColorThemeKey]
        if (encoded != null) {
            ActiveColorTheme.decode(encoded)
        } else {
            // Migrate older preset-only preference.
            val legacy = prefs[legacyColorThemeKey]
                ?.let { runCatching { ColorTheme.valueOf(it) }.getOrNull() }
            if (legacy != null) {
                ActiveColorTheme.Preset(legacy)
            } else {
                ActiveColorTheme.default
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
        context.applyAppNightMode(mode)
    }

    suspend fun applyStoredNightMode() {
        context.applyAppNightMode(themeMode.first())
    }

    suspend fun setActiveColorTheme(theme: ActiveColorTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[activeColorThemeKey] = theme.encode()
        }
    }
}
