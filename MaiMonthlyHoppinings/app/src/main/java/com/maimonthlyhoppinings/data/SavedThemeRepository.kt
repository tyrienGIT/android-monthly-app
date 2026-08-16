package com.maimonthlyhoppinings.data

import kotlinx.coroutines.flow.Flow

class SavedThemeRepository(
    private val savedColorThemeDao: SavedColorThemeDao,
) {
    fun observeSavedThemes(): Flow<List<SavedColorTheme>> = savedColorThemeDao.observeAll()

    fun observeSavedTheme(id: Long): Flow<SavedColorTheme?> = savedColorThemeDao.observeById(id)

    suspend fun getSavedTheme(id: Long): SavedColorTheme? = savedColorThemeDao.getById(id)

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
        return savedColorThemeDao.insert(
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
        savedColorThemeDao.deleteById(id)
    }
}
