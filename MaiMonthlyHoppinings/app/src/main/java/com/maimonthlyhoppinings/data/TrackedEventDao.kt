package com.maimonthlyhoppinings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedEventDao {
    @Transaction
    @Query(
        """
        SELECT * FROM tracked_events
        ORDER BY createdAtMillis DESC
        """,
    )
    fun observeAllWithEntries(): Flow<List<EventWithEntries>>

    @Query(
        """
        SELECT * FROM tracked_events
        ORDER BY createdAtMillis DESC
        """,
    )
    fun observeAll(): Flow<List<TrackedEvent>>

    @Query(
        """
        SELECT * FROM tracked_events
        WHERE startDateEpochDay <= :dateEpochDay AND endDateEpochDay >= :dateEpochDay
        ORDER BY startDateEpochDay ASC, createdAtMillis DESC
        """,
    )
    fun observeOverlappingDay(dateEpochDay: Long): Flow<List<TrackedEvent>>

    @Query(
        """
        SELECT * FROM tracked_events
        WHERE startDateEpochDay <= :endInclusive AND endDateEpochDay >= :startInclusive
        ORDER BY startDateEpochDay ASC, createdAtMillis ASC
        """,
    )
    fun observeOverlappingRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<TrackedEvent>>

    @Transaction
    @Query("SELECT * FROM tracked_events WHERE id = :id LIMIT 1")
    fun observeWithEntries(id: Long): Flow<EventWithEntries?>

    @Transaction
    @Query("SELECT * FROM tracked_events WHERE id = :id LIMIT 1")
    suspend fun getWithEntries(id: Long): EventWithEntries?

    @Query("SELECT * FROM tracked_events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TrackedEvent?

    @Query("SELECT * FROM tracked_events")
    suspend fun getAll(): List<TrackedEvent>

    @Insert
    suspend fun insert(event: TrackedEvent): Long

    @Update
    suspend fun update(event: TrackedEvent)

    @Query("DELETE FROM tracked_events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tracked_events WHERE id NOT IN (:keepIds)")
    suspend fun deleteNotIn(keepIds: List<Long>)

    @Query("DELETE FROM tracked_events")
    suspend fun deleteAll()
}
