package com.maimonthlyhoppinings.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_color_themes")
data class SavedColorTheme(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lightPrimaryArgb: Int,
    val lightSecondaryArgb: Int,
    val lightTertiaryArgb: Int,
    val darkPrimaryArgb: Int,
    val darkSecondaryArgb: Int,
    val darkTertiaryArgb: Int,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
