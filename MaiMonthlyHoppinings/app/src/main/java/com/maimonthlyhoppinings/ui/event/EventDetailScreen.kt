package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EventEntry
import com.maimonthlyhoppinings.data.EventRepository
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.TrackedEvent
import com.maimonthlyhoppinings.data.dateLabel
import com.maimonthlyhoppinings.data.dateRangeLabel
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.data.startTimeLabel
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import com.maimonthlyhoppinings.ui.tutorial.TutorialHelpAction
import com.maimonthlyhoppinings.ui.tutorial.TutorialSection
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetIds
import com.maimonthlyhoppinings.ui.tutorial.tutorialTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    eventRepository: EventRepository,
    onBack: () -> Unit,
    onEditEvent: (Long) -> Unit,
    onAddEntry: (Long) -> Unit,
    onOpenEntry: (Long) -> Unit,
) {
    val pagerViewModel: EventPagerViewModel = viewModel(
        factory = EventPagerViewModel.factory(eventRepository),
    )
    val eventIdsState = pagerViewModel.eventIds.collectAsStateWithLifecycle()
    val eventIds = eventIdsState.value
    val pagerState = rememberPagerState(
        pageCount = { eventIdsState.value.size.coerceAtLeast(1) },
    )
    var alignedToOpenedEvent by remember(eventId) { mutableStateOf(false) }

    LaunchedEffect(eventIds, eventId) {
        val index = eventIds.indexOf(eventId)
        if (index >= 0 && !alignedToOpenedEvent) {
            pagerState.scrollToPage(index)
            alignedToOpenedEvent = true
        }
    }

    val currentEventId = eventIds.getOrNull(pagerState.currentPage) ?: eventId
    val currentViewModel: EventDetailViewModel = viewModel(
        key = "event-$currentEventId",
        factory = EventDetailViewModel.factory(currentEventId, eventRepository),
    )
    val state by currentViewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteEntry by remember { mutableStateOf<EventEntry?>(null) }
    var pendingDeleteEvent by remember { mutableStateOf(false) }

    LaunchedEffect(currentEventId) {
        pendingDeleteEntry = null
        pendingDeleteEvent = false
    }

    val event = state.eventWithEntries?.event
    val types = state.types

    pendingDeleteEntry?.let { entry ->
        val parent = state.eventWithEntries?.event
        ConfirmDeleteDialog(
            eventTitle = parent?.let { entry.title.trim().ifEmpty { it.displayTitle(types) } }
                ?: entry.dateLabel(),
            entityLabel = "entry",
            onConfirm = {
                currentViewModel.deleteEntry(entry.id)
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null },
        )
    }

    if (pendingDeleteEvent && event != null) {
        ConfirmDeleteDialog(
            eventTitle = event.displayTitle(types),
            entityLabel = "event",
            onConfirm = {
                pendingDeleteEvent = false
                currentViewModel.deleteEvent(onDeleted = onBack)
            },
            onDismiss = { pendingDeleteEvent = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.tutorialTarget(TutorialTargetIds.EVENT_SWIPE)) {
                        Text(event?.displayTitle(types) ?: "Event")
                        if (eventIds.size > 1) {
                            Text(
                                text = "${pagerState.currentPage + 1} of ${eventIds.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TutorialHelpAction(TutorialSection.EventDetail)
                    if (event != null) {
                        IconButton(onClick = { onEditEvent(currentEventId) }) {
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
                    onClick = { onAddEntry(currentEventId) },
                    modifier = Modifier.tutorialTarget(TutorialTargetIds.EVENT_ADD),
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
            eventIds.isNotEmpty() -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    userScrollEnabled = eventIds.size > 1,
                    beyondViewportPageCount = 1,
                ) { page ->
                    val pageId = eventIds.getOrNull(page) ?: eventId
                    val pageViewModel: EventDetailViewModel = viewModel(
                        key = "event-$pageId",
                        factory = EventDetailViewModel.factory(pageId, eventRepository),
                    )
                    val pageState by pageViewModel.uiState.collectAsStateWithLifecycle()
                    val pageEvent = pageState.eventWithEntries?.event
                    if (pageEvent == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (pageState.notFound) "Event not found" else "Loading…")
                        }
                    } else {
                        EventDetailPage(
                            event = pageEvent,
                            state = pageState,
                            types = pageState.types,
                            typeColor = colorForEventType(pageEvent.eventTypeId, pageState.types),
                            onOpenEntry = onOpenEntry,
                            onDeleteEntry = { pendingDeleteEntry = it },
                        )
                    }
                }
            }
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
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Loading…")
                }
            }
        }
    }
}

@Composable
private fun EventDetailPage(
    event: TrackedEvent,
    state: EventDetailUiState,
    types: EventTypeLookup,
    typeColor: Color,
    onOpenEntry: (Long) -> Unit,
    onDeleteEntry: (EventEntry) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = types.label(event.eventTypeId),
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
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .tutorialTarget(TutorialTargetIds.EVENT_HEATMAP),
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
                modifier = Modifier
                    .padding(top = 8.dp)
                    .tutorialTarget(TutorialTargetIds.EVENT_ENTRIES),
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
                types = types,
                typeColor = typeColor,
                onClick = { onOpenEntry(entry.id) },
                onDelete = { onDeleteEntry(entry) },
            )
        }
    }
}

@Composable
private fun EntryRow(
    entry: EventEntry,
    parentEvent: TrackedEvent,
    types: EventTypeLookup,
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
                            text = entry.title.trim().ifEmpty { parentEvent.displayTitle(types) },
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
