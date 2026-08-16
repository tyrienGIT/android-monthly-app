package com.maimonthlyhoppinings.data

import androidx.room.migration.Migration

/**
 * Ordered Room migrations. Version 10 is the committed baseline
 * (`app/schemas/.../10.json`). There is no path back through 1–9.
 *
 * For every schema change: bump [AppDatabase] version, add a
 * `Migration(from, to)` here, and commit the new schema JSON.
 * Do not add `fallbackToDestructiveMigration()`.
 */
object AppDatabaseMigrations {
    val all: Array<Migration> = emptyArray()
}
