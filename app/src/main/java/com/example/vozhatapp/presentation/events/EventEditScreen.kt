@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.vozhatapp.presentation.events

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun EventEditScreen(
    eventId: Long? = null,
    onNavigateBack: () -> Unit,
    onEventSaved: (Long) -> Unit,
    viewModel: EventEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Determine if we're in edit or add mode
    val isEditMode = eventId != null
    val screenTitle = if (isEditMode) "Редактирование события" else "Новое событие"

    // Load event data if in edit mode
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.loadEvent(eventId)
        } else {
            viewModel.initializeNewEvent()
        }
    }

    // Handle success message
    LaunchedEffect(uiState.savedEventId) {
        uiState.savedEventId?.let { savedId ->
            onEventSaved(savedId)
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Date and time picker dialogs
    if (uiState.showStartDatePicker) {
        DatePickerDialog(
            initialDate = uiState.startDate,
            onDateSelected = { viewModel.updateStartDate(it) },
            onDismiss = { viewModel.toggleStartDatePicker(false) }
        )
    }

    if (uiState.showStartTimePicker) {
        TimePickerDialog(
            initialTime = uiState.startTime,
            onTimeSelected = { viewModel.updateStartTime(it) },
            onDismiss = { viewModel.toggleStartTimePicker(false) }
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerDialog(
            initialDate = uiState.endDate,
            onDateSelected = { viewModel.updateEndDate(it) },
            onDismiss = { viewModel.toggleEndDatePicker(false) }
        )
    }

    if (uiState.showEndTimePicker) {
        TimePickerDialog(
            initialTime = uiState.endTime,
            onTimeSelected = { viewModel.updateEndTime(it) },
            onDismiss = { viewModel.toggleEndTimePicker(false) }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(screenTitle)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.saveEvent()
                        },
                        enabled = uiState.isValid
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Сохранить",
                            tint = if (uiState.isValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)  // Increased spacing between elements
            ) {
                // Title field
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Название события") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.titleError != null,
                    supportingText = {
                        uiState.titleError?.let { Text(it) }
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Event, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )

                // Date and time section - IMPROVED
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),  // Increased padding
                        verticalArrangement = Arrangement.spacedBy(20.dp)  // Increased spacing
                    ) {
                        Text(
                            text = "Дата и время",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Start date and time - IMPROVED LAYOUT
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Начало события:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleStartDatePicker(true) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val dateFormatter =
                                        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
                                    Text(uiState.startDate.format(dateFormatter))
                                }

                                OutlinedButton(
                                    onClick = { viewModel.toggleStartTimePicker(true) },
                                    modifier = Modifier.width(120.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                    Text(timeFormatter.format(uiState.startTime))
                                }
                            }
                        }

                        // End date and time - IMPROVED LAYOUT
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Окончание события:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleEndDatePicker(true) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val dateFormatter =
                                        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
                                    Text(uiState.endDate.format(dateFormatter))
                                }

                                OutlinedButton(
                                    onClick = { viewModel.toggleEndTimePicker(true) },
                                    modifier = Modifier.width(120.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                    Text(timeFormatter.format(uiState.endTime))
                                }
                            }
                        }

                        // Date validation error
                        uiState.dateTimeError?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Duration summary - IMPROVED VISIBILITY
                        val duration = Duration.between(
                            uiState.startDate.atTime(uiState.startTime),
                            uiState.endDate.atTime(uiState.endTime)
                        )
                        val hours = duration.toHours()
                        val minutes = duration.toMinutes() % 60

                        if (!duration.isNegative) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Продолжительность: ${formatDuration(hours, minutes)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Location field
                OutlinedTextField(
                    value = uiState.location ?: "",
                    onValueChange = { viewModel.updateLocation(it) },
                    label = { Text("Место проведения") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    supportingText = { Text("Необязательно") },
                    singleLine = true
                )

                // Description field - IMPROVED
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Описание",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.description ?: "",
                        onValueChange = { viewModel.updateDescription(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),  // Use heightIn instead of fixed height
                        placeholder = { Text("Добавьте описание события...") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        supportingText = { Text("Необязательно") }
                    )
                }
// Status section - VERTICAL LAYOUT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Статус события",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Vertical column of status chips
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Upcoming status
                            FilterChip(
                                selected = uiState.status == 0,
                                onClick = { viewModel.updateStatus(0) },
                                label = { Text("Предстоит", modifier = Modifier.padding(end = 8.dp)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Upcoming,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // In progress status
                            FilterChip(
                                selected = uiState.status == 1,
                                onClick = { viewModel.updateStatus(1) },
                                label = { Text("В процессе", modifier = Modifier.padding(end = 8.dp)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.RotateRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Completed status
                            FilterChip(
                                selected = uiState.status == 2,
                                onClick = { viewModel.updateStatus(2) },
                                label = { Text("Завершено", modifier = Modifier.padding(end = 8.dp)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                // Save button - IMPROVED
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.saveEvent()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.isValid,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))  // Increased space between icon and text
                    Text(if (isEditMode) "Сохранить изменения" else "Создать событие")
                }

                // Add some bottom space for better scrolling experience
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

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

    DatePickerDialog(
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

// Helper function to format duration
private fun formatDuration(hours: Long, minutes: Long): String {
    return when {
        hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
        hours > 0 -> "$hours ч"
        else -> "$minutes мин"
    }
}