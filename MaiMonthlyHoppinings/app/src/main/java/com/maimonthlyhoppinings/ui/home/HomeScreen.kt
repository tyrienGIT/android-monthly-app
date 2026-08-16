package com.maimonthlyhoppinings.ui.home

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EventWithEntries
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.data.entryCountLabel
import com.maimonthlyhoppinings.data.latestEntry
import com.maimonthlyhoppinings.data.shortDateLabel
import com.maimonthlyhoppinings.data.shortDateRangeLabel
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog
import com.maimonthlyhoppinings.ui.theme.colorForEventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
    onStartEvent: () -> Unit,
    onOpenEvent: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<EventWithEntries?>(null) }

    pendingDelete?.let { event ->
        ConfirmDeleteDialog(
            eventTitle = event.event.displayTitle(),
            onConfirm = {
                viewModel.deleteEvent(event.event.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mai Monthly Hoppinings") },
                actions = {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Open calendar",
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartEvent,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Start event",
                )
            }
        },
    ) { innerPadding ->
        if (state.events.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No events yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Start an event, then add dated entries under it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        text = "Events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(state.events, key = { it.event.id }) { event ->
                    HomeEventRow(
                        event = event,
                        onClick = { onOpenEvent(event.event.id) },
                        onDelete = { pendingDelete = event },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEventRow(
    event: EventWithEntries,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val typeColor = colorForEventType(event.event.eventType)
    val latest = event.latestEntry()

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
                    Text(
                        text = event.event.displayTitle(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = event.event.eventType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = typeColor,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text = buildString {
                            append(event.event.shortDateRangeLabel())
                            append(" · ")
                            append(event.entryCountLabel())
                            if (latest != null) {
                                append(" · Latest ")
                                append(latest.shortDateLabel())
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete event",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
