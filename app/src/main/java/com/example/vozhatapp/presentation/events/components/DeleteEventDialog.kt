package com.example.vozhatapp.presentation.events.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun DeleteEventDialog(
    eventTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удаление события") },
        text = { Text("Вы уверены, что хотите удалить событие \"$eventTitle\"? Это действие нельзя отменить.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}