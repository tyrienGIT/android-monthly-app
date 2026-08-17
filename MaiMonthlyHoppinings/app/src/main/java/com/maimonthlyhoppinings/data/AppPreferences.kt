package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.concurrent.TimeUnit

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
    private val autoBackupFrequencyKey = stringPreferencesKey("auto_backup_frequency")
    private val autoBackupFrequencyDaysKey = intPreferencesKey("auto_backup_frequency_days")
    private val lastAutoBackupEpochDayKey = longPreferencesKey("last_auto_backup_epoch_day")
    private val lastAutoBackupMillisKey = longPreferencesKey("last_auto_backup_millis")

    val tutorialCompleted: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[tutorialCompletedKey] ?: false
    }

    val autoBackupSettings: Flow<AutoBackupSettings> = context.appDataStore.data.map { prefs ->
        AutoBackupSettings(
            enabled = prefs[autoBackupEnabledKey] ?: true,
            retainMonths = (prefs[autoBackupRetainMonthsKey] ?: 2).coerceIn(1, 240),
            maxCount = (prefs[autoBackupMaxCountKey] ?: 60).coerceIn(1, 9_999),
            frequency = prefs[autoBackupFrequencyKey]
                ?.let { runCatching { AutoBackupFrequency.valueOf(it) }.getOrNull() }
                ?: AutoBackupFrequency.DAILY,
            frequencyDays = (prefs[autoBackupFrequencyDaysKey] ?: 1).coerceIn(1, 3_650),
            lastBackupEpochDay = prefs[lastAutoBackupEpochDayKey] ?: -1L,
            lastBackupMillis = prefs[lastAutoBackupMillisKey] ?: -1L,
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
            prefs[autoBackupRetainMonthsKey] = months.coerceIn(1, 240)
        }
    }

    suspend fun setAutoBackupMaxCount(count: Int) {
        context.appDataStore.edit { prefs ->
            prefs[autoBackupMaxCountKey] = count.coerceIn(1, 9_999)
        }
    }

    suspend fun setAutoBackupFrequency(frequency: AutoBackupFrequency, days: Int? = null) {
        context.appDataStore.edit { prefs ->
            prefs[autoBackupFrequencyKey] = frequency.name
            if (days != null) {
                prefs[autoBackupFrequencyDaysKey] = days.coerceIn(1, 3_650)
            }
        }
    }

    suspend fun setLastAutoBackup(epochDay: Long, millis: Long) {
        context.appDataStore.edit { prefs ->
            prefs[lastAutoBackupEpochDayKey] = epochDay
            prefs[lastAutoBackupMillisKey] = millis
        }
    }
}

data class AutoBackupSettings(
    val enabled: Boolean = true,
    val retainMonths: Int = 2,
    val maxCount: Int = 60,
    val frequency: AutoBackupFrequency = AutoBackupFrequency.DAILY,
    val frequencyDays: Int = 1,
    val lastBackupEpochDay: Long = -1L,
    val lastBackupMillis: Long = -1L,
)

enum class AutoBackupFrequency(
    val label: String,
) {
    EVERY_OPEN("Every open"),
    DAILY("Daily"),
    EVERY_3_DAYS("Every 3 days"),
    WEEKLY("Weekly"),
    CUSTOM("Other"),
    ;

    fun isDue(
        lastEpochDay: Long,
        lastMillis: Long,
        customDays: Int = 1,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        if (lastEpochDay < 0L && lastMillis < 0L) return true
        val today = LocalDate.now().toEpochDay()
        val daysSince = if (lastEpochDay >= 0L) today - lastEpochDay else Long.MAX_VALUE
        return when (this) {
            EVERY_OPEN -> lastMillis < 0L || nowMillis - lastMillis >= EVERY_OPEN_MIN_INTERVAL_MS
            DAILY -> daysSince >= 1L
            EVERY_3_DAYS -> daysSince >= 3L
            WEEKLY -> daysSince >= 7L
            CUSTOM -> daysSince >= customDays.coerceAtLeast(1).toLong()
        }
    }
}

private val EVERY_OPEN_MIN_INTERVAL_MS = TimeUnit.MINUTES.toMillis(2)
