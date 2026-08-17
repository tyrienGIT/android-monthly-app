package com.maimonthlyhoppinings.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

data class EventInput(
    val title: String,
    val eventTypeId: String = EventType.defaultId,
    val details: String = "",
    val emoji: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class EntryInput(
    val eventId: Long,
    val title: String = "",
    val emoji: String = "",
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val details: String = "",
    val intensity: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepository(
    private val books: BookManager,
) {
    private val trackedEventDao: TrackedEventDao
        get() = books.database.trackedEventDao()
    private val eventEntryDao: EventEntryDao
        get() = books.database.eventEntryDao()
    private val eventTypeDao: EventTypeDao
        get() = books.database.eventTypeDao()

    fun observeTypes(): Flow<List<EventTypeEntity>> {
        return books.databaseFlow.flatMapLatest { it.eventTypeDao().observeAll() }
    }

    suspend fun getTypes(): List<EventTypeEntity> = eventTypeDao.getAll()

    fun observeTypeLookup(): Flow<EventTypeLookup> {
        return observeTypes().map { EventTypeLookup(it) }
    }

    suspend fun addType(
        label: String,
        color: EventTypeColor = EventTypeColor.Red,
    ): String {
        val existing = eventTypeDao.getAll()
        val nextNumber = existing
            .mapNotNull { it.id.removePrefix("type_").toIntOrNull() }
            .maxOrNull()
            ?.plus(1)
            ?: (EventType.seeds.size + 1)
        val id = "type_$nextNumber"
        val resolvedLabel = label.trim().ifEmpty { "Type $nextNumber" }
        eventTypeDao.upsert(
            EventTypeEntity(
                id = id,
                label = resolvedLabel,
                color = color.name,
            ),
        )
        return id
    }

    suspend fun updateType(
        id: String,
        label: String? = null,
        color: EventTypeColor? = null,
    ) {
        val existing = eventTypeDao.getById(id) ?: return
        val nextLabel = label?.trim()?.ifEmpty { existing.label } ?: existing.label
        eventTypeDao.update(
            existing.copy(
                label = nextLabel,
                color = color?.name ?: existing.color,
            ),
        )
    }

    fun observeEventsWithEntries(): Flow<List<EventWithEntries>> {
        return books.databaseFlow.flatMapLatest { it.trackedEventDao().observeAllWithEntries() }
    }

    fun observeEvents(): Flow<List<TrackedEvent>> {
        return books.databaseFlow.flatMapLatest { it.trackedEventDao().observeAll() }
    }

    fun observeEvent(eventId: Long): Flow<EventWithEntries?> {
        return books.databaseFlow.flatMapLatest { it.trackedEventDao().observeWithEntries(eventId) }
    }

    suspend fun getEvent(eventId: Long): EventWithEntries? {
        return trackedEventDao.getWithEntries(eventId)
    }

    fun observeEntriesForDay(date: LocalDate): Flow<List<EntryWithEvent>> {
        return books.databaseFlow.flatMapLatest {
            it.eventEntryDao().observeEntriesForDay(date.toEpochDay())
        }
    }

    fun observeEntriesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<EntryWithEvent>> {
        return books.databaseFlow.flatMapLatest {
            it.eventEntryDao().observeEntriesInRange(startDate.toEpochDay(), endDate.toEpochDay())
        }
    }

    fun observeEventsForDay(date: LocalDate): Flow<List<TrackedEvent>> {
        return books.databaseFlow.flatMapLatest {
            it.trackedEventDao().observeOverlappingDay(date.toEpochDay())
        }
    }

    suspend fun getEntry(entryId: Long): EntryWithEvent? {
        return eventEntryDao.getWithEvent(entryId)
    }

    /**
     * Heat across the calendar: one continuous band per parent event across its
     * start–end span. Each band is a gradient of that event's sub-entry intensities
     * (chronological). Days without an entry still paint, sampling the gradient.
     */
    fun observeHeatSegmentsInRange(
        startDate: LocalDate,
        endDate: LocalDate,
        maxSegmentsPerDay: Int = 5,
    ): Flow<Map<Long, List<DayHeatSegment>>> {
        val start = startDate.toEpochDay()
        val end = endDate.toEpochDay()
        return books.databaseFlow.flatMapLatest { db ->
            combine(
                db.trackedEventDao().observeOverlappingRange(start, end),
                db.eventEntryDao().observeEntriesForEventsOverlappingRange(start, end),
                db.eventTypeDao().observeAll(),
            ) { events, entryItems, types ->
            val typeLookup = EventTypeLookup(types)
            val entriesByEvent = entryItems.groupBy { it.entry.eventId }
            val segmentsByDay = linkedMapOf<Long, MutableList<DayHeatSegment>>()

            events.forEach { event ->
                val from = maxOf(event.startDateEpochDay, start)
                val to = minOf(event.endDateEpochDay, end)
                if (from > to) return@forEach

                val orderedEntries = entriesByEvent[event.id]
                    .orEmpty()
                    .map { it.entry }
                    .sortedWith(entryChronologicalOrder)
                val intensityStops = orderedEntries
                    .map { it.intensity.coerceIn(1, 10) }
                    .ifEmpty { listOf(EVENT_SPAN_PRESENCE_INTENSITY) }
                val color = typeLookup.color(event.eventTypeId)
                val spanLength = (event.endDateEpochDay - event.startDateEpochDay)
                    .coerceAtLeast(0L)

                for (day in from..to) {
                    val progress = if (spanLength == 0L) {
                        0.5f
                    } else {
                        ((day - event.startDateEpochDay).toFloat() / spanLength.toFloat())
                            .coerceIn(0f, 1f)
                    }
                    segmentsByDay.getOrPut(day) { mutableListOf() }.add(
                        DayHeatSegment(
                            eventId = event.id,
                            color = color,
                            intensityStops = intensityStops,
                            spanProgress = progress,
                        ),
                    )
                }
            }

            segmentsByDay.mapValues { (_, daySegments) ->
                selectHeatSegmentsForDay(daySegments, maxSegmentsPerDay)
            }
            }
        }
    }

    suspend fun startEvent(input: EventInput): Long {
        validateEvent(input)
        return trackedEventDao.insert(input.toEntity())
    }

    suspend fun updateEvent(id: Long, input: EventInput) {
        validateEvent(input)
        val existing = trackedEventDao.getById(id) ?: error("Event not found")
        val covered = coverEntries(id, input.startDate, input.endDate)
        // Must use update (not insert/REPLACE): REPLACE deletes the row first and
        // CASCADE would wipe all child entries.
        trackedEventDao.update(
            existing.copy(
                title = input.title.trim().ifEmpty { typeLabel(input.eventTypeId) },
                eventTypeId = input.eventTypeId,
                details = input.details.trim(),
                emoji = EmojiTags.encode(EmojiTags.decode(input.emoji)),
                startDateEpochDay = covered.first,
                endDateEpochDay = covered.second,
            ),
        )
    }

    suspend fun deleteEvent(id: Long) {
        trackedEventDao.deleteById(id)
    }

    suspend fun deleteAllEvents() {
        trackedEventDao.deleteAll()
    }

    suspend fun addEntry(input: EntryInput): Long {
        validateEntry(input)
        require(trackedEventDao.getById(input.eventId) != null) { "Event not found" }
        val id = eventEntryDao.insert(input.toEntity())
        expandEventToInclude(input.eventId, input.date)
        return id
    }

    suspend fun updateEntry(id: Long, input: EntryInput) {
        validateEntry(input)
        require(eventEntryDao.getWithEvent(id) != null) { "Entry not found" }
        eventEntryDao.update(input.toEntity(id = id))
        expandEventToInclude(input.eventId, input.date)
    }

    suspend fun deleteEntry(id: Long) {
        eventEntryDao.deleteById(id)
    }

    private suspend fun expandEventToInclude(eventId: Long, date: LocalDate) {
        val event = trackedEventDao.getById(eventId) ?: return
        val day = date.toEpochDay()
        val newStart = minOf(event.startDateEpochDay, day)
        val newEnd = maxOf(event.endDateEpochDay, day)
        if (newStart != event.startDateEpochDay || newEnd != event.endDateEpochDay) {
            trackedEventDao.update(
                event.copy(startDateEpochDay = newStart, endDateEpochDay = newEnd),
            )
        }
    }

    /** Manual dates may widen; entry dates always keep the span at least as wide. */
    private suspend fun coverEntries(
        eventId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Pair<Long, Long> {
        var start = startDate.toEpochDay()
        var end = endDate.toEpochDay()
        eventEntryDao.getForEvent(eventId).forEach { entry ->
            start = minOf(start, entry.dateEpochDay)
            end = maxOf(end, entry.dateEpochDay)
        }
        return start to end
    }

    private fun selectHeatSegmentsForDay(
        daySegments: List<DayHeatSegment>,
        maxSegments: Int,
    ): List<DayHeatSegment> {
        if (daySegments.isEmpty()) return emptyList()
        val capped = if (daySegments.size <= maxSegments) {
            daySegments
        } else {
            daySegments
                .sortedWith(
                    compareByDescending<DayHeatSegment> { it.peakIntensity() }
                        .thenBy { it.eventId },
                )
                .take(maxSegments)
        }
        // Stable stack order so the same event stays on a consistent lane across days.
        return capped.sortedWith(
            compareBy<DayHeatSegment> { it.color.ordinal }
                .thenBy { it.eventId },
        )
    }

    private suspend fun validateEvent(input: EventInput) {
        require(EventType.isValidId(input.eventTypeId, eventTypeDao.getAll())) {
            "Invalid event type"
        }
        require(!input.endDate.isBefore(input.startDate)) { "End date must be on or after start date" }
    }

    private fun validateEntry(input: EntryInput) {
        require(input.intensity in 1..10) { "Intensity must be between 1 and 10" }
    }

    private suspend fun typeLabel(typeId: String): String {
        return eventTypeDao.getById(typeId)?.label ?: EventType.seedLabel(typeId)
    }

    private suspend fun EventInput.toEntity(id: Long = 0): TrackedEvent {
        val resolvedTitle = title.trim().ifEmpty { typeLabel(eventTypeId) }
        return TrackedEvent(
            id = id,
            title = resolvedTitle,
            eventTypeId = eventTypeId,
            details = details.trim(),
            emoji = EmojiTags.encode(EmojiTags.decode(emoji)),
            startDateEpochDay = startDate.toEpochDay(),
            endDateEpochDay = endDate.toEpochDay(),
        )
    }

    private fun EntryInput.toEntity(id: Long = 0): EventEntry {
        return EventEntry(
            id = id,
            eventId = eventId,
            title = title.trim(),
            emoji = EmojiTags.encode(EmojiTags.decode(emoji)),
            dateEpochDay = date.toEpochDay(),
            startTimeMinutesOfDay = startTime?.let { it.hour * 60 + it.minute },
            details = details.trim(),
            intensity = intensity,
        )
    }

    companion object {
        /** Soft intensity when an event has a span but no entries yet. */
        const val EVENT_SPAN_PRESENCE_INTENSITY = 2

        private val entryChronologicalOrder =
            compareBy<EventEntry> { it.dateEpochDay }
                .thenBy { it.startTimeMinutesOfDay == null }
                .thenBy { it.startTimeMinutesOfDay ?: Int.MAX_VALUE }
                .thenBy { it.createdAtMillis }
    }
}

fun DayHeatSegment.peakIntensity(): Int = intensityStops.maxOrNull() ?: 1

/** Sample the chronological intensity gradient at [spanProgress] (0..1). */
fun DayHeatSegment.intensityAtProgress(): Int {
    val stops = intensityStops
    if (stops.isEmpty()) return 1
    if (stops.size == 1) return stops.first()
    val t = spanProgress.coerceIn(0f, 1f) * (stops.lastIndex)
    val i = t.toInt().coerceIn(0, stops.lastIndex - 1)
    val frac = t - i
    val a = stops[i]
    val b = stops[i + 1]
    return (a + (b - a) * frac).toInt().coerceIn(1, 10)
}
