package com.maimonthlyhoppinings.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmSaveDialog(
    eventTitle: String,
    isEditing: Boolean = false,
    entityLabel: String = "event",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Save changes?" else "Save $entityLabel?",
            )
        },
        text = {
            Text(
                if (isEditing) {
                    "Are you sure you want to save changes to \"$eventTitle\"?"
                } else {
                    "Are you sure you want to save \"$eventTitle\"?"
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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
