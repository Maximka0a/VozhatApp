package com.example.vozhatapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.AchievementRepository
import com.example.vozhatapp.data.repository.EventRepository
import com.example.vozhatapp.data.repository.NoteRepository
import com.example.vozhatapp.presentation.home.model.ChildRankingItem
import com.example.vozhatapp.presentation.home.model.ReminderItem
import com.example.vozhatapp.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val eventRepository: EventRepository,
    private val noteRepository: NoteRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val currentTimeMillis = System.currentTimeMillis()

        // Начало и конец текущего дня
        val todayStart = DateUtils.getStartOfDay(currentTimeMillis)
        val todayEnd = DateUtils.getEndOfDay(currentTimeMillis)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Загружаем события на сегодня
            loadTodayEvents(todayStart, todayEnd)

            // Загружаем рейтинг детей по достижениям
            loadChildrenRanking()

            // Загружаем напоминания на сегодня
            loadUpcomingReminders(todayStart, todayEnd)

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadTodayEvents(todayStart: Long, todayEnd: Long) {
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                Log.d("HomeViewModel", "Загрузка событий с ${sdf.format(Date(todayStart))} по ${sdf.format(Date(todayEnd))}")

                eventRepository.allEvents.collect { allEvents ->
                    // Фильтруем события на текущий день
                    val todayEvents = allEvents.filter { event ->
                        event.startTime in todayStart..todayEnd
                    }

                    Log.d("HomeViewModel", "Отфильтровано событий на сегодня: ${todayEvents.size}")
                    _uiState.update { it.copy(todayEvents = todayEvents) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Ошибка при загрузке событий: ${e.message}", e)
            }
        }
    }

    private fun loadChildrenRanking() {
        viewModelScope.launch {
            try {
                // Используем репозиторий достижений для получения рейтинга детей
                achievementRepository.getChildrenRanking().collect { childrenWithPoints ->
                    Log.d("HomeViewModel", "Получено ${childrenWithPoints.size} детей с баллами")

                    // Преобразуем данные из репозитория в модель представления
                    val childrenRanking = childrenWithPoints.map { childWithPoints ->
                        ChildRankingItem(
                            id = childWithPoints.id,
                            name = childWithPoints.name,
                            lastName = childWithPoints.lastName,
                            squadName = childWithPoints.squadName,
                            points = childWithPoints.totalPoints ?: 0
                        )
                    }.sortedByDescending { it.points } // Сортируем по убыванию баллов
                        .take(10) // Берем только топ-10

                    _uiState.update { it.copy(topChildren = childrenRanking) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Ошибка при загрузке рейтинга детей: ${e.message}", e)
            }
        }
    }

    private fun loadUpcomingReminders(todayStart: Long, todayEnd: Long) {
        viewModelScope.launch {
            try {
                noteRepository.getTodayReminders().collect { notes ->
                    val todayReminders = notes.filter { note ->
                        val reminderTime = note.reminderDate ?: 0
                        reminderTime in todayStart..todayEnd
                    }

                    val reminderItems = todayReminders.map { note ->
                        ReminderItem(
                            id = note.id,
                            title = note.title,
                            description = note.content,
                            date = note.reminderDate ?: System.currentTimeMillis()
                        )
                    }
                    _uiState.update { it.copy(upcomingReminders = reminderItems) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Ошибка при загрузке напоминаний: ${e.message}", e)
            }
        }
    }

    fun refresh() {
        loadData()
    }

    fun onChildClick(childId: Long) {
        // Обработка нажатия на карточку ребенка
        // Например, навигация на экран с деталями ребенка
    }
}


data class HomeUiState(
    val isLoading: Boolean = false,
    val todayEvents: List<Event> = emptyList(),
    val topChildren: List<ChildRankingItem> = emptyList(),
    val upcomingReminders: List<ReminderItem> = emptyList()
)