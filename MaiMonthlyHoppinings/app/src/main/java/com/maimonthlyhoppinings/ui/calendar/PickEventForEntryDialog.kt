package com.maimonthlyhoppinings.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.TrackedEvent
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PickEventForEntryDialog(
    date: LocalDate,
    events: List<TrackedEvent>,
    types: EventTypeLookup,
    onStartNewEvent: () -> Unit,
    onPickEvent: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val dateLabel = date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dateLabel) },
        text = {
            Column {
                Text(
                    text = "Add an entry under an event, or start a new event.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                LazyColumn {
                    items(events, key = { it.id }) { event ->
                        PickEventRow(
                            event = event,
                            types = types,
                            onClick = { onPickEvent(event.id) },
                        )
                        HorizontalDivider()
                    }
                    item {
                        Text(
                            text = "Start new event",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onStartNewEvent)
                                .padding(vertical = 14.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun PickEventRow(
    event: TrackedEvent,
    types: EventTypeLookup,
    onClick: () -> Unit,
) {
    val typeColor = colorForEventType(event.eventTypeId, types)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(typeColor),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.displayTitle(types),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = types.label(event.eventTypeId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
