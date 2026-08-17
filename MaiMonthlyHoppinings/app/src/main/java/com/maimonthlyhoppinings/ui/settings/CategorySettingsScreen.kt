package com.maimonthlyhoppinings.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.EventTypeColor
import com.maimonthlyhoppinings.data.EventTypeEntity
import com.maimonthlyhoppinings.ui.theme.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySettingsScreen(
    viewModel: CategorySettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
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
                onClick = viewModel::addCategory,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add category",
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
            item { SettingsSectionHeader("Event types") }
            items(state.categories, key = { it.id }) { category ->
                CategoryNameRow(
                    category = category,
                    onLabelChange = { viewModel.updateLabel(category.id, it) },
                    onColorChange = { viewModel.updateColor(category.id, it) },
                )
            }
        }
    }
}

@Composable
private fun CategoryNameRow(
    category: EventTypeEntity,
    onLabelChange: (String) -> Unit,
    onColorChange: (EventTypeColor) -> Unit,
) {
    val typeColor = category.colorEnum().toComposeColor()
    var draft by remember(category.id) { mutableStateOf(category.label) }
    var colorMenuOpen by remember(category.id) { mutableStateOf(false) }
    LaunchedEffect(category.label) {
        if (draft != category.label) {
            draft = category.label
        }
    }

    fun commit() {
        val trimmed = draft.trim()
        if (trimmed.isNotEmpty() && trimmed != category.label) {
            onLabelChange(trimmed)
        } else {
            draft = category.label
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(end = 12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(typeColor)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = CircleShape,
                    )
                    .clickable { colorMenuOpen = true },
            )
            DropdownMenu(
                expanded = colorMenuOpen,
                onDismissRequest = { colorMenuOpen = false },
            ) {
                EventTypeColor.entries.forEach { color ->
                    val swatch = color.toComposeColor()
                    val selected = color == category.colorEnum()
                    DropdownMenuItem(
                        text = { Text(color.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                        },
                                        shape = CircleShape,
                                    ),
                            )
                        },
                        onClick = {
                            onColorChange(color)
                            colorMenuOpen = false
                        },
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focus ->
                        if (!focus.isFocused) commit()
                    },
                singleLine = true,
                label = { Text("Name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commit() }),
            )
        }
    }
}
