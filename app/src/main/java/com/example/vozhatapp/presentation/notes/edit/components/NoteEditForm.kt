package com.example.vozhatapp.presentation.notes.edit.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.notes.edit.NoteEditUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteEditForm(
    state: NoteEditUiState,
    titleFocusRequester: FocusRequester,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onNoteTypeChange: () -> Unit,
    onShowChildSelector: () -> Unit,
    onClearSelectedChild: () -> Unit,
    onShowDatePicker: () -> Unit,
    onClearReminderDate: () -> Unit,
    onSave: () -> Unit
) {
    val isReminder = state.noteType == 1
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Note Type Selector
        NoteTypeSelector(
            isReminder = isReminder,
            onNoteTypeChange = onNoteTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

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
        ChildSelectorCard(
            selectedChild = state.selectedChild,
            onClick = onShowChildSelector,
            onClear = onClearSelectedChild
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reminder date selector (visible only if note type is Reminder)
        AnimatedVisibility(
            visible = isReminder,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            DateTimeSelectorCard(
                reminderDate = state.reminderDate,
                onClick = onShowDatePicker,
                onClear = onClearReminderDate
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        Button(
            onClick = onSave,
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

@Composable
fun NoteTypeSelector(
    isReminder: Boolean,
    onNoteTypeChange: () -> Unit
) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSelectorCard(
    selectedChild: com.example.vozhatapp.data.local.entity.Child?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedChild != null)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                    text = selectedChild?.fullName ?: "Выберите ребенка (необязательно)",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (selectedChild != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Очистить выбор",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSelectorCard(
    reminderDate: Long?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val formattedDate = remember(reminderDate) {
        reminderDate?.let {
            SimpleDateFormat("dd MMMM yyyy в HH:mm", Locale.getDefault())
                .format(Date(it))
        } ?: "Выберите дату и время"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reminderDate != null)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (reminderDate != null) {
                IconButton(onClick = onClear) {
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