package com.maimonthlyhoppinings.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EntryWithEvent
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeColor
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.displayTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

enum class TrendsRange(val days: Long, val label: String) {
    THREE_MONTHS(90, "3 months"),
    SIX_MONTHS(180, "6 months"),
    YEAR(365, "1 year"),
}

data class TrendPoint(
    val epochDay: Long,
    val intensity: Int,
)

data class TrendSeries(
    val typeId: String,
    val label: String,
    val color: EventTypeColor,
    val points: List<TrendPoint>,
    val selected: Boolean,
    val averageIntensity: Float?,
    val entryCount: Int,
    val eventCount: Int,
)

data class TrendEventRow(
    val eventId: Long,
    val title: String,
    val typeId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val entryCount: Int,
    val averageIntensity: Float?,
)

private data class TrendFilters(
    val range: TrendsRange,
    val selectedTypeId: String?,
    val selectedDay: Long?,
    val focusedEventId: Long?,
)

data class TrendsUiState(
    val range: TrendsRange = TrendsRange.THREE_MONTHS,
    val selectedTypeId: String? = null,
    val startDate: LocalDate = LocalDate.now().minusDays(TrendsRange.THREE_MONTHS.days - 1),
    val endDate: LocalDate = LocalDate.now(),
    val series: List<TrendSeries> = emptyList(),
    val categoryStats: List<TrendSeries> = emptyList(),
    val events: List<TrendEventRow> = emptyList(),
    val focusedEvent: TrendEventRow? = null,
    val focusedEntries: List<EntryWithEvent> = emptyList(),
    val selectedEpochDay: Long? = null,
    val selectedDayEntries: List<EntryWithEvent> = emptyList(),
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
    val hasAnyPoints: Boolean = false,
)

class TrendsViewModel(
    eventRepository: EventRepository,
) : ViewModel() {
    private val range = MutableStateFlow(TrendsRange.THREE_MONTHS)
    private val selectedTypeId = MutableStateFlow<String?>(null)
    private val selectedEpochDay = MutableStateFlow<Long?>(null)
    private val focusedEventId = MutableStateFlow<Long?>(null)

    private val endDate = LocalDate.now()
    private val widestStart = endDate.minusDays(TrendsRange.YEAR.days - 1)

    private val filters = combine(
        range,
        selectedTypeId,
        selectedEpochDay,
        focusedEventId,
    ) { selectedRange, typeId, selectedDay, focusedId ->
        TrendFilters(selectedRange, typeId, selectedDay, focusedId)
    }

    val uiState: StateFlow<TrendsUiState> = combine(
        filters,
        eventRepository.observeEntriesInRange(widestStart, endDate),
        eventRepository.observeTypeLookup(),
    ) { filter, entries, types ->
        val rangeEnd = endDate
        val rangeStart = rangeEnd.minusDays(filter.range.days - 1)
        val startEpoch = rangeStart.toEpochDay()
        val endEpoch = rangeEnd.toEpochDay()
        val inRange = entries.filter { it.entry.dateEpochDay in startEpoch..endEpoch }
        val eventRows = buildEventRows(inRange, types)
        val defaultTypeId = inRange
            .groupingBy { it.event.eventTypeId }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        val availableTypeIds = eventRows.map { it.typeId }.toSet()
        val activeTypeId = filter.selectedTypeId
            ?.takeIf { it in availableTypeIds }
            ?: defaultTypeId
        val categoryStats = buildSeries(inRange, eventRows, types, activeTypeId)
        val typeEntries = inRange.filter { it.event.eventTypeId == activeTypeId }
        val typeEvents = eventRows
            .filter { it.typeId == activeTypeId }
            .sortedByDescending { it.startDate }
        val focused = typeEvents.firstOrNull { it.eventId == filter.focusedEventId }
        val focusedEntries = if (focused != null) {
            typeEntries
                .filter { it.event.id == focused.eventId }
                .sortedBy { it.entry.dateEpochDay }
        } else {
            emptyList()
        }
        val chartEntries = if (focused != null) focusedEntries else typeEntries
        val chartStart = if (focused != null) {
            focused.startDate.minusDays(3).coerceAtLeast(rangeStart)
        } else {
            rangeStart
        }
        val chartEnd = if (focused != null) {
            focused.endDate.plusDays(3).coerceAtMost(rangeEnd)
        } else {
            rangeEnd
        }
        val series = buildSeries(chartEntries, typeEvents, types, activeTypeId)
            .filter { it.selected }
        val dayEntries = filter.selectedDay?.let { day ->
            typeEntries.filter { it.entry.dateEpochDay == day }
        }.orEmpty()
        TrendsUiState(
            range = filter.range,
            selectedTypeId = activeTypeId,
            startDate = chartStart,
            endDate = chartEnd,
            series = series,
            categoryStats = categoryStats,
            events = typeEvents,
            focusedEvent = focused,
            focusedEntries = focusedEntries,
            selectedEpochDay = filter.selectedDay,
            selectedDayEntries = dayEntries,
            types = types,
            hasAnyPoints = series.any { it.points.isNotEmpty() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrendsUiState(),
    )

    fun setRange(next: TrendsRange) {
        range.value = next
        selectedEpochDay.value = null
        focusedEventId.value = null
    }

    fun selectType(typeId: String) {
        if (selectedTypeId.value == typeId) return
        selectedTypeId.value = typeId
        focusedEventId.value = null
        selectedEpochDay.value = null
    }

    fun selectDay(epochDay: Long?) {
        selectedEpochDay.value = epochDay
    }

    fun focusEvent(eventId: Long?) {
        focusedEventId.value = eventId
        selectedEpochDay.value = null
    }

    companion object {
        fun factory(eventRepository: EventRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TrendsViewModel(eventRepository) as T
                }
            }
        }

        internal fun buildEventRows(
            entries: List<EntryWithEvent>,
            types: EventTypeLookup,
        ): List<TrendEventRow> {
            return entries
                .groupBy { it.event.id }
                .map { (_, items) ->
                    val event = items.first().event
                    TrendEventRow(
                        eventId = event.id,
                        title = event.displayTitle(types),
                        typeId = event.eventTypeId,
                        startDate = LocalDate.ofEpochDay(event.startDateEpochDay),
                        endDate = LocalDate.ofEpochDay(event.endDateEpochDay),
                        entryCount = items.size,
                        averageIntensity = items.map { it.entry.intensity }.average().toFloat(),
                    )
                }
        }

        internal fun buildSeries(
            entries: List<EntryWithEvent>,
            events: List<TrendEventRow>,
            types: EventTypeLookup,
            selectedTypeId: String?,
        ): List<TrendSeries> {
            val peaks = entries
                .groupBy { it.event.eventTypeId to it.entry.dateEpochDay }
                .map { (key, items) ->
                    Triple(key.first, key.second, items.maxOf { it.entry.intensity })
                }
            val eventCountByType = events.groupingBy { it.typeId }.eachCount()
            val typeOrder = types.all.map { it.id }
            val typeIds = (typeOrder + peaks.map { it.first })
                .distinct()
                .filter { typeId -> peaks.any { it.first == typeId } }
            return typeIds.map { typeId ->
                val points = peaks
                    .filter { it.first == typeId }
                    .map { TrendPoint(epochDay = it.second, intensity = it.third) }
                    .sortedBy { it.epochDay }
                TrendSeries(
                    typeId = typeId,
                    label = types.label(typeId),
                    color = types.color(typeId),
                    points = points,
                    selected = typeId == selectedTypeId,
                    averageIntensity = points.takeIf { it.isNotEmpty() }
                        ?.map { it.intensity }?.average()?.toFloat(),
                    entryCount = points.size,
                    eventCount = eventCountByType[typeId] ?: 0,
                )
            }
        }
    }
}
