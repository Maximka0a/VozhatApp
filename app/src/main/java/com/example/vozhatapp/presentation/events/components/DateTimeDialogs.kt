package com.example.vozhatapp.presentation.events.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.vozhatapp.presentation.events.Edit.EventEditUiState
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeDialogs(
    uiState: EventEditUiState,
    onStartDateSelected: (LocalDate) -> Unit,
    onStartTimeSelected: (LocalTime) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    onStartDateDialogDismiss: () -> Unit,
    onStartTimeDialogDismiss: () -> Unit,
    onEndDateDialogDismiss: () -> Unit,
    onEndTimeDialogDismiss: () -> Unit
) {
    if (uiState.showStartDatePicker) {
        DatePickerDialog(
            initialDate = uiState.startDate,
            onDateSelected = onStartDateSelected,
            onDismiss = onStartDateDialogDismiss
        )
    }

    if (uiState.showStartTimePicker) {
        TimePickerDialog(
            initialTime = uiState.startTime,
            onTimeSelected = onStartTimeSelected,
            onDismiss = onStartTimeDialogDismiss
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerDialog(
            initialDate = uiState.endDate,
            onDateSelected = onEndDateSelected,
            onDismiss = onEndDateDialogDismiss
        )
    }

    if (uiState.showEndTimePicker) {
        TimePickerDialog(
            initialTime = uiState.endTime,
            onTimeSelected = onEndTimeSelected,
            onDismiss = onEndTimeDialogDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedDate = Instant
                        .ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(selectedDate)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                val selectedTime = LocalTime.of(
                    timePickerState.hour,
                    timePickerState.minute
                )
                onTimeSelected(selectedTime)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}