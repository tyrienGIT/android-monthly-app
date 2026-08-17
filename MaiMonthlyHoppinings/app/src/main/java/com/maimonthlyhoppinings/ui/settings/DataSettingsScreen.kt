package com.maimonthlyhoppinings.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.AutoBackupFrequency
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

private fun backupFileName(bookName: String): String {
    val slug = bookName.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifEmpty { "book" }
    return "$slug-backup.json"
}

private sealed interface BackupPrompt {
    data object Hidden : BackupPrompt
    data class ChooseImport(val json: String) : BackupPrompt
    data class ConfirmReplace(val json: String) : BackupPrompt
    data class Message(val text: String) : BackupPrompt
}

private val retainMonthOptions = listOf(1, 2, 3, 6, 12)
private val maxCountOptions = listOf(30, 60, 90, 180)
private val frequencyPresets = listOf(
    AutoBackupFrequency.EVERY_OPEN,
    AutoBackupFrequency.DAILY,
    AutoBackupFrequency.EVERY_3_DAYS,
    AutoBackupFrequency.WEEKLY,
)
private val lastBackupFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

private sealed interface NumberPrompt {
    data object Hidden : NumberPrompt
    data object FrequencyDays : NumberPrompt
    data object RetainMonths : NumberPrompt
    data object MaxCount : NumberPrompt
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DataSettingsScreen(
    viewModel: DataBackupViewModel,
    bookName: String,
    onBack: () -> Unit,
) {
    val autoBackup by viewModel.autoBackup.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf<BackupPrompt>(BackupPrompt.Hidden) }
    var numberPrompt by remember { mutableStateOf<NumberPrompt>(NumberPrompt.Hidden) }
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
                        subtitle = "Save a JSON backup of this book’s events, types, and themes",
                        onClick = {
                            scope.launch {
                                try {
                                    pendingExportJson = viewModel.export()
                                    createDocument.launch(backupFileName(bookName))
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
                    title = "Automatic backup",
                    subtitle = if (autoBackup.enabled) {
                        val last = if (autoBackup.lastBackupEpochDay >= 0L) {
                            "Last copy ${LocalDate.ofEpochDay(autoBackup.lastBackupEpochDay).format(lastBackupFormatter)}. "
                        } else {
                            ""
                        }
                        last + "Runs when you open the app, if the interval has passed. Stays on this device."
                    } else {
                        "Write a JSON copy when you open the app, on the interval you pick."
                    },
                    checked = autoBackup.enabled,
                    onCheckedChange = viewModel::setAutoBackupEnabled,
                )
            }
            if (autoBackup.enabled) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Frequency",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "How often a new copy is written.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            frequencyPresets.forEach { frequency ->
                                FilterChip(
                                    selected = autoBackup.frequency == frequency,
                                    onClick = { viewModel.setFrequency(frequency) },
                                    label = { Text(frequency.label) },
                                )
                            }
                            FilterChip(
                                selected = autoBackup.frequency == AutoBackupFrequency.CUSTOM,
                                onClick = { numberPrompt = NumberPrompt.FrequencyDays },
                                label = {
                                    Text(
                                        if (autoBackup.frequency == AutoBackupFrequency.CUSTOM) {
                                            if (autoBackup.frequencyDays == 1) {
                                                "Every 1 day"
                                            } else {
                                                "Every ${autoBackup.frequencyDays} days"
                                            }
                                        } else {
                                            "Other"
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
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
                            val customRetain = autoBackup.retainMonths !in retainMonthOptions
                            FilterChip(
                                selected = customRetain,
                                onClick = { numberPrompt = NumberPrompt.RetainMonths },
                                label = {
                                    Text(
                                        if (customRetain) {
                                            if (autoBackup.retainMonths == 1) "1 month" else "${autoBackup.retainMonths} months"
                                        } else {
                                            "Other"
                                        },
                                    )
                                },
                            )
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
                            val customMax = autoBackup.maxCount !in maxCountOptions
                            FilterChip(
                                selected = customMax,
                                onClick = { numberPrompt = NumberPrompt.MaxCount },
                                label = {
                                    Text(if (customMax) "${autoBackup.maxCount}" else "Other")
                                },
                            )
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
                        "This only changes the open book. Merge keeps its items and updates " +
                            "matching IDs. Replace all deletes items in this book that are not in the file.",
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
                title = { Text("Replace this book?") },
                text = {
                    Text(
                        "Events, entries, types, and custom themes in this book that are not " +
                            "in this backup will be deleted. Other books are left alone. This can't be undone.",
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

    when (numberPrompt) {
        NumberPrompt.Hidden -> Unit
        NumberPrompt.FrequencyDays -> {
            AutoBackupNumberDialog(
                title = "Every how many days?",
                initial = autoBackup.frequencyDays.coerceAtLeast(1),
                min = 1,
                max = 3_650,
                onConfirm = { days ->
                    viewModel.setFrequency(AutoBackupFrequency.CUSTOM, days)
                    numberPrompt = NumberPrompt.Hidden
                },
                onDismiss = { numberPrompt = NumberPrompt.Hidden },
            )
        }
        NumberPrompt.RetainMonths -> {
            AutoBackupNumberDialog(
                title = "Keep for how many months?",
                initial = autoBackup.retainMonths,
                min = 1,
                max = 240,
                onConfirm = { months ->
                    viewModel.setRetainMonths(months)
                    numberPrompt = NumberPrompt.Hidden
                },
                onDismiss = { numberPrompt = NumberPrompt.Hidden },
            )
        }
        NumberPrompt.MaxCount -> {
            AutoBackupNumberDialog(
                title = "How many copies?",
                initial = autoBackup.maxCount,
                min = 1,
                max = 9_999,
                onConfirm = { count ->
                    viewModel.setMaxCount(count)
                    numberPrompt = NumberPrompt.Hidden
                },
                onDismiss = { numberPrompt = NumberPrompt.Hidden },
            )
        }
    }
}

@Composable
private fun AutoBackupNumberDialog(
    title: String,
    initial: Int,
    min: Int,
    max: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(initial) { mutableStateOf(initial.toString()) }
    val parsed = text.toIntOrNull()
    val valid = parsed != null && parsed in min..max
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { candidate ->
                    if (candidate.length <= 5 && candidate.all { it.isDigit() }) {
                        text = candidate
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onConfirm) },
                enabled = valid,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
