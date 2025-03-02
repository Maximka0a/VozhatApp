package com.example.vozhatapp.presentation.events

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    onEditEvent: (Long) -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load event data
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
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

    // Status color animation
    val statusColor by animateColorAsState(
        targetValue = when (uiState.event?.status) {
            0 -> Color(0xFF2196F3) // Upcoming: Blue
            1 -> Color(0xFF4CAF50) // In progress: Green
            else -> Color(0xFF9E9E9E) // Completed: Gray
        },
        label = "statusColor"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = uiState.event?.title ?: "Детали события",
                        maxLines = 2
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
                    if (uiState.event != null) {
                        IconButton(onClick = { onEditEvent(eventId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleDeleteDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = statusColor.copy(alpha = 0.15f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
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
        } else if (uiState.event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventBusy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "Событие не найдено",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            val event = uiState.event!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status header
                StatusHeader(event.status)

                // Event details
                EventDetailsContent(
                    event = event,
                    onChangeStatus = { newStatus ->
                        viewModel.updateEventStatus(newStatus)
                    }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDeleteDialog() },
            title = { Text("Удаление события") },
            text = { Text("Вы уверены, что хотите удалить событие \"${uiState.event?.title}\"? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent()
                        viewModel.toggleDeleteDialog()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun StatusHeader(status: Int) {
    val (statusText, statusIcon, statusColor) = when (status) {
        0 -> Triple("Предстоящее", Icons.Outlined.Upcoming, Color(0xFF2196F3))
        1 -> Triple("В процессе", Icons.Outlined.RotateRight, Color(0xFF4CAF50))
        else -> Triple("Завершено", Icons.Outlined.Done, Color(0xFF9E9E9E))
    }

    Surface(
        color = statusColor.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun EventDetailsContent(
    event: com.example.vozhatapp.data.local.entity.Event,
    onChangeStatus: (Int) -> Unit
) {
    val startDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(event.startTime),
        ZoneId.systemDefault()
    )
    val endDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(event.endTime),
        ZoneId.systemDefault()
    )

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Date and time section
        EventDetailSection(
            title = "Дата и время",
            icon = Icons.Outlined.Schedule
        ) {
            // Using a Column instead of Row to prevent overlapping
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Start time section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )

                    Column {
                        Text(
                            text = "Начало:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = startDateTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = startDateTime.format(timeFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // End time section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )

                    Column {
                        Text(
                            text = "Окончание:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (startDateTime.toLocalDate() == endDateTime.toLocalDate())
                                "В тот же день"
                            else
                                endDateTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = endDateTime.format(timeFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Duration section with divider above
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                val duration = Duration.between(startDateTime, endDateTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )

                    Text(
                        text = "Продолжительность: ${formatDuration(hours, minutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }


        // Location section if available
        if (!event.location.isNullOrEmpty()) {
            EventDetailSection(
                title = "Место проведения",
                icon = Icons.Outlined.LocationOn
            ) {
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Description section if available
        if (!event.description.isNullOrEmpty()) {
            EventDetailSection(
                title = "Описание",
                icon = Icons.Outlined.Description
            ) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Status change section
        EventDetailSection(
            title = "Статус события",
            icon = Icons.Outlined.Update
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatusButton(
                    text = "Предстоит",
                    icon = Icons.Outlined.Upcoming,
                    color = Color(0xFF2196F3),
                    isSelected = event.status == 0,
                    onClick = { onChangeStatus(0) }
                )

                StatusButton(
                    text = "В процессе",
                    icon = Icons.Outlined.RotateRight,
                    color = Color(0xFF4CAF50),
                    isSelected = event.status == 1,
                    onClick = { onChangeStatus(1) }
                )

                StatusButton(
                    text = "Завершено",
                    icon = Icons.Outlined.Done,
                    color = Color(0xFF9E9E9E),
                    isSelected = event.status == 2,
                    onClick = { onChangeStatus(2) }
                )
            }
        }

        // Created info
        val createdDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.createdAt),
            ZoneId.systemDefault()
        )

        Text(
            text = "Создано: ${createdDateTime.format(dateFormatter)} в ${createdDateTime.format(timeFormatter)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EventDetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun StatusButton(
    text: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isSelected) color else color.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Helper function
private fun formatDuration(hours: Long, minutes: Long): String {
    return when {
        hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
        hours > 0 -> "$hours ч"
        else -> "$minutes мин"
    }
}