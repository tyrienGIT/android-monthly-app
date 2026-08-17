package com.maimonthlyhoppinings.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.ActiveColorTheme
import com.maimonthlyhoppinings.data.ColorTheme
import com.maimonthlyhoppinings.data.SavedThemeRepository
import com.maimonthlyhoppinings.data.ThemeMode
import com.maimonthlyhoppinings.data.ThemePreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModel(
    private val themePreferences: ThemePreferences,
    private val savedThemeRepository: SavedThemeRepository,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.default,
        )

    val activeColorTheme: StateFlow<ActiveColorTheme> = themePreferences.activeColorTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActiveColorTheme.default,
        )

    val savedThemes = savedThemeRepository.observeSavedThemes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val activePalette: StateFlow<AppColorPalette> = themePreferences.activeColorTheme
        .flatMapLatest { active ->
            when (active) {
                is ActiveColorTheme.Preset -> flowOf(active.theme.palette())
                is ActiveColorTheme.Custom -> {
                    savedThemeRepository.observeSavedTheme(active.themeId).map { saved ->
                        saved?.toPalette() ?: ColorTheme.default.palette()
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ColorTheme.default.palette(),
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setPresetColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            themePreferences.setActiveColorTheme(ActiveColorTheme.Preset(theme))
        }
    }

    fun setCustomColorTheme(themeId: Long) {
        viewModelScope.launch {
            themePreferences.setActiveColorTheme(ActiveColorTheme.Custom(themeId))
        }
    }

    fun saveCustomTheme(
        name: String,
        lightPrimaryArgb: Int,
        lightSecondaryArgb: Int,
        lightTertiaryArgb: Int,
        darkPrimaryArgb: Int,
        darkSecondaryArgb: Int,
        darkTertiaryArgb: Int,
        applyAfterSave: Boolean,
    ) {
        viewModelScope.launch {
            val id = savedThemeRepository.saveTheme(
                name = name,
                lightPrimaryArgb = lightPrimaryArgb,
                lightSecondaryArgb = lightSecondaryArgb,
                lightTertiaryArgb = lightTertiaryArgb,
                darkPrimaryArgb = darkPrimaryArgb,
                darkSecondaryArgb = darkSecondaryArgb,
                darkTertiaryArgb = darkTertiaryArgb,
            )
            if (applyAfterSave) {
                themePreferences.setActiveColorTheme(ActiveColorTheme.Custom(id))
            }
        }
    }

    fun deleteSavedTheme(themeId: Long) {
        viewModelScope.launch {
            val active = activeColorTheme.value
            savedThemeRepository.deleteTheme(themeId)
            if (active is ActiveColorTheme.Custom && active.themeId == themeId) {
                themePreferences.setActiveColorTheme(ActiveColorTheme.default)
            }
        }
    }

    companion object {
        fun factory(
            themePreferences: ThemePreferences,
            savedThemeRepository: SavedThemeRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ThemeViewModel(themePreferences, savedThemeRepository) as T
                }
            }
        }
    }
}
