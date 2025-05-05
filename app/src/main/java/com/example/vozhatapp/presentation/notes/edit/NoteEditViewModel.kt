package com.example.vozhatapp.presentation.notes.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val childRepository: ChildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    private var originalNote: Note? = null

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            try {
                childRepository.getAllChildren()
                    .catch { e ->
                        _uiState.update { it.copy(
                            message = "Ошибка при загрузке списка детей: ${e.message}"
                        ) }
                    }
                    .collect { children ->
                        _uiState.update { it.copy(
                            availableChildren = children
                        ) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка при загрузке списка детей: ${e.message}"
                ) }
            }
        }
    }

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Try to find the note in general notes
                var foundNote: Note? = null

                noteRepository.getAllGeneralNotes()
                    .collect { notes ->
                        val note = notes.find { it.id == noteId }
                        if (note != null) {
                            foundNote = note
                            updateStateWithNote(note)
                        }
                    }

                // If not found, check each child's notes
                if (foundNote == null) {
                    childRepository.getAllChildren().first().forEach { child ->
                        noteRepository.getNotesForChild(child.id).collect { notes ->
                            val note = notes.find { it.id == noteId }
                            if (note != null) {
                                foundNote = note
                                updateStateWithNote(note, child)
                            }
                        }
                    }
                }

                if (foundNote == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        message = "Заметка не найдена"
                    ) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    message = "Ошибка при загрузке заметки: ${e.message}"
                ) }
            }
        }
    }

    private suspend fun updateStateWithNote(note: Note, child: Child? = null) {
        originalNote = note

        _uiState.update { it.copy(
            isLoading = false,
            noteId = note.id,
            title = note.title,
            content = note.content,
            noteType = note.type,
            reminderDate = note.reminderDate,
            selectedChild = if (note.childId != null) {
                child ?: childRepository.getChildById(note.childId).first()
            } else null
        ) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(
            title = title,
            hasChanges = true
        ) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(
            content = content,
            hasChanges = true
        ) }
    }

    fun toggleNoteType() {
        val newType = if (_uiState.value.noteType == 0) 1 else 0
        _uiState.update { it.copy(
            noteType = newType,
            hasChanges = true
        ) }
    }

    fun toggleChildSelectorDialog() {
        _uiState.update { it.copy(
            showChildSelector = !it.showChildSelector,
            childSearchQuery = ""
        ) }
    }

    fun updateChildSearchQuery(query: String) {
        _uiState.update { it.copy(childSearchQuery = query) }
    }

    fun selectChild(child: Child) {
        _uiState.update { it.copy(
            selectedChild = child,
            showChildSelector = false, // Закрываем диалог при выборе ребенка
            hasChanges = true
        ) }
    }

    fun clearSelectedChild() {
        _uiState.update { it.copy(
            selectedChild = null,
            hasChanges = true
        ) }
    }

    fun toggleDatePickerDialog() {
        _uiState.update { it.copy(
            showDatePicker = !it.showDatePicker
        ) }
    }

    fun setReminderDate(date: Long) {
        val currentTime = System.currentTimeMillis()

        // Проверяем, что дата напоминания находится в будущем
        if (date <= currentTime) {
            _uiState.update { it.copy(
                message = "Нельзя установить напоминание на прошедшую дату и время",
                showDatePicker = false
            ) }
            return
        }

        _uiState.update { it.copy(
            reminderDate = date,
            showDatePicker = false, // Закрываем диалог при выборе даты
            hasChanges = true
        ) }
    }

    fun clearReminderDate() {
        _uiState.update { it.copy(
            reminderDate = null,
            hasChanges = true
        ) }
    }

    fun saveNote() {
        viewModelScope.launch {
            try {
                val state = _uiState.value

                // Basic validation
                if (state.title.isBlank() || state.content.isBlank()) {
                    _uiState.update { it.copy(
                        message = "Заголовок и содержание не могут быть пустыми"
                    ) }
                    return@launch
                }

                // If it's a reminder, validate reminder date
                if (state.noteType == 1) {
                    if (state.reminderDate == null) {
                        _uiState.update { it.copy(
                            message = "Для напоминания необходимо указать дату"
                        ) }
                        return@launch
                    }

                    // Дополнительная проверка даты напоминания перед сохранением
                    val currentTime = System.currentTimeMillis()
                    if (state.reminderDate <= currentTime) {
                        _uiState.update { it.copy(
                            message = "Нельзя установить напоминание на прошедшую дату и время"
                        ) }
                        return@launch
                    }
                }

                val note = Note(
                    id = state.noteId,
                    title = state.title,
                    content = state.content,
                    childId = state.selectedChild?.id,
                    type = state.noteType,
                    reminderDate = state.reminderDate,
                    createdAt = originalNote?.createdAt ?: System.currentTimeMillis()
                )

                val resultId = if (state.noteId > 0) {
                    noteRepository.updateNote(note)
                    state.noteId
                } else {
                    noteRepository.insertNote(note)
                }

                _uiState.update { it.copy(
                    noteId = resultId,
                    message = "Заметка сохранена",
                    noteSaved = true, // Устанавливаем флаг для навигации назад
                    hasChanges = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка при сохранении заметки: ${e.message}"
                ) }
            }
        }
    }

    fun showDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = true) }
    }

    fun hideDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class NoteEditUiState(
    val isLoading: Boolean = false,
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val noteType: Int = 0, // 0 - note, 1 - reminder
    val reminderDate: Long? = null,
    val selectedChild: Child? = null,
    val availableChildren: List<Child> = emptyList(),
    val showChildSelector: Boolean = false,
    val childSearchQuery: String = "",
    val showDatePicker: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val hasChanges: Boolean = false,
    val noteSaved: Boolean = false,
    val message: String? = null
)