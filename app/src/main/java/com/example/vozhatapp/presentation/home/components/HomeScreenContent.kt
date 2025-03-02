@file:OptIn(ExperimentalMaterialApi::class)

package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.example.vozhatapp.presentation.home.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToChildDetails: (Long) -> Unit,
    onNavigateToEventDetails: (Long) -> Unit,
    onNavigateToNoteDetails: (Long) -> Unit  // Добавлен новый параметр
) {
    // Настройка состояния для pull-to-refresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = onRefresh
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)  // Применяем pull-to-refresh к контейнеру
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Пространство для FAB
        ) {
            // Панель со сводными данными (статистика)
            item {
                HomeDashboardPanel(
                    childrenCount = uiState.totalChildrenCount,
                    eventsCount = uiState.todayEvents.size,
                    remindersCount = uiState.upcomingReminders.size,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Секция быстрого доступа
            item {
                QuickAccessSection(
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToChildren = onNavigateToChildren,
                    onNavigateToAttendance = onNavigateToAttendance,
                    onNavigateToNotes = onNavigateToNotes,
                    onNavigateToGames = onNavigateToGames,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
            }

            // Секция с сегодняшними событиями
            item {
                TodaysEventsSection(
                    events = uiState.todayEvents,
                    onEventClick = onNavigateToEventDetails,
                    onSeeAllClick = onNavigateToEvents
                )
            }

            // Секция с детьми (топ достижений)
            item {
                TopChildrenSection(
                    children = uiState.topChildren,
                    onChildClick = onNavigateToChildDetails,
                    onSeeAllClick = onNavigateToChildren
                )
            }

            // Секция с напоминаниями
            item {
                RemindersSection(
                    reminders = uiState.upcomingReminders,
                    onSeeAllClick = onNavigateToNotes,
                    onReminderClick = onNavigateToNoteDetails  // Передаем навигацию к деталям заметки
                )
            }

            // Отступ внизу для защиты от перекрытия контента FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Индикатор Pull-to-refresh (показывается поверх контента)
        PullRefreshIndicator(
            refreshing = uiState.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}