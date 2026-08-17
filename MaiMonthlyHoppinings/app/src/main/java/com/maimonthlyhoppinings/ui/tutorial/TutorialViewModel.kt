package com.maimonthlyhoppinings.ui.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TutorialUiState(
    val active: Boolean = false,
    val steps: List<TutorialStep> = emptyList(),
    val index: Int = 0,
    val isFullTour: Boolean = false,
) {
    val step: TutorialStep? get() = steps.getOrNull(index)
    val isFirst: Boolean get() = index <= 0
    val isLast: Boolean get() = steps.isNotEmpty() && index >= steps.lastIndex
}

class TutorialViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel(), TutorialController {
    private val _uiState = MutableStateFlow(TutorialUiState())
    val uiState: StateFlow<TutorialUiState> = _uiState.asStateFlow()

    private var firstRunChecked = false

    fun startFirstRunIfNeeded() {
        if (firstRunChecked) return
        firstRunChecked = true
        viewModelScope.launch {
            if (!appPreferences.tutorialCompleted.first()) {
                startFullTour()
            }
        }
    }

    override fun startFullTour() {
        _uiState.value = TutorialUiState(
            active = true,
            steps = TutorialSteps.fullTour,
            index = 0,
            isFullTour = true,
        )
    }

    override fun startSection(section: TutorialSection) {
        _uiState.value = TutorialUiState(
            active = true,
            steps = TutorialSteps.stepsFor(section),
            index = 0,
            isFullTour = false,
        )
    }

    fun next() {
        _uiState.update { state ->
            if (!state.active || state.steps.isEmpty()) return@update state
            if (state.isLast) return@update state
            state.copy(index = state.index + 1)
        }
    }

    fun back() {
        _uiState.update { state ->
            if (!state.active || state.isFirst) return@update state
            state.copy(index = state.index - 1)
        }
    }

    fun skip() {
        complete()
    }

    fun finish() {
        complete()
    }

    private fun complete() {
        val wasActive = _uiState.value.active
        _uiState.value = TutorialUiState()
        if (wasActive) {
            viewModelScope.launch {
                appPreferences.setTutorialCompleted(true)
            }
        }
    }

    companion object {
        fun factory(appPreferences: AppPreferences): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TutorialViewModel(appPreferences) as T
                }
            }
        }
    }
}
