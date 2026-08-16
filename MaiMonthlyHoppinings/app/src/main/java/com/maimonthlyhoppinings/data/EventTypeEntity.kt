package com.maimonthlyhoppinings.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_types")
data class EventTypeEntity(
    @PrimaryKey val id: String,
    val label: String,
    val color: String,
) {
    fun colorEnum(): EventTypeColor = EventTypeColor.fromName(color)
}
