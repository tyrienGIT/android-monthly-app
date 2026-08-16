package com.maimonthlyhoppinings.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A started event of a given type. Entries (single dates/times/intensity) hang off this.
 * [startDateEpochDay]–[endDateEpochDay] spans the calendar listing and expands to cover entries.
 */
@Entity(tableName = "tracked_events")
data class TrackedEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val eventType: String = EventType.default,
    val details: String = "",
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
