package com.example.vozhatapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.EventRepository
import com.example.vozhatapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val eventRepository: EventRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun createTestEvents(): List<Event> {
        val currentTime = System.currentTimeMillis()

        return listOf(
            Event(
                id = 101,
                title = "Утренняя зарядка",
                description = "Общая зарядка для всех отрядов",
                startTime = Date(currentTime - 30 * 60 * 1000), // 30 минут назад
                endTime = Date(currentTime + 15 * 60 * 1000), // 15 минут вперед
                location = "Спортивная площадка",
                status = 1, // В процессе
                createdBy = 4,
                createdAt = Date(currentTime - 24 * 60 * 60 * 1000) // Создано вчера
            ),
            Event(
                id = 102,
                title = "Завтрак",
                description = "Общий завтрак для всех отрядов",
                startTime = Date(currentTime + 30 * 60 * 1000), // Через 30 минут
                endTime = Date(currentTime + 90 * 60 * 1000), // Через 1.5 часа
                location = "Столовая",
                status = 0, // Предстоит
                createdBy = 4,
                createdAt = Date(currentTime - 24 * 60 * 60 * 1000) // Создано вчера
            ),
            Event(
                id = 103,
                title = "Творческий мастер-класс",
                description = "Рисование на камнях и создание поделок из природных материалов",
                startTime = Date(currentTime + 180 * 60 * 1000), // Через 3 часа
                endTime = Date(currentTime + 270 * 60 * 1000), // Через 4.5 часа
                location = "Творческая мастерская",
                status = 0, // Предстоит
                createdBy = 4,
                createdAt = Date(currentTime - 24 * 60 * 60 * 1000) // Создано вчера
            )
        )
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Сначала загружаем тестовые события для быстрого отображения интерфейса
            val testEvents = createTestEvents()
            _uiState.update { it.copy(todayEvents = testEvents) }

            try {
                // Получаем текущую системную дату
                val calendar = Calendar.getInstance()

                // Начало текущего дня (00:00:00)
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                // Конец текущего дня (23:59:59)
                val todayEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                Log.d("HomeViewModel", "Текущая дата: ${sdf.format(Date())}")
                Log.d("HomeViewModel", "Загрузка событий с ${sdf.format(todayStart)} по ${sdf.format(todayEnd)}")

                // Загружаем события на текущий день
                eventRepository.getEventsByDateRange(todayStart, todayEnd).collect { dbEvents ->
                    Log.d("HomeViewModel", "Получено из базы данных: ${dbEvents.size} событий")
                    if (dbEvents.isNotEmpty()) {
                        // Если в БД есть события на текущий день, используем их
                        _uiState.update { it.copy(todayEvents = dbEvents) }
                    } else {
                        // Если событий в БД нет, используем тестовые события
                        Log.d("HomeViewModel", "Используем тестовые события, так как в БД ничего не найдено")
                        _uiState.update { it.copy(todayEvents = testEvents) }
                    }
                }
            } catch (e: Exception) {
                // В случае ошибки используем тестовые события
                Log.e("HomeViewModel", "Ошибка при загрузке событий: ${e.message}", e)
                _uiState.update { it.copy(todayEvents = testEvents) }
            }
        }

        viewModelScope.launch {
            // Загружаем список детей
            childRepository.allChildren.collect { children ->
                val sortedChildren = children.sortedByDescending { child ->
                    // В реальном приложении здесь будет логика сортировки по достижениям
                    // Сейчас просто используем случайные значения для демонстрации
                    (0..100).random()
                }.take(10)

                _uiState.update { it.copy(topChildren = sortedChildren) }
            }
        }

        viewModelScope.launch {
            // Загружаем предстоящие напоминания
            noteRepository.getUpcomingReminders().collect { notes ->
                val reminders = notes.map { note ->
                    ReminderItem(
                        id = note.id,
                        title = note.title,
                        description = note.content,
                        date = note.reminderDate ?: Date()
                    )
                }
                _uiState.update { it.copy(upcomingReminders = reminders, isLoading = false) }
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val todayEvents: List<Event> = emptyList(),
    val topChildren: List<Child> = emptyList(),
    val upcomingReminders: List<ReminderItem> = emptyList()
)

data class ReminderItem(
    val id: Long,
    val title: String,
    val description: String,
    val date: Date
)