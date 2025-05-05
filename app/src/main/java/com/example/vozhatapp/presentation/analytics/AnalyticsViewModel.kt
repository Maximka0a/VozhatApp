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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        // Устанавливаем начальный диапазон дат - последние 30 дней
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

    /**
     * Выбор вкладки аналитики
     */
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    /**
     * Показать/скрыть диалог выбора периода
     */
    fun toggleDateRangePicker() {
        _uiState.update { it.copy(showDateRangePicker = !it.showDateRangePicker) }
    }

    /**
     * Показать/скрыть диалог экспорта
     */
    fun toggleExportDialog() {
        _uiState.update { it.copy(showExportDialog = !it.showExportDialog) }
    }

    /**
     * Обновить диапазон дат для анализа
     */
    fun updateDateRange(startDate: Long, endDate: Long) {
        // Проверка, что конечная дата не раньше начальной
        if (endDate < startDate) {
            // Если конечная дата раньше начальной, выдаем ошибку и не меняем текущие даты
            _uiState.update {
                it.copy(
                    message = "Ошибка: конечная дата не может быть раньше начальной",
                    showDateRangePicker = true // Оставляем диалог открытым для исправления
                )
            }
            return
        }

        // Даты корректны, обновляем состояние
        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                isLoading = true,
                showDateRangePicker = false
            )
        }

        loadAllAnalyticsData(startDate, endDate)
    }
    /**
     * Очистить сообщение
     */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /**
     * Загружает все данные аналитики параллельно
     */
    private fun loadAllAnalyticsData(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Загрузка базовых данных для расчетов
                val children = try {
                    childRepository.getAllChildren().first()
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка загрузки данных о детях", e)
                    emptyList()
                }

                val events = try {
                    eventRepository.getEventsByDateRange(startDate, endDate).first()
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка загрузки данных о событиях", e)
                    emptyList()
                }

                if (children.isEmpty() && events.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Нет данных для анализа в выбранном периоде"
                        )
                    }
                    return@launch
                }

                // Инициализируем базовые метрики
                val squads = children.map { it.squadName }.distinct()
                val ageDistribution = children.groupBy { it.age }.mapValues { it.value.size }
                val squadDistribution = children.groupBy { it.squadName }.mapValues { it.value.size }

                // Обновляем начальное состояние с базовыми метриками
                _uiState.update { current ->
                    current.copy(
                        totalChildren = children.size,
                        totalEvents = events.size,
                        squadCount = squads.size,
                        ageDistribution = ageDistribution,
                        squadDistribution = squadDistribution,
                        recentEvents = events.sortedByDescending { it.startTime }.take(5)
                    )
                }

                // Параллельно загружаем данные аналитики
                val attendanceResult = async { loadAttendanceAnalytics(events, children, startDate) }
                val achievementsResult = async { loadAchievementsAnalytics(children, startDate, endDate) }
                val eventsAnalyticsResult = async { loadEventsAnalytics(events) }

                // Ждем завершения всех задач
                val (attendanceAnalytics, achievementsAnalytics, eventsAnalytics) =
                    awaitAll(attendanceResult, achievementsResult, eventsAnalyticsResult)

                // Обновляем модель состояния с полученными данными
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,

                        // Общие метрики
                        overallAttendanceRate = (attendanceAnalytics as AttendanceAnalytics).overallRate,
                        attendanceTrend = (attendanceAnalytics as AttendanceAnalytics).trend,
                        totalAchievements = (achievementsAnalytics as AchievementsAnalytics).totalCount,
                        achievementsTrend = (achievementsAnalytics as AchievementsAnalytics).trend,
                        averageEventDurationHours = (eventsAnalytics as EventsAnalytics).averageDurationHours,

                        // Посещаемость
                        squadAttendanceRates = (attendanceAnalytics as AttendanceAnalytics).squadRates,
                        eventAttendanceRates = (attendanceAnalytics as AttendanceAnalytics).eventRates,
                        attendanceByDay = (attendanceAnalytics as AttendanceAnalytics).byDay,
                        mostActiveChildren = (attendanceAnalytics as AttendanceAnalytics).mostActiveChildren,
                        topAttendedEvents = (attendanceAnalytics as AttendanceAnalytics).topEvents,

                        // Достижения
                        topChildrenByAchievements = (achievementsAnalytics as AchievementsAnalytics).topChildren,
                        achievementsByCategory = (achievementsAnalytics as AchievementsAnalytics).byCategory,
                        achievementsByDay = (achievementsAnalytics as AchievementsAnalytics).byDay,

                        // События
                        eventsByDayOfWeek = (eventsAnalytics as EventsAnalytics).byDayOfWeek,
                        eventsByHour = (eventsAnalytics as EventsAnalytics).byHour,
                        eventsByDay = (eventsAnalytics as EventsAnalytics).byDay,

                        // Рейтинг детей (композитный)
                        topChildrenOverall = calculateOverallRanking(
                            (attendanceAnalytics as AttendanceAnalytics).mostActiveChildren,
                            (achievementsAnalytics as AchievementsAnalytics).topChildren
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке данных аналитики", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Произошла ошибка при загрузке данных: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Загружает и обрабатывает данные о посещаемости
     */
    private suspend fun loadAttendanceAnalytics(events: List<Event>, children: List<Child>, startDate: Long): AttendanceAnalytics {
        return withContext(Dispatchers.Default) {
            try {
                // Временные переменные для хранения статистики
                var totalAttendance = 0
                var totalPossibleAttendance = 0
                val attendanceByChild = mutableMapOf<Long, Pair<Int, Int>>() // childId -> (attended, total)
                val eventRates = mutableMapOf<Long, Int>() // eventId -> rate
                val dayRates = mutableMapOf<Int, Pair<Int, Int>>() // day -> (totalRate, eventCount)

                // Получаем данные о посещаемости для каждого события
                events.forEach { event ->
                    try {
                        val attendance = attendanceRepository.getAttendanceForEvent(event.id).first()
                        if (attendance.isNotEmpty()) {
                            val present = attendance.count { it.isPresent }
                            val rate = (present * 100) / attendance.size

                            // Сохраняем посещаемость мероприятия
                            eventRates[event.id] = rate

                            // Добавляем в общий счетчик
                            totalAttendance += present
                            totalPossibleAttendance += attendance.size

                            // Добавляем в счетчик по дням
                            val dayIndex = getDayIndex(event.startTime, startDate)
                            val currentDayRate = dayRates[dayIndex] ?: Pair(0, 0)
                            dayRates[dayIndex] = currentDayRate.copy(
                                first = currentDayRate.first + rate,
                                second = currentDayRate.second + 1
                            )

                            // Обновляем посещаемость для каждого ребенка
                            attendance.forEach { record ->
                                val childStats = attendanceByChild[record.childId] ?: Pair(0, 0)
                                attendanceByChild[record.childId] = if (record.isPresent) {
                                    childStats.copy(first = childStats.first + 1, second = childStats.second + 1)
                                } else {
                                    childStats.copy(second = childStats.second + 1)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Ошибка при обработке посещаемости для события ${event.id}", e)
                    }
                }

                // Вычисляем статистику посещаемости по отрядам
                val squadAttendance = mutableMapOf<String, Pair<Int, Int>>() // squad -> (totalRate, childCount)

                children.forEach { child ->
                    val stats = attendanceByChild[child.id]
                    if (stats != null && stats.second > 0) {
                        val rate = (stats.first * 100) / stats.second
                        val squad = squadAttendance[child.squadName] ?: Pair(0, 0)
                        squadAttendance[child.squadName] = squad.copy(
                            first = squad.first + rate,
                            second = squad.second + 1
                        )
                    }
                }

                // Преобразуем статистику по отрядам в список пар
                val squadRates = squadAttendance.map { (squad, stats) ->
                    Pair(squad, if (stats.second > 0) stats.first / stats.second else 0)
                }.sortedByDescending { it.second }

                // Вычисляем общий процент посещаемости
                val overallRate = if (totalPossibleAttendance > 0) {
                    (totalAttendance * 100) / totalPossibleAttendance
                } else 0

                // Преобразуем статистику по дням
                val attendanceByDay = dayRates.mapValues { (_, pair) ->
                    if (pair.second > 0) pair.first / pair.second else 0
                }

                // Самые активные дети
                val mostActive = attendanceByChild.entries
                    .filter { it.value.second >= 3 } // минимум 3 события для значимости
                    .map { (childId, stats) ->
                        val child = children.find { it.id == childId }
                        val attendanceRate = if (stats.second > 0) (stats.first * 100) / stats.second else 0

                        ChildRankingItem(
                            childId = childId,
                            childName = child?.let { "${it.name} ${it.lastName}" } ?: "Unknown",
                            squadName = child?.squadName ?: "",
                            points = 0,
                            achievementCount = 0,
                            attendanceRate = attendanceRate,
                            eventsAttended = stats.first,
                            compositeScore = attendanceRate
                        )
                    }
                    .sortedByDescending { it.attendanceRate }
                    .take(10)

                // Лучшие мероприятия по посещаемости
                val topEvents = eventRates.entries
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

                // Вычисляем тренд (сравниваем среднюю первую и вторую половину периода)
                val trend = calculateAttendanceTrend(attendanceByDay)

                AttendanceAnalytics(
                    overallRate = overallRate,
                    trend = trend,
                    squadRates = squadRates,
                    eventRates = eventRates,
                    byDay = attendanceByDay,
                    mostActiveChildren = mostActive,
                    topEvents = topEvents
                )
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при анализе посещаемости", e)
                AttendanceAnalytics() // возвращаем пустой объект в случае ошибки
            }
        }
    }

    /**
     * Загружает и обрабатывает данные о достижениях
     */
    private suspend fun loadAchievementsAnalytics(children: List<Child>, startDate: Long, endDate: Long): AchievementsAnalytics {
        return withContext(Dispatchers.Default) {
            try {
                // Получаем рейтинг детей по достижениям
                val childrenRanking = achievementRepository.getChildrenRanking().first()

                // Получаем все достижения для анализа категорий и трендов
                val allAchievements = mutableListOf<Achievement>()
                childrenRanking.forEach { childWithPoints ->
                    try {
                        val achievements = achievementRepository.getAchievementsForChild(childWithPoints.id).first()
                            .filter { it.date in startDate..endDate }
                        allAchievements.addAll(achievements)
                    } catch (e: Exception) {
                        Log.w(TAG, "Не удалось загрузить достижения для ребенка ${childWithPoints.id}", e)
                    }
                }

                // Определяем категории достижений по заголовкам и описаниям
                val categories = mapOf(
                    "Спортивные" to listOf("спорт", "бег", "физкультура", "соревнова", "футбол", "волейбол"),
                    "Творческие" to listOf("творчес", "рисунок", "рисова", "танцы", "пение", "артис", "музык"),
                    "Интеллектуальные" to listOf("интеллект", "учеба", "математика", "наука", "знания", "умник"),
                    "Социальные" to listOf("социал", "помощь", "взаимодей", "коммуникаци", "друж", "команд"),
                    "Организационные" to listOf("организаци", "лидерств", "ответствен", "дисциплин")
                )

                // Распределяем достижения по категориям
                val achievementsByCategory = mutableMapOf<String, Int>()
                categories.keys.forEach { achievementsByCategory[it] = 0 }

                allAchievements.forEach { achievement ->
                    val text = (achievement.title + " " + (achievement.description ?: "")).lowercase()
                    var assigned = false

                    categories.forEach { (category, keywords) ->
                        if (!assigned && keywords.any { text.contains(it) }) {
                            achievementsByCategory[category] = (achievementsByCategory[category] ?: 0) + 1
                            assigned = true
                        }
                    }

                    if (!assigned) {
                        achievementsByCategory["Другие"] = (achievementsByCategory["Другие"] ?: 0) + 1
                    }
                }

                // Распределение по дням
                val achievementsByDay = allAchievements
                    .groupBy { getDayIndex(it.date, startDate) }
                    .mapValues { it.value.size }

                // Создаем список лучших детей
                val topChildren = childrenRanking
                    .sortedByDescending { it.totalPoints ?: 0 }
                    .take(10)
                    .map { ranking ->
                        val child = children.find { it.id == ranking.id }
                        val achievementsList = try {
                            achievementRepository.getAchievementsForChild(ranking.id).first()
                                .filter { it.date in startDate..endDate }
                        } catch (e: Exception) {
                            emptyList()
                        }

                        ChildRankingItem(
                            childId = ranking.id,
                            childName = child?.let { "${it.name} ${it.lastName}" } ?: "Unknown",
                            squadName = child?.squadName ?: "",
                            points = ranking.totalPoints ?: 0,
                            achievementCount = achievementsList.size,
                            attendanceRate = 0,
                            eventsAttended = 0,
                            compositeScore = ranking.totalPoints ?: 0
                        )
                    }

                // Вычисляем тренд
                val trend = calculateAchievementsTrend(achievementsByDay)

                AchievementsAnalytics(
                    totalCount = allAchievements.size,
                    totalPoints = allAchievements.sumOf { it.points },
                    trend = trend,
                    byCategory = achievementsByCategory,
                    byDay = achievementsByDay,
                    topChildren = topChildren
                )

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при анализе достижений", e)
                AchievementsAnalytics() // возвращаем пустой объект в случае ошибки
            }
        }
    }

    /**
     * Загружает и обрабатывает данные о событиях
     */
    private suspend fun loadEventsAnalytics(events: List<Event>): EventsAnalytics {
        return withContext(Dispatchers.Default) {
            try {
                // Статистика по дням недели
                val eventsByDayOfWeek = Array(7) { 0 }
                events.forEach { event ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = event.startTime
                    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 6) % 7 // 0 - Пн, 6 - Вс
                    eventsByDayOfWeek[dayOfWeek]++
                }

                // Статистика по часам
                val eventsByHour = Array(24) { 0 }
                events.forEach { event ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = event.startTime
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    eventsByHour[hour]++
                }

                // Средняя продолжительность события в часах
                val totalDurationMillis = events.sumOf { it.endTime - it.startTime }
                val avgDurationHours = if (events.isNotEmpty()) {
                    totalDurationMillis / (events.size * 3600000.0)
                } else 0.0

                // События по дням
                val earliestEvent = events.minByOrNull { it.startTime }?.startTime ?: 0L
                val eventsByDay = events.groupBy { getDayIndex(it.startTime, earliestEvent) }
                    .mapValues { it.value.size }

                EventsAnalytics(
                    byDayOfWeek = eventsByDayOfWeek.toList(),
                    byHour = eventsByHour.toList(),
                    averageDurationHours = avgDurationHours.toFloat(),
                    byDay = eventsByDay
                )

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при анализе событий", e)
                EventsAnalytics()
            }
        }
    }

    /**
     * Вычисляет общий рейтинг детей на основе посещаемости и достижений
     */
    private fun calculateOverallRanking(
        attendanceRanking: List<ChildRankingItem>,
        achievementsRanking: List<ChildRankingItem>
    ): List<ChildRankingItem> {
        val overallScores = mutableMapOf<Long, ChildRankingItem>()

        // Добавляем очки за посещаемость
        attendanceRanking.forEach { child ->
            overallScores[child.childId] = child.copy(
                compositeScore = child.attendanceRate * 5 // умножаем на вес
            )
        }

        // Добавляем очки за достижения
        achievementsRanking.forEach { child ->
            val existing = overallScores[child.childId]
            if (existing != null) {
                overallScores[child.childId] = existing.copy(
                    points = child.points,
                    achievementCount = child.achievementCount,
                    compositeScore = existing.compositeScore + child.points
                )
            } else {
                overallScores[child.childId] = child
            }
        }

        return overallScores.values.sortedByDescending { it.compositeScore }.take(10)
    }

    /**
     * Определяет тренд посещаемости, сравнивая первую и вторую половину периода
     */
    private fun calculateAttendanceTrend(attendanceByDay: Map<Int, Int>): Float {
        if (attendanceByDay.isEmpty()) return 0f

        val days = attendanceByDay.keys.sorted()
        if (days.size < 4) return 0f // нужно минимум 4 дня для значимого тренда

        val midPoint = days.size / 2
        val firstHalf = days.take(midPoint)
        val secondHalf = days.drop(midPoint)

        val firstHalfAvg = firstHalf.map { attendanceByDay[it] ?: 0 }.average()
        val secondHalfAvg = secondHalf.map { attendanceByDay[it] ?: 0 }.average()

        if (firstHalfAvg == 0.0) return 0f

        // Вычисляем процентное изменение
        return ((secondHalfAvg - firstHalfAvg) / firstHalfAvg * 100).toFloat()
    }

    /**
     * Определяет тренд достижений, сравнивая первую и вторую половину периода
     */
    private fun calculateAchievementsTrend(achievementsByDay: Map<Int, Int>): Float {
        if (achievementsByDay.isEmpty()) return 0f

        val days = achievementsByDay.keys.sorted()
        if (days.size < 4) return 0f // нужно минимум 4 дня для значимого тренда

        val midPoint = days.size / 2
        val firstHalf = days.take(midPoint)
        val secondHalf = days.drop(midPoint)

        val firstHalfSum = firstHalf.sumOf { achievementsByDay[it] ?: 0 }
        val secondHalfSum = secondHalf.sumOf { achievementsByDay[it] ?: 0 }

        if (firstHalfSum == 0) return 0f

        // Вычисляем процентное изменение
        return ((secondHalfSum - firstHalfSum).toFloat() / firstHalfSum * 100)
    }

    /**
     * Получает индекс дня относительно базовой даты
     */
    private fun getDayIndex(timestamp: Long, baseTimestamp: Long): Int {
        return ((timestamp - baseTimestamp) / (24 * 60 * 60 * 1000)).toInt()
    }

    companion object {
        private const val TAG = "AnalyticsViewModel"
    }

    // Вспомогательные классы для инкапсуляции данных аналитики

    private data class AttendanceAnalytics(
        val overallRate: Int = 0,
        val trend: Float = 0f,
        val squadRates: List<Pair<String, Int>> = emptyList(),
        val eventRates: Map<Long, Int> = emptyMap(),
        val byDay: Map<Int, Int> = emptyMap(),
        val mostActiveChildren: List<ChildRankingItem> = emptyList(),
        val topEvents: List<EventRankingItem> = emptyList()
    )

    private data class AchievementsAnalytics(
        val totalCount: Int = 0,
        val totalPoints: Int = 0,
        val trend: Float = 0f,
        val byCategory: Map<String, Int> = emptyMap(),
        val byDay: Map<Int, Int> = emptyMap(),
        val topChildren: List<ChildRankingItem> = emptyList()
    )

    private data class EventsAnalytics(
        val byDayOfWeek: List<Int> = List(7) { 0 },
        val byHour: List<Int> = List(24) { 0 },
        val averageDurationHours: Float = 0f,
        val byDay: Map<Int, Int> = emptyMap()
    )
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