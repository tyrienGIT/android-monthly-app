package com.maimonthlyhoppinings.data

/**
 * One calendar heat layer for a parent event on a given day.
 * The event paints continuously across its start–end span; [intensityStops]
 * are chronological sub-entry intensities used to draw that band as a gradient.
 */
data class DayHeatSegment(
    val eventId: Long,
    val color: EventTypeColor,
    /**
     * Chronological intensity samples (1..10) from this event's entries.
     * At least one value; used as gradient stops for the continuous event band.
     */
    val intensityStops: List<Int>,
    /** 0f at event start date … 1f at event end date (for sampling along the span). */
    val spanProgress: Float,
)
