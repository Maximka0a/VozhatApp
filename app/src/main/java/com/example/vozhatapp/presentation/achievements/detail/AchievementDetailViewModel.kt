package com.example.vozhatapp.presentation.achievements.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Achievement
import com.example.vozhatapp.data.repository.AchievementRepository
import com.example.vozhatapp.data.local.repository.ChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementDetailViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val childRepository: ChildRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(AchievementDetailUiState())
    val uiState: StateFlow<AchievementDetailUiState> = _uiState.asStateFlow()

    fun getChildIdForAchievement(achievementId: Long): Long {
        return uiState.value.achievement?.childId ?: -1L
    }

    fun loadAchievementData(achievementId: Long) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Загрузка достижения
                val achievement = achievementRepository.getAchievementById(achievementId)

                if (achievement != null) {
                    // Загрузка имени ребенка
                    val child = childRepository.getChildById(achievement.childId).firstOrNull()
                    val childName = if (child != null) "${child.name} ${child.lastName}" else null

                    _uiState.update {
                        it.copy(
                            achievement = achievement,
                            childId = achievement.childId,
                            childName = childName,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            achievement = null,
                            error = "Достижение не найдено",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Ошибка загрузки: ${e.localizedMessage ?: e.toString()}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteAchievement() {
        val achievement = _uiState.value.achievement ?: return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                achievementRepository.deleteAchievement(achievement)
                _uiState.update {
                    it.copy(
                        deletedSuccessfully = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Ошибка удаления: ${e.localizedMessage ?: e.toString()}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AchievementDetailUiState(
    val achievement: Achievement? = null,
    val childId: Long = -1L, // Добавляем это поле
    val childName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val deletedSuccessfully: Boolean = false
)