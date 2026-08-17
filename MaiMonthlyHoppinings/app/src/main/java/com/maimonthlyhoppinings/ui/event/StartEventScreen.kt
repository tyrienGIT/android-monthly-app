package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.maimonthlyhoppinings.ui.tutorial.TutorialHelpAction
import com.maimonthlyhoppinings.ui.tutorial.TutorialSection
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetIds
import com.maimonthlyhoppinings.ui.tutorial.tutorialTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartEventScreen(
    viewModel: StartEventViewModel,
    onBack: () -> Unit,
    onSaved: (eventId: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSaveConfirm by remember { mutableStateOf(false) }
    var dateRangePickTarget by remember { mutableStateOf<DateRangePickTarget?>(null) }
    val typeColor = colorForEventType(state.draft.eventTypeId, state.types)
    val onTypeColor = if (typeColor.luminance() > 0.55f) Color.Black else Color.White
    val fieldColors = eventTypeFieldColors(typeColor)

    LaunchedEffect(viewModel) {
        viewModel.savedEvents.collect { eventId ->
            onSaved(eventId)
        }
    }

    if (showSaveConfirm) {
        ConfirmSaveDialog(
            eventTitle = viewModel.resolvedTitle(),
            isEditing = state.isEditing,
            onConfirm = {
                showSaveConfirm = false
                viewModel.save()
            },
            onDismiss = { showSaveConfirm = false },
        )
    }

    dateRangePickTarget?.let { target ->
        EventDateRangePickerDialog(
            target = target,
            initialStartEpochDay = state.draft.startDate.toEpochDay(),
            initialEndEpochDay = state.draft.endDate.toEpochDay(),
            onConfirm = { startEpochDay, endEpochDay ->
                viewModel.onDateRangeSelected(startEpochDay, endEpochDay)
                dateRangePickTarget = null
            },
            onDismiss = { dateRangePickTarget = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditing) "Edit event" else "Start event")
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
                    TutorialHelpAction(TutorialSection.StartEvent)
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
                    text = if (state.isEditing) {
                        "Update the event. The date span expands to cover entries and can be widened manually."
                    } else {
                        "Start an event with a date span, then add single-day entries under it."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.tutorialTarget(TutorialTargetIds.START_INTRO),
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Title (optional)") },
                    placeholder = { Text("Defaults to ${state.types.label(state.draft.eventTypeId)}") },
                    colors = fieldColors,
                )
            }
            item {
                Column(
                    modifier = Modifier.tutorialTarget(TutorialTargetIds.START_CATEGORY_DATES),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EventTypeDropdown(
                        selectedTypeId = state.draft.eventTypeId,
                        types = state.types,
                        typeColor = typeColor,
                        onTypeSelected = viewModel::onEventTypeChange,
                    )
                    Column(modifier = Modifier.tutorialTarget(TutorialTargetIds.START_SPAN)) {
                        DateRangeField(
                            startLabel = state.startDateLabel,
                            endLabel = state.endDateLabel,
                            typeColor = typeColor,
                            onStartClick = { dateRangePickTarget = DateRangePickTarget.Start },
                            onEndClick = { dateRangePickTarget = DateRangePickTarget.End },
                        )
                    }
                }
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
                    Text(
                        if (state.isEditing) "Save changes" else "Start event",
                        fontWeight = FontWeight.SemiBold,
                    )
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
