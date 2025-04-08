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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val eventRepository: EventRepository,
    private val noteRepository: NoteRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val TAG = "HomeViewModel"
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val currentTimeMillis = System.currentTimeMillis()
        val todayStart = DateUtils.getStartOfDay(currentTimeMillis)
        val todayEnd = DateUtils.getEndOfDay(currentTimeMillis)

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, message = null) }
                Log.d(TAG, "Начало загрузки данных для главного экрана")

                // Используем async для параллельного запуска всех загрузок
                val childrenCountDeferred = async {
                    try {
                        withTimeoutOrNull(5000) { // 5 секунд тайм-аут
                            childRepository.getAllChildren()
                                .catch { e ->
                                    Log.e(TAG, "Ошибка при получении списка детей", e)
                                    emptyList<Any>()
                                }.first()
                        }?.size ?: 0
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при загрузке количества детей", e)
                        0
                    }
                }

                val eventsDeferred = async {
                    try {
                        withTimeoutOrNull(5000) {
                            eventRepository.getEventsByDateRange(todayStart, todayEnd)
                                .catch { e ->
                                    Log.e(TAG, "Ошибка при загрузке событий", e)
                                    emptyList<Event>()
                                }.first()
                        } ?: emptyList()
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при загрузке событий: ${e.message}", e)
                        emptyList()
                    }
                }

                val rankingDeferred = async {
                    try {
                        withTimeoutOrNull(5000) {
                            achievementRepository.getChildrenRanking()
                                .catch { e ->
                                    Log.e(TAG, "Ошибка при загрузке рейтинга", e)
                                    emptyList<Any>()
                                }.first()
                        }?.map { childWithPoints ->
                            ChildRankingItem(
                                id = childWithPoints.id,
                                name = childWithPoints.name,
                                lastName = childWithPoints.lastName,
                                squadName = childWithPoints.squadName,
                                points = childWithPoints.totalPoints ?: 0,
                                photoUrl = null // Установка в null, чтобы избежать ошибок
                            )
                        }?.sortedByDescending { it.points } ?: emptyList()
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при загрузке рейтинга детей: ${e.message}", e)
                        emptyList()
                    }
                }

                val remindersDeferred = async {
                    try {
                        withTimeoutOrNull(5000) {
                            noteRepository.getTodayReminders()
                                .catch { e ->
                                    Log.e(TAG, "Ошибка при загрузке напоминаний", e)
                                    emptyList<Any>()
                                }.first()
                        }?.filter { it.reminderDate != null && it.reminderDate in todayStart..todayEnd }
                            ?.map { note ->
                                ReminderItem(
                                    id = note.id,
                                    title = note.title,
                                    description = note.content,
                                    date = note.reminderDate ?: currentTimeMillis
                                )
                            }?.sortedBy { it.date } ?: emptyList()
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при загрузке напоминаний: ${e.message}", e)
                        emptyList()
                    }
                }

                // Ждем завершения всех async задач
                val childrenCount = childrenCountDeferred.await()
                val events = eventsDeferred.await()
                val ranking = rankingDeferred.await()
                val reminders = remindersDeferred.await()

                // Обновляем UI-состояние одним вызовом
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        totalChildrenCount = childrenCount,
                        todayEvents = events,
                        topChildren = ranking,
                        upcomingReminders = reminders
                    )
                }

                Log.d(TAG, "Загрузка данных завершена: детей=${childrenCount}, событий=${events.size}, " +
                        "детей в рейтинге=${ranking.size}, напоминаний=${reminders.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Неожиданная ошибка при загрузке данных", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Ошибка при загрузке данных: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val todayEvents: List<Event> = emptyList(),
    val topChildren: List<ChildRankingItem> = emptyList(),
    val upcomingReminders: List<ReminderItem> = emptyList(),
    val totalChildrenCount: Int = 0,
    val message: String? = null
)