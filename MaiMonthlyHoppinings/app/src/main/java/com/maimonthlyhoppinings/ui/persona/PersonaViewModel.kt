package com.maimonthlyhoppinings.ui.persona

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.Persona
import com.maimonthlyhoppinings.data.PersonaManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PersonasUiState(
    val personas: List<Persona> = listOf(Persona.default()),
    val active: Persona = Persona.default(),
) {
    val canDelete: Boolean
        get() = personas.size > 1
}

class PersonaViewModel(
    private val personaManager: PersonaManager,
) : ViewModel() {
    val uiState: StateFlow<PersonasUiState> = combine(
        personaManager.personas,
        personaManager.activePersona,
    ) { personas, active ->
        PersonasUiState(personas = personas, active = active)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PersonasUiState(
            personas = personaManager.personas.value,
            active = personaManager.activePersona.value,
        ),
    )

    fun switchTo(id: String) {
        viewModelScope.launch {
            personaManager.switchTo(id)
        }
    }

    fun create(name: String) {
        viewModelScope.launch {
            personaManager.create(name)
        }
    }

    fun rename(id: String, name: String) {
        viewModelScope.launch {
            personaManager.rename(id, name)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            personaManager.delete(id)
        }
    }

    companion object {
        fun factory(personaManager: PersonaManager): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PersonaViewModel(personaManager) as T
                }
            }
        }
    }
}
