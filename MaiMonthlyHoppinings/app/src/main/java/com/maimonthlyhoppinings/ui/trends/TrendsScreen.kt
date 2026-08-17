package com.maimonthlyhoppinings.ui.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EntryWithEvent
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.data.displayTitle
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import com.maimonthlyhoppinings.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val selectedDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Intensity over time",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "One category at a time reads clearest. Drag across to inspect a day.",
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
                                onSelectDay = viewModel::selectDay,
                            )
                        }
                    }
                }
            }

            state.selectedEpochDay?.let { day ->
                item {
                    SelectedDayCard(
                        epochDay = day,
                        entries = state.selectedDayEntries,
                        types = state.types,
                    )
                }
            }

            if (state.series.isNotEmpty()) {
                item {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        state.series.forEach { series ->
                            FilterChip(
                                selected = series.visible,
                                onClick = { viewModel.toggleType(series.typeId) },
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

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.series.filter { it.visible }.forEach { series ->
                            val average = series.averageIntensity
                            Text(
                                text = buildString {
                                    append(series.label)
                                    append(" · ")
                                    append(series.entryCount)
                                    append(if (series.entryCount == 1) " day" else " days")
                                    if (average != null) {
                                        append(" · avg ")
                                        append(String.format(Locale.getDefault(), "%.1f", average))
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorForEventType(series.typeId, state.types),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayCard(
    epochDay: Long,
    entries: List<EntryWithEvent>,
    types: EventTypeLookup,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        )
                    }
                }
            }
        }
    }
}
