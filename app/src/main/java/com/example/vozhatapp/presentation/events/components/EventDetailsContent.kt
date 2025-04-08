package com.example.vozhatapp.presentation.events.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Event
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@Composable
fun EventDetailsContent(
    event: Event,
    onChangeStatus: (Int) -> Unit
) {
    // Create and remember formatters once to avoid recreation on recompositions
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru")) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // Convert event timestamps to LocalDateTime only once and remember the result
    val startDateTime = remember(event.startTime) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.startTime),
            ZoneId.systemDefault()
        )
    }

    val endDateTime = remember(event.endTime) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.endTime),
            ZoneId.systemDefault()
        )
    }

    val createdDateTime = remember(event.createdAt) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.createdAt),
            ZoneId.systemDefault()
        )
    }

    // Calculate duration once and remember the result
    val duration = remember(startDateTime, endDateTime) {
        Duration.between(startDateTime, endDateTime)
    }
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

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
            DateTimeContent(
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                dateFormatter = dateFormatter,
                timeFormatter = timeFormatter,
                duration = Pair(hours, minutes)
            )
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
            StatusButtonsRow(
                currentStatus = event.status,
                onChangeStatus = onChangeStatus
            )
        }

        // Created info
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
private fun DateTimeContent(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    duration: Pair<Long, Long>
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
                text = "Продолжительность: ${formatDuration(duration.first, duration.second)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusButtonsRow(
    currentStatus: Int,
    onChangeStatus: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatusButton(
            text = "Предстоит",
            icon = Icons.Outlined.Upcoming,
            color = Color(0xFF2196F3),
            isSelected = currentStatus == 0,
            onClick = { onChangeStatus(0) }
        )

        StatusButton(
            text = "В процессе",
            icon = Icons.Outlined.RotateRight,
            color = Color(0xFF4CAF50),
            isSelected = currentStatus == 1,
            onClick = { onChangeStatus(1) }
        )

        StatusButton(
            text = "Завершено",
            icon = Icons.Outlined.Done,
            color = Color(0xFF9E9E9E),
            isSelected = currentStatus == 2,
            onClick = { onChangeStatus(2) }
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