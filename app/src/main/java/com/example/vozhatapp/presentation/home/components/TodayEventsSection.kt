package com.example.vozhatapp.presentation.home.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.presentation.home.common.EmptyStateItem
import com.example.vozhatapp.presentation.home.common.SectionWithTitle


@Composable
fun TodayEventsSection(
    events: List<Event>,
    onEventClick: (Long) -> Unit
) {
    Log.d("TodayEventsSection", "Отображение секции событий: получено ${events.size} событий")

    SectionWithTitle(
        title = "Сегодняшние события",
        showSeeAllButton = events.size > 3,
        onSeeAllClick = {}
    ) {
        if (events.isEmpty()) {
            // Показываем сообщение, что нет событий
            Log.d("TodayEventsSection", "Список событий пуст, показываем заглушку")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.EventBusy,
                    message = "На сегодня нет запланированных событий",
                    actionText = "Создать событие",
                    onActionClick = {}
                )
            }
        } else {
            // Надежный вывод событий без анимации
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEach { event ->
                    Log.d("TodayEventsSection", "Отображаем событие: ${event.title}")

                    EventCard(
                        event = event,
                        onClick = {
                            Log.d("TodayEventsSection", "Клик по событию ID: ${event.id}")
                            onEventClick(event.id)
                        }
                    )
                }
            }
        }
    }
}