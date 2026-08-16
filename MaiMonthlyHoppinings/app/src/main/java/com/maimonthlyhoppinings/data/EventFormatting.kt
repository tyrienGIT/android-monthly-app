package com.maimonthlyhoppinings.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

fun EventEntry.startTimeLabel(): String? {
    val minutes = startTimeMinutesOfDay ?: return null
    return LocalTime.of(minutes / 60, minutes % 60).format(timeFormatter)
}

fun EventEntry.dateLabel(): String {
    return LocalDate.ofEpochDay(dateEpochDay).format(dateFormatter)
}

fun EventEntry.shortDateLabel(): String {
    return LocalDate.ofEpochDay(dateEpochDay).format(shortDateFormatter)
}

fun TrackedEvent.dateRangeLabel(): String {
    val start = LocalDate.ofEpochDay(startDateEpochDay)
    val end = LocalDate.ofEpochDay(endDateEpochDay)
    return if (start == end) {
        start.format(dateFormatter)
    } else {
        "${start.format(dateFormatter)} – ${end.format(dateFormatter)}"
    }
}

fun TrackedEvent.shortDateRangeLabel(): String {
    val start = LocalDate.ofEpochDay(startDateEpochDay)
    val end = LocalDate.ofEpochDay(endDateEpochDay)
    return if (start == end) {
        start.format(shortDateFormatter)
    } else {
        "${start.format(shortDateFormatter)} – ${end.format(shortDateFormatter)}"
    }
}

fun TrackedEvent.displayTitle(): String {
    return title.trim().ifEmpty { eventType }
}

/** Entry title if set; otherwise falls back to the parent event title. */
fun EventEntry.displayTitle(parent: TrackedEvent): String {
    return title.trim().ifEmpty { parent.displayTitle() }
}

fun EventWithEntries.entryCountLabel(): String {
    val count = entries.size
    return if (count == 1) "1 entry" else "$count entries"
}

fun EventWithEntries.latestEntry(): EventEntry? {
    return entries.maxWithOrNull(
        compareBy<EventEntry> { it.dateEpochDay }
            .thenBy { it.startTimeMinutesOfDay ?: -1 }
            .thenBy { it.createdAtMillis },
    )
}
