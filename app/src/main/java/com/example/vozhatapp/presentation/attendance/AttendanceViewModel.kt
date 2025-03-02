package com.example.vozhatapp.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.entity.Attendance
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.AttendanceRepository
import com.example.vozhatapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val childRepository: ChildRepository
) : ViewModel() {

    // State
    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    // Track local changes to attendance before saving to database
    private val _pendingAttendanceChanges = mutableMapOf<Long, Boolean>()
    private val _pendingNoteChanges = mutableMapOf<Long, String?>()

    init {
        loadEvents()
        loadSquads()
    }

    // Add this to your AttendanceViewModel
    private fun loadEvents() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                eventRepository.allEvents
                    .catch { exception ->
                        // Handle any exceptions during data loading
                        _uiState.update { it.copy(
                            isLoading = false,
                            message = "Ошибка загрузки событий: ${exception.localizedMessage}"
                        ) }
                    }
                    .map { events ->
                        // Safely filter events
                        events.filter { it.status >= 1 }
                            .sortedByDescending { it.startTime }
                    }
                    .collect { events ->
                        _uiState.update { it.copy(
                            availableEvents = events,
                            isLoading = false
                        ) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    message = "Ошибка загрузки событий: ${e.localizedMessage}"
                ) }
            }
        }
    }

    private fun loadSquads() {
        viewModelScope.launch {
            try {
                childRepository.getAllSquadNames()
                    .collect { squads ->
                        _uiState.update { it.copy(
                            availableSquads = squads
                        ) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка загрузки отрядов: ${e.message}"
                ) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectEvent(event: Event) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    selectedEvent = event,
                    isLoading = true
                ) }

                // Clear any pending changes
                _pendingAttendanceChanges.clear()
                _pendingNoteChanges.clear()

                // Get children and attendance records for this event
                val attendanceFlow = attendanceRepository.getAttendanceForEvent(event.id)
                val childrenFlow = childRepository.getAllChildren()

                // Combine the flows to get a list of attendance records with child data
                attendanceFlow.combine(childrenFlow) { attendanceList, children ->
                    // Create map of child id to child object for quick lookups
                    val childMap = children.associateBy { it.id }

                    // Convert attendance records to UI model
                    attendanceList.mapNotNull { attendance ->
                        val child = childMap[attendance.childId] ?: return@mapNotNull null
                        AttendanceRecord(
                            child = child,
                            isPresent = attendance.isPresent,
                            note = attendance.note
                        )
                    }
                }.collect { records ->
                    // Get list of children who are not already in the attendance records
                    val existingChildIds = records.map { it.child.id }.toSet()
                    val availableChildrenFlow = childRepository.getAllChildren()

                    availableChildrenFlow.collect { allChildren ->
                        val availableToAdd = allChildren
                            .filter { it.id !in existingChildIds }
                            .map { child ->
                                ChildSelectionItem(
                                    id = child.id,
                                    name = child.fullName,
                                    squad = child.squadName
                                )
                            }

                        _uiState.update { it.copy(
                            attendanceRecords = records,
                            availableChildrenToAdd = availableToAdd,
                            isLoading = false
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    message = "Ошибка загрузки посещаемости: ${e.message}"
                ) }
            }
        }
    }

    // Toggle attendance for a child
    fun toggleAttendance(childId: Long) {
        val records = _uiState.value.attendanceRecords.toMutableList()
        val index = records.indexOfFirst { it.child.id == childId }

        if (index != -1) {
            // Update the record in the UI state
            val record = records[index]
            records[index] = record.copy(isPresent = !record.isPresent)

            // Track the change to save later
            _pendingAttendanceChanges[childId] = !record.isPresent

            _uiState.update { it.copy(attendanceRecords = records) }
        }
    }

    // Save a note for a child
    fun saveNote(note: String) {
        val childId = _uiState.value.selectedChildForNote?.child?.id ?: return

        val records = _uiState.value.attendanceRecords.toMutableList()
        val index = records.indexOfFirst { it.child.id == childId }

        if (index != -1) {
            // Update the record in the UI state
            val record = records[index]
            val trimmedNote = note.trim().takeIf { it.isNotEmpty() }
            records[index] = record.copy(note = trimmedNote)

            // Track the note change to save later
            _pendingNoteChanges[childId] = trimmedNote

            _uiState.update { it.copy(
                attendanceRecords = records,
                selectedChildForNote = null
            ) }
        }
    }

    // Save all pending changes to the database
    fun saveAttendance() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }

                val eventId = _uiState.value.selectedEvent?.id ?: return@launch

                // Process attendance changes
                for ((childId, isPresent) in _pendingAttendanceChanges) {
                    // Check if this child has an attendance record
                    val existingAttendance = attendanceRepository
                        .getAttendanceForEvent(eventId)
                        .first()
                        .find { it.childId == childId }

                    if (existingAttendance != null) {
                        // Update existing record
                        val updatedAttendance = existingAttendance.copy(
                            isPresent = isPresent,
                            markedAt = System.currentTimeMillis()
                        )
                        attendanceRepository.updateAttendance(updatedAttendance)
                    } else {
                        // Create new record
                        val newAttendance = Attendance(
                            eventId = eventId,
                            childId = childId,
                            isPresent = isPresent,
                            markedAt = System.currentTimeMillis()
                        )
                        attendanceRepository.markAttendance(newAttendance)
                    }
                }

                // Process note changes
                for ((childId, note) in _pendingNoteChanges) {
                    // Check if this child has an attendance record
                    val existingAttendance = attendanceRepository
                        .getAttendanceForEvent(eventId)
                        .first()
                        .find { it.childId == childId }

                    if (existingAttendance != null) {
                        // Update existing record
                        val updatedAttendance = existingAttendance.copy(
                            note = note,
                            markedAt = System.currentTimeMillis()
                        )
                        attendanceRepository.updateAttendance(updatedAttendance)
                    }
                }

                // Clear pending changes after successful save
                _pendingAttendanceChanges.clear()
                _pendingNoteChanges.clear()

                _uiState.update { it.copy(
                    isSaving = false,
                    message = "Посещаемость сохранена"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSaving = false,
                    message = "Ошибка сохранения: ${e.message}"
                ) }
            }
        }
    }

    // Add multiple children to an event's attendance
    fun addChildrenToEvent(childIds: List<Long>) {
        viewModelScope.launch {
            try {
                val eventId = _uiState.value.selectedEvent?.id ?: return@launch

                // Get all children data
                val allChildren = childRepository.getAllChildren().first()
                val childMap = allChildren.associateBy { it.id }

                // Create attendance records for selected children
                val newAttendances = childIds.mapNotNull { childId ->
                    val child = childMap[childId] ?: return@mapNotNull null

                    Attendance(
                        eventId = eventId,
                        childId = childId,
                        isPresent = true, // Default to present
                        markedAt = System.currentTimeMillis()
                    )
                }

                if (newAttendances.isNotEmpty()) {
                    attendanceRepository.markBulkAttendance(newAttendances)

                    // Refresh attendance data for this event
                    selectEvent(_uiState.value.selectedEvent!!)

                    _uiState.update { it.copy(
                        message = "Добавлено ${newAttendances.size} детей"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Ошибка добавления детей: ${e.message}"
                ) }
            }
        }
    }

    // Mark all children as present/absent
    fun markAllPresent(isPresent: Boolean) {
        val squadFilter = _uiState.value.squadFilter
        val records = _uiState.value.attendanceRecords.toMutableList()

        records.forEachIndexed { index, record ->
            // Only update records matching the current squad filter (if any)
            if (squadFilter == null || record.child.squadName == squadFilter) {
                records[index] = record.copy(isPresent = isPresent)
                _pendingAttendanceChanges[record.child.id] = isPresent
            }
        }

        _uiState.update { it.copy(
            attendanceRecords = records,
            message = if (isPresent) "Все отмечены как присутствующие" else "Все отмечены как отсутствующие"
        ) }
    }

    // Export attendance data
    fun exportAttendanceData() {
        // This would typically involve creating a CSV file or similar
        // For now, just show a message
        _uiState.update { it.copy(
            message = "Экспорт данных не реализован"
        ) }
    }

    // UI state update functions

    fun toggleEventDropdown() {
        _uiState.update { it.copy(showEventDropdown = !it.showEventDropdown) }
    }

    fun toggleFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = !it.showFilterDialog) }
    }

    fun toggleMenuDropdown() {
        _uiState.update { it.copy(showMenuDropdown = !it.showMenuDropdown) }
    }

    fun toggleNoteDialog(childId: Long?) {
        val selectedChild = if (childId != null) {
            _uiState.value.attendanceRecords.find { it.child.id == childId }
        } else {
            null
        }

        _uiState.update { it.copy(
            showNoteDialog = childId != null,
            selectedChildForNote = selectedChild
        ) }
    }

    fun toggleAddChildrenDialog() {
        _uiState.update { it.copy(showAddChildrenDialog = !it.showAddChildrenDialog) }
    }

    fun updateSquadFilter(squad: String?) {
        _uiState.update { it.copy(squadFilter = squad) }
    }

    fun toggleShowAbsentOnly() {
        _uiState.update { it.copy(showAbsentOnly = !it.showAbsentOnly) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val selectedEvent: Event? = null,
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val availableEvents: List<Event> = emptyList(),
    val availableSquads: List<String> = emptyList(),
    val availableChildrenToAdd: List<ChildSelectionItem> = emptyList(),

    // Filters
    val squadFilter: String? = null,
    val showAbsentOnly: Boolean = false,

    // UI state
    val showEventDropdown: Boolean = false,
    val showFilterDialog: Boolean = false,
    val showMenuDropdown: Boolean = false,
    val showNoteDialog: Boolean = false,
    val showAddChildrenDialog: Boolean = false,

    // Selected child for adding note
    val selectedChildForNote: AttendanceRecord? = null,

    // Feedback message
    val message: String? = null
)

data class AttendanceRecord(
    val child: Child,
    val isPresent: Boolean,
    val note: String? = null
)

data class ChildSelectionItem(
    val id: Long,
    val name: String,
    val squad: String
)