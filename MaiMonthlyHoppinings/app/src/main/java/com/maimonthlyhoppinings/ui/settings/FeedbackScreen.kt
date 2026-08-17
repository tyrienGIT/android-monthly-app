package com.maimonthlyhoppinings.ui.settings

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maimonthlyhoppinings.data.FeedbackNote
import com.maimonthlyhoppinings.data.prefixSelectedLines
import com.maimonthlyhoppinings.data.toggleHeading
import com.maimonthlyhoppinings.data.wrapInline
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.openNoteId == null) {
        FeedbackListPane(
            notes = state.notes,
            onOpen = viewModel::open,
            onCreate = viewModel::create,
            onDelete = viewModel::delete,
            onBack = onBack,
        )
    } else {
        FeedbackEditorPane(
            state = state,
            onDraftChanged = viewModel::onDraftChanged,
            onSave = viewModel::save,
            onClose = { viewModel.closeEditor(saveIfDirty = true) },
            onDelete = {
                state.openNoteId?.let(viewModel::delete)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackListPane(
    notes: List<FeedbackNote>,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<FeedbackNote?>(null) }
    pendingDelete?.let { note ->
        ConfirmDeleteDialog(
            eventTitle = note.title,
            entityLabel = "note",
            onConfirm = {
                onDelete(note.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback") },
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
            FloatingActionButton(onClick = onCreate) {
                Icon(Icons.Default.Add, contentDescription = "New note")
            }
        },
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Write a note, save it on this phone, and share it when you want.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(notes, key = { it.id }) { note ->
                    FeedbackNoteRow(
                        note = note,
                        onClick = { onOpen(note.id) },
                        onDelete = { pendingDelete = note },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackNoteRow(
    note: FeedbackNote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = noteDateLabel(note.updatedAtMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete note",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackEditorPane(
    state: FeedbackUiState,
    onDraftChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    var field by remember(state.openNoteId) { mutableStateOf(TextFieldValue(state.draft)) }
    var preview by remember(state.openNoteId) { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(state.savedDraft, state.justSaved) {
        if (state.justSaved && field.text != state.savedDraft) {
            field = TextFieldValue(state.savedDraft)
        }
    }

    BackHandler(onBack = onClose)

    if (pendingDelete) {
        ConfirmDeleteDialog(
            eventTitle = state.openNote?.title ?: "Untitled note",
            entityLabel = "note",
            onConfirm = {
                pendingDelete = false
                onDelete()
            },
            onDismiss = { pendingDelete = false },
        )
    }

    fun apply(transform: TextFieldValue.() -> TextFieldValue) {
        val next = field.transform()
        field = next
        onDraftChanged(next.text)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.dirty) "Note" else state.openNote?.title ?: "Note") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { pendingDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(
                        onClick = onSave,
                        enabled = state.dirty,
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                    IconButton(
                        onClick = { shareMarkdown(context, field.text) },
                        enabled = field.text.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = when {
                    state.justSaved -> "Saved on this phone as markdown."
                    state.dirty -> "Unsaved changes."
                    else -> "Write a note, save it, then share when you want."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !preview,
                    onClick = { preview = false },
                    label = { Text("Write") },
                )
                FilterChip(
                    selected = preview,
                    onClick = { preview = true },
                    label = { Text("Preview") },
                )
            }
            if (!preview) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = { apply { wrapInline("**") } }) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                    }
                    TextButton(onClick = { apply { wrapInline("*") } }) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                    }
                    TextButton(onClick = { apply { toggleHeading() } }) {
                        Icon(Icons.Default.Title, contentDescription = "Heading")
                    }
                    TextButton(onClick = { apply { prefixSelectedLines("- ") } }) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "List")
                    }
                    TextButton(onClick = { apply { prefixSelectedLines("> ") } }) {
                        Icon(Icons.Default.FormatQuote, contentDescription = "Quote")
                    }
                }
                OutlinedTextField(
                    value = field,
                    onValueChange = {
                        field = it
                        onDraftChanged(it.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    placeholder = { Text("What worked, what didn’t, what you expected…") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            } else {
                MarkdownPreview(
                    markdown = field.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onSave,
                    enabled = state.dirty,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Save")
                }
                FilledTonalButton(
                    onClick = { shareMarkdown(context, field.text) },
                    enabled = field.text.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text("Share")
                }
            }
        }
    }
}

private fun shareMarkdown(context: android.content.Context, markdown: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Mai Monthly Hoppinings feedback")
        putExtra(Intent.EXTRA_TEXT, markdown)
    }
    context.startActivity(Intent.createChooser(send, "Share feedback"))
}

private val noteDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault())

private fun noteDateLabel(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(noteDateFormatter)
}
