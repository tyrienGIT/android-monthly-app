package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    val tutorialCompleted: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[tutorialCompletedKey] ?: false
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[tutorialCompletedKey] = completed
        }
    }
}
