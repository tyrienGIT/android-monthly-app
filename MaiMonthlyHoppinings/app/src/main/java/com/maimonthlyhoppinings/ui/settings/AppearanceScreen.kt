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
import com.maimonthlyhoppinings.data.ActiveColorTheme
import com.maimonthlyhoppinings.data.SavedColorTheme
import com.maimonthlyhoppinings.data.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    themeMode: ThemeMode,
    activeColorTheme: ActiveColorTheme,
    savedThemes: List<SavedColorTheme>,
    onOpenLightDark: () -> Unit,
    onOpenColorThemes: () -> Unit,
    onOpenThemeBuilder: () -> Unit,
    onBack: () -> Unit,
) {
    val modeLabel = when (themeMode) {
        ThemeMode.SYSTEM -> "System default"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }
    val colourLabel = when (activeColorTheme) {
        is ActiveColorTheme.Preset -> activeColorTheme.theme.displayName
        is ActiveColorTheme.Custom -> {
            savedThemes.firstOrNull { it.id == activeColorTheme.themeId }?.name
                ?: "Custom theme"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
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
            item { SettingsSectionHeader("Theme controls") }
            item {
                SettingsNavRow(
                    title = "Light / Dark",
                    subtitle = modeLabel,
                    onClick = onOpenLightDark,
                )
            }
            item {
                SettingsNavRow(
                    title = "Colour theme",
                    subtitle = colourLabel,
                    onClick = onOpenColorThemes,
                )
            }
            item {
                SettingsNavRow(
                    title = "Theme builder",
                    subtitle = "Create and save a custom colour theme",
                    onClick = onOpenThemeBuilder,
                )
            }
        }
    }
}
