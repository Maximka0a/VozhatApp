package com.example.vozhatapp.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.AttendanceRepository
import com.example.vozhatapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildAttendanceViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildAttendanceUiState())
    val uiState: StateFlow<ChildAttendanceUiState> = _uiState.asStateFlow()

    fun loadChildAttendance(childId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Load child data
                childRepository.getChildById(childId).collect { child ->
                    _uiState.update { it.copy(child = child) }

                    // Load attendance records for this child
                    loadAttendanceHistory(childId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    message = "Ошибка загрузки данных: ${e.message}"
                ) }
            }
        }
    }

    private suspend fun loadAttendanceHistory(childId: Long) {
        try {
            // Get attendance records
            val attendanceRecords = attendanceRepository.getAttendanceForChild(childId).first()

            // Get all events to match with attendance records
            val allEvents = eventRepository.allEvents.first()
            val eventMap = allEvents.associateBy { it.id }

            // Map to attendance history items
            val historyItems = attendanceRecords.mapNotNull { attendance ->
                val event = eventMap[attendance.eventId] ?: return@mapNotNull null

                AttendanceHistoryItem(
                    eventId = event.id,
                    eventTitle = event.title,
                    eventDate = event.startTime,
                    isPresent = attendance.isPresent,
                    note = attendance.note
                )
            }.sortedByDescending { it.eventDate } // Most recent first

            _uiState.update { it.copy(
                attendanceRecords = historyItems,
                isLoading = false
            ) }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isLoading = false,
                message = "Ошибка загрузки истории посещаемости: ${e.message}"
            ) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class ChildAttendanceUiState(
    val isLoading: Boolean = true,
    val child: Child? = null,
    val attendanceRecords: List<AttendanceHistoryItem> = emptyList(),
    val message: String? = null
)

data class AttendanceHistoryItem(
    val eventId: Long,
    val eventTitle: String,
    val eventDate: Long,
    val isPresent: Boolean,
    val note: String? = null
)