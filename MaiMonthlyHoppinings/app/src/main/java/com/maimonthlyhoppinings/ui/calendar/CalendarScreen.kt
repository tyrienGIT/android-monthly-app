package com.maimonthlyhoppinings.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.DayHeatSegment
import com.maimonthlyhoppinings.data.EventEntry
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.data.intensityAtProgress
import com.maimonthlyhoppinings.data.startTimeLabel
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import com.maimonthlyhoppinings.ui.theme.heatContentColor
import com.maimonthlyhoppinings.ui.theme.intensityHeatAlpha
import com.maimonthlyhoppinings.ui.theme.toComposeColor
import com.maimonthlyhoppinings.ui.theme.withIntensityHeat
import androidx.compose.ui.graphics.Brush
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit,
    onStartEvent: (LocalDate) -> Unit,
    onAddEntryForEvent: (date: LocalDate, eventId: Long) -> Unit,
    onOpenEvent: (eventId: Long) -> Unit,
    onEditEntry: (entryId: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var didInitialCenterScroll by remember { mutableStateOf(false) }
    var showPickEvent by remember { mutableStateOf(false) }

    fun requestAdd() {
        if (state.allEvents.isEmpty()) {
            onStartEvent(state.selectedDate)
        } else {
            showPickEvent = true
        }
    }

    if (showPickEvent) {
        PickEventForEntryDialog(
            date = state.selectedDate,
            events = state.allEvents,
            types = state.types,
            onStartNewEvent = {
                showPickEvent = false
                onStartEvent(state.selectedDate)
            },
            onPickEvent = { eventId ->
                showPickEvent = false
                onAddEntryForEvent(state.selectedDate, eventId)
            },
            onDismiss = { showPickEvent = false },
        )
    }

    LaunchedEffect(state.weeks.size, state.initialCenterWeekIndex) {
        if (didInitialCenterScroll || state.weeks.isEmpty()) return@LaunchedEffect
        val targetIndex = state.initialCenterWeekIndex.coerceIn(0, state.weeks.lastIndex)
        listState.scrollToItem(targetIndex)
        snapshotFlow { listState.layoutInfo }
            .filter { info ->
                info.viewportEndOffset > info.viewportStartOffset &&
                    info.visibleItemsInfo.any { it.index == targetIndex }
            }
            .first()
        val layoutInfo = listState.layoutInfo
        val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
        val itemSize = layoutInfo.visibleItemsInfo
            .first { it.index == targetIndex }
            .size
        listState.scrollToItem(
            index = targetIndex,
            scrollOffset = -((viewportHeight - itemSize) / 2),
        )
        didInitialCenterScroll = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { requestAdd() }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add entry",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Upper ~2/3: scrolling calendar
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
            ) {
                WeekdayHeader(labels = state.weekdayLabels)
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    itemsIndexed(
                        items = state.weeks,
                        key = { _, week -> week.weekStart.toEpochDay() },
                    ) { _, week ->
                        if (week.monthLabel != null) {
                            Text(
                                text = week.monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                        WeekRow(
                            week = week,
                            selectedDate = state.selectedDate,
                            onDayClick = viewModel::selectDay,
                        )
                    }
                }
            }

            HorizontalDivider()

            // Lower ~1/3: events for the selected day (with sub-entries)
            SelectedDayEventsPane(
                dateLabel = state.selectedDateLabel,
                groups = state.selectedDayGroups,
                types = state.types,
                onAdd = { requestAdd() },
                onOpenEvent = onOpenEvent,
                onEditEntry = onEditEntry,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

private const val VisibleSubEntries = 3

@Composable
private fun SelectedDayEventsPane(
    dateLabel: String,
    groups: List<DayEventGroup>,
    types: EventTypeLookup,
    onAdd: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    onEditEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (groups.isEmpty()) {
                        "No events"
                    } else {
                        "${groups.size} event${if (groups.size == 1) "" else "s"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onAdd) {
                Text("Add")
            }
        }

        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tap a day above, or add an entry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(groups, key = { it.event.id }) { group ->
                    SelectedDayEventGroup(
                        group = group,
                        types = types,
                        onOpenEvent = { onOpenEvent(group.event.id) },
                        onEditEntry = onEditEntry,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedDayEventGroup(
    group: DayEventGroup,
    types: EventTypeLookup,
    onOpenEvent: () -> Unit,
    onEditEntry: (Long) -> Unit,
) {
    val typeColor = colorForEventType(group.event.eventTypeId, types)
    var expanded by remember(group.event.id) { mutableStateOf(false) }
    val hiddenCount = (group.entries.size - VisibleSubEntries).coerceAtLeast(0)
    val visibleEntries = when {
        expanded -> group.entries
        else -> group.entries.take(VisibleSubEntries)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenEvent)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(typeColor),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.event.displayTitle(types),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = types.label(group.event.eventTypeId),
                    style = MaterialTheme.typography.bodySmall,
                    color = typeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (visibleEntries.isEmpty()) {
            Text(
                text = "No sub-events on this day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 2.dp),
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                visibleEntries.forEach { entry ->
                    SelectedDaySubEntryRow(
                        entryTitle = entry.title.trim().ifEmpty { group.event.displayTitle(types) },
                        entry = entry,
                        typeColor = typeColor,
                        onClick = { onEditEntry(entry.id) },
                    )
                }
            }
        }

        // …+ expands remaining sub-events; once expanded (or if few), opens the event.
        val overflowLabel = when {
            hiddenCount > 0 && !expanded -> "…+$hiddenCount"
            else -> "…"
        }
        Text(
            text = overflowLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 4.dp, top = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    if (hiddenCount > 0 && !expanded) {
                        expanded = true
                    } else {
                        onOpenEvent()
                    }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SelectedDaySubEntryRow(
    entryTitle: String,
    entry: EventEntry,
    typeColor: Color,
    onClick: () -> Unit,
) {
    val intensity = entry.intensity.coerceIn(1, 10)
    val fill = typeColor.withIntensityHeat(intensity)
    val content = heatContentColor(typeColor, intensity)
    val timeLabel = entry.startTimeLabel()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(fill)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entryTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    if (timeLabel != null) {
                        append(timeLabel)
                        append(" · ")
                    }
                    append("Intensity $intensity/10")
                },
                style = MaterialTheme.typography.bodySmall,
                color = content.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun WeekdayHeader(labels: List<String>) {
    val weekendColor = MaterialTheme.colorScheme.surfaceVariant
    val weekdays = labels.take(5)
    val weekend = labels.drop(5)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(5f)) {
            weekdays.forEach { label ->
                WeekdayLabel(
                    label = label,
                    modifier = Modifier.weight(1f),
                    emphasized = false,
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(2f)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(weekendColor),
        ) {
            weekend.forEach { label ->
                WeekdayLabel(
                    label = label,
                    modifier = Modifier.weight(1f),
                    emphasized = true,
                )
            }
        }
    }
}

@Composable
private fun WeekdayLabel(
    label: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier.padding(vertical = 8.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun WeekRow(
    week: CalendarWeek,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
) {
    val weekendColor = MaterialTheme.colorScheme.surfaceVariant
    val weekdays = week.days.take(5)
    val weekend = week.days.drop(5)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(72.dp),
    ) {
        Row(modifier = Modifier.weight(5f)) {
            weekdays.forEach { day ->
                DayCell(
                    day = day,
                    selected = day.date == selectedDate,
                    onClick = { onDayClick(day.date) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .background(weekendColor),
        ) {
            weekend.forEach { day ->
                DayCell(
                    day = day,
                    selected = day.date == selectedDate,
                    onClick = { onDayClick(day.date) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .padding(1.dp)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
                        .border(1.5.dp, MaterialTheme.colorScheme.secondary, shape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (day.isToday) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    day.isToday -> MaterialTheme.colorScheme.onPrimary
                    selected -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday || selected) FontWeight.Bold else FontWeight.Normal,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HeatBar(
            segments = day.heatSegments,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .height(if (day.heatSegments.isEmpty()) 8.dp else (day.heatSegments.size * 4).dp),
        )
    }
}

@Composable
private fun HeatBar(
    segments: List<DayHeatSegment>,
    modifier: Modifier = Modifier,
) {
    if (segments.isEmpty()) {
        Spacer(modifier = modifier)
        return
    }
    Column(
        modifier = modifier.clip(RoundedCornerShape(3.dp)),
    ) {
        segments.forEachIndexed { index, segment ->
            if (index > 0) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                )
            }
            val base = segment.color.toComposeColor()
            val brush = eventHeatBrush(base, segment)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(brush),
            )
        }
    }
}

/**
 * Horizontal gradient of all sub-entry intensities for this event.
 * Sampled so neighbouring days continue the same band start→finish.
 */
private fun eventHeatBrush(
    base: Color,
    segment: DayHeatSegment,
): Brush {
    val stops = segment.intensityStops
    if (stops.size <= 1) {
        val intensity = segment.intensityAtProgress()
        return Brush.horizontalGradient(
            listOf(base.withIntensityHeat(intensity), base.withIntensityHeat(intensity)),
        )
    }
    // Window around this day's progress so the ribbon reads continuously across cells.
    val window = 0.35f
    val center = segment.spanProgress.coerceIn(0f, 1f)
    val from = (center - window / 2f).coerceAtLeast(0f)
    val to = (center + window / 2f).coerceAtMost(1f)
    val sampleCount = (stops.size + 1).coerceIn(2, 6)
    val colors = List(sampleCount) { i ->
        val t = if (sampleCount == 1) center else from + (to - from) * (i / (sampleCount - 1f))
        val intensity = sampleIntensityStops(stops, t)
        base.copy(alpha = intensityHeatAlpha(intensity))
    }
    return Brush.horizontalGradient(colors)
}

private fun sampleIntensityStops(stops: List<Int>, progress: Float): Int {
    if (stops.isEmpty()) return 1
    if (stops.size == 1) return stops.first()
    val t = progress.coerceIn(0f, 1f) * (stops.lastIndex)
    val i = t.toInt().coerceIn(0, stops.lastIndex - 1)
    val frac = t - i
    val a = stops[i]
    val b = stops[i + 1]
    return (a + (b - a) * frac).toInt().coerceIn(1, 10)
}
