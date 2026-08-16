package com.maimonthlyhoppinings.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single-day timed intensity entry belonging to a [TrackedEvent].
 */
@Entity(
    tableName = "event_entries",
    foreignKeys = [
        ForeignKey(
            entity = TrackedEvent::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("eventId"), Index("dateEpochDay")],
)
data class EventEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val title: String = "",
    val dateEpochDay: Long,
    /** Minutes from midnight (0..1439). Null means no specific start time. */
    val startTimeMinutesOfDay: Int? = null,
    val details: String = "",
    val intensity: Int = 5,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
