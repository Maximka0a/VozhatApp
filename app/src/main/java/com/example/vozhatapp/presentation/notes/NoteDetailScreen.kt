package com.example.vozhatapp.presentation.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToChild: (Long) -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isLoading) "Загрузка..." else
                            if (state.note?.type == 1) "Напоминание" else "Заметка"
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
                    IconButton(onClick = { onNavigateToEdit(noteId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать"
                        )
                    }

                    IconButton(onClick = { viewModel.deleteNote() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
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
            } else if (state.note == null) {
                Text(
                    text = "Заметка не найдена",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                NoteDetailContent(
                    state = state,
                    onChildClick = onNavigateToChild,
                    onDeleteConfirm = { viewModel.confirmDelete() }
                )
            }

            // Delete confirmation dialog
            if (state.showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideDeleteDialog() },
                    title = { Text("Удалить заметку?") },
                    text = { Text("Эта операция не может быть отменена.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.confirmDelete()
                                onNavigateBack()
                            }
                        ) {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NoteDetailContent(
    state: NoteDetailUiState,
    onChildClick: (Long) -> Unit,
    onDeleteConfirm: () -> Unit
) {
    val note = state.note ?: return
    val childName = state.childName
    val isReminder = note.type == 1

    val createdDate = remember(note.createdAt) {
        SimpleDateFormat("dd MMMM yyyy в HH:mm", Locale.getDefault())
            .format(Date(note.createdAt))
    }

    val reminderDate = remember(note.reminderDate) {
        note.reminderDate?.let {
            SimpleDateFormat("dd MMMM yyyy в HH:mm", Locale.getDefault())
                .format(Date(it))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Type badge
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .background(
                    color = if (isReminder)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isReminder) Icons.Default.Alarm else Icons.Default.Notes,
                    contentDescription = null,
                    tint = if (isReminder)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (isReminder) "Напоминание" else "Заметка",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isReminder)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onTertiary
                )
            }
        }

        // Title
        Text(
            text = note.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Creation info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "Создано $createdDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Child info if available
        if (childName != null) {
            Spacer(modifier = Modifier.height(12.dp))

            ElevatedCard(
                onClick = { note.childId?.let { onChildClick(it) } },
                modifier = Modifier.fillMaxWidth()
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
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = "Ребенок",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = childName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Подробнее",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Reminder date if applicable
        if (reminderDate != null) {
            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                icon = Icons.Default.Schedule,
                title = "Дата напоминания",
                content = reminderDate,
                iconTint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main content
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Delete button
        Button(
            onClick = onDeleteConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Удалить заметку")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}