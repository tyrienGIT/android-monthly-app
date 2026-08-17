package com.maimonthlyhoppinings

import android.app.Application
import com.maimonthlyhoppinings.data.AppPreferences
import com.maimonthlyhoppinings.data.AutoBackupRepository
import com.maimonthlyhoppinings.data.BackupRepository
import com.maimonthlyhoppinings.data.PersonaManager
import com.maimonthlyhoppinings.data.PersonaPreferences
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.SavedThemeRepository
import com.maimonthlyhoppinings.data.ThemePreferences
import kotlinx.coroutines.runBlocking

open class MaiMonthlyHoppiningsApp : Application() {
    lateinit var personaManager: PersonaManager
        private set

    lateinit var eventRepository: EventRepository
        private set

    lateinit var savedThemeRepository: SavedThemeRepository
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    lateinit var appPreferences: AppPreferences
        private set

    lateinit var backupRepository: BackupRepository
        private set

    lateinit var autoBackupRepository: AutoBackupRepository
        private set

    override fun onCreate() {
        super.onCreate()
        themePreferences = ThemePreferences(this)
        appPreferences = AppPreferences(this)
        personaManager = PersonaManager(this, PersonaPreferences(this))
        runBlocking {
            themePreferences.applyStoredNightMode()
            personaManager.start()
        }
        eventRepository = EventRepository(personaManager)
        savedThemeRepository = SavedThemeRepository(personaManager)
        backupRepository = BackupRepository(
            personas = personaManager,
            themePreferences = themePreferences,
        )
        autoBackupRepository = AutoBackupRepository(
            context = this,
            backupRepository = backupRepository,
            appPreferences = appPreferences,
            personaManager = personaManager,
        )
    }
}
