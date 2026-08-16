package com.maimonthlyhoppinings.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventEntryDao {
    @Transaction
    @Query(
        """
        SELECT * FROM event_entries
        WHERE dateEpochDay = :dateEpochDay
        ORDER BY
            CASE WHEN startTimeMinutesOfDay IS NULL THEN 1 ELSE 0 END ASC,
            startTimeMinutesOfDay DESC,
            createdAtMillis DESC
        """,
    )
    fun observeEntriesForDay(dateEpochDay: Long): Flow<List<EntryWithEvent>>

    @Transaction
    @Query(
        """
        SELECT * FROM event_entries
        WHERE dateEpochDay >= :startInclusive AND dateEpochDay <= :endInclusive
        ORDER BY dateEpochDay ASC, createdAtMillis ASC
        """,
    )
    fun observeEntriesInRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<EntryWithEvent>>

    /** All entries belonging to events whose span overlaps [startInclusive, endInclusive]. */
    @Transaction
    @Query(
        """
        SELECT e.* FROM event_entries e
        INNER JOIN tracked_events t ON e.eventId = t.id
        WHERE t.startDateEpochDay <= :endInclusive AND t.endDateEpochDay >= :startInclusive
        ORDER BY e.dateEpochDay ASC, e.createdAtMillis ASC
        """,
    )
    fun observeEntriesForEventsOverlappingRange(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<EntryWithEvent>>

    @Query(
        """
        SELECT * FROM event_entries
        WHERE eventId = :eventId
        ORDER BY
            dateEpochDay DESC,
            CASE WHEN startTimeMinutesOfDay IS NULL THEN 1 ELSE 0 END ASC,
            startTimeMinutesOfDay DESC,
            createdAtMillis DESC
        """,
    )
    fun observeForEvent(eventId: Long): Flow<List<EventEntry>>

    @Query(
        """
        SELECT * FROM event_entries
        WHERE eventId = :eventId
        ORDER BY dateEpochDay ASC
        """,
    )
    suspend fun getForEvent(eventId: Long): List<EventEntry>

    @Transaction
    @Query("SELECT * FROM event_entries WHERE id = :id LIMIT 1")
    suspend fun getWithEvent(id: Long): EntryWithEvent?

    @Insert
    suspend fun insert(entry: EventEntry): Long

    @Update
    suspend fun update(entry: EventEntry)

    @Query("DELETE FROM event_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
