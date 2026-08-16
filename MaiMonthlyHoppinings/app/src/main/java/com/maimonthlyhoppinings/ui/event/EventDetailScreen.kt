package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EventEntry
import com.maimonthlyhoppinings.data.TrackedEvent
import com.maimonthlyhoppinings.data.dateLabel
import com.maimonthlyhoppinings.data.dateRangeLabel
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.data.startTimeLabel
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog
import com.maimonthlyhoppinings.ui.theme.colorForEventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit,
    onEditEvent: () -> Unit,
    onAddEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteEntry by remember { mutableStateOf<EventEntry?>(null) }
    var pendingDeleteEvent by remember { mutableStateOf(false) }

    val event = state.eventWithEntries?.event
    val typeColor = colorForEventType(event?.eventType ?: "")

    pendingDeleteEntry?.let { entry ->
        val parent = state.eventWithEntries?.event
        ConfirmDeleteDialog(
            eventTitle = parent?.let { entry.displayTitle(it) } ?: entry.dateLabel(),
            entityLabel = "entry",
            onConfirm = {
                viewModel.deleteEntry(entry.id)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null },
        )
    }

    if (pendingDeleteEvent && event != null) {
        ConfirmDeleteDialog(
            eventTitle = event.displayTitle(),
            entityLabel = "event",
            onConfirm = {
                pendingDeleteEvent = false
                viewModel.deleteEvent(onDeleted = onBack)
            },
            onDismiss = { pendingDeleteEvent = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.displayTitle() ?: "Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (event != null) {
                        IconButton(onClick = onEditEvent) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit event",
                            )
                        }
                        IconButton(onClick = { pendingDeleteEvent = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete event",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (event != null) {
                FloatingActionButton(
                    onClick = onAddEntry,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add entry",
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.notFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Event not found")
                }
            }
            event == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Loading…")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                            text = event.eventType,
                            style = MaterialTheme.typography.titleMedium,
                            color = typeColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = event.dateRangeLabel(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (event.details.isNotBlank()) {
                            Text(
                                text = event.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    if (state.heatmapDays.isNotEmpty()) {
                        item {
                            EventIntensityHeatmap(
                                days = state.heatmapDays,
                                typeColor = typeColor,
                                startLabel = state.heatmapStartLabel,
                                endLabel = state.heatmapEndLabel,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    item {
                        Text(
                            text = if (state.sortedEntries.isEmpty()) {
                                "No entries yet"
                            } else {
                                "Entries"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        if (state.sortedEntries.isEmpty()) {
                            Text(
                                text = "Add a single-day entry with optional time and intensity.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    items(state.sortedEntries, key = { it.id }) { entry ->
                        EntryRow(
                            entry = entry,
                            parentEvent = event,
                            typeColor = typeColor,
                            onClick = { onOpenEntry(entry.id) },
                            onDelete = { pendingDeleteEntry = entry },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(
    entry: EventEntry,
    parentEvent: TrackedEvent,
    typeColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val timeLabel = entry.startTimeLabel()
    val hasCustomTitle = entry.title.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(typeColor),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (hasCustomTitle) {
                        Text(
                            text = entry.displayTitle(parentEvent),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = entry.dateLabel(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    } else {
                        Text(
                            text = entry.dateLabel(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = buildString {
                            if (timeLabel != null) {
                                append(timeLabel)
                                append(" · ")
                            }
                            append("Intensity ${entry.intensity}/10")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    if (entry.details.isNotBlank()) {
                        Text(
                            text = entry.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
