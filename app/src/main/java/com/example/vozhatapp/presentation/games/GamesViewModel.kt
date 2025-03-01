package com.example.vozhatapp.presentation.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Game
import com.example.vozhatapp.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    // UI state for the games screen
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    // Search query
    private val searchQuery = MutableStateFlow("")

    // Selected category filter
    private val selectedCategory = MutableStateFlow<String?>(null)

    // Available categories (extracted from games)
    private val categories = MutableStateFlow<List<String>>(emptyList())

    // Additional filters
    private val minAgeFilter = MutableStateFlow<Int?>(null)
    private val maxAgeFilter = MutableStateFlow<Int?>(null)
    private val minPlayersFilter = MutableStateFlow<Int?>(null)
    private val maxPlayersFilter = MutableStateFlow<Int?>(null)
    private val maxDurationFilter = MutableStateFlow<Int?>(null)

    // Filter dialog visibility
    private val showFilterDialog = MutableStateFlow(false)

    // All games from repository
    private val allGames = MutableStateFlow<List<Game>>(emptyList())

    init {
        loadGames()

        // Combine flows to update UI state
        viewModelScope.launch {
            // Combine all state flows that affect UI
            combine(
                searchQuery,
                selectedCategory,
                categories,
                minAgeFilter,
                maxAgeFilter,
                minPlayersFilter,
                maxPlayersFilter,
                maxDurationFilter,
                showFilterDialog,
                allGames
            ) { stateValues ->
                val query = stateValues[0] as String
                val category = stateValues[1] as String?
                val categoriesList = stateValues[2] as List<String>
                val minAge = stateValues[3] as Int?
                val maxAge = stateValues[4] as Int?
                val minPlayers = stateValues[5] as Int?
                val maxPlayers = stateValues[6] as Int?
                val maxDuration = stateValues[7] as Int?
                val showDialog = stateValues[8] as Boolean
                val games = stateValues[9] as List<Game>

                // Apply filters to games
                val filteredGames = filterGames(
                    games = games,
                    query = query,
                    category = category,
                    minAge = minAge,
                    maxAge = maxAge,
                    minPlayers = minPlayers,
                    maxPlayers = maxPlayers,
                    maxDuration = maxDuration
                )

                // Return updated UI state
                GamesUiState(
                    isLoading = false,
                    searchQuery = query,
                    selectedCategory = category,
                    categories = categoriesList,
                    minAgeFilter = minAge,
                    maxAgeFilter = maxAge,
                    minPlayersFilter = minPlayers,
                    maxPlayersFilter = maxPlayers,
                    maxDurationFilter = maxDuration,
                    showFilterDialog = showDialog,
                    filteredGames = filteredGames,
                    allGames = games
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Collect games from repository
                gameRepository.allGames.collect { games ->
                    allGames.value = games

                    // Extract unique categories
                    val uniqueCategories = games.map { it.category }.distinct().sorted()
                    categories.value = uniqueCategories

                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки игр: ${e.message}"
                    )
                }
            }
        }
    }

    private fun filterGames(
        games: List<Game>,
        query: String,
        category: String?,
        minAge: Int?,
        maxAge: Int?,
        minPlayers: Int?,
        maxPlayers: Int?,
        maxDuration: Int?
    ): List<Game> {
        return games.filter { game ->
            // Text search filter
            val matchesQuery = query.isEmpty() ||
                    game.title.contains(query, ignoreCase = true) ||
                    game.description.contains(query, ignoreCase = true)

            // Category filter
            val matchesCategory = category == null || game.category == category

            // Age filter
            val matchesMinAge = minAge == null || (game.maxAge == null || game.maxAge >= minAge)
            val matchesMaxAge = maxAge == null || (game.minAge == null || game.minAge <= maxAge)

            // Players filter
            val matchesMinPlayers = minPlayers == null ||
                    (game.maxPlayers == null || game.maxPlayers >= minPlayers)
            val matchesMaxPlayers = maxPlayers == null ||
                    (game.minPlayers == null || game.minPlayers <= maxPlayers)

            // Duration filter
            val matchesDuration = maxDuration == null ||
                    (game.duration == null || game.duration <= maxDuration)

            // Apply all filters
            matchesQuery &&
                    matchesCategory &&
                    matchesMinAge &&
                    matchesMaxAge &&
                    matchesMinPlayers &&
                    matchesMaxPlayers &&
                    matchesDuration
        }
    }

    // Public functions for UI interactions

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        selectedCategory.value = category
    }

    fun toggleFilterDialog() {
        showFilterDialog.value = !showFilterDialog.value
    }

    fun setMinAgeFilter(value: Int?) {
        minAgeFilter.value = value
    }

    fun setMaxAgeFilter(value: Int?) {
        maxAgeFilter.value = value
    }

    fun setMinPlayersFilter(value: Int?) {
        minPlayersFilter.value = value
    }

    fun setMaxPlayersFilter(value: Int?) {
        maxPlayersFilter.value = value
    }

    fun setMaxDurationFilter(value: Int?) {
        maxDurationFilter.value = value
    }

    fun resetFilters() {
        minAgeFilter.value = null
        maxAgeFilter.value = null
        minPlayersFilter.value = null
        maxPlayersFilter.value = null
        maxDurationFilter.value = null
    }

    fun applyFilters() {
        // No additional action needed as state flows will automatically update UI
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun searchGames(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    loadGames()
                } else {
                    gameRepository.searchGames(query).collect { games ->
                        allGames.value = games
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Ошибка поиска игр: ${e.message}")
                }
            }
        }
    }
}

data class GamesUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val minAgeFilter: Int? = null,
    val maxAgeFilter: Int? = null,
    val minPlayersFilter: Int? = null,
    val maxPlayersFilter: Int? = null,
    val maxDurationFilter: Int? = null,
    val showFilterDialog: Boolean = false,
    val filteredGames: List<Game> = emptyList(),
    val allGames: List<Game> = emptyList(),
    val error: String? = null
)