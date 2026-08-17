package com.maimonthlyhoppinings.ui.settings

import androidx.compose.foundation.layout.Column
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
import com.maimonthlyhoppinings.ui.tutorial.TutorialHelpAction
import com.maimonthlyhoppinings.ui.tutorial.TutorialSection
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetIds
import com.maimonthlyhoppinings.ui.tutorial.tutorialTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    onOpenBooks: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenData: () -> Unit,
    onOpenFeedback: () -> Unit,
    onReplayTutorial: () -> Unit,
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
                actions = {
                    TutorialHelpAction(TutorialSection.Settings)
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
                Column(modifier = Modifier.tutorialTarget(TutorialTargetIds.SETTINGS_PREFS)) {
                    SettingsNavRow(
                        title = "Books",
                        subtitle = "Separate journals on this phone",
                        onClick = onOpenBooks,
                        modifier = Modifier.tutorialTarget(TutorialTargetIds.SETTINGS_BOOKS),
                    )
                    SettingsNavRow(
                        title = "Appearance",
                        subtitle = "Light/dark mode, colour themes, theme builder",
                        onClick = onOpenAppearance,
                    )
                    SettingsNavRow(
                        title = "Categories",
                        subtitle = "Names and colours for this book",
                        onClick = onOpenCategories,
                        modifier = Modifier.tutorialTarget(TutorialTargetIds.SETTINGS_CATEGORIES),
                    )
                    SettingsNavRow(
                        title = "Data",
                        subtitle = "Export and import this book’s JSON backup",
                        onClick = onOpenData,
                        modifier = Modifier.tutorialTarget(TutorialTargetIds.SETTINGS_DATA),
                    )
                }
            }
            item { SettingsSectionHeader("Help") }
            item {
                SettingsNavRow(
                    title = "Feedback",
                    subtitle = "Save markdown notes on this phone, then share when you want",
                    onClick = onOpenFeedback,
                    modifier = Modifier.tutorialTarget(TutorialTargetIds.SETTINGS_FEEDBACK),
                )
                SettingsNavRow(
                    title = "View tutorial",
                    subtitle = "Walk through Home, Calendar, and Trends on the real screens.",
                    onClick = onReplayTutorial,
                    modifier = Modifier.tutorialTarget(TutorialTargetIds.SETTINGS_VIEW_TUTORIAL),
                )
            }
        }
    }
}
