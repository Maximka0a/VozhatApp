package com.example.vozhatapp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Custom date range picker dialog component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var showStartDatePicker by remember { mutableStateOf(true) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    // Show the dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (showStartDatePicker) "Выберите начальную дату" else "Выберите конечную дату"
            )
        },
        text = {
            Column {
                // Date selection summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateChip(
                        label = "Начало",
                        date = startDate,
                        dateFormatter = dateFormatter,
                        isSelected = showStartDatePicker,
                        onClick = { showStartDatePicker = true; showEndDatePicker = false }
                    )

                    DateChip(
                        label = "Конец",
                        date = endDate,
                        dateFormatter = dateFormatter,
                        isSelected = showEndDatePicker,
                        onClick = { showEndDatePicker = true; showStartDatePicker = false }
                    )
                }

                // Date picker
                if (showStartDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = startDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    )

                    DatePicker(
                        state = datePickerState
                        // DateValidator is not available in the current Material3 version
                        // We'll handle validation after selection
                    )

                    // Update start date when selected
                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            // Ensure start date is not after end date
                            if (!date.isAfter(endDate)) {
                                startDate = date
                            }
                        }
                    }
                }

                if (showEndDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = endDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    )

                    DatePicker(
                        state = datePickerState
                        // DateValidator is not available in the current Material3 version
                        // We'll handle validation after selection
                    )

                    // Update end date when selected
                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            // Ensure end date is not before start date
                            if (!date.isBefore(startDate)) {
                                endDate = date
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateRangeSelected(startDate, endDate)
                }
            ) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChip(
    label: String,
    date: LocalDate,
    dateFormatter: DateTimeFormatter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text = date.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}