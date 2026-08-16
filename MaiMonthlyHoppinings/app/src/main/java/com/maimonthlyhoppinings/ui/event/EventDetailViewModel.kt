package com.maimonthlyhoppinings.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EventEntry
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventWithEntries
import com.maimonthlyhoppinings.data.IntensityHeatDay
import com.maimonthlyhoppinings.data.intensityHeatmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val eventWithEntries: EventWithEntries? = null,
    val sortedEntries: List<EventEntry> = emptyList(),
    val heatmapDays: List<IntensityHeatDay> = emptyList(),
    val heatmapStartLabel: String = "",
    val heatmapEndLabel: String = "",
    val notFound: Boolean = false,
)

class EventDetailViewModel(
    private val eventId: Long,
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val heatDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    val uiState: StateFlow<EventDetailUiState> = eventRepository.observeEvent(eventId)
        .map { event ->
            if (event == null) {
                EventDetailUiState(notFound = true)
            } else {
                val heatmap = event.event.intensityHeatmap(event.entries)
                EventDetailUiState(
                    eventWithEntries = event,
                    sortedEntries = event.entries.sortedWith(
                        compareByDescending<EventEntry> { it.dateEpochDay }
                            .thenByDescending { it.startTimeMinutesOfDay ?: Int.MIN_VALUE }
                            .thenByDescending { it.createdAtMillis },
                    ),
                    heatmapDays = heatmap,
                    heatmapStartLabel = LocalDate.ofEpochDay(event.event.startDateEpochDay)
                        .format(heatDateFormatter),
                    heatmapEndLabel = LocalDate.ofEpochDay(event.event.endDateEpochDay)
                        .format(heatDateFormatter),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EventDetailUiState(),
        )

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            eventRepository.deleteEntry(entryId)
        }
    }

    fun deleteEvent(onDeleted: () -> Unit) {
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
            onDeleted()
        }
    }

    companion object {
        fun factory(
            eventId: Long,
            eventRepository: EventRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventDetailViewModel(eventId, eventRepository) as T
                }
            }
        }
    }
}
