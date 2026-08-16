package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.ui.ConfirmSaveDialog
import com.maimonthlyhoppinings.ui.theme.colorForEventType
import java.time.LocalTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    viewModel: EntryEditorViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }

    val typeColor = colorForEventType(state.event?.eventTypeId.orEmpty(), state.types)
    val onTypeColor = if (typeColor.luminance() > 0.55f) Color.Black else Color.White
    val fieldColors = eventTypeFieldColors(typeColor)

    LaunchedEffect(viewModel) {
        viewModel.savedEvents.collect {
            onSaved()
        }
    }

    if (showSaveConfirm) {
        ConfirmSaveDialog(
            eventTitle = viewModel.confirmLabel(),
            isEditing = state.isEditing,
            entityLabel = "entry",
            onConfirm = {
                showSaveConfirm = false
                viewModel.save()
            },
            onDismiss = { showSaveConfirm = false },
        )
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            initialEpochDay = state.draft.date.toEpochDay(),
            onConfirm = { epochDay ->
                viewModel.onDateSelected(epochDay)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        val initial = state.draft.startTime ?: LocalTime.now()
        EventTimePickerDialog(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            onConfirm = { hour, minute ->
                viewModel.onTimeSelected(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            onClear = {
                viewModel.clearTime()
                showTimePicker = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditing) "Edit entry" else "Add entry")
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = state.event?.let {
                        "${state.types.label(it.eventTypeId)} · ${viewModel.eventTitle()}"
                    }
                        ?: "Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = typeColor,
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Title (optional)") },
                    placeholder = { Text("Defaults to ${viewModel.eventTitle()}") },
                    colors = fieldColors,
                )
            }
            item {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = typeColor),
                    border = BorderStroke(1.dp, typeColor),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Date: ${state.dateLabel}")
                }
            }
            item {
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = typeColor),
                    border = BorderStroke(1.dp, typeColor),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Start time: ${state.startTimeLabel}")
                }
            }
            item {
                Text(
                    text = "Intensity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = typeColor,
                )
                Text(
                    text = "${state.draft.intensity} / 10",
                    style = MaterialTheme.typography.bodyLarge,
                    color = typeColor,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Slider(
                    value = state.draft.intensity.toFloat(),
                    onValueChange = { viewModel.onIntensityChange(it.roundToInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = typeColor,
                        activeTrackColor = typeColor,
                        inactiveTrackColor = typeColor.copy(alpha = 0.25f),
                        activeTickColor = onTypeColor,
                        inactiveTickColor = typeColor.copy(alpha = 0.4f),
                    ),
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.details,
                    onValueChange = viewModel::onDetailsChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Details (optional)") },
                    colors = fieldColors,
                )
            }
            item {
                Button(
                    onClick = { showSaveConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = typeColor,
                        contentColor = onTypeColor,
                    ),
                ) {
                    Text(if (state.isEditing) "Save changes" else "Add entry")
                }
            }
            state.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
