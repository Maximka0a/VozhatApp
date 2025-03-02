package com.example.vozhatapp.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vozhatapp.presentation.home.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChildDetails: (Long) -> Unit,
    onNavigateToEventDetails: (Long) -> Unit,
    onNavigateToNoteDetails: (Long) -> Unit,  // Добавлен новый параметр
    onCreateNewEvent: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopAppBar(
                scrollBehavior = scrollBehavior,
                onMenuClick = { onNavigateToSettings() },
                onRefresh = { viewModel.loadData() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onCreateNewEvent() },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Создать событие") },
                expanded = scrollBehavior.state.collapsedFraction < 0.5
            )
        }
    ) { paddingValues ->
        HomeScreenContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onRefresh = { viewModel.loadData() },
            onNavigateToEvents = onNavigateToEvents,
            onNavigateToChildren = onNavigateToChildren,
            onNavigateToGames = onNavigateToGames,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToNotes = onNavigateToNotes,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToChildDetails = onNavigateToChildDetails,
            onNavigateToEventDetails = onNavigateToEventDetails,
            onNavigateToNoteDetails = onNavigateToNoteDetails  // Передаем навигацию
        )

        // Показываем сообщение об ошибке, если оно есть
        uiState.message?.let { message ->
            Toast.makeText(LocalContext.current, message, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}