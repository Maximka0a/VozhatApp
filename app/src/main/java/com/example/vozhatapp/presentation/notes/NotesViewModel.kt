package com.example.vozhatapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.NoteRepository
import com.example.vozhatapp.utils.DateUtils
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

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combine all data sources
            combine(
                noteRepository.getAllGeneralNotes(),
                childRepository.getAllChildren(),
                noteRepository.getTodayReminders()
            ) { allNotes, children, todayReminders ->
                // Create a map of child id to child object for quick lookups
                val childrenMap = children.associateBy { it.id }

                // Get notes for specific children
                val childIds = children.map { it.id }
                val childNotesFlows = childIds.map { childId ->
                    noteRepository.getNotesForChild(childId).map { notes ->
                        notes.map { it.copy() } // Make a copy to avoid reference issues
                    }
                }

                // Combine all child notes
                val childNotes = if (childNotesFlows.isNotEmpty()) {
                    childNotesFlows.asFlow().flattenMerge().first()
                } else {
                    emptyList()
                }

                // Combine general notes and child-specific notes
                val allNotesList = allNotes + childNotes

                NotesUiState(
                    isLoading = false,
                    notes = allNotesList,
                    todayReminders = todayReminders,
                    childrenMap = childrenMap
                )
            }.catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Ошибка загрузки заметок: ${error.message}"
                    )
                }
            }.collect { newState ->
                _uiState.update { it.copy(
                    isLoading = false,
                    notes = newState.notes,
                    todayReminders = newState.todayReminders,
                    childrenMap = newState.childrenMap
                ) }
            }
        }
    }

    // Rest of the functions remain the same...
    fun selectTab(index: Int) {
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

    fun setScrolling(isScrolling: Boolean) {
        _uiState.update { it.copy(isScrolling = isScrolling) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
                _uiState.update { it.copy(
                    message = "Заметка удалена"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка при удалении заметки: ${e.message}"
                ) }
            }
        }
    }

    fun completeReminder(reminder: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(reminder)
                _uiState.update { it.copy(
                    message = "Напоминание выполнено"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка при выполнении напоминания: ${e.message}"
                ) }
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
    // Changed from Map<Long?, Child> to Map<Long, Child> to match the type inferred by associateBy
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