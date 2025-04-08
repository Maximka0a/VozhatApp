package com.example.vozhatapp.presentation.notes

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.notes.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToNoteDetail: (Long) -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToChildDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle snackbar messages
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    // Refresh data when screen becomes active
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NotesTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
                onSearchClick = { viewModel.toggleSearchBar() },
                onFilterClick = { viewModel.toggleFilterDialog() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            NotesFab(
                expanded = !state.isScrolling,
                onClick = onNavigateToCreateNote
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar (collapsible)
                SearchBar(
                    visible = state.isSearchBarVisible,
                    searchQuery = state.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onClear = { viewModel.clearSearch() }
                )

                // Category tabs
                NotesTabRow(
                    selectedIndex = state.selectedTabIndex,
                    onTabSelected = { viewModel.selectTab(it) }
                )

                // Reminders section (visible only in All and Reminders tabs)
                if ((state.selectedTabIndex == 0 || state.selectedTabIndex == 2) &&
                    state.todayReminders.isNotEmpty() && state.searchQuery.isEmpty()) {
                    TodayRemindersSection(
                        reminders = state.todayReminders,
                        onReminderClick = onNavigateToNoteDetail,
                        onCompleteReminder = { viewModel.completeReminder(it) }
                    )
                }

                // Main content
                NotesContent(
                    state = state,
                    onNoteClick = onNavigateToNoteDetail,
                    onDeleteNote = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Удалить заметку?",
                                actionLabel = "Да",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.deleteNote(it)
                            }
                        }
                    },
                    onChildClick = onNavigateToChildDetail,
                    onScroll = { isScrolling -> viewModel.setScrolling(isScrolling) }
                )
            }

            // Show loading indicator
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            // Filter dialog
            if (state.showFilterDialog) {
                NotesFilterDialog(
                    currentOrder = state.sortOrder,
                    onOrderSelected = { viewModel.updateSortOrder(it) },
                    showRemindersOnly = state.showRemindersOnly,
                    onRemindersOnlyChanged = { viewModel.toggleShowRemindersOnly() },
                    onDismiss = { viewModel.toggleFilterDialog() }
                )
            }
        }
    }
}