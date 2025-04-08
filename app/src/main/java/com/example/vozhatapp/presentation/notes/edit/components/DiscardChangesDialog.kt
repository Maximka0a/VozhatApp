package com.example.vozhatapp.presentation.notes.edit.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отменить изменения?") },
        text = { Text("Все несохраненные изменения будут потеряны.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Отменить изменения")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Продолжить редактирование")
            }
        }
    )
}