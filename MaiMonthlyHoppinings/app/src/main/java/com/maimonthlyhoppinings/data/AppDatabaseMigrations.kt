package com.maimonthlyhoppinings.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Ordered Room migrations. Version 10 is the committed baseline
 * (`app/schemas/.../10.json`). There is no path back through 1–9.
 *
 * For every schema change: bump [AppDatabase] version, add a
 * `Migration(from, to)` here, and commit the new schema JSON.
 * Do not add `fallbackToDestructiveMigration()`.
 */
object AppDatabaseMigrations {
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `event_types` (
                    `id` TEXT NOT NULL,
                    `label` TEXT NOT NULL,
                    `color` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            EventType.seeds.forEach { seed ->
                db.execSQL(
                    "INSERT OR IGNORE INTO `event_types` (`id`, `label`, `color`) VALUES (?, ?, ?)",
                    arrayOf(seed.id, seed.label, seed.color.name),
                )
            }
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tracked_events_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `eventTypeId` TEXT NOT NULL,
                    `details` TEXT NOT NULL,
                    `startDateEpochDay` INTEGER NOT NULL,
                    `endDateEpochDay` INTEGER NOT NULL,
                    `createdAtMillis` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO `tracked_events_new` (
                    `id`, `title`, `eventTypeId`, `details`,
                    `startDateEpochDay`, `endDateEpochDay`, `createdAtMillis`
                )
                SELECT
                    `id`,
                    `title`,
                    CASE `eventType`
                        WHEN 'Placeholder type 1' THEN 'type_1'
                        WHEN 'Placeholder type 2' THEN 'type_2'
                        WHEN 'Placeholder type 3' THEN 'type_3'
                        WHEN 'Placeholder type 4' THEN 'type_4'
                        WHEN 'Placeholder type 5' THEN 'type_5'
                        ELSE 'type_1'
                    END,
                    `details`,
                    `startDateEpochDay`,
                    `endDateEpochDay`,
                    `createdAtMillis`
                FROM `tracked_events`
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE `tracked_events`")
            db.execSQL("ALTER TABLE `tracked_events_new` RENAME TO `tracked_events`")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_tracked_events_eventTypeId` ON `tracked_events` (`eventTypeId`)",
            )
        }
    }

    val all: Array<Migration> = arrayOf(MIGRATION_10_11)
}
