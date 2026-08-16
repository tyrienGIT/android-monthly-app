package com.maimonthlyhoppinings.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.data.ActiveColorTheme
import com.maimonthlyhoppinings.data.ColorTheme
import com.maimonthlyhoppinings.data.SavedColorTheme
import com.maimonthlyhoppinings.ui.theme.palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorThemeSettingsScreen(
    activeColorTheme: ActiveColorTheme,
    savedThemes: List<SavedColorTheme>,
    onPresetSelected: (ColorTheme) -> Unit,
    onCustomSelected: (Long) -> Unit,
    onDeleteCustom: (Long) -> Unit,
    onOpenBuilder: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Colour theme") },
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
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                SettingsNavRow(
                    title = "Theme builder",
                    subtitle = "Create a new custom theme",
                    onClick = onOpenBuilder,
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            if (savedThemes.isNotEmpty()) {
                item { SettingsSectionHeader("Saved themes") }
                items(savedThemes, key = { "saved-${it.id}" }) { theme ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            SettingsChoiceRow(
                                label = theme.name,
                                description = "Custom saved theme",
                                selected = activeColorTheme is ActiveColorTheme.Custom &&
                                    activeColorTheme.themeId == theme.id,
                                onClick = { onCustomSelected(theme.id) },
                                leadingColor = Color(theme.lightPrimaryArgb),
                            )
                        }
                        IconButton(onClick = { onDeleteCustom(theme.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete theme",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            item { SettingsSectionHeader("Built-in themes") }
            items(ColorTheme.entries, key = { it.name }) { theme ->
                val palette = theme.palette()
                SettingsChoiceRow(
                    label = theme.displayName,
                    description = theme.description,
                    selected = activeColorTheme is ActiveColorTheme.Preset &&
                        activeColorTheme.theme == theme,
                    onClick = { onPresetSelected(theme) },
                    leadingColor = palette.swatch,
                )
            }
        }
    }
}
