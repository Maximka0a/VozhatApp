package com.example.vozhatapp.presentation.events.Detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                eventRepository.getEventById(eventId).collect { event ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            event = event
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки события: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateEventStatus(newStatus: Int) {
        viewModelScope.launch {
            try {
                val event = _uiState.value.event ?: return@launch

                val updatedEvent = event.copy(status = newStatus)
                eventRepository.updateEvent(updatedEvent)

                // Update the state with the new event
                _uiState.update { it.copy(event = updatedEvent) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Ошибка обновления статуса: ${e.message}")
                }
            }
        }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            try {
                val event = _uiState.value.event ?: return@launch
                eventRepository.deleteEvent(event)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Ошибка удаления события: ${e.message}")
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

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)