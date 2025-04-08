package com.example.vozhatapp.presentation.events.List

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isCalendarExpanded = MutableStateFlow(false)
    private val _isSearchMode = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _eventsForSelectedDate = _selectedDate
        .flatMapLatest { date ->
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            eventRepository.getEventsByDateRange(startOfDay, endOfDay)
                .catch { e ->
                    _error.value = "Ошибка при загрузке событий: ${e.message}"
                    emit(emptyList())
                }
        }

    // All events from repository
    private val allEvents = eventRepository.allEvents
        .catch { e ->
            _error.value = "Ошибка при загрузке событий: ${e.message}"
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered events based on search query
    private val filteredEvents = combine(allEvents, _searchQuery) { events, query ->
        if (query.isBlank()) {
            events
        } else {
            events.filter { event ->
                event.title.contains(query, ignoreCase = true) ||
                        (event.description?.contains(query, ignoreCase = true) ?: false) ||
                        (event.location?.contains(query, ignoreCase = true) ?: false)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upcoming events (status = 0 or status = 1)
    private val upcomingEvents = filteredEvents
        .map { events ->
            events.filter { it.status < 2 }
                .sortedBy { it.startTime }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Completed events (status = 2)
    private val pastEvents = filteredEvents
        .map { events ->
            events.filter { it.status == 2 }
                .sortedByDescending { it.endTime }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine all state into a UI state
    val uiState = combine(
        _selectedDate,
        _isCalendarExpanded,
        _isSearchMode,
        _searchQuery,
        _eventsForSelectedDate,
        filteredEvents,
        upcomingEvents,
        pastEvents,
        _isLoading,
        _error
    ) { stateValues ->
        val selectedDate = stateValues[0] as LocalDate
        val isCalendarExpanded = stateValues[1] as Boolean
        val isSearchMode = stateValues[2] as Boolean
        val searchQuery = stateValues[3] as String
        val eventsForSelectedDate = stateValues[4] as List<Event>
        val filteredEvents = stateValues[5] as List<Event>
        val upcomingEvents = stateValues[6] as List<Event>
        val pastEvents = stateValues[7] as List<Event>
        val isLoading = stateValues[8] as Boolean
        val error = stateValues[9] as String?

        EventsUiState(
            selectedDate = selectedDate,
            isCalendarExpanded = isCalendarExpanded,
            isSearchMode = isSearchMode,
            searchQuery = searchQuery,
            eventsForSelectedDate = eventsForSelectedDate,
            filteredEvents = filteredEvents,
            upcomingEvents = upcomingEvents,
            pastEvents = pastEvents,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventsUiState()
    )

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Initial load handled by flow collection
            } catch (e: Exception) {
                _error.value = "Ошибка при загрузке событий: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Public methods for UI interactions

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleCalendarExpanded() {
        _isCalendarExpanded.value = !_isCalendarExpanded.value
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !_isSearchMode.value
        if (!_isSearchMode.value) {
            _searchQuery.value = ""
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _error.value = null
    }
}

data class EventsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isCalendarExpanded: Boolean = false,
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val eventsForSelectedDate: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val pastEvents: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)