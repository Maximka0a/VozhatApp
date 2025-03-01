package com.example.vozhatapp.presentation.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Game
import com.example.vozhatapp.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameEditViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameEditUiState())
    val uiState: StateFlow<GameEditUiState> = _uiState.asStateFlow()

    // Predefined categories
    val availableCategories = listOf(
        "Активные",
        "Настольные",
        "Творческие",
        "Интеллектуальные",
        "Командные",
        "Музыкальные",
        "Вечерние",
        "Другое"
    )

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                gameRepository.getGameById(gameId).collect { game ->
                    if (game != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                id = game.id,
                                title = game.title,
                                category = game.category,
                                description = game.description,
                                minAge = game.minAge,
                                maxAge = game.maxAge,
                                minPlayers = game.minPlayers,
                                maxPlayers = game.maxPlayers,
                                duration = game.duration,
                                materials = game.materials,
                                isValid = validateForm(
                                    game.title,
                                    game.category,
                                    game.description
                                )
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Игра не найдена"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки игры: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val game = Game(
                    id = _uiState.value.id ?: 0,
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    category = _uiState.value.category,
                    minAge = _uiState.value.minAge,
                    maxAge = _uiState.value.maxAge,
                    minPlayers = _uiState.value.minPlayers,
                    maxPlayers = _uiState.value.maxPlayers,
                    duration = _uiState.value.duration,
                    materials = _uiState.value.materials?.takeIf { it.isNotBlank() }
                )

                val gameId = if (_uiState.value.id != null) {
                    gameRepository.updateGame(game)
                    _uiState.value.id
                } else {
                    gameRepository.insertGame(game)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        savedGameId = gameId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка сохранения игры: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateForm(title: String, category: String, description: String): Boolean {
        val isTitleValid = title.length >= 3
        val isCategoryValid = category.isNotBlank()
        val isDescriptionValid = description.length >= 10

        return isTitleValid && isCategoryValid && isDescriptionValid
    }

    // Field update methods

    fun updateTitle(value: String) {
        val titleError = when {
            value.isBlank() -> "Название не может быть пустым"
            value.length < 3 -> "Минимум 3 символа"
            else -> null
        }

        _uiState.update {
            it.copy(
                title = value,
                titleError = titleError,
                isValid = validateForm(
                    value,
                    it.category,
                    it.description
                )
            )
        }
    }

    fun updateCategory(value: String) {
        val categoryError = when {
            value.isBlank() -> "Выберите категорию"
            else -> null
        }

        _uiState.update {
            it.copy(
                category = value,
                categoryError = categoryError,
                isValid = validateForm(
                    it.title,
                    value,
                    it.description
                )
            )
        }
    }

    fun updateDescription(value: String) {
        val descriptionError = when {
            value.isBlank() -> "Описание не может быть пустым"
            value.length < 10 -> "Минимум 10 символов"
            else -> null
        }

        _uiState.update {
            it.copy(
                description = value,
                descriptionError = descriptionError,
                isValid = validateForm(
                    it.title,
                    it.category,
                    value
                )
            )
        }
    }

    fun updateMinAge(value: Int?) {
        _uiState.update { it.copy(minAge = value) }
    }

    fun updateMaxAge(value: Int?) {
        _uiState.update { it.copy(maxAge = value) }
    }

    fun updateMinPlayers(value: Int?) {
        _uiState.update { it.copy(minPlayers = value) }
    }

    fun updateMaxPlayers(value: Int?) {
        _uiState.update { it.copy(maxPlayers = value) }
    }

    fun updateDuration(value: Int?) {
        _uiState.update { it.copy(duration = value) }
    }

    fun updateMaterials(value: String) {
        _uiState.update { it.copy(materials = value.takeIf { it.isNotBlank() }) }
    }

    fun toggleCategoryDropdown() {
        _uiState.update { it.copy(showCategoryDropdown = !it.showCategoryDropdown) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class GameEditUiState(
    val isLoading: Boolean = false,
    val id: Long? = null,
    val title: String = "",
    val titleError: String? = null,
    val category: String = "",
    val categoryError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val duration: Int? = null,
    val materials: String? = null,
    val showCategoryDropdown: Boolean = false,
    val isValid: Boolean = false,
    val savedGameId: Long? = null,
    val error: String? = null
)