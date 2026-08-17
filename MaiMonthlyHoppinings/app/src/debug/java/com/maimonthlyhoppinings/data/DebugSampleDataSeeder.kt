package com.maimonthlyhoppinings.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

/**
 * Debug-only sample history so the calendar and day lists look lived-in.
 * Compiled into debug builds only. Runs once per install; never deletes existing events.
 */
object DebugSampleDataSeeder {
    private const val PREFS = "debug_sample_data"
    private const val KEY_SEEDED = "seeded_v1"

    suspend fun seedOnce(context: Context, events: EventRepository) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        seed(events)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private suspend fun seed(events: EventRepository) {
        val today = LocalDate.now()
        val currentPeriodStart = today.minusDays(2)

        seedCycle(
            events = events,
            periodStart = currentPeriodStart,
            periodIntensities = listOf(5, 7, 8, 6, 3),
            crampIntensities = listOf(6, 8, 5),
            anxiousIntensities = listOf(5, 6),
            periodNotes = listOf(
                "Started mid-morning",
                "Heavier, needed a backup",
                "Peak day, moved slowly",
                "Easing off",
                "Mostly spotting",
            ),
        )
        seedCycle(
            events = events,
            periodStart = currentPeriodStart.minusDays(28),
            periodIntensities = listOf(4, 8, 9, 7, 4),
            crampIntensities = listOf(7, 9, 6),
            anxiousIntensities = listOf(6, 7),
            periodNotes = listOf(
                "Light start after lunch",
                "Woke up already heavy",
                "Stayed on the couch",
                "Better by evening",
                "Almost done",
            ),
        )
        seedCycle(
            events = events,
            periodStart = currentPeriodStart.minusDays(55),
            periodIntensities = listOf(3, 6, 8, 5, 2),
            crampIntensities = listOf(5, 8, 4),
            anxiousIntensities = listOf(4, 5),
            periodNotes = listOf(
                "Barely there at first",
                "Caught up overnight",
                "Hot water bottle day",
                "Manageable",
                "Finished earlier than usual",
            ),
        )
        seedCycle(
            events = events,
            periodStart = currentPeriodStart.minusDays(83),
            periodIntensities = listOf(5, 7, 7, 6, 4),
            crampIntensities = listOf(6, 7, 5),
            anxiousIntensities = listOf(5),
            periodNotes = listOf(
                "On time this month",
                "Normal flow",
                "A bit tired",
                "Back to routine",
                "Wrapping up",
            ),
        )

        events.addDays(
            typeId = "type_3",
            title = "Happy",
            details = "Easy, bright day",
            time = LocalTime.of(14, 30),
            days = listOf(
                currentPeriodStart.minusDays(16) to 7,
                currentPeriodStart.minusDays(15) to 6,
                currentPeriodStart.minusDays(44) to 8,
                currentPeriodStart.minusDays(72) to 6,
                today.minusDays(9) to 7,
            ),
        )
        events.addDays(
            typeId = "type_4",
            title = "Sad",
            details = "Low energy, wanted quiet",
            time = LocalTime.of(19, 15),
            days = listOf(
                currentPeriodStart.minusDays(10) to 4,
                currentPeriodStart.minusDays(38) to 5,
                today.minusDays(21) to 3,
            ),
        )
    }

    private suspend fun seedCycle(
        events: EventRepository,
        periodStart: LocalDate,
        periodIntensities: List<Int>,
        crampIntensities: List<Int>,
        anxiousIntensities: List<Int>,
        periodNotes: List<String>,
    ) {
        val periodDays = periodIntensities.indices.map { offset ->
            Triple(
                periodStart.plusDays(offset.toLong()),
                periodIntensities[offset],
                periodNotes.getOrElse(offset) { "" },
            )
        }
        events.addSpan(
            typeId = "type_1",
            title = "Period",
            time = LocalTime.of(8, 0),
            days = periodDays,
        )

        val crampStart = periodStart.plusDays(1)
        events.addSpan(
            typeId = "type_5",
            title = "Cramps",
            time = LocalTime.of(11, 30),
            days = crampIntensities.mapIndexed { offset, intensity ->
                Triple(
                    crampStart.plusDays(offset.toLong()),
                    intensity,
                    if (intensity >= 8) "Needed to lie down" else "Dull ache",
                )
            },
        )

        val anxiousStart = periodStart.minusDays(anxiousIntensities.size.toLong())
        events.addSpan(
            typeId = "type_2",
            title = "Anxious",
            time = LocalTime.of(21, 0),
            days = anxiousIntensities.mapIndexed { offset, intensity ->
                Triple(
                    anxiousStart.plusDays(offset.toLong()),
                    intensity,
                    "Restless before it started",
                )
            },
        )
    }

    private suspend fun EventRepository.addSpan(
        typeId: String,
        title: String,
        time: LocalTime,
        days: List<Triple<LocalDate, Int, String>>,
    ) {
        if (days.isEmpty()) return
        val ordered = days.sortedBy { it.first }
        val eventId = startEvent(
            EventInput(
                title = title,
                eventTypeId = typeId,
                startDate = ordered.first().first,
                endDate = ordered.last().first,
            ),
        )
        ordered.forEach { (date, intensity, details) ->
            addEntry(
                EntryInput(
                    eventId = eventId,
                    date = date,
                    startTime = time,
                    details = details,
                    intensity = intensity,
                ),
            )
        }
    }

    private suspend fun EventRepository.addDays(
        typeId: String,
        title: String,
        details: String,
        time: LocalTime,
        days: List<Pair<LocalDate, Int>>,
    ) {
        days.forEach { (date, intensity) ->
            addSpan(
                typeId = typeId,
                title = title,
                time = time,
                days = listOf(Triple(date, intensity, details)),
            )
        }
    }
}
