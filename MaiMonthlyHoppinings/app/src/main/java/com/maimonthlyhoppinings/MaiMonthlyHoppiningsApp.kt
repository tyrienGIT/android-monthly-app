package com.maimonthlyhoppinings

import android.app.Application
import com.maimonthlyhoppinings.data.AppPreferences
import com.maimonthlyhoppinings.data.AutoBackupRepository
import com.maimonthlyhoppinings.data.BackupRepository
import com.maimonthlyhoppinings.data.BookManager
import com.maimonthlyhoppinings.data.BookPreferences
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.SavedThemeRepository
import com.maimonthlyhoppinings.data.ThemePreferences
import kotlinx.coroutines.runBlocking

open class MaiMonthlyHoppiningsApp : Application() {
    lateinit var bookManager: BookManager
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
        bookManager = BookManager(this, BookPreferences(this))
        runBlocking {
            themePreferences.applyStoredNightMode()
            bookManager.start()
        }
        eventRepository = EventRepository(bookManager)
        savedThemeRepository = SavedThemeRepository(bookManager)
        backupRepository = BackupRepository(
            books = bookManager,
            themePreferences = themePreferences,
        )
        autoBackupRepository = AutoBackupRepository(
            context = this,
            backupRepository = backupRepository,
            appPreferences = appPreferences,
            bookManager = bookManager,
        )
    }
}
