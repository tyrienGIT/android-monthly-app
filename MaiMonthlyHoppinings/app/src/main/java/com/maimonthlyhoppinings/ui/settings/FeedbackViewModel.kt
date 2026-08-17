package com.maimonthlyhoppinings.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.FeedbackNote
import com.maimonthlyhoppinings.data.FeedbackNotesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FeedbackUiState(
    val notes: List<FeedbackNote> = emptyList(),
    val openNoteId: String? = null,
    val draft: String = "",
    val savedDraft: String = "",
    val justSaved: Boolean = false,
) {
    val openNote: FeedbackNote? = notes.firstOrNull { it.id == openNoteId }
    val dirty: Boolean = openNoteId != null && draft != savedDraft
}

class FeedbackViewModel(
    private val store: FeedbackNotesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val notes = withContext(Dispatchers.IO) { store.list() }
            _uiState.update { it.copy(notes = notes) }
        }
    }

    fun open(id: String) {
        viewModelScope.launch {
            val note = withContext(Dispatchers.IO) { store.read(id) } ?: return@launch
            _uiState.update {
                it.copy(
                    notes = it.notes.map { existing -> if (existing.id == note.id) note else existing },
                    openNoteId = note.id,
                    draft = note.markdown,
                    savedDraft = note.markdown,
                    justSaved = false,
                )
            }
        }
    }

    fun create() {
        viewModelScope.launch {
            val note = withContext(Dispatchers.IO) { store.create() }
            _uiState.update {
                it.copy(
                    notes = listOf(note) + it.notes.filterNot { existing -> existing.id == note.id },
                    openNoteId = note.id,
                    draft = "",
                    savedDraft = "",
                    justSaved = false,
                )
            }
        }
    }

    fun onDraftChanged(text: String) {
        _uiState.update { it.copy(draft = text, justSaved = false) }
    }

    fun save() {
        val id = _uiState.value.openNoteId ?: return
        val text = _uiState.value.draft
        viewModelScope.launch {
            val note = withContext(Dispatchers.IO) { store.write(id, text) }
            _uiState.update { state ->
                state.copy(
                    notes = (listOf(note) + state.notes.filterNot { it.id == note.id })
                        .sortedByDescending { it.updatedAtMillis },
                    draft = note.markdown,
                    savedDraft = note.markdown,
                    justSaved = true,
                )
            }
        }
    }

    fun closeEditor(saveIfDirty: Boolean = true) {
        val state = _uiState.value
        val id = state.openNoteId
        if (id != null && state.draft.isBlank() && state.savedDraft.isBlank()) {
            delete(id)
            return
        }
        if (saveIfDirty && state.dirty) {
            save()
        }
        _uiState.update {
            it.copy(openNoteId = null, draft = "", savedDraft = "", justSaved = false)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { store.delete(id) }
            _uiState.update { state ->
                state.copy(
                    notes = state.notes.filterNot { it.id == id },
                    openNoteId = if (state.openNoteId == id) null else state.openNoteId,
                    draft = if (state.openNoteId == id) "" else state.draft,
                    savedDraft = if (state.openNoteId == id) "" else state.savedDraft,
                )
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FeedbackViewModel(FeedbackNotesStore(application)) as T
                }
            }
        }
    }
}
