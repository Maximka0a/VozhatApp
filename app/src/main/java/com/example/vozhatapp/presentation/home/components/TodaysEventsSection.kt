package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.presentation.home.common.EmptyStateItem

@Composable
fun TodaysEventsSection(
    events: List<Event>,
    onEventClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit
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
                text = "События сегодня",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onSeeAllClick) {
                Text("Все события")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.EventBusy,
                    message = "На сегодня нет запланированных событий"
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.take(3).forEach { event ->
                    EventCardImproved(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }

                if (events.size > 3) {
                    TextButton(
                        onClick = onSeeAllClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Показать все ${events.size} событий")
                    }
                }
            }
        }
    }
}