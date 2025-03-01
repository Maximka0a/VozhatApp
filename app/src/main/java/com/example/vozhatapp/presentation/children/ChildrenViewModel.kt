package com.example.vozhatapp.presentation.children


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.repository.ChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject



data class ChildrenListState(
    val children: List<Child> = emptyList(),
    val squadNames: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildrenViewModel @Inject constructor(
    private val childrenRepository: ChildRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSquad = MutableStateFlow<String?>(null)
    val selectedSquad: StateFlow<String?> = _selectedSquad.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val childrenState: StateFlow<ChildrenListState> = combine(
        _searchQuery,
        _selectedSquad,
        childrenRepository.getAllSquadNames(),
        _isLoading
    ) { query, selectedSquad, squadNames, isLoading ->
        val childrenFlow = when {
            query.isNotBlank() -> childrenRepository.searchChildren(query)
            selectedSquad != null -> childrenRepository.getChildrenBySquad(selectedSquad)
            else -> childrenRepository.getAllChildren()
        }

        Triple(childrenFlow, squadNames, isLoading)
    }.flatMapLatest { (childrenFlow, squadNames, isLoading) ->
        childrenFlow.map { children ->
            ChildrenListState(
                children = children,
                squadNames = squadNames,
                isLoading = isLoading
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChildrenListState(isLoading = true)
    )

    init {
        // Check if we need to populate with sample data
        // In a real app, you'd check a flag in SharedPreferences or similar
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // For demonstration purposes, uncomment this when you want to reset data
                // childrenRepository.populateSampleData()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedSquad(squad: String?) {
        _selectedSquad.value = squad
    }

    fun addChild(child: Child) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                childrenRepository.insertChild(child)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}