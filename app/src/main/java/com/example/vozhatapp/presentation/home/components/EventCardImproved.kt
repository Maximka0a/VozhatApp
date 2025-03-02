package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Event
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCardImproved(
    event: Event,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTime = remember(event.startTime) { dateFormatter.format(Date(event.startTime)) }
    val endTime = remember(event.endTime) { dateFormatter.format(Date(event.endTime)) }
    val currentTime = remember { System.currentTimeMillis() }

    val eventStatus = when {
        currentTime < event.startTime -> EventStatus.UPCOMING
        currentTime <= event.endTime -> EventStatus.IN_PROGRESS
        else -> EventStatus.COMPLETED
    }

    val statusColor = when(eventStatus) {
        EventStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        EventStatus.IN_PROGRESS -> Color(0xFF4CAF50) // Green
        EventStatus.COMPLETED -> MaterialTheme.colorScheme.outline
    }

    val statusText = when(eventStatus) {
        EventStatus.UPCOMING -> "Предстоит"
        EventStatus.IN_PROGRESS -> "В процессе"
        EventStatus.COMPLETED -> "Завершено"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Индикатор статуса
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                AssistChip(
                    onClick = { },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    ),
                    border = null,
                    label = { Text(statusText) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Время
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$startTime - $endTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (eventStatus == EventStatus.IN_PROGRESS) {
                    Spacer(modifier = Modifier.width(8.dp))

                    // Индикатор прогресса события
                    val progress = remember(event.startTime, event.endTime, currentTime) {
                        val totalDuration = event.endTime - event.startTime
                        val elapsed = currentTime - event.startTime
                        (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Описание
            if (!event.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Местоположение
            if (!event.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class EventStatus {
    UPCOMING, IN_PROGRESS, COMPLETED
}