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

    @Query("SELECT * FROM saved_color_themes")
    suspend fun getAll(): List<SavedColorTheme>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(theme: SavedColorTheme): Long

    @Query("DELETE FROM saved_color_themes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_color_themes WHERE id NOT IN (:keepIds)")
    suspend fun deleteNotIn(keepIds: List<Long>)

    @Query("DELETE FROM saved_color_themes")
    suspend fun deleteAll()
}
