package com.example.vozhatapp.presentation.events.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun EventDateTimeSection(
    startDate: LocalDate,
    startTime: LocalTime,
    endDate: LocalDate,
    endTime: LocalTime,
    dateTimeError: String?,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Дата и время",
                style = MaterialTheme.typography.titleMedium
            )

            // Start date and time
            DateTimeSelectionRow(
                label = "Начало события:",
                date = startDate,
                time = startTime,
                onDateClick = onStartDateClick,
                onTimeClick = onStartTimeClick
            )

            // End date and time
            DateTimeSelectionRow(
                label = "Окончание события:",
                date = endDate,
                time = endTime,
                onDateClick = onEndDateClick,
                onTimeClick = onEndTimeClick
            )

            // Date validation error
            dateTimeError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Duration summary
            DurationSummary(startDate, startTime, endDate, endTime)
        }
    }
}

@Composable
fun DateTimeSelectionRow(
    label: String,
    date: LocalDate,
    time: LocalTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDateClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val dateFormatter = remember {
                    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
                }
                Text(date.format(dateFormatter))
            }

            OutlinedButton(
                onClick = onTimeClick,
                modifier = Modifier.width(120.dp),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val timeFormatter = remember {
                    DateTimeFormatter.ofPattern("HH:mm")
                }
                Text(timeFormatter.format(time))
            }
        }
    }
}

@Composable
fun DurationSummary(
    startDate: LocalDate,
    startTime: LocalTime,
    endDate: LocalDate,
    endTime: LocalTime
) {
    val startDateTime = startDate.atTime(startTime)
    val endDateTime = endDate.atTime(endTime)
    val duration = Duration.between(startDateTime, endDateTime)

    if (!duration.isNegative) {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60

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

// Helper function to format duration
private fun formatDuration(hours: Long, minutes: Long): String {
    return when {
        hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
        hours > 0 -> "$hours ч"
        else -> "$minutes мин"
    }
}