package com.maimonthlyhoppinings.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maimonthlyhoppinings.data.BackupRepository

class DataBackupViewModel(
    private val backupRepository: BackupRepository,
) : ViewModel() {
    suspend fun export(): String = backupRepository.export()

    suspend fun import(json: String, replace: Boolean) {
        backupRepository.import(json, replace)
    }

    companion object {
        fun factory(backupRepository: BackupRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DataBackupViewModel(backupRepository) as T
                }
            }
        }
    }
}
