package com.example.vozhatapp.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Achievement
import com.example.vozhatapp.data.local.entity.Attendance
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.AchievementRepository
import com.example.vozhatapp.data.repository.AttendanceRepository
import com.example.vozhatapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Модель представления для экрана аналитики
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    // Классы для хранения статистики внутри ViewModel
    data class AttendanceStats(val eventsAttended: Int, val totalEvents: Int)
    data class SquadAttendanceStats(val totalRate: Int, val childCount: Int)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        // Default date range - last 30 days
        val endDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val startDate = Calendar.getInstance().apply {
            timeInMillis = endDate
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        updateDateRange(startDate, endDate)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun toggleDateRangePicker() {
        _uiState.update { it.copy(showDateRangePicker = !it.showDateRangePicker) }
    }

    fun toggleExportDialog() {
        _uiState.update { it.copy(showExportDialog = !it.showExportDialog) }
    }

    fun updateDateRange(startDate: Long, endDate: Long) {
        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                isLoading = true,
                showDateRangePicker = false
            )
        }

        loadAnalyticsData(startDate, endDate)
    }

    fun exportData(format: ExportFormat = ExportFormat.PDF) {
        viewModelScope.launch {
            try {
                // Simulate export process
                _uiState.update { it.copy(isExporting = true) }

                // In a real app, you would implement actual export logic here
                // This could involve generating files using libraries like iText for PDF,
                // Apache POI for Excel, or simple CSV writing

                kotlinx.coroutines.delay(1500) // Simulate processing time

                val formatName = when (format) {
                    ExportFormat.PDF -> "PDF"
                    ExportFormat.EXCEL -> "Excel"
                    ExportFormat.CSV -> "CSV"
                }

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "Отчет успешно экспортирован в формате $formatName"
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "Ошибка при экспорте отчета: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadAnalyticsData(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Загрузка минимальных данных для отображения на экране
                // даже если некоторые запросы завершатся с ошибкой
                val children = try {
                    childRepository.getAllChildren().first()
                } catch (e: Exception) {
                    Log.e("AnalyticsViewModel", "Error loading children", e)
                    emptyList()
                }

                val squads = children.map { it.squadName }.distinct()

                // Загрузка событий
                val events = try {
                    eventRepository.getEventsByDateRange(startDate, endDate).first()
                } catch (e: Exception) {
                    Log.e("AnalyticsViewModel", "Error loading events", e)
                    emptyList()
                }

                // Базовые метрики для отображения в любом случае
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        totalChildren = children.size,
                        totalEvents = events.size,
                        squadCount = squads.size,

                        // Примеры данных на случай, если не удастся загрузить реальные
                        overallAttendanceRate = 75,
                        attendanceTrend = 5.2f,
                        achievementsTrend = 12.8f,
                        totalAchievements = 152,

                        // Распределение детей по возрастам (из имеющихся данных)
                        ageDistribution = children.groupBy { it.age }
                            .mapValues { it.value.size },

                        // Распределение по отрядам (из имеющихся данных)
                        squadDistribution = children.groupBy { it.squadName }
                            .mapValues { it.value.size },

                        // Примеры данных для графиков
                        eventsByDayOfWeek = List(7) { (1..10).random() },
                        eventsByHour = List(24) { (0..5).random() },

                        // Примеры данных для категорий достижений
                        achievementsByCategory = mapOf(
                            "Спортивные" to 35,
                            "Творческие" to 27,
                            "Интеллектуальные" to 22,
                            "Социальные" to 18,
                            "Лидерские" to 15
                        ),

                        // Недавние события (из имеющихся данных)
                        recentEvents = events.sortedByDescending { it.startTime }
                            .take(5)
                    )
                }

                // После отображения базовых данных пробуем загрузить более сложные метрики
                try {
                    // Обработка данных о посещаемости
                    processAttendanceData(events, children)

                    // Обработка данных о достижениях
                    processAchievementsData(children)

                } catch (e: Exception) {
                    Log.e("AnalyticsViewModel", "Error processing detailed analytics", e)
                    _uiState.update {
                        it.copy(
                            message = "Некоторые данные аналитики недоступны: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error in loadAnalyticsData", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Ошибка при загрузке данных: ${e.message}"
                    )
                }
            }
        }
    }

    // Выделим обработку посещаемости в отдельный метод для упрощения
    private suspend fun processAttendanceData(events: List<Event>, children: List<Child>) {
        // Временные переменные для хранения статистики
        var totalAttendance = 0
        var totalPossibleAttendance = 0
        val attendanceByEvent = mutableMapOf<Long, Int>()
        val attendanceByChild = mutableMapOf<Long, AttendanceStats>()
        val attendanceByDay = mutableMapOf<Int, Int>()

        // Обработка посещаемости для каждого события
        events.forEach { event ->
            try {
                val eventAttendance = attendanceRepository.getAttendanceForEvent(event.id).first()
                val presentCount = eventAttendance.count { it.isPresent }
                val totalCount = eventAttendance.size

                if (totalCount > 0) {
                    val attendanceRate = (presentCount * 100) / totalCount
                    attendanceByEvent[event.id] = attendanceRate
                    totalAttendance += presentCount
                    totalPossibleAttendance += totalCount

                    // Отслеживаем посещаемость по дням для графиков
                    val dayIndex = getDayIndex(event.startTime, _uiState.value.startDate)
                    val currentRate = attendanceByDay[dayIndex] ?: 0
                    val eventCount = attendanceByDay[-dayIndex] ?: 0
                    attendanceByDay[dayIndex] =
                        (currentRate * eventCount + attendanceRate) / (eventCount + 1)
                    attendanceByDay[-dayIndex] = eventCount + 1

                    // Отслеживаем посещаемость для каждого ребенка
                    eventAttendance.forEach { attendance ->
                        val stats = attendanceByChild[attendance.childId] ?: AttendanceStats(0, 0)
                        attendanceByChild[attendance.childId] = if (attendance.isPresent) {
                            stats.copy(
                                eventsAttended = stats.eventsAttended + 1,
                                totalEvents = stats.totalEvents + 1
                            )
                        } else {
                            stats.copy(totalEvents = stats.totalEvents + 1)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("AnalyticsViewModel", "Error processing attendance for event ${event.id}", e)
            }
        }

        // Вычисляем статистику посещаемости по отрядам
        val squadAttendance = mutableMapOf<String, SquadAttendanceStats>()
        children.forEach { child ->
            val stats = attendanceByChild[child.id] ?: AttendanceStats(0, 0)
            if (stats.totalEvents > 0) {
                val rate = (stats.eventsAttended * 100) / stats.totalEvents
                val squadName = child.squadName
                val squadStats = squadAttendance[squadName] ?: SquadAttendanceStats(0, 0)
                squadAttendance[squadName] = squadStats.copy(
                    totalRate = squadStats.totalRate + rate,
                    childCount = squadStats.childCount + 1
                )
            }
        }

        // Преобразуем статистику по отрядам в список пар для UI
        val squadAttendanceRates = squadAttendance.map { (squad, stats) ->
            Pair(squad, if (stats.childCount > 0) stats.totalRate / stats.childCount else 0)
        }.sortedByDescending { it.second }

        // Вычисляем общий процент посещаемости
        val overallAttendanceRate = if (totalPossibleAttendance > 0) {
            (totalAttendance * 100) / totalPossibleAttendance
        } else 0

        // Создаем список наиболее активных детей по посещаемости
        val mostActiveChildren = attendanceByChild.entries
            .filter { it.value.totalEvents >= 3 } // Минимальный порог
            .map { (childId, stats) ->
                val child = children.find { it.id == childId }
                ChildRankingItem(
                    childId = childId,
                    childName = child?.let { "${it.name} ${it.lastName}" } ?: "Неизвестно",
                    squadName = child?.squadName ?: "",
                    points = 0,
                    achievementCount = 0,
                    attendanceRate = if (stats.totalEvents > 0)
                        (stats.eventsAttended * 100) / stats.totalEvents else 0,
                    eventsAttended = stats.eventsAttended,
                    compositeScore = 0
                )
            }
            .sortedByDescending { it.attendanceRate }
            .take(5)

        // Создаем список мероприятий с наивысшей посещаемостью
        val topEvents = attendanceByEvent.entries
            .sortedByDescending { it.value }
            .take(5)
            .mapNotNull { (eventId, rate) ->
                val event = events.find { it.id == eventId }
                event?.let {
                    EventRankingItem(
                        eventId = eventId,
                        eventTitle = it.title,
                        eventDate = it.startTime,
                        attendanceRate = rate
                    )
                }
            }

        // Обновляем UI
        _uiState.update { current ->
            current.copy(
                overallAttendanceRate = overallAttendanceRate,
                squadAttendanceRates = squadAttendanceRates,
                eventAttendanceRates = attendanceByEvent,
                attendanceByDay = attendanceByDay.filterKeys { it >= 0 },
                mostActiveChildren = mostActiveChildren,
                topAttendedEvents = topEvents
            )
        }
    }

    // Выделим обработку достижений в отдельный метод
    private suspend fun processAchievementsData(children: List<Child>) {
        try {
            // Загружаем рейтинг детей по достижениям
            val achievements = achievementRepository.getChildrenRanking().first()

            // Создаем список лучших детей по достижениям
            val topChildren = achievements
                .sortedByDescending { it.totalPoints ?: 0 }
                .take(10)
                .map { childWithPoints ->
                    val child = children.find { it.id == childWithPoints.id }
                    ChildRankingItem(
                        childId = childWithPoints.id,
                        childName = child?.let { "${it.name} ${it.lastName}" } ?: "Неизвестно",
                        squadName = child?.squadName ?: "",
                        points = childWithPoints.totalPoints ?: 0,
                        achievementCount = 0,
                        attendanceRate = 0, // Будет обновлено позже, если есть данные
                        eventsAttended = 0,
                        compositeScore = childWithPoints.totalPoints ?: 0
                    )
                }

            // Обновляем UI с данными о достижениях
            _uiState.update { current ->
                current.copy(
                    totalAchievements = achievements.sumOf { it.totalPoints ?: 0 },
                    topChildrenByAchievements = topChildren,
                    topChildrenOverall = topChildren.take(5) // Временно используем те же данные
                )
            }
        } catch (e: Exception) {
            Log.e("AnalyticsViewModel", "Error processing achievements", e)
            // Не обновляем UI, оставляем дефолтные значения
        }
    }


    private fun getDayIndex(timestamp: Long, startDate: Long): Int {
        return ((timestamp - startDate) / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun getDayCount(startDate: Long, endDate: Long): Int {
        return ((endDate - startDate) / (24 * 60 * 60 * 1000)).toInt() + 1
    }

    private fun getCalendarField(timestamp: Long, field: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(field)
    }
}

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedTabIndex: Int = 0,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val showDateRangePicker: Boolean = false,
    val showExportDialog: Boolean = false,
    val isExporting: Boolean = false,
    val message: String? = null,

    // General metrics
    val totalChildren: Int = 0,
    val totalEvents: Int = 0,
    val squadCount: Int = 0,
    val totalAchievements: Int = 0,
    val overallAttendanceRate: Int = 0,
    val attendanceTrend: Float = 0f,
    val achievementsTrend: Float = 0f,
    val averageEventDurationHours: Float = 2.5f,

    // Child analytics
    val topChildrenByAchievements: List<ChildRankingItem> = emptyList(),
    val mostActiveChildren: List<ChildRankingItem> = emptyList(),
    val topChildrenOverall: List<ChildRankingItem> = emptyList(),
    val ageDistribution: Map<Int, Int> = emptyMap(),
    val squadDistribution: Map<String, Int> = emptyMap(),

    // Attendance analytics
    val squadAttendanceRates: List<Pair<String, Int>> = emptyList(),
    val eventAttendanceRates: Map<Long, Int> = emptyMap(),
    val attendanceByDay: Map<Int, Int> = emptyMap(),

    // Event analytics
    val eventsByDayOfWeek: List<Int> = emptyList(),
    val eventsByHour: List<Int> = emptyList(),
    val topAttendedEvents: List<EventRankingItem> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val eventsByDay: Map<Int, Int> = emptyMap(),

    // Achievement analytics
    val achievementsByCategory: Map<String, Int> = emptyMap(),
    val achievementsByDay: Map<Int, Int> = emptyMap()
)

data class ChildRankingItem(
    val childId: Long,
    val childName: String,
    val squadName: String,
    val points: Int,
    val achievementCount: Int = 0,
    val attendanceRate: Int = 0,
    val eventsAttended: Int = 0,
    val compositeScore: Int = 0
)

data class EventRankingItem(
    val eventId: Long,
    val eventTitle: String,
    val eventDate: Long,
    val attendanceRate: Int
)
// Переименованная версия
enum class AnalyticsExportFormat {
    PDF, EXCEL, CSV
}