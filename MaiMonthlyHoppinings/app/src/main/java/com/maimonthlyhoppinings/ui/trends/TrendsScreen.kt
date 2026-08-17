package com.maimonthlyhoppinings.ui.trends

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EntryWithEvent
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.data.shortDateLabel
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import com.maimonthlyhoppinings.ui.theme.toComposeColor
import com.maimonthlyhoppinings.ui.tutorial.TutorialHelpAction
import com.maimonthlyhoppinings.ui.tutorial.TutorialSection
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetIds
import com.maimonthlyhoppinings.ui.tutorial.tutorialTarget
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val selectedDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel,
    onBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focused = state.focusedEvent
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(focused?.title ?: "Trends") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (focused != null) {
                                viewModel.focusEvent(null)
                            } else {
                                onBack()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (focused != null) "All events" else "Back",
                        )
                    }
                },
                actions = {
                    TutorialHelpAction(TutorialSection.Trends)
                },
            )
        },
    ) { innerPadding ->
        if (landscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                TrendsChartCard(
                    state = state,
                    focused = focused,
                    expandChart = true,
                    onSelectDay = viewModel::selectDay,
                    onShowAllEvents = { viewModel.focusEvent(null) },
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
                        .tutorialTarget(TutorialTargetIds.TRENDS_WAVE),
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    trendsFilterItems(state, focused, viewModel)
                    trendsDetailItems(state, focused, viewModel, onOpenEvent)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                trendsFilterItems(state, focused, viewModel)
                item {
                    TrendsChartCard(
                        state = state,
                        focused = focused,
                        expandChart = false,
                        onSelectDay = viewModel::selectDay,
                        onShowAllEvents = { viewModel.focusEvent(null) },
                        modifier = Modifier.tutorialTarget(TutorialTargetIds.TRENDS_WAVE),
                    )
                }
                trendsDetailItems(state, focused, viewModel, onOpenEvent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.trendsFilterItems(
    state: TrendsUiState,
    focused: TrendEventRow?,
    viewModel: TrendsViewModel,
) {
    item {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TrendsRange.entries.forEach { range ->
                FilterChip(
                    selected = state.range == range,
                    onClick = { viewModel.setRange(range) },
                    label = { Text(range.label) },
                )
            }
        }
    }
    if (state.categoryStats.isNotEmpty() && focused == null) {
        item {
            FlowRow(
                modifier = Modifier.tutorialTarget(TutorialTargetIds.TRENDS_CHIPS),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.categoryStats.forEach { series ->
                    FilterChip(
                        selected = series.selected,
                        onClick = { viewModel.selectType(series.typeId) },
                        label = { Text(series.label) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(series.color.toComposeColor()),
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun LazyListScope.trendsDetailItems(
    state: TrendsUiState,
    focused: TrendEventRow?,
    viewModel: TrendsViewModel,
    onOpenEvent: (Long) -> Unit,
) {
    state.selectedEpochDay?.let { day ->
        item {
            SelectedDayCard(
                epochDay = day,
                entries = state.selectedDayEntries,
                types = state.types,
                onOpenEvent = { eventId ->
                    viewModel.focusEvent(eventId)
                },
            )
        }
    }

    if (focused == null && state.categoryStats.isNotEmpty()) {
        item {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = if (state.events.isEmpty()) {
                    Modifier.tutorialTarget(TutorialTargetIds.TRENDS_EVENTS)
                } else {
                    Modifier
                },
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    state.categoryStats.forEach { series ->
                        val selected = series.selected
                        Text(
                            text = buildString {
                                append(series.label)
                                append(" · ")
                                append(series.eventCount)
                                append(if (series.eventCount == 1) " event" else " events")
                                append(" · ")
                                append(series.entryCount)
                                append(if (series.entryCount == 1) " day" else " days")
                                series.averageIntensity?.let { average ->
                                    append(" · avg ")
                                    append(String.format(Locale.getDefault(), "%.1f", average))
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = colorForEventType(series.typeId, state.types),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectType(series.typeId) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }

    if (focused != null) {
        item {
            Text(
                text = "Entries",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        items(state.focusedEntries, key = { it.entry.id }) { item ->
            FocusedEntryRow(
                item = item,
                types = state.types,
                onClick = { viewModel.selectDay(item.entry.dateEpochDay) },
            )
        }
        item {
            TextButton(onClick = { onOpenEvent(focused.eventId) }) {
                Text("Open event")
            }
        }
    } else if (state.events.isNotEmpty()) {
        item {
            Text(
                text = "Events",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.tutorialTarget(TutorialTargetIds.TRENDS_EVENTS),
            )
        }
        items(state.events, key = { it.eventId }) { event ->
            TrendEventRowCard(
                event = event,
                types = state.types,
                onClick = { viewModel.focusEvent(event.eventId) },
                onOpenEvent = { onOpenEvent(event.eventId) },
            )
        }
    }
}

@Composable
private fun TrendsChartCard(
    state: TrendsUiState,
    focused: TrendEventRow?,
    expandChart: Boolean,
    onSelectDay: (Long?) -> Unit,
    onShowAllEvents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .then(if (expandChart) Modifier.fillMaxSize() else Modifier)
                .padding(12.dp),
        ) {
            Text(
                text = if (focused != null) {
                    "This event"
                } else {
                    "Intensity over time"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (focused != null) {
                    "Daily entries for this event. Drag to inspect a day."
                } else {
                    "One category at a time. Tap another chip to switch, or an event below to zoom in."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            if (!state.hasAnyPoints) {
                Text(
                    text = "No dated entries in this range yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                TrendsChart(
                    series = state.series,
                    startEpochDay = state.startDate.toEpochDay(),
                    endEpochDay = state.endDate.toEpochDay(),
                    selectedEpochDay = state.selectedEpochDay,
                    onSelectDay = onSelectDay,
                    modifier = if (expandChart) {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    },
                )
            }
            if (focused != null) {
                TextButton(onClick = onShowAllEvents) {
                    Text("All events")
                }
            }
        }
    }
}

@Composable
private fun TrendEventRowCard(
    event: TrendEventRow,
    types: EventTypeLookup,
    onClick: () -> Unit,
    onOpenEvent: () -> Unit,
) {
    val typeColor = colorForEventType(event.typeId, types)
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = types.label(event.typeId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = typeColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = buildString {
                        append(shortRange(event.startDate, event.endDate))
                        append(" · ")
                        append(event.entryCount)
                        append(if (event.entryCount == 1) " entry" else " entries")
                        event.averageIntensity?.let { average ->
                            append(" · avg ")
                            append(String.format(Locale.getDefault(), "%.1f", average))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            IconButton(onClick = onOpenEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open event",
                )
            }
        }
    }
}

@Composable
private fun FocusedEntryRow(
    item: EntryWithEvent,
    types: EventTypeLookup,
    onClick: () -> Unit,
) {
    val typeColor = colorForEventType(item.event.eventTypeId, types)
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(typeColor),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.entry.title.trim().ifEmpty { item.entry.shortDateLabel() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = buildString {
                        if (item.entry.title.isNotBlank()) {
                            append(item.entry.shortDateLabel())
                            append(" · ")
                        }
                        append("Intensity ${item.entry.intensity}/10")
                        if (item.entry.details.isNotBlank()) {
                            append(" · ")
                            append(item.entry.details)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectedDayCard(
    epochDay: Long,
    entries: List<EntryWithEvent>,
    types: EventTypeLookup,
    onOpenEvent: (Long) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = LocalDate.ofEpochDay(epochDay).format(selectedDayFormatter),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (entries.isEmpty()) {
                Text(
                    text = "No visible entries on this day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                entries.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenEvent(item.event.id) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorForEventType(item.event.eventTypeId, types)),
                        )
                        Text(
                            text = buildString {
                                append(item.event.displayTitle(types))
                                append(" · ")
                                append(item.entry.intensity)
                                if (item.entry.details.isNotBlank()) {
                                    append(" · ")
                                    append(item.entry.details)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private fun shortRange(start: LocalDate, end: LocalDate): String {
    return if (start == end) {
        start.shortLabel()
    } else {
        "${start.shortLabel()} – ${end.shortLabel()}"
    }
}

private fun LocalDate.shortLabel(): String {
    return format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
}
