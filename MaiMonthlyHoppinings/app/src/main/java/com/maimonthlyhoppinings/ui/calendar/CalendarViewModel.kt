package com.maimonthlyhoppinings.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EventEntry
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.TrackedEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayEventGroup(
    val event: TrackedEvent,
    val entries: List<EventEntry>,
)

data class CalendarUiState(
    val weekdayLabels: List<String>,
    val weeks: List<CalendarWeek>,
    val todayWeekIndex: Int,
    /** Week index to open centered in the viewport (middle of the current month). */
    val initialCenterWeekIndex: Int,
    val selectedDate: LocalDate,
    val selectedDateLabel: String,
    val selectedDayGroups: List<DayEventGroup> = emptyList(),
    val allEvents: List<TrackedEvent> = emptyList(),
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val today = LocalDate.now()
    private val todayMonday = mondayOfWeek(today)
    private val weeksBefore = WEEKS_BEFORE
    private val weeksAfter = WEEKS_AFTER
    private val rangeStart = todayMonday.minusWeeks(weeksBefore.toLong())
    private val rangeEnd = todayMonday.plusWeeks(weeksAfter.toLong()).plusDays(6)
    private val selectedDate = MutableStateFlow(today)

    private val heatByDay = eventRepository.observeHeatSegmentsInRange(
        rangeStart,
        rangeEnd,
        maxSegmentsPerDay = 5,
    )

    val uiState: StateFlow<CalendarUiState> = combine(
        heatByDay,
        selectedDate,
        eventRepository.observeEvents(),
    ) { heat, date, events ->
        Triple(heat, date, events)
    }.flatMapLatest { (heat, date, allEvents) ->
        combine(
            eventRepository.observeEventsForDay(date),
            eventRepository.observeEntriesForDay(date),
            eventRepository.observeTypeLookup(),
        ) { dayEvents, dayEntries, types ->
            val entriesByEvent = dayEntries
                .groupBy { it.entry.eventId }
                .mapValues { (_, items) ->
                    items.map { it.entry }.sortedWith(entryComparator)
                }
            // Events spanning the day, plus any event that has an entry today
            // (in case span is stale). Prefer span order.
            val eventIds = linkedSetOf<Long>()
            dayEvents.forEach { eventIds.add(it.id) }
            entriesByEvent.keys.forEach { eventIds.add(it) }
            val eventsById = (dayEvents + allEvents).associateBy { it.id }
            val groups = eventIds.mapNotNull { id ->
                val event = eventsById[id] ?: return@mapNotNull null
                DayEventGroup(
                    event = event,
                    entries = entriesByEvent[id].orEmpty(),
                )
            }.sortedWith(
                compareBy<DayEventGroup> { it.event.startDateEpochDay }
                    .thenBy { it.event.id },
            )

            val weeks = buildWeeks(
                startMonday = rangeStart,
                weekCount = weeksBefore + weeksAfter + 1,
                heatByDay = heat,
                today = today,
            )
            CalendarUiState(
                weekdayLabels = weekdayLabelsMondayFirst(),
                weeks = weeks,
                todayWeekIndex = weeksBefore,
                initialCenterWeekIndex = centerWeekIndexForMonth(weeks, today, fallback = weeksBefore),
                selectedDate = date,
                selectedDateLabel = date.format(selectedDateFormatter),
                selectedDayGroups = groups,
                allEvents = allEvents,
                types = types,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = run {
            val weeks = buildWeeks(
                startMonday = rangeStart,
                weekCount = weeksBefore + weeksAfter + 1,
                heatByDay = emptyMap(),
                today = today,
            )
            CalendarUiState(
                weekdayLabels = weekdayLabelsMondayFirst(),
                weeks = weeks,
                todayWeekIndex = weeksBefore,
                initialCenterWeekIndex = centerWeekIndexForMonth(weeks, today, fallback = weeksBefore),
                selectedDate = today,
                selectedDateLabel = today.format(selectedDateFormatter),
            )
        },
    )

    fun selectDay(date: LocalDate) {
        selectedDate.value = date
    }

    companion object {
        private const val WEEKS_BEFORE = 52
        private const val WEEKS_AFTER = 52
        private val selectedDateFormatter =
            DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())

        private val entryComparator =
            compareBy<EventEntry> { it.startTimeMinutesOfDay == null }
                .thenBy { it.startTimeMinutesOfDay ?: Int.MAX_VALUE }
                .thenByDescending { it.intensity }
                .thenByDescending { it.createdAtMillis }

        private fun centerWeekIndexForMonth(
            weeks: List<CalendarWeek>,
            today: LocalDate,
            fallback: Int,
        ): Int {
            val monthWeekIndices = weeks.mapIndexedNotNull { index, week ->
                val inCurrentMonth = week.days.any {
                    it.date.year == today.year && it.date.month == today.month
                }
                if (inCurrentMonth) index else null
            }
            if (monthWeekIndices.isEmpty()) return fallback
            return monthWeekIndices[monthWeekIndices.size / 2]
        }

        fun factory(eventRepository: EventRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CalendarViewModel(eventRepository) as T
                }
            }
        }
    }
}
