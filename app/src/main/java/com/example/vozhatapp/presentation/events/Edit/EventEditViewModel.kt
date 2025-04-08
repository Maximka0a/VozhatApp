package com.example.vozhatapp.presentation.events.Edit

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
import java.time.*
import javax.inject.Inject

@HiltViewModel
class EventEditViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventEditUiState())
    val uiState: StateFlow<EventEditUiState> = _uiState.asStateFlow()

    // Current user ID (would typically come from auth service)
    private val currentUserId = 1L

    fun initializeNewEvent() {
        // Set default values for a new event
        val now = LocalDateTime.now()
        val startDateTime = now.plusMinutes(30).withSecond(0).withNano(0)
        val endDateTime = startDateTime.plusHours(1)

        _uiState.update {
            it.copy(
                isLoading = false,
                startDate = startDateTime.toLocalDate(),
                startTime = startDateTime.toLocalTime(),
                endDate = endDateTime.toLocalDate(),
                endTime = endDateTime.toLocalTime(),
                status = 0,
                isValid = false
            )
        }

        validateForm()
    }

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                eventRepository.getEventById(eventId).collect { event ->
                    if (event != null) {
                        val startDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(event.startTime),
                            ZoneId.systemDefault()
                        )
                        val endDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(event.endTime),
                            ZoneId.systemDefault()
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                id = event.id,
                                title = event.title,
                                description = event.description,
                                location = event.location,
                                startDate = startDateTime.toLocalDate(),
                                startTime = startDateTime.toLocalTime(),
                                endDate = endDateTime.toLocalDate(),
                                endTime = endDateTime.toLocalTime(),
                                status = event.status,
                                createdAt = event.createdAt,
                                createdBy = event.createdBy
                            )
                        }

                        validateForm()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Событие не найдено"
                            )
                        }
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

    fun saveEvent() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val startDateTime = LocalDateTime.of(
                    _uiState.value.startDate,
                    _uiState.value.startTime
                )
                val endDateTime = LocalDateTime.of(
                    _uiState.value.endDate,
                    _uiState.value.endTime
                )

                val startMillis = startDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val endMillis = endDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val event = Event(
                    id = _uiState.value.id ?: 0,
                    title = _uiState.value.title,
                    description = _uiState.value.description?.takeIf { it.isNotBlank() },
                    startTime = startMillis,
                    endTime = endMillis,
                    location = _uiState.value.location?.takeIf { it.isNotBlank() },
                    status = _uiState.value.status,
                    createdBy = _uiState.value.createdBy ?: currentUserId,
                    createdAt = _uiState.value.createdAt ?: System.currentTimeMillis()
                )

                val savedId = if (_uiState.value.id != null) {
                    eventRepository.updateEvent(event)
                    _uiState.value.id
                } else {
                    eventRepository.insertEvent(event)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        savedEventId = savedId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка сохранения события: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateForm() {
        val startDateTime = LocalDateTime.of(_uiState.value.startDate, _uiState.value.startTime)
        val endDateTime = LocalDateTime.of(_uiState.value.endDate, _uiState.value.endTime)

        // Validate title
        val titleError = when {
            _uiState.value.title.isBlank() -> "Название события обязательно"
            _uiState.value.title.length < 3 -> "Название должно содержать минимум 3 символа"
            else -> null
        }

        // Validate date and time
        val dateTimeError = when {
            endDateTime.isBefore(startDateTime) -> "Время окончания должно быть позже времени начала"
            else -> null
        }

        // Check if form is valid
        val isValid = titleError == null && dateTimeError == null

        _uiState.update {
            it.copy(
                titleError = titleError,
                dateTimeError = dateTimeError,
                isValid = isValid
            )
        }
    }

    // Form field update methods

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value) }
        validateForm()
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value.ifBlank { null }) }
    }

    fun updateLocation(value: String) {
        _uiState.update { it.copy(location = value.ifBlank { null }) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                startDate = date,
                // If end date is before the new start date, update end date too
                endDate = if (it.endDate.isBefore(date)) date else it.endDate
            )
        }
        validateForm()
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }

        // If start and end date are the same and end time is before start time,
        // automatically update end time to be 1 hour after start time
        if (_uiState.value.startDate == _uiState.value.endDate &&
            _uiState.value.endTime.isBefore(_uiState.value.startTime)) {
            _uiState.update { it.copy(endTime = time.plusHours(1)) }
        }

        validateForm()
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
        validateForm()
    }

    fun updateEndTime(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
        validateForm()
    }

    fun updateStatus(status: Int) {
        _uiState.update { it.copy(status = status) }
    }

    // Dialog control

    fun toggleStartDatePicker(show: Boolean) {
        _uiState.update { it.copy(showStartDatePicker = show) }
    }

    fun toggleStartTimePicker(show: Boolean) {
        _uiState.update { it.copy(showStartTimePicker = show) }
    }

    fun toggleEndDatePicker(show: Boolean) {
        _uiState.update { it.copy(showEndDatePicker = show) }
    }

    fun toggleEndTimePicker(show: Boolean) {
        _uiState.update { it.copy(showEndTimePicker = show) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}