package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrackedEvent::class, EventEntry::class, SavedColorTheme::class],
    version = 10,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedEventDao(): TrackedEventDao
    abstract fun eventEntryDao(): EventEntryDao
    abstract fun savedColorThemeDao(): SavedColorThemeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mai_monthly_hoppinings.db",
                )
                    .addMigrations(*AppDatabaseMigrations.all)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
