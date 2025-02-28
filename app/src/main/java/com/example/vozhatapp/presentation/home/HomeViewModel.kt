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
import com.example.vozhatapp.presentation.home.model.ReminderItem

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

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Получаем текущую системную дату
                val currentDate = Date()

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
                Log.d("HomeViewModel", "Текущая дата: ${sdf.format(currentDate)}")
                Log.d("HomeViewModel", "Загрузка событий с ${sdf.format(todayStart)} по ${sdf.format(todayEnd)}")

                // Вывести содержимое всей таблицы событий для отладки
                eventRepository.allEvents.collect { allEvents ->
                    Log.d("HomeViewModel", "Всего событий в базе: ${allEvents.size}")
                    for (event in allEvents) {
                        Log.d("HomeViewModel", "Событие в БД: ${event.id}, ${event.title}, ${sdf.format(event.startTime)}")
                    }

                    // Фильтруем события на текущий день вручную
                    val todayEvents = allEvents.filter { event ->
                        val eventTime = event.startTime.time
                        val isToday = eventTime >= todayStart.time && eventTime <= todayEnd.time
                        Log.d("HomeViewModel", "Событие ${event.title} (${sdf.format(event.startTime)}) сегодня? $isToday")
                        isToday
                    }

                    Log.d("HomeViewModel", "Отфильтровано событий на сегодня: ${todayEvents.size}")

                    _uiState.update { it.copy(todayEvents = todayEvents, isLoading = false) }
                }
            } catch (e: Exception) {
                // В случае ошибки логируем ее
                Log.e("HomeViewModel", "Ошибка при загрузке событий: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false) }
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
                _uiState.update { it.copy(upcomingReminders = reminders) }
            }
        }
    }

    // Метод для ручного обновления данных
    fun refresh() {
        loadData()
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val todayEvents: List<Event> = emptyList(),
    val topChildren: List<Child> = emptyList(),
    val upcomingReminders: List<ReminderItem> = emptyList()
)

