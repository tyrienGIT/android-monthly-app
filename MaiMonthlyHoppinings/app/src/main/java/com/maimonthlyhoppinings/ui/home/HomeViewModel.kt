package com.maimonthlyhoppinings.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.EventWithEntries
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val events: List<EventWithEntries> = emptyList(),
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
)

class HomeViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        eventRepository.observeEventsWithEntries(),
        eventRepository.observeTypeLookup(),
    ) { events, types ->
        HomeUiState(events = events, types = types)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

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
