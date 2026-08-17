package com.maimonthlyhoppinings.ui.persona

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.maimonthlyhoppinings.data.Persona
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog

private sealed interface PersonaPrompt {
    data object Hidden : PersonaPrompt
    data object Create : PersonaPrompt
    data class Rename(val persona: Persona) : PersonaPrompt
    data class Delete(val persona: Persona) : PersonaPrompt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasScreen(
    viewModel: PersonaViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var prompt by remember { mutableStateOf<PersonaPrompt>(PersonaPrompt.Hidden) }

    when (val current = prompt) {
        PersonaPrompt.Hidden -> Unit
        PersonaPrompt.Create -> {
            PersonaNameDialog(
                title = "New persona",
                initialName = "",
                confirmLabel = "Create",
                onConfirm = { name ->
                    viewModel.create(name)
                    prompt = PersonaPrompt.Hidden
                },
                onDismiss = { prompt = PersonaPrompt.Hidden },
            )
        }
        is PersonaPrompt.Rename -> {
            PersonaNameDialog(
                title = "Rename persona",
                initialName = current.persona.name,
                confirmLabel = "Save",
                onConfirm = { name ->
                    viewModel.rename(current.persona.id, name)
                    prompt = PersonaPrompt.Hidden
                },
                onDismiss = { prompt = PersonaPrompt.Hidden },
            )
        }
        is PersonaPrompt.Delete -> {
            ConfirmDeleteDialog(
                eventTitle = current.persona.name,
                entityLabel = "persona",
                onConfirm = {
                    viewModel.delete(current.persona.id)
                    prompt = PersonaPrompt.Hidden
                },
                onDismiss = { prompt = PersonaPrompt.Hidden },
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personas") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { prompt = PersonaPrompt.Create },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New persona",
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            item {
                Text(
                    text = "Each persona is a separate journal on this phone. Switching does not mix events.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(state.personas, key = { it.id }) { persona ->
                PersonaRow(
                    persona = persona,
                    selected = persona.id == state.active.id,
                    canDelete = state.canDelete,
                    onSelect = { viewModel.switchTo(persona.id) },
                    onRename = { prompt = PersonaPrompt.Rename(persona) },
                    onDelete = { prompt = PersonaPrompt.Delete(persona) },
                )
            }
        }
    }
}

@Composable
private fun PersonaRow(
    persona: Persona,
    selected: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        ) {
            Text(
                text = persona.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = buildString {
                    if (persona.isDefault) append("Default")
                    if (selected) {
                        if (isNotEmpty()) append(" · ")
                        append("Open now")
                    }
                    if (isEmpty()) append("On this phone")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { menuOpen = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Persona options",
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        menuOpen = false
                        onRename()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    enabled = canDelete,
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}
