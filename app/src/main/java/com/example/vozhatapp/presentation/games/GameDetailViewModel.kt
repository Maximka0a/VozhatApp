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
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                gameRepository.getGameById(gameId).collect { game ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            game = game
                        )
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

    fun deleteGame() {
        viewModelScope.launch {
            try {
                val game = _uiState.value.game ?: return@launch
                gameRepository.deleteGame(game)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Ошибка удаления игры: ${e.message}")
                }
            }
        }
    }

    fun toggleDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = !it.showDeleteDialog) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class GameDetailUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)