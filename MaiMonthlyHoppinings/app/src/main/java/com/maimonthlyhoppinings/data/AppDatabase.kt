package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TrackedEvent::class,
        EventEntry::class,
        SavedColorTheme::class,
        EventTypeEntity::class,
    ],
    version = 11,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedEventDao(): TrackedEventDao
    abstract fun eventEntryDao(): EventEntryDao
    abstract fun savedColorThemeDao(): SavedColorThemeDao
    abstract fun eventTypeDao(): EventTypeDao

    fun bumpAutoincrement(table: String, maxId: Long) {
        if (maxId <= 0L) return
        val db = openHelper.writableDatabase
        val cursor = db.query("SELECT seq FROM sqlite_sequence WHERE name = ?", arrayOf(table))
        cursor.use {
            if (it.moveToFirst()) {
                val current = it.getLong(0)
                if (maxId > current) {
                    db.execSQL(
                        "UPDATE sqlite_sequence SET seq = ? WHERE name = ?",
                        arrayOf(maxId, table),
                    )
                }
            } else {
                db.execSQL(
                    "INSERT INTO sqlite_sequence(name, seq) VALUES(?, ?)",
                    arrayOf(table, maxId),
                )
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val seedTypesCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                EventType.seeds.forEach { seed ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO `event_types` (`id`, `label`, `color`) VALUES (?, ?, ?)",
                        arrayOf(seed.id, seed.label, seed.color.name),
                    )
                }
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mai_monthly_hoppinings.db",
                )
                    .addMigrations(*AppDatabaseMigrations.all)
                    .addCallback(seedTypesCallback)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
