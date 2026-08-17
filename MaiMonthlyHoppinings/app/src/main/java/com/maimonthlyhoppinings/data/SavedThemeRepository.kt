package com.maimonthlyhoppinings.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class SavedThemeRepository(
    private val personas: PersonaManager,
) {
    fun observeSavedThemes(): Flow<List<SavedColorTheme>> {
        return personas.databaseFlow.flatMapLatest { it.savedColorThemeDao().observeAll() }
    }

    fun observeSavedTheme(id: Long): Flow<SavedColorTheme?> {
        return personas.databaseFlow.flatMapLatest { it.savedColorThemeDao().observeById(id) }
    }

    suspend fun getSavedTheme(id: Long): SavedColorTheme? {
        return personas.database.savedColorThemeDao().getById(id)
    }

    suspend fun saveTheme(
        name: String,
        lightPrimaryArgb: Int,
        lightSecondaryArgb: Int,
        lightTertiaryArgb: Int,
        darkPrimaryArgb: Int,
        darkSecondaryArgb: Int,
        darkTertiaryArgb: Int,
    ): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Theme name is required" }
        return personas.database.savedColorThemeDao().insert(
            SavedColorTheme(
                name = trimmed,
                lightPrimaryArgb = lightPrimaryArgb,
                lightSecondaryArgb = lightSecondaryArgb,
                lightTertiaryArgb = lightTertiaryArgb,
                darkPrimaryArgb = darkPrimaryArgb,
                darkSecondaryArgb = darkSecondaryArgb,
                darkTertiaryArgb = darkTertiaryArgb,
            ),
        )
    }

    suspend fun deleteTheme(id: Long) {
        personas.database.savedColorThemeDao().deleteById(id)
    }
}
