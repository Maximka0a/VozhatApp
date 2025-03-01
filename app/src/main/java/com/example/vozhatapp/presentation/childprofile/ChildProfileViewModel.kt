package com.example.vozhatapp.presentation.childprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Achievement
import com.example.vozhatapp.data.local.entity.Attendance
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.AchievementRepository
import com.example.vozhatapp.data.repository.AttendanceRepository
import com.example.vozhatapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildProfileViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val achievementRepository: AchievementRepository,
    private val attendanceRepository: AttendanceRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildProfileUiState())
    val uiState: StateFlow<ChildProfileUiState> = _uiState.asStateFlow()

    fun loadChildData(childId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load child basic info
                childRepository.getChildById(childId)
                    .collect { child ->
                        _uiState.update { it.copy(child = child, isLoading = false) }
                    }

                // Load achievements
                loadAchievements(childId)

                // Load attendance
                loadAttendance(childId)

                // Load notes
                loadNotes(childId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Не удалось загрузить данные: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadAchievements(childId: Long) {
        viewModelScope.launch {
            try {
                achievementRepository.getAchievementsForChild(childId)
                    .collect { achievements ->
                        val totalPoints = achievements.sumOf { it.points }
                        _uiState.update {
                            it.copy(
                                achievements = achievements,
                                totalPoints = totalPoints
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Не удалось загрузить достижения: ${e.message}")
                }
            }
        }
    }

    fun loadAttendance(childId: Long) {
        viewModelScope.launch {
            try {
                // Get attendance records
                attendanceRepository.getAttendanceForChild(childId)
                    .collect { records ->
                        if (records.isEmpty()) {
                            _uiState.update { it.copy(attendance = emptyList(), attendanceRate = 0f) }
                        } else {
                            val presentCount = records.count { it.isPresent }
                            val attendanceRate = presentCount.toFloat() / records.size
                            _uiState.update {
                                it.copy(
                                    attendance = records,
                                    attendanceRate = attendanceRate
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Не удалось загрузить данные о посещаемости: ${e.message}")
                }
            }
        }
    }

    fun loadNotes(childId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.getNotesForChild(childId)
                    .collect { notes ->
                        _uiState.update { it.copy(notes = notes) }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Не удалось загрузить заметки: ${e.message}")
                }
            }
        }
    }

    fun showAchievementDetails(achievement: Achievement) {
        _uiState.update { it.copy(selectedAchievement = achievement) }
    }

    fun dismissAchievementDetails() {
        _uiState.update { it.copy(selectedAchievement = null) }
    }

    fun showNoteDetails(note: Note) {
        _uiState.update { it.copy(selectedNote = note) }
    }

    fun dismissNoteDetails() {
        _uiState.update { it.copy(selectedNote = null) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
                // Note will be automatically removed from the list
                // when the flow is updated
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Не удалось удалить заметку: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChildProfileUiState(
    val isLoading: Boolean = false,
    val child: Child? = null,
    val achievements: List<Achievement> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
    val notes: List<Note> = emptyList(),
    val selectedAchievement: Achievement? = null,
    val selectedNote: Note? = null,
    val totalPoints: Int = 0,
    val attendanceRate: Float = 0f,
    val error: String? = null
)