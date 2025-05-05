package com.example.vozhatapp.presentation.achievements.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Achievement
import com.example.vozhatapp.data.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AchievementEditViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementEditUiState())
    val uiState: StateFlow<AchievementEditUiState> = _uiState.asStateFlow()

    fun initialize(childId: Long, achievementId: Long?) {
        _uiState.update { it.copy(isLoading = true, childId = childId) }

        viewModelScope.launch {
            try {
                if (achievementId != null) {
                    // Load existing achievement for editing
                    achievementRepository.getAchievementById(achievementId)?.let { achievement ->
                        _uiState.update {
                            it.copy(
                                title = achievement.title,
                                description = achievement.description ?: "",
                                points = achievement.points.toString(),
                                date = Date(achievement.date),
                                isLoading = false,
                                isEditing = true,
                                achievementId = achievementId
                            )
                        }
                    } ?: run {
                        _uiState.update {
                            it.copy(
                                error = "Достижение не найдено",
                                isLoading = false
                            )
                        }
                    }
                } else {
                    // Set up for creating new achievement
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Ошибка загрузки данных: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = if (title.isBlank()) "Название не может быть пустым" else null
            )
        }
        validateForm()
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updatePoints(points: String) {
        _uiState.update {
            val pointsError = when {
                points.isBlank() -> null  // Points can be blank (will default to 0)
                points.toIntOrNull() == null -> "Введите корректное число"
                points.toInt() < 0 -> "Баллы не могут быть отрицательными"
                else -> null
            }
            it.copy(points = points, pointsError = pointsError)
        }
        validateForm()
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    fun saveAchievement() {
        if (!validateForm()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val achievement = Achievement(
                    id = if (_uiState.value.isEditing) _uiState.value.achievementId else 0,
                    childId = _uiState.value.childId,
                    title = _uiState.value.title.trim(),
                    description = _uiState.value.description.trim().ifBlank { null },
                    points = _uiState.value.points.toIntOrNull() ?: 0,
                    date = _uiState.value.date.time
                )

                val achievementId = if (_uiState.value.isEditing) {
                    achievementRepository.updateAchievement(achievement)
                    achievement.id
                } else {
                    achievementRepository.insertAchievement(achievement)
                }

                _uiState.update {
                    it.copy(
                        savedAchievementId = achievementId,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Ошибка сохранения: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val titleValid = _uiState.value.title.isNotBlank()
        val pointsValid = _uiState.value.points.isBlank() ||
                (_uiState.value.points.toIntOrNull() != null &&
                        _uiState.value.points.toInt() >= 0)

        val canSave = titleValid && pointsValid

        _uiState.update { it.copy(canSave = canSave) }
        return canSave
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSavedId() {
        _uiState.update { it.copy(savedAchievementId = null) }
    }
}

data class AchievementEditUiState(
    val childId: Long = 0,
    val achievementId: Long = 0,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val points: String = "",
    val pointsError: String? = null,
    val date: Date = Date(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedAchievementId: Long? = null,
    val canSave: Boolean = false,
    val isEditing: Boolean = false
)