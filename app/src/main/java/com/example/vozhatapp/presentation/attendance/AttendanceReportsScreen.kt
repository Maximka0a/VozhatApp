package com.example.vozhatapp.presentation.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.components.DateRangePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle errors and messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    // Date range picker dialog
    if (uiState.showDateRangePicker) {
        DateRangePicker(
            initialStartDate = uiState.startDate,
            initialEndDate = uiState.endDate,
            onDateRangeSelected = { startDate, endDate ->
                viewModel.updateDateRange(startDate, endDate)
            },
            onDismiss = {
                viewModel.toggleDateRangePicker()
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text("Отчеты по посещаемости")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDateRangePicker() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Выбрать период"
                        )
                    }

                    IconButton(onClick = { viewModel.exportReport() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Экспорт отчета"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ReportsContent(uiState)
            }
        }
    }
}

@Composable
private fun ReportsContent(uiState: AttendanceReportsUiState) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Date range header
        DateRangeHeader(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onDateRangeClick = { uiState.onDateRangeClick() }
        )

        // Summary statistics
        AttendanceSummaryStats(uiState.summaryStats)

        // Tab selector for different report views
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("По отрядам", "По мероприятиям", "По детям")

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        // Show different report based on selected tab
        when (selectedTab) {
            0 -> SquadReport(uiState.squadStats)
            1 -> EventReport(uiState.eventStats)
            2 -> ChildReport(uiState.childStats)
        }
    }
}

@Composable
private fun DateRangeHeader(
    startDate: LocalDate,
    endDate: LocalDate,
    onDateRangeClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${startDate.format(dateFormatter)} — ${endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            IconButton(onClick = onDateRangeClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Изменить период",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AttendanceSummaryStats(stats: AttendanceSummaryStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Общая статистика",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(
                    value = stats.totalEvents.toString(),
                    label = "Событий"
                )

                StatColumn(
                    value = stats.totalChildren.toString(),
                    label = "Детей"
                )

                StatColumn(
                    value = stats.totalAttendance.toString(),
                    label = "Посещений"
                )

                StatColumn(
                    value = "${stats.averageAttendanceRate}%",
                    label = "Средняя посещаемость"
                )
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SquadReport(stats: List<SquadAttendanceStats>) {
    if (stats.isEmpty()) {
        EmptyReportMessage()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { squadStat ->
                SquadReportItem(squadStat)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SquadReportItem(stat: SquadAttendanceStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stat.squadName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AttendanceRateBadge(stat.attendanceRate)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Количество детей: ${stat.childrenCount}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Всего посещений: ${stat.totalAttendance}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Всего пропусков: ${stat.totalAbsences}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EventReport(stats: List<EventAttendanceStats>) {
    if (stats.isEmpty()) {
        EmptyReportMessage()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { eventStat ->
                EventReportItem(eventStat)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EventReportItem(stat: EventAttendanceStats) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.eventTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                AttendanceRateBadge(stat.attendanceRate)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = stat.eventDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Присутствовало: ${stat.presentCount}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Отсутствовало: ${stat.absentCount}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ChildReport(stats: List<ChildAttendanceStats>) {
    if (stats.isEmpty()) {
        EmptyReportMessage()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { childStat ->
                ChildReportItem(childStat)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ChildReportItem(stat: ChildAttendanceStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.childName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                AttendanceRateBadge(stat.attendanceRate)
            }

            Text(
                text = "Отряд: ${stat.squadName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Посещено событий: ${stat.eventsAttended}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Пропущено событий: ${stat.eventsMissed}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AttendanceRateBadge(rate: Int) {
    val (color, textColor) = when {
        rate >= 90 -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        rate >= 75 -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        rate >= 50 -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Box(
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$rate%",
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyReportMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нет данных за выбранный период",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Измените диапазон дат или добавьте данные о посещаемости",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}