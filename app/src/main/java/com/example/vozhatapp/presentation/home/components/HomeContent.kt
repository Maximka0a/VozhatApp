package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.home.HomeUiState

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    uiState: HomeUiState,
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToChildDetails: (Long) -> Unit,
    onNavigateToEventDetails: (Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Добавляем отступ снизу для FAB
    ) {
        // Карточки для быстрой навигации
        NavigationCards(
            onNavigateToEvents = onNavigateToEvents,
            onNavigateToChildren = onNavigateToChildren,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToNotes = onNavigateToNotes,
            onNavigateToGames = onNavigateToGames
        )

        // Секция с сегодняшними событиями
        TodayEventsSection(
            events = uiState.todayEvents,
            onEventClick = onNavigateToEventDetails
        )

        // Секция с детьми
        TopChildrenSection(
            children = uiState.topChildren,
            onChildClick = onNavigateToChildDetails
        )

        // Секция с предстоящими напоминаниями
        RemindersSection(reminders = uiState.upcomingReminders)

        // Индикатор загрузки внизу экрана вместо pull-to-refresh
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
