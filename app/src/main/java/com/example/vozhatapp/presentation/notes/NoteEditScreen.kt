package com.example.vozhatapp.presentation.notes

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.data.local.entity.Child
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long = -1, // -1 means create new note
    onNavigateBack: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val titleFocusRequester = remember { FocusRequester() }

    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            viewModel.loadNote(noteId)
        } else {
            // Focus on title field when creating new note
            delay(300) // Small delay to ensure the UI is ready
            titleFocusRequester.requestFocus()
        }
    }

    // Handle snackbar messages
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    // Handle navigation after save
    LaunchedEffect(state.noteSaved) {
        if (state.noteSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId > 0) "Редактировать заметку" else "Новая заметка"
                    )
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
                        onClick = { viewModel.saveNote() },
                        enabled = state.title.isNotBlank() && state.content.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Сохранить"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                NoteEditForm(
                    state = state,
                    titleFocusRequester = titleFocusRequester,
                    onTitleChange = { viewModel.updateTitle(it) },
                    onContentChange = { viewModel.updateContent(it) },
                    onNoteTypeChange = { viewModel.toggleNoteType() },
                    onShowChildSelector = { viewModel.toggleChildSelectorDialog() },
                    onClearSelectedChild = { viewModel.clearSelectedChild() },
                    onShowDatePicker = { viewModel.toggleDatePickerDialog() },
                    onClearReminderDate = { viewModel.clearReminderDate() }
                )
            }

            // Child selector dialog
            if (state.showChildSelector) {
                ChildSelectorDialog(
                    children = state.availableChildren,
                    searchQuery = state.childSearchQuery,
                    onSearchQueryChange = { viewModel.updateChildSearchQuery(it) },
                    onChildSelect = { viewModel.selectChild(it) },
                    onDismiss = { viewModel.toggleChildSelectorDialog() }
                )
            }

            // Date picker dialog
            if (state.showDatePicker) {
                DateTimePickerDialog(
                    initialDateTimeMillis = state.reminderDate
                        ?: System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Default to tomorrow
                    onDateTimeSelected = { viewModel.setReminderDate(it) },
                    onDismiss = { viewModel.toggleDatePickerDialog() }
                )
            }

            // Discard changes confirmation dialog
            if (state.showDiscardDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideDiscardDialog() },
                    title = { Text("Отменить изменения?") },
                    text = { Text("Все несохраненные изменения будут потеряны.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.hideDiscardDialog()
                                onNavigateBack()
                            }
                        ) {
                            Text("Отменить изменения")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.hideDiscardDialog() }) {
                            Text("Продолжить редактирование")
                        }
                    }
                )
            }
        }
    }

    // Handle back button logic
    BackHandler(enabled = state.hasChanges) {
        viewModel.showDiscardDialog()
    }
}

@Composable
private fun NoteEditForm(
    state: NoteEditUiState,
    titleFocusRequester: FocusRequester,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onNoteTypeChange: () -> Unit,
    onShowChildSelector: () -> Unit,
    onClearSelectedChild: () -> Unit,
    onShowDatePicker: () -> Unit,
    onClearReminderDate: () -> Unit
) {
    val isReminder = state.noteType == 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Note Type Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = !isReminder,
                onClick = { if (isReminder) onNoteTypeChange() },
                label = { Text("Заметка") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )

            FilterChip(
                selected = isReminder,
                onClick = { if (!isReminder) onNoteTypeChange() },
                label = { Text("Напоминание") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )
        }

        // Title field
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Заголовок") },
            placeholder = { Text("Введите заголовок") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocusRequester),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content field
        OutlinedTextField(
            value = state.content,
            onValueChange = onContentChange,
            label = { Text("Содержание") },
            placeholder = { Text("Введите текст заметки") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Related Child selector
        Card(
            onClick = onShowChildSelector,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.selectedChild != null)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Ребенок",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = state.selectedChild?.fullName ?: "Выберите ребенка (необязательно)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (state.selectedChild != null) {
                    IconButton(onClick = onClearSelectedChild) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить выбор",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reminder date selector (visible only if note type is Reminder)
        AnimatedVisibility(
            visible = isReminder,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                onClick = onShowDatePicker,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.reminderDate != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Дата и время напоминания",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val formattedDate = remember(state.reminderDate) {
                            state.reminderDate?.let {
                                SimpleDateFormat("dd MMMM yyyy в HH:mm", Locale.getDefault())
                                    .format(Date(it))
                            } ?: "Выберите дату и время"
                        }

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (state.reminderDate != null) {
                        IconButton(onClick = onClearReminderDate) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Очистить дату",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        Button(
            onClick = { /* Use the save action from the top app bar */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.title.isNotBlank() && state.content.isNotBlank() &&
                    (!isReminder || state.reminderDate != null)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Сохранить")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildSelectorDialog(
    children: List<Child>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onChildSelect: (Child) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Выберите ребенка",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Поиск по имени") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter children by search query
                val filteredChildren = remember(children, searchQuery) {
                    if (searchQuery.isBlank()) {
                        children
                    } else {
                        val query = searchQuery.lowercase()
                        children.filter { child ->
                            child.fullName.lowercase().contains(query)
                        }
                    }
                }

                // Show empty state if no children match the search
                if (filteredChildren.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (children.isEmpty()) {
                                "Нет доступных детей"
                            } else {
                                "Нет детей, соответствующих запросу"
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Child list
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredChildren) { child ->
                            ChildListItem(
                                child = child,
                                onClick = {
                                    onChildSelect(child)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}

@Composable
private fun ChildListItem(
    child: Child,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder or image
            Surface(
                modifier = Modifier
                    .size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = child.name.firstOrNull()?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Child details
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = child.fullName,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Возраст: ${child.age} лет • ${child.squadName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun DateTimePickerDialog(
    initialDateTimeMillis: Long,
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDateTimeMillis
    }

    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    var showDatePicker by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (showDatePicker) "Выберите дату" else "Выберите время",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (showDatePicker) {
                    // Simple calendar date picker implementation
                    // In a real app, you would use the Material date picker
                    // This is a simplified version for the example
                    CalendarView(
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        selectedDay = selectedDay,
                        onDateSelected = { year, month, day ->
                            selectedYear = year
                            selectedMonth = month
                            selectedDay = day
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена")
                        }

                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Далее")
                        }
                    }
                } else {
                    // Simple time picker implementation
                    TimePickerView(
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        onTimeSelected = { hour, minute ->
                            selectedHour = hour
                            selectedMinute = minute
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Назад")
                        }

                        TextButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, selectedYear)
                                    set(Calendar.MONTH, selectedMonth)
                                    set(Calendar.DAY_OF_MONTH, selectedDay)
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onDateTimeSelected(cal.timeInMillis)
                                onDismiss()
                            }
                        ) {
                            Text("Готово")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarView(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDateSelected: (Int, Int, Int) -> Unit
) {
    // Simplified calendar implementation
    // In a real app, use a proper calendar component
    Text(
        text = "Выбрана дата: $selectedDay/${selectedMonth + 1}/$selectedYear",
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Year selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Год:", modifier = Modifier.width(50.dp))

        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { year ->
                    if (year in 2020..2100) {
                        onDateSelected(year, selectedMonth, selectedDay)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Month selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Месяц:", modifier = Modifier.width(50.dp))

        val monthNames = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

        OutlinedTextField(
            value = monthNames[selectedMonth],
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {
                            val newMonth = if (selectedMonth > 0) selectedMonth - 1 else 11
                            onDateSelected(selectedYear, newMonth, selectedDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий")
                    }

                    IconButton(
                        onClick = {
                            val newMonth = if (selectedMonth < 11) selectedMonth + 1 else 0
                            onDateSelected(selectedYear, newMonth, selectedDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий")
                    }
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Day selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("День:", modifier = Modifier.width(50.dp))

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        OutlinedTextField(
            value = selectedDay.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { day ->
                    if (day in 1..daysInMonth) {
                        onDateSelected(selectedYear, selectedMonth, day)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {
                            val newDay = if (selectedDay > 1) selectedDay - 1 else daysInMonth
                            onDateSelected(selectedYear, selectedMonth, newDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий")
                    }

                    IconButton(
                        onClick = {
                            val newDay = if (selectedDay < daysInMonth) selectedDay + 1 else 1
                            onDateSelected(selectedYear, selectedMonth, newDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий")
                    }
                }
            }
        )
    }
}

@Composable
private fun TimePickerView(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    // Simple time picker
    Text(
        text = "Выбрано время: ${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hour picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Часы")

            IconButton(
                onClick = {
                    val newHour = if (selectedHour < 23) selectedHour + 1 else 0
                    onTimeSelected(newHour, selectedMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Увеличить")
            }

            Text(
                text = selectedHour.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(
                onClick = {
                    val newHour = if (selectedHour > 0) selectedHour - 1 else 23
                    onTimeSelected(newHour, selectedMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Уменьшить")
            }
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Minute picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Минуты")

            IconButton(
                onClick = {
                    val newMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                    onTimeSelected(selectedHour, newMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Увеличить")
            }

            Text(
                text = selectedMinute.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(
                onClick = {
                    val newMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                    onTimeSelected(selectedHour, newMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Уменьшить")
            }
        }
    }
}

@Composable
private fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val backCallback = remember {
        object : androidx.activity.OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Update the callback's enabled state
    LaunchedEffect(enabled) {
        backCallback.isEnabled = enabled
    }

    DisposableEffect(backDispatcher) {
        backDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }
}