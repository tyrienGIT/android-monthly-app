package com.maimonthlyhoppinings.data

import android.content.Context
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AutoBackupRepository(
    private val context: Context,
    private val backupRepository: BackupRepository,
    private val appPreferences: AppPreferences,
) {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    private val backupDir: File
        get() = File(context.filesDir, "autobackups").also { it.mkdirs() }

    suspend fun runIfDue() {
        val settings = appPreferences.autoBackupSettings.first()
        if (!settings.enabled) return
        val today = LocalDate.now().toEpochDay()
        if (settings.lastBackupEpochDay == today) {
            prune(settings)
            return
        }
        writeToday()
        appPreferences.setLastAutoBackupEpochDay(today)
        prune(appPreferences.autoBackupSettings.first())
    }

    suspend fun backupNow() {
        val settings = appPreferences.autoBackupSettings.first()
        if (!settings.enabled) return
        writeToday()
        appPreferences.setLastAutoBackupEpochDay(LocalDate.now().toEpochDay())
        prune(settings)
    }

    suspend fun applyRetention() {
        prune(appPreferences.autoBackupSettings.first())
    }

    private suspend fun writeToday() {
        val json = backupRepository.export()
        val name = "mai-auto-${LocalDate.now().format(dateFormat)}.json"
        val target = File(backupDir, name)
        val temp = File(backupDir, "$name.tmp")
        temp.writeText(json, Charsets.UTF_8)
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    private fun prune(settings: AutoBackupSettings) {
        val files = backupDir.listFiles { file ->
            file.isFile && file.name.startsWith("mai-auto-") && file.name.endsWith(".json")
        }?.sortedBy { it.name } ?: return
        val cutoff = LocalDate.now().minusMonths(settings.retainMonths.toLong())
        files.forEach { file ->
            val date = dateFromName(file.name)
            if (date != null && date.isBefore(cutoff)) {
                file.delete()
            }
        }
        val remaining = backupDir.listFiles { file ->
            file.isFile && file.name.startsWith("mai-auto-") && file.name.endsWith(".json")
        }?.sortedBy { it.name } ?: return
        val extra = remaining.size - settings.maxCount
        if (extra > 0) {
            remaining.take(extra).forEach { it.delete() }
        }
    }

    private fun dateFromName(name: String): LocalDate? {
        val stamp = name.removePrefix("mai-auto-").removeSuffix(".json")
        return runCatching { LocalDate.parse(stamp, dateFormat) }.getOrNull()
    }
}
