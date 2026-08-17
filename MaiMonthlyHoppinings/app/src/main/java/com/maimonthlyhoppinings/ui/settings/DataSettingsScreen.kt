package com.maimonthlyhoppinings.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.BackupFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.maimonthlyhoppinings.ui.tutorial.TutorialHelpAction
import com.maimonthlyhoppinings.ui.tutorial.TutorialSection
import com.maimonthlyhoppinings.ui.tutorial.TutorialTargetIds
import com.maimonthlyhoppinings.ui.tutorial.tutorialTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BackupFileName = "mai-monthly-hoppinings-backup.json"

private sealed interface BackupPrompt {
    data object Hidden : BackupPrompt
    data class ChooseImport(val json: String) : BackupPrompt
    data class ConfirmReplace(val json: String) : BackupPrompt
    data class Message(val text: String) : BackupPrompt
}

private val retainMonthOptions = listOf(1, 2, 3, 6, 12)
private val maxCountOptions = listOf(30, 60, 90, 180)
private val lastBackupFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DataSettingsScreen(
    viewModel: DataBackupViewModel,
    onBack: () -> Unit,
) {
    val autoBackup by viewModel.autoBackup.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf<BackupPrompt>(BackupPrompt.Hidden) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri == null || json == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("Could not open the selected file")
                }
                prompt = BackupPrompt.Message("Backup exported.")
            } catch (e: Exception) {
                prompt = BackupPrompt.Message(e.message ?: "Export failed")
            }
        }
    }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                        ?: error("Could not read the selected file")
                }
                BackupFile.parse(json)
                prompt = BackupPrompt.ChooseImport(json)
            } catch (e: Exception) {
                prompt = BackupPrompt.Message(e.message ?: "Could not read backup")
            }
        }
    }

    fun runImport(json: String, replace: Boolean) {
        scope.launch {
            try {
                viewModel.import(json, replace)
                prompt = BackupPrompt.Message(
                    if (replace) "Backup imported (replace)." else "Backup imported (merge).",
                )
            } catch (e: Exception) {
                prompt = BackupPrompt.Message(e.message ?: "Import failed")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TutorialHelpAction(TutorialSection.Data)
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
                Column(modifier = Modifier.tutorialTarget(TutorialTargetIds.DATA_LOCAL)) {
                    SettingsSectionHeader("Backup")
                    SettingsNavRow(
                        title = "Export",
                        subtitle = "Save a JSON backup of events, types, and themes",
                        onClick = {
                            scope.launch {
                                try {
                                    pendingExportJson = viewModel.export()
                                    createDocument.launch(BackupFileName)
                                } catch (e: Exception) {
                                    prompt = BackupPrompt.Message(e.message ?: "Export failed")
                                }
                            }
                        },
                    )
                    SettingsNavRow(
                        title = "Import",
                        subtitle = "Restore from a JSON backup",
                        onClick = {
                            openDocument.launch(
                                arrayOf("application/json", "application/octet-stream", "text/*"),
                            )
                        },
                        modifier = Modifier.tutorialTarget(TutorialTargetIds.DATA_MERGE_REPLACE),
                    )
                }
            }
            item { SettingsSectionHeader("Automatic copies") }
            item {
                SettingsSwitchRow(
                    title = "Daily backup",
                    subtitle = if (autoBackup.enabled) {
                        val last = if (autoBackup.lastBackupEpochDay >= 0L) {
                            "Last copy ${LocalDate.ofEpochDay(autoBackup.lastBackupEpochDay).format(lastBackupFormatter)}. "
                        } else {
                            ""
                        }
                        last + "Once a day when you open the app. Stays on this device."
                    } else {
                        "Write a JSON copy once a day when you open the app."
                    },
                    checked = autoBackup.enabled,
                    onCheckedChange = viewModel::setAutoBackupEnabled,
                )
            }
            if (autoBackup.enabled) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Keep for",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "Older copies are deleted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            retainMonthOptions.forEach { months ->
                                FilterChip(
                                    selected = autoBackup.retainMonths == months,
                                    onClick = { viewModel.setRetainMonths(months) },
                                    label = {
                                        Text(if (months == 1) "1 month" else "$months months")
                                    },
                                )
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Maximum copies",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "Oldest files go first if you hit the cap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            maxCountOptions.forEach { count ->
                                FilterChip(
                                    selected = autoBackup.maxCount == count,
                                    onClick = { viewModel.setMaxCount(count) },
                                    label = { Text("$count") },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (val current = prompt) {
        BackupPrompt.Hidden -> Unit
        is BackupPrompt.ChooseImport -> {
            AlertDialog(
                onDismissRequest = { prompt = BackupPrompt.Hidden },
                title = { Text("Import backup") },
                text = {
                    Text(
                        "Merge keeps existing items and updates matching IDs. " +
                            "Replace all deletes local items that are not in this file.",
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { runImport(current.json, replace = false) },
                    ) {
                        Text("Merge")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { prompt = BackupPrompt.ConfirmReplace(current.json) },
                    ) {
                        Text("Replace all")
                    }
                },
            )
        }
        is BackupPrompt.ConfirmReplace -> {
            AlertDialog(
                onDismissRequest = { prompt = BackupPrompt.Hidden },
                title = { Text("Replace all data?") },
                text = {
                    Text(
                        "Events, entries, types, and custom themes that are not in this " +
                            "backup will be deleted. This can't be undone.",
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { runImport(current.json, replace = true) },
                    ) {
                        Text("Replace")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { prompt = BackupPrompt.Hidden }) {
                        Text("Cancel")
                    }
                },
            )
        }
        is BackupPrompt.Message -> {
            AlertDialog(
                onDismissRequest = { prompt = BackupPrompt.Hidden },
                title = { Text("Data backup") },
                text = { Text(current.text) },
                confirmButton = {
                    TextButton(onClick = { prompt = BackupPrompt.Hidden }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}
