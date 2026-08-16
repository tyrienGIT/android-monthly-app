package com.maimonthlyhoppinings.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class IntensityHeatDay(
    val epochDay: Long,
    /** Peak intensity from entries on this day, or null if none. */
    val intensity: Int?,
    val entryCount: Int,
)

private val heatDayLabelFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

fun TrackedEvent.intensityHeatmap(entries: List<EventEntry>): List<IntensityHeatDay> {
    val start = startDateEpochDay
    val end = endDateEpochDay
    if (end < start) return emptyList()

    val byDay = entries.groupBy { it.dateEpochDay }
    return (start..end).map { day ->
        val dayEntries = byDay[day].orEmpty()
        IntensityHeatDay(
            epochDay = day,
            intensity = dayEntries.maxOfOrNull { it.intensity.coerceIn(1, 10) },
            entryCount = dayEntries.size,
        )
    }
}

fun IntensityHeatDay.shortLabel(): String {
    return LocalDate.ofEpochDay(epochDay).format(heatDayLabelFormatter)
}
