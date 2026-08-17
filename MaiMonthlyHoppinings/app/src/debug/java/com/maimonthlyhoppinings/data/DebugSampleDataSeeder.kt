package com.maimonthlyhoppinings.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

/**
 * Debug-only sample history so the calendar and day lists look lived-in.
 * Compiled into debug builds only. Replaces prior sample rows when the seed
 * version changes; does not run in release.
 */
object DebugSampleDataSeeder {
    private const val PREFS = "debug_sample_data"
    private const val KEY_SEEDED = "seeded_v2"

    suspend fun seedOnce(context: Context, events: EventRepository) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        events.deleteAllEvents()
        seed(events)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private suspend fun seed(events: EventRepository) {
        val today = LocalDate.now()
        val currentPeriodStart = today.minusDays(2)

        seedCycle(
            events = events,
            periodStart = currentPeriodStart,
            periodTitle = "Came early this time",
            periodDetails = "Started on a workday, packing extras",
            periodIntensities = listOf(5, 7, 8, 6, 3),
            periodNotes = listOf(
                "Caught me at the office",
                "Heavier after lunch, needed a backup",
                "Peak day, cancelled a walk",
                "Easing off, still tired",
                "Mostly spotting",
            ),
            crampTitle = "Heating pad evening",
            crampIntensities = listOf(6, 8, 5),
            crampNotes = listOf(
                "Low ache while commuting",
                "Had to lie down after dinner",
                "Dull, tea helped",
            ),
            anxiousTitle = "Sunday dread",
            anxiousIntensities = listOf(5, 6),
            anxiousNotes = listOf(
                "Restless, kept checking the calendar",
                "Couldn't settle before bed",
            ),
        )
        seedCycle(
            events = events,
            periodStart = currentPeriodStart.minusDays(28),
            periodTitle = "Heavy week",
            periodDetails = "Worse than usual, stayed close to home",
            periodIntensities = listOf(4, 8, 9, 7, 4),
            periodNotes = listOf(
                "Light start after lunch",
                "Woke up already heavy",
                "Stayed on the couch",
                "Better by evening",
                "Almost done",
            ),
            crampTitle = "Bad cramps",
            crampIntensities = listOf(7, 9, 6),
            crampNotes = listOf(
                "Ibuprofen at 11",
                "Could not stand long",
                "Fading, still tender",
            ),
            anxiousTitle = "PMS spiral",
            anxiousIntensities = listOf(6, 7),
            anxiousNotes = listOf(
                "Snapped at a small thing",
                "Mind racing after 9",
            ),
        )
        seedCycle(
            events = events,
            periodStart = currentPeriodStart.minusDays(55),
            periodTitle = "Light one",
            periodDetails = "Shorter and quieter than last month",
            periodIntensities = listOf(3, 6, 8, 5, 2),
            periodNotes = listOf(
                "Barely there at first",
                "Caught up overnight",
                "Hot water bottle day",
                "Manageable",
                "Finished earlier than usual",
            ),
            crampTitle = "Twinges",
            crampIntensities = listOf(5, 4),
            crampNotes = listOf(
                "On and off in the afternoon",
                "Mostly gone by night",
            ),
            anxiousTitle = "Jumpy before it started",
            anxiousIntensities = listOf(4),
            anxiousNotes = listOf("A bit on edge, nothing specific"),
        )
        seedCycle(
            events = events,
            periodStart = currentPeriodStart.minusDays(83),
            periodTitle = "On time",
            periodDetails = "Normal week, nothing dramatic",
            periodIntensities = listOf(5, 7, 7, 6, 4),
            periodNotes = listOf(
                "Right on schedule",
                "Normal flow",
                "A bit tired",
                "Back to routine",
                "Wrapping up",
            ),
            crampTitle = "Cramps after lunch",
            crampIntensities = listOf(6, 7, 5),
            crampNotes = listOf(
                "Walked it off",
                "Needed to sit",
                "Mild by evening",
            ),
            anxiousTitle = "Can't sleep",
            anxiousIntensities = listOf(5),
            anxiousNotes = listOf("Up too late scrolling"),
        )

        events.addNamedDay("type_3", "Beach afternoon", 8, currentPeriodStart.minusDays(44), LocalTime.of(15, 10), "Sun, salt, no plans")
        events.addNamedDay("type_3", "Dinner with friends", 7, currentPeriodStart.minusDays(16), LocalTime.of(19, 40), "Laughed until it hurt")
        events.addNamedDay("type_3", "Good energy", 6, currentPeriodStart.minusDays(15), LocalTime.of(11, 5), "Got through the to-do list")
        events.addNamedDay("type_3", "Slow Saturday", 6, currentPeriodStart.minusDays(72), LocalTime.of(10, 20), "Coffee and a long walk")
        events.addNamedDay("type_3", "Tiny win at work", 7, today.minusDays(9), LocalTime.of(16, 45), "A thing finally shipped")

        events.addNamedDay("type_4", "Grey day", 4, currentPeriodStart.minusDays(10), LocalTime.of(20, 5), "Wanted quiet, skipped plans")
        events.addNamedDay("type_4", "Needed a cry", 5, currentPeriodStart.minusDays(38), LocalTime.of(21, 30), "Better after, still wrung out")
        events.addNamedDay("type_4", "Low and foggy", 3, today.minusDays(21), LocalTime.of(18, 15), "Hard to start anything")
    }

    private suspend fun seedCycle(
        events: EventRepository,
        periodStart: LocalDate,
        periodTitle: String,
        periodDetails: String,
        periodIntensities: List<Int>,
        periodNotes: List<String>,
        crampTitle: String,
        crampIntensities: List<Int>,
        crampNotes: List<String>,
        anxiousTitle: String,
        anxiousIntensities: List<Int>,
        anxiousNotes: List<String>,
    ) {
        events.addSpan(
            typeId = "type_1",
            title = periodTitle,
            details = periodDetails,
            time = LocalTime.of(8, 0),
            days = periodIntensities.indices.map { offset ->
                Triple(
                    periodStart.plusDays(offset.toLong()),
                    periodIntensities[offset],
                    periodNotes.getOrElse(offset) { "" },
                )
            },
        )

        if (crampIntensities.isNotEmpty()) {
            val crampStart = periodStart.plusDays(1)
            events.addSpan(
                typeId = "type_5",
                title = crampTitle,
                time = LocalTime.of(11, 30),
                days = crampIntensities.mapIndexed { offset, intensity ->
                    Triple(
                        crampStart.plusDays(offset.toLong()),
                        intensity,
                        crampNotes.getOrElse(offset) { "" },
                    )
                },
            )
        }

        if (anxiousIntensities.isNotEmpty()) {
            val anxiousStart = periodStart.minusDays(anxiousIntensities.size.toLong())
            events.addSpan(
                typeId = "type_2",
                title = anxiousTitle,
                time = LocalTime.of(21, 0),
                days = anxiousIntensities.mapIndexed { offset, intensity ->
                    Triple(
                        anxiousStart.plusDays(offset.toLong()),
                        intensity,
                        anxiousNotes.getOrElse(offset) { "" },
                    )
                },
            )
        }
    }

    private suspend fun EventRepository.addSpan(
        typeId: String,
        title: String,
        details: String = "",
        time: LocalTime,
        days: List<Triple<LocalDate, Int, String>>,
    ) {
        if (days.isEmpty()) return
        val ordered = days.sortedBy { it.first }
        val eventId = startEvent(
            EventInput(
                title = title,
                eventTypeId = typeId,
                details = details,
                startDate = ordered.first().first,
                endDate = ordered.last().first,
            ),
        )
        ordered.forEach { (date, intensity, entryDetails) ->
            addEntry(
                EntryInput(
                    eventId = eventId,
                    date = date,
                    startTime = time,
                    details = entryDetails,
                    intensity = intensity,
                ),
            )
        }
    }

    private suspend fun EventRepository.addNamedDay(
        typeId: String,
        title: String,
        intensity: Int,
        date: LocalDate,
        time: LocalTime,
        details: String,
    ) {
        addSpan(
            typeId = typeId,
            title = title,
            time = time,
            days = listOf(Triple(date, intensity, details)),
        )
    }
}
