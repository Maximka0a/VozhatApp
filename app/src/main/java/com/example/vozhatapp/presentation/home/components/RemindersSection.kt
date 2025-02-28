package com.example.vozhatapp.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.home.common.EmptyStateItem
import com.example.vozhatapp.presentation.home.common.SectionWithTitle
import com.example.vozhatapp.presentation.home.model.ReminderItem

@Composable
fun RemindersSection(reminders: List<ReminderItem>) {
    SectionWithTitle(
        title = "Предстоящие напоминания",
        showSeeAllButton = reminders.size > 3,
        onSeeAllClick = {}
    ) {
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.Notifications,
                    message = "Нет предстоящих напоминаний",
                    actionText = "Создать напоминание",
                    onActionClick = {}
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reminders.forEachIndexed { index, reminder ->
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(key1 = Unit) {
                        kotlinx.coroutines.delay(300 + index * 100L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + expandVertically()
                    ) {
                        ReminderCard(reminder = reminder)
                    }
                }
            }
        }
    }
}
