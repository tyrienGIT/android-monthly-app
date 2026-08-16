package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class DateRangePickTarget {
    /** Clear selection and pick a new start, then end. */
    Start,
    /** Keep the current start; next tap sets / adjusts the end. */
    End,
}

private val rangePickerDayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d")

private const val UtcDayMillis = 24L * 60L * 60L * 1000L

@Composable
fun DateRangeField(
    startLabel: String,
    endLabel: String,
    typeColor: Color,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Dates",
            style = MaterialTheme.typography.bodySmall,
            color = typeColor.copy(alpha = 0.85f),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Text(
            text = "Tap Start to pick a new range · tap End to adjust the end date",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .border(BorderStroke(1.dp, typeColor), shape)
                .clip(shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateRangeSegment(
                caption = "Start",
                value = startLabel,
                typeColor = typeColor,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onStartClick),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            DateRangeSegment(
                caption = "End",
                value = endLabel,
                typeColor = typeColor,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEndClick),
            )
        }
    }
}

@Composable
private fun DateRangeSegment(
    caption: String,
    value: String,
    typeColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            color = typeColor.copy(alpha = 0.8f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = typeColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDateRangePickerDialog(
    target: DateRangePickTarget,
    initialStartEpochDay: Long,
    initialEndEpochDay: Long,
    onConfirm: (startEpochDay: Long, endEpochDay: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialStartMillis = initialStartEpochDay * UtcDayMillis
    val initialEndMillis = initialEndEpochDay * UtcDayMillis

    val rangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = when (target) {
            DateRangePickTarget.Start -> null
            DateRangePickTarget.End -> initialStartMillis
        },
        initialSelectedEndDateMillis = null,
        initialDisplayedMonthMillis = when (target) {
            DateRangePickTarget.Start -> initialStartMillis
            DateRangePickTarget.End -> initialEndMillis
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = rangeState.selectedStartDateMillis ?: return@TextButton
                    val endMillis = rangeState.selectedEndDateMillis
                        ?: if (target == DateRangePickTarget.End) initialEndMillis else startMillis
                    onConfirm(utcMillisToEpochDay(startMillis), utcMillisToEpochDay(endMillis))
                },
                enabled = rangeState.selectedStartDateMillis != null,
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DateRangePicker(
            state = rangeState,
            title = {
                Text(
                    text = when (target) {
                        DateRangePickTarget.Start -> "Pick start, then end"
                        DateRangePickTarget.End -> "Adjust end date"
                    },
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            headline = {
                DateRangePickerStatusHeadline(
                    startMillis = rangeState.selectedStartDateMillis,
                    endMillis = rangeState.selectedEndDateMillis,
                    target = target,
                    fallbackStartMillis = initialStartMillis,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                )
            },
            showModeToggle = false,
            modifier = Modifier.height(500.dp),
        )
    }
}

@Composable
private fun DateRangePickerStatusHeadline(
    startMillis: Long?,
    endMillis: Long?,
    target: DateRangePickTarget,
    fallbackStartMillis: Long,
    modifier: Modifier = Modifier,
) {
    val startText = formatPickerDay(startMillis)
        ?: if (target == DateRangePickTarget.End) {
            formatPickerDay(fallbackStartMillis).orEmpty()
        } else {
            "Tap a date"
        }
    val endText = formatPickerDay(endMillis) ?: "Tap a date"
    val waitingForStart = startMillis == null
    val waitingForEnd = startMillis != null && endMillis == null

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RangeHeadlineChip(
            label = "Start",
            value = startText,
            emphasized = waitingForStart,
            modifier = Modifier.weight(1f),
        )
        Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
        RangeHeadlineChip(
            label = "End",
            value = endText,
            emphasized = waitingForEnd,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RangeHeadlineChip(
    label: String,
    value: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (emphasized) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private fun formatPickerDay(utcMillis: Long?): String? {
    if (utcMillis == null) return null
    return Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(rangePickerDayFormatter)
}

private fun utcMillisToEpochDay(utcMillis: Long): Long {
    return Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDatePickerDialog(
    initialEpochDay: Long,
    onConfirm: (epochDay: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochDay * UtcDayMillis,
        initialDisplayedMonthMillis = initialEpochDay * UtcDayMillis,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    onConfirm(utcMillisToEpochDay(millis))
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(state = datePickerState, showModeToggle = false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("No time")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}
