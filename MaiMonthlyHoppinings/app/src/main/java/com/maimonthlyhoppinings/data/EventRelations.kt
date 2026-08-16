package com.maimonthlyhoppinings.data

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithEntries(
    @Embedded val event: TrackedEvent,
    @Relation(
        parentColumn = "id",
        entityColumn = "eventId",
    )
    val entries: List<EventEntry>,
)

data class EntryWithEvent(
    @Embedded val entry: EventEntry,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "id",
    )
    val event: TrackedEvent,
)
