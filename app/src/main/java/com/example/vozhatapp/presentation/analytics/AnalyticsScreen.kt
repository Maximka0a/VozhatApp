package com.example.vozhatapp.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (Long) -> Unit,
    onNavigateToChildDetail: (Long) -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Аналитика и отчеты") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDateRangePicker() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Выбрать период")
                    }

                    IconButton(onClick = { viewModel.toggleExportDialog() }) {
                        Icon(Icons.Default.Share, contentDescription = "Экспорт")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date range display
            DateRangeSelectionBar(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onDateRangeClick = { viewModel.toggleDateRangePicker() }
            )

            // Tab bar for analytics categories
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = colorScheme.background,
                contentColor = colorScheme.primary
            ) {
                AnalyticsTabs.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = null) }
                    )
                }
            }

            // Main content area - show appropriate analytics based on selected tab
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        AnalyticsContent(
                            tabIndex = uiState.selectedTabIndex,
                            uiState = uiState,
                            onNavigateToEventDetail = onNavigateToEventDetail,
                            onNavigateToChildDetail = onNavigateToChildDetail,
                            onExportData = { viewModel.exportData() }
                        )
                    }
                }
            }
        }

        // Date Range Picker Dialog
        if (uiState.showDateRangePicker) {
            DateRangePickerDialog(
                initialStartDate = uiState.startDate,
                initialEndDate = uiState.endDate,
                onDateRangeSelected = { start, end ->
                    viewModel.updateDateRange(start, end)
                },
                onDismiss = { viewModel.toggleDateRangePicker() }
            )
        }

        // Export Dialog
        if (uiState.showExportDialog) {
            ExportDialog(
                onExportFormat = { format ->
                    viewModel.exportData(format)
                    viewModel.toggleExportDialog()
                },
                onDismiss = { viewModel.toggleExportDialog() }
            )
        }
    }
}

@Composable
private fun DateRangeSelectionBar(
    startDate: Long,
    endDate: Long,
    onDateRangeClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onDateRangeClick,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Период анализа",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalyticsContent(
    tabIndex: Int,
    uiState: AnalyticsUiState,
    onNavigateToEventDetail: (Long) -> Unit,
    onNavigateToChildDetail: (Long) -> Unit,
    onExportData: () -> Unit
) {
    val selectedTab = AnalyticsTabs.entries[tabIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        when (selectedTab) {
            AnalyticsTabs.ATTENDANCE -> AttendanceAnalyticsContent(uiState, onNavigateToEventDetail)
            AnalyticsTabs.ACHIEVEMENTS -> AchievementsAnalyticsContent(uiState, onNavigateToChildDetail)
            AnalyticsTabs.CHILDREN -> ChildrenAnalyticsContent(uiState, onNavigateToChildDetail)
            AnalyticsTabs.EVENTS -> EventsAnalyticsContent(uiState, onNavigateToEventDetail)
            AnalyticsTabs.OVERVIEW -> OverviewAnalyticsContent(uiState)
        }

        // Export button at bottom of screen
        Button(
            onClick = onExportData,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Экспортировать данные")
        }
    }
}

@Composable
private fun AttendanceAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToEventDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary.toArgb()
    val onSurfaceColor = colorScheme.onSurface
    val onPrimaryColor = colorScheme.onPrimary
    val onSurfaceVariantColor = colorScheme.onSurfaceVariant
    val primaryBgColor = colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth()) {
        // Attendance summary card
        AnalyticsSummaryCard(
            title = "Посещаемость",
            mainMetric = "${uiState.overallAttendanceRate}%",
            description = "средняя посещаемость за период",
            trendValue = uiState.attendanceTrend,
            trendDescription = "по сравнению с прошлым периодом"
        )

        // Attendance over time chart
        ChartCard(
            title = "Динамика посещаемости",
            description = "Процент посещаемости событий по дням"
        ) {
            // Using MPAndroidChart for line chart
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = true
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = 100f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Используем ранее полученный primaryColor
                    // Больше не обращаемся к colorScheme напрямую

                    // Sample data - replace with actual data from ViewModel
                    val entries = uiState.attendanceByDay.map { (date, rate) ->
                        Entry(date.toFloat(), rate.toFloat())
                    }

                    val dataSet = LineDataSet(entries, "Посещаемость (%)").apply {
                        color = primaryColor
                        setCircleColor(primaryColor)
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawValues(false)
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        uiState.attendanceByDay.map { (day, _) ->
                            SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(day.toLong() * 86400000))
                        }
                    )
                    chart.invalidate()
                }
            )
        }

        // Squad attendance comparison
        ChartCard(
            title = "Посещаемость по отрядам",
            description = "Сравнение посещаемости между отрядами"
        ) {
            // Horizontal bar chart for squad comparison
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = false
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = 100f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Sample squad attendance data
                    val entries = uiState.squadAttendanceRates.mapIndexed { index, (_, rate) ->
                        BarEntry(index.toFloat(), rate.toFloat())
                    }

                    val dataSet = BarDataSet(entries, "Посещаемость (%)").apply {
                        colors = uiState.squadAttendanceRates.mapIndexed { index, _ ->
                            ColorTemplate.MATERIAL_COLORS[index % ColorTemplate.MATERIAL_COLORS.size]
                        }
                        setDrawValues(true)
                        valueTextSize = 10f
                    }

                    chart.data = BarData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        uiState.squadAttendanceRates.map { (squad, _) -> squad }
                    )
                    chart.invalidate()
                }
            )
        }

        // Top events by attendance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Топ событий по посещаемости",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor // Используем ранее полученный цвет
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.topAttendedEvents.forEach { eventWithRate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToEventDetail(eventWithRate.eventId) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryBgColor) // Используем ранее полученный цвет
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${eventWithRate.attendanceRate}%",
                                    color = onPrimaryColor, // Используем ранее полученный цвет
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = eventWithRate.eventTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = SimpleDateFormat("dd MMMM", Locale.getDefault())
                                        .format(Date(eventWithRate.eventDate)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor // Используем ранее полученный цвет
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = onSurfaceVariantColor // Используем ранее полученный цвет
                            )
                        }

                        if (eventWithRate != uiState.topAttendedEvents.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToChildDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = MaterialTheme.colorScheme
    val onSurfaceColor = colorScheme.onSurface
    val secondaryColor = colorScheme.secondary.toArgb()
    val surfaceVariantColor = colorScheme.surfaceVariant
    val onSurfaceVariantColor = colorScheme.onSurfaceVariant
    val primaryContainerColor = colorScheme.primaryContainer
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer

    Column(modifier = Modifier.fillMaxWidth()) {
        // Achievement summary card
        AnalyticsSummaryCard(
            title = "Достижения",
            mainMetric = uiState.totalAchievements.toString(),
            description = "достижений выдано за период",
            trendValue = uiState.achievementsTrend,
            trendDescription = "по сравнению с прошлым периодом"
        )

        // Achievement distribution pie chart
        ChartCard(
            title = "Распределение достижений",
            description = "По категориям достижений"
        ) {
            // Using MPAndroidChart for pie chart
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setDrawEntryLabels(false)
                        legend.isEnabled = true
                        legend.textSize = 12f
                        setUsePercentValues(true)
                        holeRadius = 45f
                        transparentCircleRadius = 50f
                        setDrawCenterText(true)
                        centerText = "Достижения"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { chart ->
                    // Sample data for achievement categories
                    val entries = uiState.achievementsByCategory.map { (category, count) ->
                        PieEntry(count.toFloat(), category)
                    }

                    val colors = ColorTemplate.MATERIAL_COLORS.toList() +
                            ColorTemplate.VORDIPLOM_COLORS.toList()

                    val dataSet = PieDataSet(entries, "Категории").apply {
                        setColors(colors)
                        setDrawValues(true)
                        valueTextSize = 12f
                        valueTextColor = Color.White.toArgb()
                        valueFormatter = PercentFormatter(chart)
                    }

                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }
            )
        }

        // Top children by achievements
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Топ детей по достижениям",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor // Используем ранее полученный цвет
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.topChildrenByAchievements.forEachIndexed { index, child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToChildDetail(child.childId) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge showing rank
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (index) {
                                            0 -> Color(0xFFFFD700) // Gold
                                            1 -> Color(0xFFC0C0C0) // Silver
                                            2 -> Color(0xFFCD7F32) // Bronze
                                            else -> surfaceVariantColor // Используем ранее полученный цвет
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = child.childName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = child.squadName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor // Используем ранее полученный цвет
                                )
                            }

                            // Points badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(primaryContainerColor) // Используем ранее полученный цвет
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${child.points} очков",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = onPrimaryContainerColor, // Используем ранее полученный цвет
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (index < uiState.topChildrenByAchievements.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }

        // Achievement trends over time
        ChartCard(
            title = "Динамика достижений",
            description = "Количество новых достижений по дням"
        ) {
            // Line chart for achievement trends
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = true
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Используем ранее полученный цвет secondaryColor

                    // Sample achievement data over time
                    val entries = uiState.achievementsByDay.map { (day, count) ->
                        Entry(day.toFloat(), count.toFloat())
                    }

                    val dataSet = LineDataSet(entries, "Новые достижения").apply {
                        color = secondaryColor
                        setCircleColor(secondaryColor)
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawValues(false)
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        uiState.achievementsByDay.map { (day, _) ->
                            SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(day.toLong() * 86400000))
                        }
                    )
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun ChildrenAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToChildDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = MaterialTheme.colorScheme
    val tertiaryColor = colorScheme.tertiary.toArgb()
    val onSurfaceColor = colorScheme.onSurface
    val primaryContainerColor = colorScheme.primaryContainer
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer
    val onSurfaceVariantColor = colorScheme.onSurfaceVariant
    val surfaceVariantColor = colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        // Children summary metrics
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Всего детей",
                value = uiState.totalChildren.toString(),
                icon = Icons.Outlined.People,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            )

            MetricCard(
                title = "Отрядов",
                value = uiState.squadCount.toString(),
                icon = Icons.Outlined.Groups,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        // Age distribution chart
        ChartCard(
            title = "Распределение по возрасту",
            description = "Количество детей разных возрастов"
        ) {
            // Bar chart for age distribution
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = false
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Используем заранее полученный цвет вместо доступа к colorScheme

                    // Sample age distribution data
                    val entries = uiState.ageDistribution.map { (age, count) ->
                        BarEntry(age.toFloat(), count.toFloat())
                    }.sortedBy { it.x }

                    val dataSet = BarDataSet(entries, "Количество детей").apply {
                        color = tertiaryColor // Используем предварительно полученный цвет
                        setDrawValues(true)
                        valueTextSize = 10f
                    }

                    chart.data = BarData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        entries.map { "${it.x.toInt()} лет" }
                    )
                    chart.invalidate()
                }
            )
        }

        // Squad distribution pie chart
        ChartCard(
            title = "Распределение по отрядам",
            description = "Количество детей в каждом отряде"
        ) {
            // Pie chart for squad distribution
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setDrawEntryLabels(false)
                        legend.isEnabled = true
                        legend.textSize = 12f
                        setUsePercentValues(true)
                        holeRadius = 45f
                        transparentCircleRadius = 50f
                        setDrawCenterText(true)
                        centerText = "Отряды"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { chart ->
                    // Sample data for squad distribution
                    val entries = uiState.squadDistribution.map { (squad, count) ->
                        PieEntry(count.toFloat(), squad)
                    }

                    val colors = ColorTemplate.MATERIAL_COLORS.toList() +
                            ColorTemplate.VORDIPLOM_COLORS.toList()

                    val dataSet = PieDataSet(entries, "Отряды").apply {
                        setColors(colors)
                        setDrawValues(true)
                        valueTextSize = 12f
                        valueTextColor = Color.White.toArgb()
                        valueFormatter = PercentFormatter(chart)
                    }

                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }
            )
        }

        // Children with most attendance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Топ детей по посещаемости",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor // Используем предварительно полученный цвет
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.mostActiveChildren.forEachIndexed { index, child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToChildDetail(child.childId) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Attendance percentage circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(primaryContainerColor), // Используем предварительно полученный цвет
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${child.attendanceRate}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onPrimaryContainerColor // Используем предварительно полученный цвет
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = child.childName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = child.squadName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor // Используем предварительно полученный цвет
                                )
                            }

                            // Events count badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(surfaceVariantColor) // Используем предварительно полученный цвет
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${child.eventsAttended} соб.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = onSurfaceVariantColor // Используем предварительно полученный цвет
                                )
                            }
                        }

                        if (index < uiState.mostActiveChildren.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventsAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToEventDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary.toArgb()
    val secondaryColor = colorScheme.secondary.toArgb()
    val onSurfaceColor = colorScheme.onSurface
    val onSurfaceVariantColor = colorScheme.onSurfaceVariant
    val primaryContainerColor = colorScheme.primaryContainer
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer
    val tertiaryContainerColor = colorScheme.tertiaryContainer
    val onTertiaryContainerColor = colorScheme.onTertiaryContainer
    val errorContainerColor = colorScheme.errorContainer
    val onErrorContainerColor = colorScheme.onErrorContainer

    Column(modifier = Modifier.fillMaxWidth()) {
        // Events summary metrics
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Всего событий",
                value = uiState.totalEvents.toString(),
                icon = Icons.Outlined.Event,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            )

            MetricCard(
                title = "Ср. продолж.",
                value = "${uiState.averageEventDurationHours}ч",
                icon = Icons.Outlined.Timer,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        // Events distribution by day of week
        ChartCard(
            title = "Распределение по дням недели",
            description = "Количество событий в каждый день недели"
        ) {
            // Bar chart for events by day of week
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = false
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Используем ранее полученный цвет

                    // Sample data for events by day of week
                    val entries = uiState.eventsByDayOfWeek.mapIndexed { index, count ->
                        BarEntry(index.toFloat(), count.toFloat())
                    }

                    val dataSet = BarDataSet(entries, "Количество событий").apply {
                        color = primaryColor
                        setDrawValues(true)
                        valueTextSize = 10f
                    }

                    chart.data = BarData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    )
                    chart.invalidate()
                }
            )
        }

        // Events distribution by hour
        ChartCard(
            title = "Распределение по времени суток",
            description = "Количество событий в разное время дня"
        ) {
            // Line chart for events by hour
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = false
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Используем ранее полученный цвет

                    // Sample data for events by hour
                    val entries = uiState.eventsByHour.mapIndexed { index, count ->
                        Entry(index.toFloat(), count.toFloat())
                    }

                    val dataSet = LineDataSet(entries, "Количество событий").apply {
                        color = secondaryColor
                        setCircleColor(secondaryColor)
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawValues(false)
                        fillColor = secondaryColor
                        fillAlpha = 50
                        setDrawFilled(true)
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        (0..23 step 3).map { "$it:00" }
                    )
                    chart.invalidate()
                }
            )
        }

        // Recent events table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Недавние события",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor // Используем ранее полученный цвет
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.recentEvents.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToEventDetail(event.id) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Event status indicator
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(
                                        when (event.status) {
                                            0 -> Color(0xFF2196F3) // Upcoming - Blue
                                            1 -> Color(0xFF4CAF50) // In progress - Green
                                            2 -> Color(0xFF9E9E9E) // Completed - Gray
                                            else -> Color.Gray
                                        }
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                        .format(Date(event.startTime)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor // Используем ранее полученный цвет
                                )
                            }

                            // Attendance rate indicator
                            val attendanceRate = uiState.eventAttendanceRates[event.id] ?: 0

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            attendanceRate >= 80 -> primaryContainerColor
                                            attendanceRate >= 50 -> tertiaryContainerColor
                                            else -> errorContainerColor
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$attendanceRate%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when {
                                        attendanceRate >= 80 -> onPrimaryContainerColor
                                        attendanceRate >= 50 -> onTertiaryContainerColor
                                        else -> onErrorContainerColor
                                    }
                                )
                            }
                        }

                        if (event != uiState.recentEvents.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewAnalyticsContent(
    uiState: AnalyticsUiState
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary.toArgb()
    val secondaryColor = colorScheme.secondary.toArgb()
    val onSurfaceColor = colorScheme.onSurface
    val surfaceVariantColor = colorScheme.surfaceVariant
    val onSurfaceVariantColor = colorScheme.onSurfaceVariant
    val primaryContainerColor = colorScheme.primaryContainer
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer

    Column(modifier = Modifier.fillMaxWidth()) {
        // Overview summary metrics
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Всего детей",
                value = uiState.totalChildren.toString(),
                icon = Icons.Outlined.People,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            )

            MetricCard(
                title = "Всего событий",
                value = uiState.totalEvents.toString(),
                icon = Icons.Outlined.Event,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Посещаемость",
                value = "${uiState.overallAttendanceRate}%",
                icon = Icons.Outlined.CheckCircle,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            )

            MetricCard(
                title = "Достижения",
                value = uiState.totalAchievements.toString(),
                icon = Icons.Outlined.EmojiEvents,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        // Timeline of activities
        ChartCard(
            title = "Активность за период",
            description = "События и достижения по дням"
        ) {
            // Combined line chart for events and achievements
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = true
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Используем заранее полученные цвета

                    // Sample data for events by day
                    val eventEntries = uiState.eventsByDay.map { (day, count) ->
                        Entry(day.toFloat(), count.toFloat())
                    }

                    val achievementEntries = uiState.achievementsByDay.map { (day, count) ->
                        Entry(day.toFloat(), count.toFloat())
                    }

                    val eventDataSet = LineDataSet(eventEntries, "События").apply {
                        color = primaryColor  // Используем полученный цвет
                        setCircleColor(primaryColor)  // Используем полученный цвет
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawValues(false)
                    }

                    val achievementDataSet = LineDataSet(achievementEntries, "Достижения").apply {
                        color = secondaryColor  // Используем полученный цвет
                        setCircleColor(secondaryColor)  // Используем полученный цвет
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawValues(false)
                    }

                    chart.data = LineData(eventDataSet, achievementDataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        uiState.eventsByDay.map { (day, _) ->
                            SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(day.toLong() * 86400000))
                        }
                    )
                    chart.invalidate()
                }
            )
        }

        // Attendance by squad bar chart
        ChartCard(
            title = "Общая посещаемость по отрядам",
            description = "Процент посещаемости для каждого отряда"
        ) {
            // Horizontal bar chart for squad attendance
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        isScaleXEnabled = false
                        isScaleYEnabled = false
                        legend.isEnabled = true
                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = 100f
                            setDrawGridLines(true)
                        }
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                update = { chart ->
                    // Sample squad attendance data
                    val entries = uiState.squadAttendanceRates.mapIndexed { index, (_, rate) ->
                        BarEntry(index.toFloat(), rate.toFloat())
                    }

                    val colorList = listOf(
                        Color(0xFF4CAF50), // Green
                        Color(0xFF2196F3), // Blue
                        Color(0xFFFF9800), // Orange
                        Color(0xFFF44336), // Red
                        Color(0xFF9C27B0)  // Purple
                    )

                    val dataSet = BarDataSet(entries, "Посещаемость (%)").apply {
                        colors = uiState.squadAttendanceRates.mapIndexed { index, _ ->
                            colorList[index % colorList.size].toArgb()
                        }
                        setDrawValues(true)
                        valueTextSize = 10f
                    }

                    chart.data = BarData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                        uiState.squadAttendanceRates.map { (squad, _) -> squad }
                    )
                    chart.invalidate()
                }
            )
        }

        // Top children overall card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Общий рейтинг детей",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor  // Используем полученный цвет
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.topChildrenOverall.forEachIndexed { index, child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (index) {
                                            0 -> Color(0xFFFFD700) // Gold
                                            1 -> Color(0xFFC0C0C0) // Silver
                                            2 -> Color(0xFFCD7F32) // Bronze
                                            else -> surfaceVariantColor  // Используем полученный цвет
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = child.childName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = child.squadName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor  // Используем полученный цвет
                                )
                            }

                            // Composite score
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(primaryContainerColor)  // Используем полученный цвет
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${child.compositeScore} б.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = onPrimaryContainerColor,  // Используем полученный цвет
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (index < uiState.topChildrenOverall.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSummaryCard(
    title: String,
    mainMetric: String,
    description: String,
    trendValue: Float,
    trendDescription: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mainMetric,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )

                Text(
                    text = " $description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            trendValue > 0 -> colorScheme.primaryContainer.copy(alpha = 0.7f)
                            trendValue < 0 -> colorScheme.errorContainer.copy(alpha = 0.7f)
                            else -> colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = when {
                        trendValue > 0 -> Icons.Default.TrendingUp
                        trendValue < 0 -> Icons.Default.TrendingDown
                        else -> Icons.Default.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when {
                        trendValue > 0 -> colorScheme.onPrimaryContainer
                        trendValue < 0 -> colorScheme.onErrorContainer
                        else -> colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = when {
                        trendValue > 0 -> "+${trendValue.toInt()}%"
                        else -> "${trendValue.toInt()}%"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        trendValue > 0 -> colorScheme.onPrimaryContainer
                        trendValue < 0 -> colorScheme.onErrorContainer
                        else -> colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )

                Text(
                    text = trendDescription,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun DateRangePickerDialog(
    initialStartDate: Long,
    initialEndDate: Long,
    onDateRangeSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()

    // Convert milliseconds to year, month, day
    val startCalendar = Calendar.getInstance().apply { timeInMillis = initialStartDate }
    val endCalendar = Calendar.getInstance().apply { timeInMillis = initialEndDate }

    var startYear by remember { mutableIntStateOf(startCalendar.get(Calendar.YEAR)) }
    var startMonth by remember { mutableIntStateOf(startCalendar.get(Calendar.MONTH)) }
    var startDay by remember { mutableIntStateOf(startCalendar.get(Calendar.DAY_OF_MONTH)) }

    var endYear by remember { mutableIntStateOf(endCalendar.get(Calendar.YEAR)) }
    var endMonth by remember { mutableIntStateOf(endCalendar.get(Calendar.MONTH)) }
    var endDay by remember { mutableIntStateOf(endCalendar.get(Calendar.DAY_OF_MONTH)) }

    var currentTab by remember { mutableIntStateOf(0) } // 0 for start date, 1 for end date

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Выберите период",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Tab selection for start/end date
                TabRow(
                    selectedTabIndex = currentTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("Начальная дата") }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("Конечная дата") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date picker content based on selected tab
                when (currentTab) {
                    0 -> DatePickerContent(
                        year = startYear,
                        month = startMonth,
                        day = startDay,
                        onYearChange = { startYear = it },
                        onMonthChange = { startMonth = it },
                        onDayChange = { startDay = it }
                    )
                    1 -> DatePickerContent(
                        year = endYear,
                        month = endMonth,
                        day = endDay,
                        onYearChange = { endYear = it },
                        onMonthChange = { endMonth = it },
                        onDayChange = { endDay = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected date range preview
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val startDateString = dateFormat.format(
                    Calendar.getInstance().apply {
                        set(startYear, startMonth, startDay)
                    }.time
                )
                val endDateString = dateFormat.format(
                    Calendar.getInstance().apply {
                        set(endYear, endMonth, endDay)
                    }.time
                )

                Text(
                    text = "Выбранный период:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$startDateString — $endDateString",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val startDateMillis = Calendar.getInstance().apply {
                                set(startYear, startMonth, startDay, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            val endDateMillis = Calendar.getInstance().apply {
                                set(endYear, endMonth, endDay, 23, 59, 59)
                                set(Calendar.MILLISECOND, 999)
                            }.timeInMillis

                            onDateRangeSelected(startDateMillis, endDateMillis)
                            onDismiss()
                        }
                    ) {
                        Text("Применить")
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerContent(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit
) {
    val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Year picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Год:",
                modifier = Modifier.width(80.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = { onYearChange(year - 1) }
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий год")
            }

            Text(
                text = year.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { onYearChange(year + 1) }
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий год")
            }
        }

        // Month picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Месяц:",
                modifier = Modifier.width(80.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = {
                    onMonthChange(if (month > 0) month - 1 else 11)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
            }

            Text(
                text = monthNames[month],
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    onMonthChange(if (month < 11) month + 1 else 0)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
            }
        }

        // Day picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "День:",
                modifier = Modifier.width(80.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            val calendar = Calendar.getInstance()
            calendar.set(year, month, 1)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            IconButton(
                onClick = {
                    onDayChange(if (day > 1) day - 1 else daysInMonth)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий день")
            }

            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    onDayChange(if (day < daysInMonth) day + 1 else 1)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий день")
            }
        }
    }
}

@Composable
private fun ExportDialog(
    onExportFormat: (ExportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Экспорт отчета",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Выберите формат экспорта:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Export format options
                ExportFormatOption(
                    format = ExportFormat.PDF,
                    title = "PDF документ",
                    description = "Форматированный отчет с графиками и таблицами",
                    icon = Icons.Outlined.PictureAsPdf,
                    onClick = { onExportFormat(ExportFormat.PDF) }
                )

                ExportFormatOption(
                    format = ExportFormat.EXCEL,
                    title = "Excel таблица",
                    description = "Данные в табличном формате для анализа",
                    icon = Icons.Outlined.TableChart,
                    onClick = { onExportFormat(ExportFormat.EXCEL) }
                )

                ExportFormatOption(
                    format = ExportFormat.CSV,
                    title = "CSV файл",
                    description = "Простые данные через запятую для импорта",
                    icon = Icons.Outlined.DataObject,
                    onClick = { onExportFormat(ExportFormat.CSV) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}

@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

enum class AnalyticsTabs(val title: String, val icon: ImageVector) {
    OVERVIEW("Обзор", Icons.Outlined.Dashboard),
    ATTENDANCE("Посещаемость", Icons.Outlined.EventAvailable),
    ACHIEVEMENTS("Достижения", Icons.Outlined.EmojiEvents),
    CHILDREN("Дети", Icons.Outlined.People),
    EVENTS("События", Icons.Outlined.Event)
}

enum class ExportFormat {
    PDF, EXCEL, CSV
}