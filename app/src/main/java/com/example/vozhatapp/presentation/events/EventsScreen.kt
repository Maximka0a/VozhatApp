@file:OptIn(ExperimentalFoundationApi::class)

package com.example.vozhatapp.presentation.events

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.data.local.entity.Event
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(
    onEventClick: (Long) -> Unit,
    onCreateEvent: () -> Unit,
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
                onClick = onCreateEvent,
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
                    text = { Text("Все") },
                    icon = { Icon(Icons.Filled.Event, contentDescription = null) }
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
                        events = uiState.filteredEvents,
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

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Поиск событий...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск"
            )
        },
        trailingIcon = {
            IconButton(onClick = onClearSearch) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Очистить"
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun SimplifiedCalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<Event>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Calendar header with month/year and expand toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDate.month
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                    .capitalize() + " " + selectedDate.year,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        onDateSelected(LocalDate.now())
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Сегодня"
                    )
                }

                IconButton(
                    onClick = {
                        onExpandToggle()
                    }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть"
                    )
                }
            }
        }

        if (isExpanded) {
            // Simplified month calendar view
            SimpleMonthCalendar(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                events = events
            )
        } else {
            // Week strip view
            val visibleDates = generateWeekDates(selectedDate)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(visibleDates) { date ->
                    val hasEvents = hasEventsOnDay(date, events)
                    DayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        hasEvents = hasEvents,
                        onClick = { onDateSelected(date) }
                    )
                }
            }

            // Current date events count
            if (events.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "События на ${formatDate(selectedDate)}: ${events.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleMonthCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<Event>
) {
    // Get first day of month and calculate first day to display
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value
    val daysInMonth = selectedDate.month.length(selectedDate.isLeapYear)

    // Display week days header
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 16.dp)) {
        val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        for (day in daysOfWeek) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

    // Generate dates for the calendar grid
    val dates = mutableListOf<LocalDate?>()

    // Add empty spaces for days before the first day of month
    for (i in 1 until dayOfWeek) {
        dates.add(null)
    }

    // Add days of the month
    for (i in 1..daysInMonth) {
        dates.add(selectedDate.withDayOfMonth(i))
    }

    // Calculate number of rows needed
    val rows = (dates.size + 6) / 7

    // Ensure we have enough vertical space for the calendar
    Column(modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = (rows * 50).dp) // Ensure minimum height based on number of rows
        .padding(horizontal = 16.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp), // Fixed height for rows
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    if (index < dates.size) {
                        val date = dates[index]
                        if (date != null) {
                            val hasEvents = hasEventsOnDay(date, events)
                            SimpleDayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                hasEvents = hasEvents,
                                onClick = { onDateSelected(date) }
                            )
                        } else {
                            // Empty space for days before/after month
                            Box(modifier = Modifier.size(44.dp))
                        }
                    } else {
                        Box(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun SimpleDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(50.dp)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        if (hasEvents) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}

@Composable
fun EventsList(
    events: List<Event>,
    onEventClick: (Long) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (events.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.EventBusy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Нет событий",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "События будут отображаться здесь",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = events,
                    key = { it.id }
                ) { event ->
                    EventItem(
                        event = event,
                        onEventClick = { onEventClick(event.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(
    event: Event,
    onEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(event.startTime),
        ZoneId.systemDefault()
    )
    val endTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(event.endTime),
        ZoneId.systemDefault()
    ).toLocalTime()

    val statusColor = getEventStatusColor(event.status)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    ElevatedCard(
        onClick = onEventClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(100.dp)
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Title and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    EventStatusChip(status = event.status)
                }

                // Time and date
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatDate(startTime.toLocalDate()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Location if available
                if (!event.location.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Description if available
                if (!event.description.isNullOrEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EventStatusChip(status: Int) {
    val (text, color) = when (status) {
        0 -> Pair("Предстоит", MaterialTheme.colorScheme.primary)
        1 -> Pair("В процессе", MaterialTheme.colorScheme.tertiary)
        else -> Pair("Завершено", MaterialTheme.colorScheme.outline)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Helper functions
private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    return date.format(formatter)
}

private fun hasEventsOnDay(date: LocalDate, events: List<Event>): Boolean {
    val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

    return events.any {
        it.startTime in startOfDay..endOfDay ||
                it.endTime in startOfDay..endOfDay ||
                (it.startTime <= startOfDay && it.endTime >= endOfDay)
    }
}

private fun getEventStatusColor(status: Int): Color {
    return when (status) {
        0 -> Color(0xFF2196F3) // Upcoming: Blue
        1 -> Color(0xFF4CAF50) // In progress: Green
        else -> Color(0xFF9E9E9E) // Completed: Gray
    }
}

private fun generateWeekDates(selectedDate: LocalDate): List<LocalDate> {
    val today = LocalDate.now()
    val dates = mutableListOf<LocalDate>()

    // Get the start of the week (Monday) for the selected date
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)

    // Add all 7 days of the week
    for (i in 0..6) {
        dates.add(startOfWeek.plusDays(i.toLong()))
    }

    return dates
}

private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}