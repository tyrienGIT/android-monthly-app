package com.maimonthlyhoppinings

import android.app.Application
import com.maimonthlyhoppinings.data.AppDatabase
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.SavedThemeRepository
import com.maimonthlyhoppinings.data.ThemePreferences

class MaiMonthlyHoppiningsApp : Application() {
    lateinit var eventRepository: EventRepository
        private set

    lateinit var savedThemeRepository: SavedThemeRepository
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        eventRepository = EventRepository(
            trackedEventDao = database.trackedEventDao(),
            eventEntryDao = database.eventEntryDao(),
        )
        savedThemeRepository = SavedThemeRepository(database.savedColorThemeDao())
        themePreferences = ThemePreferences(this)
    }
}
