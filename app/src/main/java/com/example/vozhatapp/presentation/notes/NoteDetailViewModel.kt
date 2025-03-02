package com.example.vozhatapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val childRepository: ChildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Find the note across all repositories
                var foundNote: Note? = null

                // Try to find in general notes first
                noteRepository.getAllGeneralNotes()
                    .map { notes -> notes.find { it.id == noteId } }
                    .collect { note ->
                        if (note != null) {
                            foundNote = note
                            _uiState.update { it.copy(
                                isLoading = false,
                                note = note
                            ) }
                        }
                    }

                if (foundNote == null) {
                    // Get all children
                    val children = childRepository.getAllChildren().first()
                    for (child in children) {
                        // For each child, check their notes
                        noteRepository.getNotesForChild(child.id)
                            .map { notes -> notes.find { it.id == noteId } }
                            .collect { note ->
                                if (note != null) {
                                    foundNote = note
                                    _uiState.update { it.copy(
                                        isLoading = false,
                                        note = note,
                                        childName = child.fullName
                                    ) }
                                }
                            }

                        // Break early if found
                        if (foundNote != null) break
                    }
                }

                // Update state with final result
                _uiState.update { it.copy(
                    isLoading = false,
                    note = foundNote
                ) }

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    message = "Ошибка при загрузке заметки: ${e.message}"
                ) }
            }
        }
    }

    fun deleteNote() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            try {
                val note = _uiState.value.note ?: return@launch
                noteRepository.deleteNote(note)
                _uiState.update { it.copy(
                    message = "Заметка удалена",
                    showDeleteDialog = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка при удалении заметки: ${e.message}",
                    showDeleteDialog = false
                ) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class NoteDetailUiState(
    val isLoading: Boolean = true,
    val note: Note? = null,
    val childName: String? = null,
    val showDeleteDialog: Boolean = false,
    val message: String? = null
)