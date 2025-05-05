package com.example.vozhatapp.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import com.github.mikephil.charting.formatter.ValueFormatter
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

            // Main content area
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
                            onNavigateToChildDetail = onNavigateToChildDetail
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
    onNavigateToChildDetail: (Long) -> Unit
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
    }
}

@Composable
private fun AttendanceAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToEventDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = colorScheme
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
            trendValue = uiState.attendanceTrend
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
    // Получаем все цвета ЗАРАНЕЕ для использования в AndroidView
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb() // Сохраняем для использования в AndroidView
    val onSurfaceVariantColorCompose = MaterialTheme.colorScheme.onSurfaceVariant // Для Text компонентов

    Column(modifier = Modifier.fillMaxWidth()) {
        // Achievement summary card
        AnalyticsSummaryCard(
            title = "Достижения",
            mainMetric = uiState.totalAchievements.toString(),
            description = "достижений выдано за период",
            trendValue = uiState.achievementsTrend
        )


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
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.topChildrenByAchievements.isEmpty()) {
                    // Показываем сообщение при отсутствии данных
                    Text(
                        text = "Нет данных о достижениях",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColorCompose,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
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
                                                else -> surfaceVariantColor
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
                                        color = onSurfaceVariantColorCompose
                                    )
                                }

                                // Points badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(primaryContainerColor)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${child.points} очков",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = onPrimaryContainerColor,
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
        }

        // Achievement trends over time
        ChartCard(
            title = "Динамика достижений",
            description = "Количество новых достижений по дням"
        ) {
            // ИСПРАВЛЕННЫЙ LineChart для достижений
            if (uiState.achievementsByDay.isEmpty()) {
                // Показываем сообщение об отсутствии данных вместо пустого графика
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных о достижениях в выбранном периоде",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColorCompose
                    )
                }
            } else {
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
                            // Установка текста при отсутствии данных ЗАРАНЕЕ
                            setNoDataText("Нет данных о достижениях")
                            setNoDataTextColor(onSurfaceColor) // Используем сохраненный цвет
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    update = { chart ->
                        // Проверяем, есть ли хотя бы 2 точки для отображения линии
                        if (uiState.achievementsByDay.size < 2) {
                            chart.data = null
                            chart.invalidate()
                            return@AndroidView
                        }

                        // Сортируем дни для правильного отображения
                        val sortedDays = uiState.achievementsByDay.toSortedMap()

                        // Создаем точки для графика
                        val entries = sortedDays.entries.mapIndexed { index, (day, count) ->
                            Entry(index.toFloat(), count.toFloat())
                        }

                        val dataSet = LineDataSet(entries, "Новые достижения").apply {
                            color = secondaryColor
                            setCircleColor(secondaryColor)
                            lineWidth = 2f
                            circleRadius = 3f
                            setDrawCircleHole(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            // Отключаем отображение значений, чтобы избежать ошибок при малом количестве точек
                            setDrawValues(false)
                        }

                        val lineData = LineData(dataSet)
                        chart.data = lineData

                        // Устанавливаем метки для оси X
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                            sortedDays.keys.mapIndexed { index, day ->
                                SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(day.toLong() * 86400000))
                            }
                        )

                        chart.invalidate()
                    }
                )
            }
        }
    }
}

@Composable
fun AgeDistributionChart(
    ageDistribution: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    // Важно: получаем все цвета здесь, вне лямбды update
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    if (ageDistribution.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет данных о возрастах детей",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariantColor
            )
        }
        return
    }

    // Подготовка данных для графика, сортируем по возрасту
    val sortedEntries = ageDistribution.entries.sortedBy { it.key }
    val maxValue = ageDistribution.values.maxOrNull()?.toFloat() ?: 1f

    // Создаем цвета для столбцов заранее
    val barColors = sortedEntries.mapIndexed { index, _ ->
        val intensity = 0.5f + ((index + 1).toFloat() / sortedEntries.size.toFloat()) * 0.5f
        Color(tertiaryColor).copy(alpha = intensity).toArgb()
    }

    // Создание меток для оси X (возраста)
    val ageLabels = sortedEntries.map { "${it.key}" }.toTypedArray()

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                // Основные настройки
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setFitBars(true)

                // Настройка взаимодействия
                isDoubleTapToZoomEnabled = false
                setPinchZoom(false)
                setScaleEnabled(false)

                // Настройка внешнего вида
                setDrawValueAboveBar(true)

                // Настройка легенды
                legend.isEnabled = false

                // Настройка оси X
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelCount = ageDistribution.size
                    textSize = 12f
                    textColor = onSurfaceColor
                }

                // Настройка левой оси Y
                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    granularity = 1f
                    textColor = onSurfaceColor
                    // Форматирование для целых чисел
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }
                }

                // Отключение правой оси
                axisRight.isEnabled = false

                // Добавляем отступ слева для меток
                extraLeftOffset = 10f
                extraBottomOffset = 10f

                // Настройка анимации
                animateY(1000)
            }
        },
        modifier = modifier.fillMaxWidth().height(220.dp),
        update = { chart ->
            // Создание набора данных
            val barEntries = sortedEntries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }

            // Настройка набора данных
            val dataSet = BarDataSet(barEntries, "Количество детей").apply {
                colors = barColors

                valueTextSize = 12f
                valueTextColor = onSurfaceColor
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }

                // Увеличиваем ширину столбцов
                barBorderWidth = 0f
                highLightAlpha = 100
            }

            // Установка форматтера меток оси X
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < ageLabels.size) ageLabels[index] else ""
                }
            }

            // Обновление данных графика
            val barData = BarData(dataSet)
            barData.barWidth = 0.7f
            chart.data = barData

            // Обновление графика
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
private fun ChildrenAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToChildDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = colorScheme
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
            // Используем новый компонент для отображения распределения по возрасту
            AgeDistributionChart(
                ageDistribution = uiState.ageDistribution,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }

        // Squad distribution pie chart - дополнительно улучшим его тоже
        ChartCard(
            title = "Распределение по отрядам",
            description = "Количество детей в каждом отряде"
        ) {
            // Улучшенное отображение круговой диаграммы отрядов
            SquadDistributionChart(
                squadDistribution = uiState.squadDistribution,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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
                    color = onSurfaceColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Если нет данных, показываем соответствующее сообщение
                if (uiState.mostActiveChildren.isEmpty()) {
                    Text(
                        text = "Нет данных о посещаемости",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
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
                                        .background(primaryContainerColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${child.attendanceRate}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = onPrimaryContainerColor
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
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = child.squadName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onSurfaceVariantColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Events count badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(surfaceVariantColor)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${child.eventsAttended} соб.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = onSurfaceVariantColor
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
}

@Composable
fun SquadDistributionChart(
    squadDistribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // Получаем все цвета заранее из Compose контекста
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val whiteColor = Color.White.toArgb()

    // Генерируем цвета из темы Material заранее
    val colorPalette = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb(),
        MaterialTheme.colorScheme.error.toArgb(),
        MaterialTheme.colorScheme.primaryContainer.toArgb(),
        MaterialTheme.colorScheme.secondaryContainer.toArgb(),
        MaterialTheme.colorScheme.tertiaryContainer.toArgb(),
        MaterialTheme.colorScheme.errorContainer.toArgb()
    )

    if (squadDistribution.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет данных об отрядах",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariantColor
            )
        }
        return
    }

    // Создаем список записей для пирога заранее
    val entries = squadDistribution
        .map { (squad, count) -> PieEntry(count.toFloat(), squad) }
        .sortedByDescending { it.value }

    // Создаем цвета для секторов заранее
    val colors = entries.indices.map {
        colorPalette[it % colorPalette.size]
    }

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                // Основные настройки
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.TRANSPARENT)
                holeRadius = 45f
                transparentCircleRadius = 50f

                // Настройка центрального текста
                setDrawCenterText(true)
                centerText = "Отряды"
                setCenterTextSize(16f)
                setCenterTextColor(onSurfaceColor)

                // Настройка легенды
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = onSurfaceColor
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    xEntrySpace = 10f
                    yEntrySpace = 0f
                }

                // Настройка внешнего вида
                setUsePercentValues(true)
                setDrawEntryLabels(false)

                // Анимация
                animateY(1200)
            }
        },
        modifier = modifier.fillMaxWidth().height(250.dp),
        update = { chart ->
            // Создаем набор данных
            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors)
                setDrawValues(true)
                valueTextSize = 14f
                valueTextColor = whiteColor
                valueFormatter = PercentFormatter(chart)
                sliceSpace = 2f
                selectionShift = 5f
            }

            // Обновляем данные графика
            chart.data = PieData(dataSet).apply {
                setValueTextSize(12f)
                setValueTextColor(whiteColor)
            }

            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
private fun EventsAnalyticsContent(
    uiState: AnalyticsUiState,
    onNavigateToEventDetail: (Long) -> Unit
) {
    // Получаем все необходимые цвета заранее
    val colorScheme = colorScheme
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
                value = "${"%.2f".format(uiState.averageEventDurationHours)}ч",
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
    val colorScheme = colorScheme
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
    trendDescription: String = "" // делаем параметр опциональным с пустым значением по умолчанию
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
                    modifier = Modifier.padding(start = 4.dp, end = if (trendDescription.isNotEmpty()) 8.dp else 0.dp)
                )

                // Показываем описание тренда только если оно не пустое
                if (trendDescription.isNotEmpty()) {
                    Text(
                        text = trendDescription,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
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


enum class AnalyticsTabs(val title: String, val icon: ImageVector) {
    OVERVIEW("Обзор", Icons.Outlined.Dashboard),
    ATTENDANCE("Посещаемость", Icons.Outlined.EventAvailable),
    ACHIEVEMENTS("Достижения", Icons.Outlined.EmojiEvents),
    CHILDREN("Дети", Icons.Outlined.People),
    EVENTS("События", Icons.Outlined.Event)
}
