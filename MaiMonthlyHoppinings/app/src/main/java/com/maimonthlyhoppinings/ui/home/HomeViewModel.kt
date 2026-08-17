package com.maimonthlyhoppinings.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.EventWithEntries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class HomeRange(val label: String) {
    THREE_MONTHS("3 months"),
    SIX_MONTHS("6 months"),
    YEAR("1 year"),
    ALL("All"),
}

data class HomeUiState(
    val events: List<EventWithEntries> = emptyList(),
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
    val range: HomeRange = HomeRange.SIX_MONTHS,
    val hasAnyEvents: Boolean = false,
)

class HomeViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val range = MutableStateFlow(HomeRange.SIX_MONTHS)

    val uiState: StateFlow<HomeUiState> = combine(
        eventRepository.observeEventsWithEntries(),
        eventRepository.observeTypeLookup(),
        range,
    ) { events, types, selectedRange ->
        HomeUiState(
            events = events.filter { it.overlaps(selectedRange) },
            types = types,
            range = selectedRange,
            hasAnyEvents = events.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun setRange(range: HomeRange) {
        this.range.value = range
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
        }
    }

    companion object {
        fun factory(eventRepository: EventRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(eventRepository) as T
                }
            }
        }
    }
}

private fun EventWithEntries.overlaps(range: HomeRange): Boolean {
    if (range == HomeRange.ALL) return true
    val cutoff = when (range) {
        HomeRange.THREE_MONTHS -> LocalDate.now().minusMonths(3)
        HomeRange.SIX_MONTHS -> LocalDate.now().minusMonths(6)
        HomeRange.YEAR -> LocalDate.now().minusYears(1)
        HomeRange.ALL -> return true
    }.toEpochDay()
    return event.endDateEpochDay >= cutoff
}
