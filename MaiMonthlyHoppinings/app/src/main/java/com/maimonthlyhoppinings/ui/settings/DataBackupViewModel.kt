package com.maimonthlyhoppinings.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.AppPreferences
import com.maimonthlyhoppinings.data.AutoBackupRepository
import com.maimonthlyhoppinings.data.AutoBackupSettings
import com.maimonthlyhoppinings.data.BackupRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataBackupViewModel(
    private val backupRepository: BackupRepository,
    private val autoBackupRepository: AutoBackupRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    val autoBackup: StateFlow<AutoBackupSettings> = appPreferences.autoBackupSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AutoBackupSettings(),
    )

    suspend fun export(): String = backupRepository.export()

    suspend fun import(json: String, replace: Boolean) {
        backupRepository.import(json, replace)
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setAutoBackupEnabled(enabled)
            if (enabled) {
                autoBackupRepository.backupNow()
            }
        }
    }

    fun setRetainMonths(months: Int) {
        viewModelScope.launch {
            appPreferences.setAutoBackupRetainMonths(months)
            autoBackupRepository.applyRetention()
        }
    }

    fun setMaxCount(count: Int) {
        viewModelScope.launch {
            appPreferences.setAutoBackupMaxCount(count)
            autoBackupRepository.applyRetention()
        }
    }

    companion object {
        fun factory(
            backupRepository: BackupRepository,
            autoBackupRepository: AutoBackupRepository,
            appPreferences: AppPreferences,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DataBackupViewModel(
                        backupRepository,
                        autoBackupRepository,
                        appPreferences,
                    ) as T
                }
            }
        }
    }
}
