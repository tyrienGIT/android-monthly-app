package com.maimonthlyhoppinings.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

/**
 * Debug-only sample history. Each row is one parent event spanning several days,
 * with one entry per day under it — not a separate event per day.
 */
object DebugSampleDataSeeder {
    private const val PREFS = "debug_sample_data"
    private const val KEY_SEEDED = "seeded_v3"

    private data class SeedEntry(
        val date: LocalDate,
        val intensity: Int,
        val details: String = "",
        val title: String = "",
        val time: LocalTime? = null,
    )

    suspend fun seedOnce(context: Context, events: EventRepository) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        events.deleteAllEvents()
        seed(events)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private suspend fun seed(events: EventRepository) {
        val today = LocalDate.now()
        val period0 = today.minusDays(2)
        val period1 = period0.minusDays(28)
        val period2 = period0.minusDays(55)
        val period3 = period0.minusDays(83)

        events.addEvent(
            typeId = "type_1",
            title = "Came early this time",
            details = "Started on a workday, packing extras",
            entries = listOf(
                SeedEntry(period0, 5, "Caught me at the office", time = LocalTime.of(8, 15)),
                SeedEntry(period0.plusDays(1), 7, "Heavier after lunch, needed a backup", time = LocalTime.of(7, 40)),
                SeedEntry(period0.plusDays(2), 8, "Peak day, cancelled a walk", time = LocalTime.of(8, 5)),
                SeedEntry(period0.plusDays(3), 6, "Easing off, still tired", time = LocalTime.of(9, 10)),
                SeedEntry(period0.plusDays(4), 3, "Mostly spotting", time = LocalTime.of(10, 0)),
            ),
        )
        events.addEvent(
            typeId = "type_5",
            title = "Heating pad evenings",
            details = "Sat on the heavier days of this cycle",
            entries = listOf(
                SeedEntry(period0.plusDays(1), 6, "Low ache while commuting", time = LocalTime.of(11, 20)),
                SeedEntry(period0.plusDays(2), 8, "Had to lie down after dinner", time = LocalTime.of(18, 45)),
                SeedEntry(period0.plusDays(3), 5, "Dull, tea helped", time = LocalTime.of(16, 10)),
            ),
        )
        events.addEvent(
            typeId = "type_2",
            title = "Sunday dread",
            details = "The two nights before it started",
            entries = listOf(
                SeedEntry(period0.minusDays(2), 5, "Restless, kept checking the calendar", time = LocalTime.of(21, 10)),
                SeedEntry(period0.minusDays(1), 6, "Couldn't settle before bed", time = LocalTime.of(22, 5)),
            ),
        )

        events.addEvent(
            typeId = "type_1",
            title = "Heavy week",
            details = "Worse than usual, stayed close to home",
            entries = listOf(
                SeedEntry(period1, 4, "Light start after lunch", time = LocalTime.of(13, 20)),
                SeedEntry(period1.plusDays(1), 8, "Woke up already heavy", time = LocalTime.of(7, 15)),
                SeedEntry(period1.plusDays(2), 9, "Stayed on the couch", time = LocalTime.of(8, 0)),
                SeedEntry(period1.plusDays(3), 7, "Better by evening", time = LocalTime.of(19, 30)),
                SeedEntry(period1.plusDays(4), 4, "Almost done", time = LocalTime.of(10, 45)),
            ),
        )
        events.addEvent(
            typeId = "type_5",
            title = "Bad cramps",
            details = "Needed painkillers the middle day",
            entries = listOf(
                SeedEntry(period1.plusDays(1), 7, "Ibuprofen at 11", time = LocalTime.of(11, 5)),
                SeedEntry(period1.plusDays(2), 9, "Could not stand long", time = LocalTime.of(14, 20)),
                SeedEntry(period1.plusDays(3), 6, "Fading, still tender", time = LocalTime.of(17, 0)),
            ),
        )
        events.addEvent(
            typeId = "type_2",
            title = "PMS spiral",
            details = "Edgy the weekend before",
            entries = listOf(
                SeedEntry(period1.minusDays(2), 6, "Snapped at a small thing", time = LocalTime.of(20, 40)),
                SeedEntry(period1.minusDays(1), 7, "Mind racing after 9", time = LocalTime.of(21, 25)),
            ),
        )

        events.addEvent(
            typeId = "type_1",
            title = "Light one",
            details = "Shorter and quieter than last month",
            entries = listOf(
                SeedEntry(period2, 3, "Barely there at first", time = LocalTime.of(16, 0)),
                SeedEntry(period2.plusDays(1), 6, "Caught up overnight", time = LocalTime.of(8, 30)),
                SeedEntry(period2.plusDays(2), 8, "Hot water bottle day", time = LocalTime.of(12, 15)),
                SeedEntry(period2.plusDays(3), 5, "Manageable", time = LocalTime.of(9, 50)),
                SeedEntry(period2.plusDays(4), 2, "Finished earlier than usual", time = LocalTime.of(11, 10)),
            ),
        )
        events.addEvent(
            typeId = "type_5",
            title = "Twinges",
            details = "Mild, two days only",
            entries = listOf(
                SeedEntry(period2.plusDays(1), 5, "On and off in the afternoon", time = LocalTime.of(15, 10)),
                SeedEntry(period2.plusDays(2), 4, "Mostly gone by night", time = LocalTime.of(20, 0)),
            ),
        )
        events.addEvent(
            typeId = "type_2",
            title = "Jumpy before it started",
            details = "A short stretch of unease",
            entries = listOf(
                SeedEntry(period2.minusDays(2), 4, "A bit on edge, nothing specific", time = LocalTime.of(21, 0)),
                SeedEntry(period2.minusDays(1), 5, "Slept lightly", time = LocalTime.of(23, 10)),
            ),
        )

        events.addEvent(
            typeId = "type_1",
            title = "On time",
            details = "Normal week, nothing dramatic",
            entries = listOf(
                SeedEntry(period3, 5, "Right on schedule", time = LocalTime.of(8, 0)),
                SeedEntry(period3.plusDays(1), 7, "Normal flow", time = LocalTime.of(8, 20)),
                SeedEntry(period3.plusDays(2), 7, "A bit tired", time = LocalTime.of(9, 0)),
                SeedEntry(period3.plusDays(3), 6, "Back to routine", time = LocalTime.of(8, 45)),
                SeedEntry(period3.plusDays(4), 4, "Wrapping up", time = LocalTime.of(10, 30)),
            ),
        )
        events.addEvent(
            typeId = "type_5",
            title = "Cramps after lunch",
            details = "Showed up with the heavier days",
            entries = listOf(
                SeedEntry(period3.plusDays(1), 6, "Walked it off", time = LocalTime.of(13, 10)),
                SeedEntry(period3.plusDays(2), 7, "Needed to sit", time = LocalTime.of(14, 0)),
                SeedEntry(period3.plusDays(3), 5, "Mild by evening", time = LocalTime.of(18, 20)),
            ),
        )
        events.addEvent(
            typeId = "type_2",
            title = "Can't sleep",
            details = "Two late nights in a row",
            entries = listOf(
                SeedEntry(period3.minusDays(2), 5, "Up too late scrolling", time = LocalTime.of(23, 40)),
                SeedEntry(period3.minusDays(1), 6, "Woke at 4 and stayed up", time = LocalTime.of(4, 20)),
            ),
        )

        events.addEvent(
            typeId = "type_3",
            title = "Easy weekend",
            details = "Mid-cycle, nothing hurt",
            entries = listOf(
                SeedEntry(period0.minusDays(16), 7, "Laughed until it hurt", title = "Dinner with friends", time = LocalTime.of(19, 40)),
                SeedEntry(period0.minusDays(15), 6, "Got through the to-do list", title = "Good energy", time = LocalTime.of(11, 5)),
                SeedEntry(period0.minusDays(14), 7, "Long walk, no rush", title = "Slow Sunday", time = LocalTime.of(10, 20)),
            ),
        )
        events.addEvent(
            typeId = "type_3",
            title = "Bright stretch",
            details = "A few easy days after the light cycle",
            entries = listOf(
                SeedEntry(period2.plusDays(12), 8, "Sun, salt, no plans", title = "Beach afternoon", time = LocalTime.of(15, 10)),
                SeedEntry(period2.plusDays(13), 6, "Coffee and a long walk", title = "Slow Saturday", time = LocalTime.of(10, 20)),
                SeedEntry(period2.plusDays(14), 7, "A thing finally shipped", title = "Tiny win at work", time = LocalTime.of(16, 45)),
            ),
        )

        events.addEvent(
            typeId = "type_4",
            title = "Low week",
            details = "Wanted quiet more than usual",
            entries = listOf(
                SeedEntry(period0.minusDays(11), 3, "Hard to start anything", title = "Low and foggy", time = LocalTime.of(18, 15)),
                SeedEntry(period0.minusDays(10), 4, "Wanted quiet, skipped plans", title = "Grey day", time = LocalTime.of(20, 5)),
                SeedEntry(period0.minusDays(9), 5, "Better after, still wrung out", title = "Needed a cry", time = LocalTime.of(21, 30)),
            ),
        )
        events.addEvent(
            typeId = "type_4",
            title = "Grey couple of days",
            details = "After the heavy week",
            entries = listOf(
                SeedEntry(period1.plusDays(10), 4, "Moved slowly", title = "Flat morning", time = LocalTime.of(9, 30)),
                SeedEntry(period1.plusDays(11), 5, "Cancelled a call", title = "Needed a night in", time = LocalTime.of(19, 15)),
            ),
        )
    }

    private suspend fun EventRepository.addEvent(
        typeId: String,
        title: String,
        details: String = "",
        entries: List<SeedEntry>,
    ) {
        if (entries.isEmpty()) return
        val ordered = entries.sortedBy { it.date }
        val eventId = startEvent(
            EventInput(
                title = title,
                eventTypeId = typeId,
                details = details,
                startDate = ordered.first().date,
                endDate = ordered.last().date,
            ),
        )
        ordered.forEach { entry ->
            addEntry(
                EntryInput(
                    eventId = eventId,
                    title = entry.title,
                    date = entry.date,
                    startTime = entry.time,
                    details = entry.details,
                    intensity = entry.intensity,
                ),
            )
        }
    }
}
