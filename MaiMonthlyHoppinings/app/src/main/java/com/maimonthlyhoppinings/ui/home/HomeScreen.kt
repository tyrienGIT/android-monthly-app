package com.maimonthlyhoppinings.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.EventWithEntries
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.data.entryCountLabel
import com.maimonthlyhoppinings.data.latestEntry
import com.maimonthlyhoppinings.data.shortDateLabel
import com.maimonthlyhoppinings.data.shortDateRangeLabel
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog
import com.maimonthlyhoppinings.ui.persona.PersonaNameDialog
import com.maimonthlyhoppinings.ui.persona.PersonaPickerDialog
import com.maimonthlyhoppinings.ui.persona.PersonaViewModel
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import com.maimonthlyhoppinings.ui.tutorial.TutorialHelpAction
import com.maimonthlyhoppinings.ui.tutorial.TutorialSection
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetIds
import com.maimonthlyhoppinings.ui.tutorial.tutorialTarget

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    personaViewModel: PersonaViewModel,
    onOpenSettings: () -> Unit,
    onOpenPersonas: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenTrends: () -> Unit,
    onStartEvent: () -> Unit,
    onOpenEvent: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val personas by personaViewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<EventWithEntries?>(null) }
    var showPersonaPicker by remember { mutableStateOf(false) }
    var showNewPersona by remember { mutableStateOf(false) }

    if (showPersonaPicker) {
        PersonaPickerDialog(
            personas = personas.personas,
            activeId = personas.active.id,
            onSelect = { id ->
                personaViewModel.switchTo(id)
                showPersonaPicker = false
            },
            onCreate = {
                showPersonaPicker = false
                showNewPersona = true
            },
            onManage = {
                showPersonaPicker = false
                onOpenPersonas()
            },
            onDismiss = { showPersonaPicker = false },
        )
    }
    if (showNewPersona) {
        PersonaNameDialog(
            title = "New persona",
            initialName = "",
            confirmLabel = "Create",
            onConfirm = { name ->
                personaViewModel.create(name)
                showNewPersona = false
            },
            onDismiss = { showNewPersona = false },
        )
    }

    pendingDelete?.let { event ->
        ConfirmDeleteDialog(
            eventTitle = event.event.displayTitle(state.types),
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
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tutorialTarget(TutorialTargetIds.HOME_WELCOME)
                            .clickable { showPersonaPicker = true }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = personas.active.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Switch persona",
                        )
                    }
                },
                actions = {
                    TutorialHelpAction(TutorialSection.Home)
                    Row(modifier = Modifier.tutorialTarget(TutorialTargetIds.HOME_NAV_ICONS)) {
                        IconButton(
                            onClick = onOpenCalendar,
                            modifier = Modifier.tutorialTarget(TutorialTargetIds.HOME_CALENDAR),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Open calendar",
                            )
                        }
                        IconButton(
                            onClick = onOpenTrends,
                            modifier = Modifier.tutorialTarget(TutorialTargetIds.HOME_TRENDS),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "Open trends",
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.tutorialTarget(TutorialTargetIds.HOME_SETTINGS),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartEvent,
                modifier = Modifier.tutorialTarget(TutorialTargetIds.HOME_FAB),
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
        if (!state.hasAnyEvents) {
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
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HomeRange.entries.forEach { range ->
                            FilterChip(
                                selected = state.range == range,
                                onClick = { viewModel.setRange(range) },
                                label = { Text(range.label) },
                            )
                        }
                    }
                }
                if (state.events.isEmpty()) {
                    item {
                        Text(
                            text = "Nothing in this range. Try a wider window, or All.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                } else {
                    items(state.events, key = { it.event.id }) { event ->
                        HomeEventRow(
                            event = event,
                            types = state.types,
                            onClick = { onOpenEvent(event.event.id) },
                            onDelete = { pendingDelete = event },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEventRow(
    event: EventWithEntries,
    types: EventTypeLookup,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val typeColor = colorForEventType(event.event.eventTypeId, types)
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
                        text = event.event.displayTitle(types),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = types.label(event.event.eventTypeId),
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
