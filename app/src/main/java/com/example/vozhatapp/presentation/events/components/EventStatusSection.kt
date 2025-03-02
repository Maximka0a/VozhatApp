package com.example.vozhatapp.presentation.events.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EventStatusSection(
    currentStatus: Int,
    onStatusChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                StatusFilterChip(
                    status = 0,
                    currentStatus = currentStatus,
                    label = "Предстоит",
                    icon = Icons.Filled.Upcoming,
                    onStatusChange = onStatusChange
                )

                StatusFilterChip(
                    status = 1,
                    currentStatus = currentStatus,
                    label = "В процессе",
                    icon = Icons.Filled.RotateRight,
                    onStatusChange = onStatusChange
                )

                StatusFilterChip(
                    status = 2,
                    currentStatus = currentStatus,
                    label = "Завершено",
                    icon = Icons.Filled.Done,
                    onStatusChange = onStatusChange
                )
            }
        }
    }
}

@Composable
fun StatusFilterChip(
    status: Int,
    currentStatus: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onStatusChange: (Int) -> Unit
) {
    FilterChip(
        selected = currentStatus == status,
        onClick = { onStatusChange(status) },
        label = { Text(label, modifier = Modifier.padding(end = 8.dp)) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}