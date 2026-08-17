package com.maimonthlyhoppinings.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EntryWithEvent
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeColor
import com.maimonthlyhoppinings.data.EventTypeLookup
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
    val visible: Boolean,
    val averageIntensity: Float?,
    val entryCount: Int,
)

data class TrendsUiState(
    val range: TrendsRange = TrendsRange.THREE_MONTHS,
    val startDate: LocalDate = LocalDate.now().minusDays(TrendsRange.THREE_MONTHS.days - 1),
    val endDate: LocalDate = LocalDate.now(),
    val series: List<TrendSeries> = emptyList(),
    val selectedEpochDay: Long? = null,
    val selectedDayEntries: List<EntryWithEvent> = emptyList(),
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
    val hasAnyPoints: Boolean = false,
)

class TrendsViewModel(
    eventRepository: EventRepository,
) : ViewModel() {
    private val range = MutableStateFlow(TrendsRange.THREE_MONTHS)
    private val hiddenOverride = MutableStateFlow<Set<String>?>(null)
    private val selectedEpochDay = MutableStateFlow<Long?>(null)

    private val endDate = LocalDate.now()
    private val widestStart = endDate.minusDays(TrendsRange.YEAR.days - 1)

    val uiState: StateFlow<TrendsUiState> = combine(
        range,
        hiddenOverride,
        selectedEpochDay,
        eventRepository.observeEntriesInRange(widestStart, endDate),
        eventRepository.observeTypeLookup(),
    ) { selectedRange, hidden, selectedDay, entries, types ->
        val startDate = endDate.minusDays(selectedRange.days - 1)
        val startEpoch = startDate.toEpochDay()
        val endEpoch = endDate.toEpochDay()
        val inRange = entries.filter { it.entry.dateEpochDay in startEpoch..endEpoch }
        val focusedTypeId = inRange
            .groupingBy { it.event.eventTypeId }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        val effectiveHidden = hidden ?: types.all
            .map { it.id }
            .filter { it != focusedTypeId }
            .toSet()
        val series = buildSeries(inRange, types, effectiveHidden)
        val visibleTypeIds = series.filter { it.visible }.map { it.typeId }.toSet()
        val dayEntries = selectedDay?.let { day ->
            inRange.filter {
                it.entry.dateEpochDay == day && it.event.eventTypeId in visibleTypeIds
            }.sortedBy { it.event.eventTypeId }
        }.orEmpty()
        TrendsUiState(
            range = selectedRange,
            startDate = startDate,
            endDate = endDate,
            series = series,
            selectedEpochDay = selectedDay,
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
    }

    fun toggleType(typeId: String) {
        val currentHidden = hiddenOverride.value
            ?: uiState.value.series.filterNot { it.visible }.map { it.typeId }.toSet()
        hiddenOverride.value = currentHidden.toMutableSet().also { ids ->
            if (!ids.add(typeId)) ids.remove(typeId)
        }
    }

    fun selectDay(epochDay: Long?) {
        selectedEpochDay.value = epochDay
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

        internal fun buildSeries(
            entries: List<EntryWithEvent>,
            types: EventTypeLookup,
            hiddenTypeIds: Set<String>,
        ): List<TrendSeries> {
            val peaks = entries
                .groupBy { it.event.eventTypeId to it.entry.dateEpochDay }
                .map { (key, items) ->
                    Triple(key.first, key.second, items.maxOf { it.entry.intensity })
                }
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
                    visible = typeId !in hiddenTypeIds,
                    averageIntensity = points.takeIf { it.isNotEmpty() }
                        ?.map { it.intensity }?.average()?.toFloat(),
                    entryCount = points.size,
                )
            }
        }
    }
}
