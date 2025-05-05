@file:OptIn(ExperimentalFoundationApi::class)

package com.example.vozhatapp.presentation.events.List

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.events.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(
    onEventClick: (Long) -> Unit,
    onCreateEvent: (sourceRoute: String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState { 3 } // 3 tabs for different views

    // Animation for calendar mode transition
    val calendarHeight by animateDpAsState(
        targetValue = if (uiState.isCalendarExpanded) 340.dp else 130.dp,
        label = "calendarHeight"
    )

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "События",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSearchMode() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleCalendarExpanded() }
                    ) {
                        Icon(
                            imageVector = if (uiState.isCalendarExpanded)
                                Icons.Default.CalendarViewDay
                            else
                                Icons.Default.CalendarMonth,
                            contentDescription = "Переключить вид календаря"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateEvent("events") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Создать событие")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search bar
            AnimatedVisibility(
                visible = uiState.isSearchMode,
                enter = slideInVertically() + expandVertically(),
                exit = slideOutVertically() + shrinkVertically()
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onClearSearch = {
                        viewModel.updateSearchQuery("")
                        viewModel.toggleSearchMode()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Calendar section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(calendarHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                SimplifiedCalendarSection(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate,
                    events = uiState.eventsForSelectedDate,
                    isExpanded = uiState.isCalendarExpanded,
                    onExpandToggle = viewModel::toggleCalendarExpanded
                )
            }

            // Event filters
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("За выбранный день") },
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Предстоящие") },
                    icon = { Icon(Icons.Filled.Upcoming, contentDescription = null) }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text("Завершенные") },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) }
                )
            }

            // Events list/grid by filter
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> EventsList(
                        events = uiState.eventsForSelectedDate,  // Используем события за выбранный день
                        onEventClick = onEventClick,
                        isLoading = uiState.isLoading
                    )
                    1 -> EventsList(
                        events = uiState.upcomingEvents,
                        onEventClick = onEventClick,
                        isLoading = uiState.isLoading
                    )
                    2 -> EventsList(
                        events = uiState.pastEvents,
                        onEventClick = onEventClick,
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
    }
}
