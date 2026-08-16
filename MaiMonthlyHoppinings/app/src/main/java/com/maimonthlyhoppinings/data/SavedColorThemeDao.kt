package com.maimonthlyhoppinings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedColorThemeDao {
    @Query("SELECT * FROM saved_color_themes ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<SavedColorTheme>>

    @Query("SELECT * FROM saved_color_themes WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<SavedColorTheme?>

    @Query("SELECT * FROM saved_color_themes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedColorTheme?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(theme: SavedColorTheme): Long

    @Query("DELETE FROM saved_color_themes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
