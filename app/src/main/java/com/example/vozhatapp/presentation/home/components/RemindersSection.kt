package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.home.common.EmptyStateItem
import com.example.vozhatapp.presentation.home.model.ReminderItem

@Composable
fun RemindersSection(
    reminders: List<ReminderItem>,
    onSeeAllClick: () -> Unit,
    onReminderClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Напоминания",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onSeeAllClick) {
                Text("Все напоминания")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.Notifications,
                    message = "У вас нет активных напоминаний"
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reminders.take(3).forEach { reminder ->
                    // Передаем обработчик клика, который вызывает onReminderClick с ID напоминания
                    ReminderCardImproved(
                        reminder = reminder,
                        onClick = { onReminderClick(reminder.id) }
                    )
                }

                if (reminders.size > 3) {
                    TextButton(
                        onClick = onSeeAllClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Показать все ${reminders.size} напоминаний")
                    }
                }
            }
        }
    }
}