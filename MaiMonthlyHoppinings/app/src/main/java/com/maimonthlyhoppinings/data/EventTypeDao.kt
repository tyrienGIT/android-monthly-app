package com.maimonthlyhoppinings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventTypeDao {
    @Query("SELECT * FROM event_types ORDER BY id ASC")
    fun observeAll(): Flow<List<EventTypeEntity>>

    @Query("SELECT * FROM event_types ORDER BY id ASC")
    suspend fun getAll(): List<EventTypeEntity>

    @Query("SELECT * FROM event_types WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EventTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(type: EventTypeEntity)

    @Update
    suspend fun update(type: EventTypeEntity)

    @Query("DELETE FROM event_types WHERE id NOT IN (:keepIds)")
    suspend fun deleteNotIn(keepIds: List<String>)

    @Query("DELETE FROM event_types")
    suspend fun deleteAll()
}
