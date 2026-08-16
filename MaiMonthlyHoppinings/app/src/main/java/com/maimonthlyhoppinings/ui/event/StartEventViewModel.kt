package com.maimonthlyhoppinings.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EventInput
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventType
import com.maimonthlyhoppinings.data.EventTypeLookup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EventMetaDraft(
    val title: String = "",
    val eventTypeId: String = EventType.defaultId,
    val details: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
)

data class StartEventUiState(
    val draft: EventMetaDraft = EventMetaDraft(),
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
    val startDateLabel: String = "",
    val endDateLabel: String = "",
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
)

class StartEventViewModel(
    private val editingEventId: Long?,
    private val seedDate: LocalDate,
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val draft = MutableStateFlow(
        EventMetaDraft(startDate = seedDate, endDate = seedDate),
    )
    private val errorMessage = MutableStateFlow<String?>(null)
    private val saved = Channel<Long>(Channel.BUFFERED)
    val savedEvents = saved.receiveAsFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

    val uiState: StateFlow<StartEventUiState> = combine(
        draft,
        errorMessage,
        eventRepository.observeTypeLookup(),
    ) { current, error, types ->
        StartEventUiState(
            draft = current,
            types = types,
            startDateLabel = current.startDate.format(dateFormatter),
            endDateLabel = current.endDate.format(dateFormatter),
            isEditing = editingEventId != null,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StartEventUiState(
            draft = draft.value,
            startDateLabel = draft.value.startDate.format(dateFormatter),
            endDateLabel = draft.value.endDate.format(dateFormatter),
            isEditing = editingEventId != null,
        ),
    )

    init {
        if (editingEventId != null) {
            viewModelScope.launch {
                val existing = eventRepository.getEvent(editingEventId)?.event
                if (existing != null) {
                    draft.value = EventMetaDraft(
                        title = existing.title,
                        eventTypeId = existing.eventTypeId,
                        details = existing.details,
                        startDate = LocalDate.ofEpochDay(existing.startDateEpochDay),
                        endDate = LocalDate.ofEpochDay(existing.endDateEpochDay),
                    )
                } else {
                    errorMessage.value = "Event not found"
                }
            }
        }
    }

    fun resolvedTitle(draft: EventMetaDraft = this.draft.value): String {
        return draft.title.trim().ifEmpty { uiState.value.types.label(draft.eventTypeId) }
    }

    fun onTitleChange(value: String) {
        draft.update { it.copy(title = value) }
        errorMessage.value = null
    }

    fun onEventTypeChange(value: String) {
        draft.update { it.copy(eventTypeId = value) }
    }

    fun onDetailsChange(value: String) {
        draft.update { it.copy(details = value) }
    }

    fun onDateRangeSelected(startEpochDay: Long, endEpochDay: Long) {
        val start = LocalDate.ofEpochDay(minOf(startEpochDay, endEpochDay))
        val end = LocalDate.ofEpochDay(maxOf(startEpochDay, endEpochDay))
        draft.update { it.copy(startDate = start, endDate = end) }
        errorMessage.value = null
    }

    fun save() {
        viewModelScope.launch {
            val current = draft.value
            if (current.endDate.isBefore(current.startDate)) {
                errorMessage.value = "End date must be on or after start date"
                return@launch
            }
            val input = EventInput(
                title = resolvedTitle(current),
                eventTypeId = current.eventTypeId,
                details = current.details,
                startDate = current.startDate,
                endDate = current.endDate,
            )
            try {
                val id = if (editingEventId != null) {
                    eventRepository.updateEvent(editingEventId, input)
                    editingEventId
                } else {
                    eventRepository.startEvent(input)
                }
                errorMessage.value = null
                saved.send(id)
            } catch (e: IllegalArgumentException) {
                errorMessage.value = e.message
            }
        }
    }

    companion object {
        fun factory(
            editingEventId: Long?,
            seedDate: LocalDate = LocalDate.now(),
            eventRepository: EventRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StartEventViewModel(editingEventId, seedDate, eventRepository) as T
                }
            }
        }
    }
}
