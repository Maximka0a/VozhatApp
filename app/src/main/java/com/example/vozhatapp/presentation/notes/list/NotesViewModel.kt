package com.example.vozhatapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val childRepository: ChildRepository
) : ViewModel() {

    // UI state for the notes screen
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    // Сделаем метод публичным чтобы можно было обновлять данные
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Получаем все заметки (общие)
                val generalNotes = noteRepository.getAllGeneralNotes().first()

                // Получаем список всех детей
                val children = childRepository.getAllChildren().first()
                val childrenMap = children.associateBy { it.id }

                // Получаем заметки для детей
                val childIds = children.map { it.id }
                val childNotesList = mutableListOf<Note>()

                // Для каждого ребенка загружаем его заметки
                childIds.forEach { childId ->
                    val childNotes = noteRepository.getNotesForChild(childId).first()
                    childNotesList.addAll(childNotes)
                }

                // Объединяем общие заметки и заметки о детях
                val allNotes = generalNotes + childNotesList

                // Заметки с напоминаниями на сегодня
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val tomorrow = today + 24 * 60 * 60 * 1000

                val todayReminders = allNotes.filter { note ->
                    note.type == 1 &&
                            note.reminderDate != null &&
                            note.reminderDate in today until tomorrow
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notes = allNotes,
                        childrenMap = childrenMap,
                        todayReminders = todayReminders
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Ошибка загрузки заметок: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectTab(index: Int) {
        // Fix for the issue where tab navigation doesn't work properly
        // We no longer set isScrolling here as that can interfere with tab selection
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun toggleSearchBar() {
        _uiState.update {
            it.copy(
                isSearchBarVisible = !it.isSearchBarVisible,
                searchQuery = if (it.isSearchBarVisible) "" else it.searchQuery
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun toggleFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = !it.showFilterDialog) }
    }

    fun updateSortOrder(order: NotesSortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun toggleShowRemindersOnly() {
        _uiState.update { it.copy(showRemindersOnly = !it.showRemindersOnly) }
    }

    // Setting scroll state without affecting isScrolling too often
    private var lastScrollTime = 0L
    fun setScrolling(isScrolling: Boolean) {
        val currentTime = System.currentTimeMillis()
        // Добавляем минимальное время между обновлениями, чтобы избежать частых изменений
        if (isScrolling || currentTime - lastScrollTime > 300) {
            _uiState.update { it.copy(isScrolling = isScrolling) }
            lastScrollTime = currentTime
        }
    }

    // Fix for delete issues - сначала обновляем состояние, потом удаляем из БД
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                // Получаем текущий список заметок
                val currentNotes = _uiState.value.notes.toMutableList()
                // Удаляем заметку из списка
                currentNotes.removeAll { it.id == note.id }

                // Обновляем UI состояние
                _uiState.update {
                    it.copy(
                        notes = currentNotes,
                        // Обновляем также список сегодняшних напоминаний, если нужно
                        todayReminders = it.todayReminders.filter { reminder -> reminder.id != note.id }
                    )
                }

                // Затем удаляем из БД
                noteRepository.deleteNote(note)

                _uiState.update { it.copy(message = "Заметка удалена") }
            } catch (e: Exception) {
                // При ошибке перезагружаем все данные
                _uiState.update {
                    it.copy(message = "Ошибка при удалении заметки: ${e.message}")
                }
                loadData() // Обновляем данные в случае ошибки
            }
        }
    }

    fun completeReminder(reminder: Note) {
        viewModelScope.launch {
            try {
                // То же самое - сначала обновляем UI
                val currentReminders = _uiState.value.todayReminders.toMutableList()
                currentReminders.removeAll { it.id == reminder.id }

                val currentNotes = _uiState.value.notes.toMutableList()
                currentNotes.removeAll { it.id == reminder.id }

                _uiState.update {
                    it.copy(
                        todayReminders = currentReminders,
                        notes = currentNotes
                    )
                }

                // Затем удаляем из БД
                noteRepository.deleteNote(reminder)

                _uiState.update { it.copy(message = "Напоминание выполнено") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = "Ошибка при выполнении напоминания: ${e.message}")
                }
                loadData() // Обновляем данные в случае ошибки
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val todayReminders: List<Note> = emptyList(),
    val childrenMap: Map<Long, Child> = emptyMap(),
    val selectedTabIndex: Int = 0,
    val isSearchBarVisible: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: NotesSortOrder = NotesSortOrder.DATE_DESC,
    val showRemindersOnly: Boolean = false,
    val showFilterDialog: Boolean = false,
    val isScrolling: Boolean = false,
    val message: String? = null
)