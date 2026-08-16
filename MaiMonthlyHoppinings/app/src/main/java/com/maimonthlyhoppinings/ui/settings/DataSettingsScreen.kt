package com.maimonthlyhoppinings.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.maimonthlyhoppinings.data.BackupFile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    viewModel: DataBackupViewModel,
    onBack: () -> Unit,
) {
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
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SettingsSectionHeader("Backup") }
            item {
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
            }
            item {
                SettingsNavRow(
                    title = "Import",
                    subtitle = "Restore from a JSON backup",
                    onClick = {
                        openDocument.launch(
                            arrayOf("application/json", "application/octet-stream", "text/*"),
                        )
                    },
                )
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
