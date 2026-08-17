package com.maimonthlyhoppinings

import android.app.Application
import com.maimonthlyhoppinings.data.AppDatabase
import com.maimonthlyhoppinings.data.BackupRepository
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.SavedThemeRepository
import com.maimonthlyhoppinings.data.ThemePreferences
import kotlinx.coroutines.runBlocking

open class MaiMonthlyHoppiningsApp : Application() {
    lateinit var eventRepository: EventRepository
        private set

    lateinit var savedThemeRepository: SavedThemeRepository
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    lateinit var backupRepository: BackupRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        eventRepository = EventRepository(
            trackedEventDao = database.trackedEventDao(),
            eventEntryDao = database.eventEntryDao(),
            eventTypeDao = database.eventTypeDao(),
        )
        savedThemeRepository = SavedThemeRepository(database.savedColorThemeDao())
        themePreferences = ThemePreferences(this)
        runBlocking { themePreferences.applyStoredNightMode() }
        backupRepository = BackupRepository(
            database = database,
            eventTypeDao = database.eventTypeDao(),
            trackedEventDao = database.trackedEventDao(),
            eventEntryDao = database.eventEntryDao(),
            savedColorThemeDao = database.savedColorThemeDao(),
            themePreferences = themePreferences,
        )
    }
}
