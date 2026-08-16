package com.maimonthlyhoppinings.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    onOpenAppearance: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenData: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            item { SettingsSectionHeader("Preferences") }
            item {
                SettingsNavRow(
                    title = "Appearance",
                    subtitle = "Light/dark mode, colour themes, theme builder",
                    onClick = onOpenAppearance,
                )
            }
            item {
                SettingsNavRow(
                    title = "Categories",
                    subtitle = "Names and colours for event types",
                    onClick = onOpenCategories,
                )
            }
            item {
                SettingsNavRow(
                    title = "Data",
                    subtitle = "Export and import a JSON backup",
                    onClick = onOpenData,
                )
            }
        }
    }
}
