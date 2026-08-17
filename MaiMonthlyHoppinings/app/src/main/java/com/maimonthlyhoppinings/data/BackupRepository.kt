package com.maimonthlyhoppinings.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

class BackupRepository(
    private val books: BookManager,
    private val themePreferences: ThemePreferences,
) {
    private val database: AppDatabase
        get() = books.database
    private val eventTypeDao: EventTypeDao
        get() = database.eventTypeDao()
    private val trackedEventDao: TrackedEventDao
        get() = database.trackedEventDao()
    private val eventEntryDao: EventEntryDao
        get() = database.eventEntryDao()
    private val savedColorThemeDao: SavedColorThemeDao
        get() = database.savedColorThemeDao()

    suspend fun export(): String {
        val types = eventTypeDao.getAll()
        val events = trackedEventDao.getAll()
        val entries = eventEntryDao.getAll()
        val themes = savedColorThemeDao.getAll()
        val themeMode = themePreferences.themeMode.first()
        val activeColorTheme = themePreferences.activeColorTheme.first()
        return BackupFile(
            exportedAtMillis = System.currentTimeMillis(),
            eventTypes = types.map { type ->
                BackupEventType(
                    id = type.id,
                    label = type.label,
                    color = type.color,
                )
            },
            events = events.map { event ->
                BackupEvent(
                    id = event.id,
                    typeId = event.eventTypeId,
                    title = event.title,
                    details = event.details,
                    emoji = event.emoji,
                    startDateEpochDay = event.startDateEpochDay,
                    endDateEpochDay = event.endDateEpochDay,
                    createdAtMillis = event.createdAtMillis,
                )
            },
            entries = entries.map { entry ->
                BackupEntry(
                    id = entry.id,
                    eventId = entry.eventId,
                    title = entry.title,
                    emoji = entry.emoji,
                    dateEpochDay = entry.dateEpochDay,
                    startTimeMinutesOfDay = entry.startTimeMinutesOfDay,
                    details = entry.details,
                    intensity = entry.intensity,
                    createdAtMillis = entry.createdAtMillis,
                )
            },
            customThemes = themes.map { theme ->
                BackupCustomTheme(
                    id = theme.id,
                    name = theme.name,
                    lightPrimaryArgb = theme.lightPrimaryArgb,
                    lightSecondaryArgb = theme.lightSecondaryArgb,
                    lightTertiaryArgb = theme.lightTertiaryArgb,
                    darkPrimaryArgb = theme.darkPrimaryArgb,
                    darkSecondaryArgb = theme.darkSecondaryArgb,
                    darkTertiaryArgb = theme.darkTertiaryArgb,
                    createdAtMillis = theme.createdAtMillis,
                )
            },
            preferences = BackupPreferences(
                themeMode = themeMode.name,
                activeColorTheme = activeColorTheme.encode(),
            ),
        ).stringify()
    }

    suspend fun import(json: String, replace: Boolean) {
        val backup = BackupFile.parse(json)
        backup.preferences?.let { prefs ->
            runCatching { ThemeMode.valueOf(prefs.themeMode) }.getOrElse {
                throw IllegalArgumentException("Unknown theme mode")
            }
        }
        database.withTransaction {
            importTypes(backup, replace)
            importEvents(backup, replace)
            importEntries(backup, replace)
            importThemes(backup, replace)
        }
        importPreferences(backup)
    }

    private suspend fun importTypes(backup: BackupFile, replace: Boolean) {
        if (replace) {
            deleteMissing(backup.eventTypes.map { it.id }, eventTypeDao::deleteNotIn, eventTypeDao::deleteAll)
        }
        backup.eventTypes.forEach { type ->
            eventTypeDao.upsert(
                EventTypeEntity(
                    id = type.id,
                    label = type.label,
                    color = type.color,
                ),
            )
        }
    }

    private suspend fun importEvents(backup: BackupFile, replace: Boolean) {
        if (replace) {
            deleteMissing(backup.events.map { it.id }, trackedEventDao::deleteNotIn, trackedEventDao::deleteAll)
        }
        backup.events.forEach { item ->
            val entity = TrackedEvent(
                id = item.id,
                title = item.title,
                eventTypeId = item.typeId,
                details = item.details,
                emoji = item.emoji,
                startDateEpochDay = item.startDateEpochDay,
                endDateEpochDay = item.endDateEpochDay,
                createdAtMillis = item.createdAtMillis,
            )
            if (trackedEventDao.getById(item.id) != null) {
                trackedEventDao.update(entity)
            } else {
                trackedEventDao.insert(entity)
            }
        }
        database.bumpAutoincrement("tracked_events", backup.events.maxOfOrNull { it.id } ?: 0L)
    }

    private suspend fun importEntries(backup: BackupFile, replace: Boolean) {
        if (replace) {
            deleteMissing(backup.entries.map { it.id }, eventEntryDao::deleteNotIn, eventEntryDao::deleteAll)
        }
        backup.entries.forEach { item ->
            val entity = EventEntry(
                id = item.id,
                eventId = item.eventId,
                title = item.title,
                emoji = item.emoji,
                dateEpochDay = item.dateEpochDay,
                startTimeMinutesOfDay = item.startTimeMinutesOfDay,
                details = item.details,
                intensity = item.intensity,
                createdAtMillis = item.createdAtMillis,
            )
            if (eventEntryDao.getById(item.id) != null) {
                eventEntryDao.update(entity)
            } else {
                eventEntryDao.insert(entity)
            }
        }
        database.bumpAutoincrement("event_entries", backup.entries.maxOfOrNull { it.id } ?: 0L)
    }

    private suspend fun importThemes(backup: BackupFile, replace: Boolean) {
        if (replace) {
            deleteMissing(
                backup.customThemes.map { it.id },
                savedColorThemeDao::deleteNotIn,
                savedColorThemeDao::deleteAll,
            )
        }
        backup.customThemes.forEach { item ->
            savedColorThemeDao.insert(
                SavedColorTheme(
                    id = item.id,
                    name = item.name,
                    lightPrimaryArgb = item.lightPrimaryArgb,
                    lightSecondaryArgb = item.lightSecondaryArgb,
                    lightTertiaryArgb = item.lightTertiaryArgb,
                    darkPrimaryArgb = item.darkPrimaryArgb,
                    darkSecondaryArgb = item.darkSecondaryArgb,
                    darkTertiaryArgb = item.darkTertiaryArgb,
                    createdAtMillis = item.createdAtMillis,
                ),
            )
        }
        database.bumpAutoincrement(
            "saved_color_themes",
            backup.customThemes.maxOfOrNull { it.id } ?: 0L,
        )
    }

    private suspend fun importPreferences(backup: BackupFile) {
        val preferences = backup.preferences ?: return
        val themeMode = runCatching { ThemeMode.valueOf(preferences.themeMode) }.getOrElse {
            throw IllegalArgumentException("Unknown theme mode")
        }
        themePreferences.setThemeMode(themeMode)
        themePreferences.setActiveColorTheme(ActiveColorTheme.decode(preferences.activeColorTheme))
    }

    private suspend fun <T> deleteMissing(
        keepIds: List<T>,
        deleteNotIn: suspend (List<T>) -> Unit,
        deleteAll: suspend () -> Unit,
    ) {
        if (keepIds.isEmpty()) {
            deleteAll()
        } else {
            deleteNotIn(keepIds)
        }
    }
}
