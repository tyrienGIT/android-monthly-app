package com.maimonthlyhoppinings.ui.calendar

import com.maimonthlyhoppinings.data.DayHeatSegment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class CalendarDay(
    val date: LocalDate,
    val isToday: Boolean,
    val isWeekend: Boolean,
    val heatSegments: List<DayHeatSegment>,
) {
    val hasEvents: Boolean get() = heatSegments.isNotEmpty()
}

data class CalendarWeek(
    val weekStart: LocalDate,
    val days: List<CalendarDay>,
    val monthLabel: String?,
)

private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

fun mondayOfWeek(date: LocalDate = LocalDate.now()): LocalDate {
    return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

fun buildWeeks(
    startMonday: LocalDate,
    weekCount: Int,
    heatByDay: Map<Long, List<DayHeatSegment>>,
    today: LocalDate = LocalDate.now(),
): List<CalendarWeek> {
    require(startMonday.dayOfWeek == DayOfWeek.MONDAY)
    var previousMonthKey: String? = null

    return (0 until weekCount).map { weekIndex ->
        val weekStart = startMonday.plusWeeks(weekIndex.toLong())
        val days = (0 until 7).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val dayOfWeek = date.dayOfWeek
            CalendarDay(
                date = date,
                isToday = date == today,
                isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY,
                heatSegments = heatByDay[date.toEpochDay()].orEmpty(),
            )
        }
        val monthKey = weekStart.format(monthLabelFormatter)
        val monthLabel = if (monthKey != previousMonthKey) {
            previousMonthKey = monthKey
            monthKey
        } else {
            null
        }
        CalendarWeek(
            weekStart = weekStart,
            days = days,
            monthLabel = monthLabel,
        )
    }
}

fun weekdayLabelsMondayFirst(locale: Locale = Locale.getDefault()): List<String> {
    return listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    ).map { it.getDisplayName(TextStyle.SHORT, locale) }
}
