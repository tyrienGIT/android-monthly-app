package com.maimonthlyhoppinings.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeColor
import com.maimonthlyhoppinings.data.EventTypeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategorySettingsUiState(
    val categories: List<EventTypeEntity> = emptyList(),
)

class CategorySettingsViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {
    val uiState: StateFlow<CategorySettingsUiState> = eventRepository.observeTypes()
        .map { types ->
            CategorySettingsUiState(categories = types.sortedBy { typeSortKey(it.id) })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategorySettingsUiState(),
        )

    fun updateLabel(id: String, label: String) {
        viewModelScope.launch {
            eventRepository.updateType(id = id, label = label)
        }
    }

    fun updateColor(id: String, color: EventTypeColor) {
        viewModelScope.launch {
            eventRepository.updateType(id = id, color = color)
        }
    }

    companion object {
        fun typeSortKey(id: String): Int {
            return id.removePrefix("type_").toIntOrNull() ?: Int.MAX_VALUE
        }

        fun factory(eventRepository: EventRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CategorySettingsViewModel(eventRepository) as T
                }
            }
        }
    }
}
