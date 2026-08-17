package com.maimonthlyhoppinings.ui.book

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
import com.maimonthlyhoppinings.data.Book
import com.maimonthlyhoppinings.ui.ConfirmDeleteDialog

private sealed interface BookPrompt {
    data object Hidden : BookPrompt
    data object Create : BookPrompt
    data class Rename(val book: Book) : BookPrompt
    data class Delete(val book: Book) : BookPrompt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var prompt by remember { mutableStateOf<BookPrompt>(BookPrompt.Hidden) }

    when (val current = prompt) {
        BookPrompt.Hidden -> Unit
        BookPrompt.Create -> {
            BookNameDialog(
                title = "New book",
                initialName = "",
                confirmLabel = "Create",
                onConfirm = { name ->
                    viewModel.create(name)
                    prompt = BookPrompt.Hidden
                },
                onDismiss = { prompt = BookPrompt.Hidden },
            )
        }
        is BookPrompt.Rename -> {
            BookNameDialog(
                title = "Rename book",
                initialName = current.book.name,
                confirmLabel = "Save",
                onConfirm = { name ->
                    viewModel.rename(current.book.id, name)
                    prompt = BookPrompt.Hidden
                },
                onDismiss = { prompt = BookPrompt.Hidden },
            )
        }
        is BookPrompt.Delete -> {
            ConfirmDeleteDialog(
                eventTitle = current.book.name,
                entityLabel = "book",
                onConfirm = {
                    viewModel.delete(current.book.id)
                    prompt = BookPrompt.Hidden
                },
                onDismiss = { prompt = BookPrompt.Hidden },
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Books") },
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
                onClick = { prompt = BookPrompt.Create },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New book",
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
                    text = "Each book is a separate journal on this phone. Switching does not mix events.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(state.books, key = { it.id }) { book ->
                BookRow(
                    book = book,
                    selected = book.id == state.active.id,
                    canDelete = state.canDelete,
                    onSelect = { viewModel.switchTo(book.id) },
                    onRename = { prompt = BookPrompt.Rename(book) },
                    onDelete = { prompt = BookPrompt.Delete(book) },
                )
            }
        }
    }
}

@Composable
private fun BookRow(
    book: Book,
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
                text = book.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = buildString {
                    if (book.isDefault) append("Default")
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
                contentDescription = "Book options",
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
