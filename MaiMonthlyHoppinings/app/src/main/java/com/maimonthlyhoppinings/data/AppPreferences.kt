package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

class AppPreferences(
    private val context: Context,
) {
    private val tutorialCompletedKey = booleanPreferencesKey("tutorial_completed")
    private val autoBackupEnabledKey = booleanPreferencesKey("auto_backup_enabled")
    private val autoBackupRetainMonthsKey = intPreferencesKey("auto_backup_retain_months")
    private val autoBackupMaxCountKey = intPreferencesKey("auto_backup_max_count")
    private val lastAutoBackupEpochDayKey = longPreferencesKey("last_auto_backup_epoch_day")

    val tutorialCompleted: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[tutorialCompletedKey] ?: false
    }

    val autoBackupSettings: Flow<AutoBackupSettings> = context.appDataStore.data.map { prefs ->
        AutoBackupSettings(
            enabled = prefs[autoBackupEnabledKey] ?: true,
            retainMonths = (prefs[autoBackupRetainMonthsKey] ?: 2).coerceIn(1, 12),
            maxCount = (prefs[autoBackupMaxCountKey] ?: 60).coerceIn(1, 366),
            lastBackupEpochDay = prefs[lastAutoBackupEpochDayKey] ?: -1L,
        )
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[tutorialCompletedKey] = completed
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[autoBackupEnabledKey] = enabled
        }
    }

    suspend fun setAutoBackupRetainMonths(months: Int) {
        context.appDataStore.edit { prefs ->
            prefs[autoBackupRetainMonthsKey] = months.coerceIn(1, 12)
        }
    }

    suspend fun setAutoBackupMaxCount(count: Int) {
        context.appDataStore.edit { prefs ->
            prefs[autoBackupMaxCountKey] = count.coerceIn(1, 366)
        }
    }

    suspend fun setLastAutoBackupEpochDay(epochDay: Long) {
        context.appDataStore.edit { prefs ->
            prefs[lastAutoBackupEpochDayKey] = epochDay
        }
    }
}

data class AutoBackupSettings(
    val enabled: Boolean = true,
    val retainMonths: Int = 2,
    val maxCount: Int = 60,
    val lastBackupEpochDay: Long = -1L,
)
