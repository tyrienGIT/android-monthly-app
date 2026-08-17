package com.maimonthlyhoppinings.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EmojiTags
import com.maimonthlyhoppinings.data.EntryInput
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.TrackedEvent
import com.maimonthlyhoppinings.data.displayTitle
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

data class EntryDraft(
    val title: String = "",
    val emoji: String = "",
    val date: LocalDate,
    val startTime: LocalTime? = LocalTime.now().withSecond(0).withNano(0),
    val details: String = "",
    val intensity: Int = 5,
)

data class EntryEditorUiState(
    val event: TrackedEvent? = null,
    val types: EventTypeLookup = EventTypeLookup(emptyList()),
    val draft: EntryDraft,
    val dateLabel: String,
    val startTimeLabel: String,
    val isEditing: Boolean,
    val errorMessage: String?,
)

class EntryEditorViewModel(
    private val eventIdArg: Long?,
    private val editingEntryId: Long?,
    private val initialDate: LocalDate,
    private val eventRepository: EventRepository,
) : ViewModel() {
    private val resolvedEventId = MutableStateFlow<Long?>(eventIdArg?.takeIf { it > 0L })
    private val draft = MutableStateFlow(EntryDraft(date = initialDate))
    private val parentEvent = MutableStateFlow<TrackedEvent?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val saved = Channel<Unit>(Channel.BUFFERED)
    val savedEvents = saved.receiveAsFlow()
    @Volatile
    private var saveInFlight = false

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    val uiState: StateFlow<EntryEditorUiState> = combine(
        parentEvent,
        draft,
        errorMessage,
        eventRepository.observeTypeLookup(),
    ) { event, currentDraft, error, types ->
        EntryEditorUiState(
            event = event,
            types = types,
            draft = currentDraft,
            dateLabel = currentDraft.date.format(dateFormatter),
            startTimeLabel = currentDraft.startTime?.format(timeFormatter) ?: "No time",
            isEditing = editingEntryId != null,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EntryEditorUiState(
            draft = draft.value,
            dateLabel = draft.value.date.format(dateFormatter),
            startTimeLabel = draft.value.startTime?.format(timeFormatter) ?: "No time",
            isEditing = editingEntryId != null,
            errorMessage = null,
        ),
    )

    init {
        viewModelScope.launch {
            if (editingEntryId != null) {
                val existing = eventRepository.getEntry(editingEntryId)
                if (existing == null) {
                    errorMessage.value = "Entry not found"
                    return@launch
                }
                resolvedEventId.value = existing.entry.eventId
                parentEvent.value = existing.event
                val minutes = existing.entry.startTimeMinutesOfDay
                draft.value = EntryDraft(
                    title = existing.entry.title,
                    emoji = existing.entry.emoji,
                    date = LocalDate.ofEpochDay(existing.entry.dateEpochDay),
                    startTime = minutes?.let { LocalTime.of(it / 60, it % 60) },
                    details = existing.entry.details,
                    intensity = existing.entry.intensity,
                )
                return@launch
            }

            val eventId = eventIdArg?.takeIf { it > 0L }
            if (eventId == null) {
                errorMessage.value = "Event not found"
                return@launch
            }
            val withEntries = eventRepository.getEvent(eventId)
            if (withEntries == null) {
                errorMessage.value = "Event not found"
                return@launch
            }
            resolvedEventId.value = eventId
            parentEvent.value = withEntries.event
        }
    }

    fun eventTitle(): String {
        return parentEvent.value?.displayTitle(uiState.value.types) ?: "Entry"
    }

    fun confirmLabel(): String {
        val custom = draft.value.title.trim()
        val base = custom.ifEmpty { eventTitle() }
        return EmojiTags.prefix(draft.value.emoji, base)
    }

    fun onTitleChange(value: String) {
        draft.update { it.copy(title = value) }
    }

    fun onEmojiChange(value: String) {
        draft.update { it.copy(emoji = value) }
    }

    fun onDetailsChange(value: String) {
        draft.update { it.copy(details = value) }
    }

    fun onDateSelected(epochDay: Long) {
        draft.update { it.copy(date = LocalDate.ofEpochDay(epochDay)) }
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        draft.update { it.copy(startTime = LocalTime.of(hour, minute)) }
    }

    fun clearTime() {
        draft.update { it.copy(startTime = null) }
    }

    fun onIntensityChange(value: Int) {
        draft.update { it.copy(intensity = value.coerceIn(1, 10)) }
    }

    fun save() {
        if (saveInFlight) return
        saveInFlight = true
        viewModelScope.launch {
            try {
                val eventId = resolvedEventId.value
                if (eventId == null) {
                    errorMessage.value = "Event not found"
                    return@launch
                }
                val current = draft.value
                val input = EntryInput(
                    eventId = eventId,
                    title = current.title,
                    emoji = current.emoji,
                    date = current.date,
                    startTime = current.startTime,
                    details = current.details,
                    intensity = current.intensity,
                )
                if (editingEntryId != null) {
                    eventRepository.updateEntry(editingEntryId, input)
                } else {
                    eventRepository.addEntry(input)
                }
                errorMessage.value = null
                saved.send(Unit)
            } catch (e: IllegalArgumentException) {
                errorMessage.value = e.message
            } finally {
                saveInFlight = false
            }
        }
    }

    companion object {
        fun factory(
            eventId: Long?,
            editingEntryId: Long?,
            initialDate: LocalDate,
            eventRepository: EventRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EntryEditorViewModel(
                        eventIdArg = eventId,
                        editingEntryId = editingEntryId,
                        initialDate = initialDate,
                        eventRepository = eventRepository,
                    ) as T
                }
            }
        }
    }
}
