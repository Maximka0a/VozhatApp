package com.example.vozhatapp.presentation.childprofile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Attendance
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceTab(
    attendance: List<Attendance>,
    attendanceRate: Float,
    modifier: Modifier = Modifier
) {
    if (attendance.isEmpty()) {
        EmptyStateView(contentType = EmptyContentType.NO_ATTENDANCE)
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            // Attendance chart
            AttendanceChart(attendanceRate = attendanceRate)

            // Attendance history
            Text(
                text = "История посещений",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyColumn {
                items(
                    items = attendance,
                    key = { it.id }
                ) { record ->
                    AttendanceItem(attendance = record)
                }
            }
        }
    }
}

@Composable
fun AttendanceChart(
    attendanceRate: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(attendanceRate) {
        animatedProgress.animateTo(
            targetValue = attendanceRate,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }

    // Получение цветов вне remember
    val progressColor = when {
        attendanceRate < 0.5f -> MaterialTheme.colorScheme.error
        attendanceRate < 0.75f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Посещаемость",
                style = MaterialTheme.typography.titleMedium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(progressColor)
                        .align(Alignment.CenterStart)
                )

                // Percentage text
                Text(
                    text = "${(animatedProgress.value * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 40.dp)
                )
            }
        }
    }
}

@Composable
fun AttendanceItem(
    attendance: Attendance,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val date = remember(attendance.markedAt) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(attendance.markedAt),
            ZoneId.systemDefault()
        ).format(dateFormatter)
    }

    val containerColor = if (attendance.isPresent)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (attendance.isPresent)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Cancel,
                contentDescription = if (attendance.isPresent) "Присутствовал" else "Отсутствовал",
                tint = if (attendance.isPresent)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = if (attendance.isPresent) "Присутствовал" else "Отсутствовал",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (!attendance.note.isNullOrBlank()) {
                    Text(
                        text = attendance.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}