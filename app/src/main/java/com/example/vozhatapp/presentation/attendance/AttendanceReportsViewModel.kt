package com.example.vozhatapp.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vozhatapp.data.local.repository.ChildRepository
import com.example.vozhatapp.data.repository.AttendanceRepository
import com.example.vozhatapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

@HiltViewModel
class AttendanceReportsViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AttendanceReportsUiState(
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            onDateRangeClick = { toggleDateRangePicker() }
        )
    )
    val uiState: StateFlow<AttendanceReportsUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    fun updateDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.update { it.copy(
            startDate = startDate,
            endDate = endDate,
            showDateRangePicker = false
        ) }

        loadReportData()
    }

    fun toggleDateRangePicker() {
        _uiState.update { it.copy(showDateRangePicker = !it.showDateRangePicker) }
    }

    fun exportReport() {
        // This would typically generate a PDF or CSV report
        // For now, just show a message
        _uiState.update { it.copy(message = "Экспорт отчетов не реализован") }
    }

    private fun loadReportData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Convert date range to milliseconds
                val startMillis = _uiState.value.startDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val endMillis = _uiState.value.endDate
                    .plusDays(1) // Include the end date
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli() - 1 // End of day

                // Get events in the date range
                val events = eventRepository.getEventsByDateRange(startMillis, endMillis).first()

                // Return early if no events in range
                if (events.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        summaryStats = AttendanceSummaryStats(),
                        squadStats = emptyList(),
                        eventStats = emptyList(),
                        childStats = emptyList()
                    ) }
                    return@launch
                }

                // Get all attendance records for these events
                val eventIds = events.map { it.id }
                val allAttendance = mutableListOf<AttendanceWithDetails>()

                // Get children map
                val children = childRepository.getAllChildren().first()
                val childMap = children.associateBy { it.id }

                // For each event, get attendance records
                for (eventId in eventIds) {
                    val eventAttendance = attendanceRepository.getAttendanceForEvent(eventId).first()

                    val event = events.find { it.id == eventId } ?: continue

                    // Map to detailed records
                    val detailedRecords = eventAttendance.mapNotNull { attendance ->
                        val child = childMap[attendance.childId] ?: return@mapNotNull null

                        AttendanceWithDetails(
                            eventId = event.id,
                            eventTitle = event.title,
                            eventDate = event.startTime,
                            childId = child.id,
                            childName = child.fullName,
                            squadName = child.squadName,
                            isPresent = attendance.isPresent,
                            note = attendance.note
                        )
                    }

                    allAttendance.addAll(detailedRecords)
                }

                // Calculate summary statistics
                val totalEvents = events.size
                val totalChildren = children.size
                val totalAttendance = allAttendance.count { it.isPresent }
                val totalPossibleAttendance = totalEvents * totalChildren
                val averageRate = if (allAttendance.isNotEmpty()) {
                    (totalAttendance * 100) / allAttendance.size
                } else {
                    0
                }

                val summaryStats = AttendanceSummaryStats(
                    totalEvents = totalEvents,
                    totalChildren = totalChildren,
                    totalAttendance = totalAttendance,
                    averageAttendanceRate = averageRate
                )

                // Calculate squad statistics
                val squadStats = calculateSquadStats(allAttendance, events.size)

                // Calculate event statistics
                val eventStats = calculateEventStats(allAttendance, events)

                // Calculate child statistics
                val childStats = calculateChildStats(allAttendance, events.size)

                _uiState.update { it.copy(
                    isLoading = false,
                    summaryStats = summaryStats,
                    squadStats = squadStats,
                    eventStats = eventStats,
                    childStats = childStats
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    message = "Ошибка загрузки отчета: ${e.message}"
                ) }
            }
        }
    }

    private fun calculateSquadStats(
        attendance: List<AttendanceWithDetails>,
        totalEvents: Int
    ): List<SquadAttendanceStats> {
        // Group attendance by squad
        val squadAttendance = attendance.groupBy { it.squadName }

        return squadAttendance.map { (squadName, records) ->
            // Count unique children in this squad
            val childrenCount = records.map { it.childId }.distinct().size

            // Count total attendance and absences
            val totalAttendance = records.count { it.isPresent }
            val totalAbsences = records.size - totalAttendance

            // Calculate attendance rate
            val attendanceRate = if (records.isNotEmpty()) {
                (totalAttendance * 100) / records.size
            } else {
                0
            }

            SquadAttendanceStats(
                squadName = squadName,
                childrenCount = childrenCount,
                totalAttendance = totalAttendance,
                totalAbsences = totalAbsences,
                attendanceRate = attendanceRate
            )
        }.sortedByDescending { it.attendanceRate }
    }

    private fun calculateEventStats(
        attendance: List<AttendanceWithDetails>,
        events: List<com.example.vozhatapp.data.local.entity.Event>
    ): List<EventAttendanceStats> {
        // Group attendance by event
        val eventAttendance = attendance.groupBy { it.eventId }

        return events.mapNotNull { event ->
            val records = eventAttendance[event.id] ?: return@mapNotNull null

            // Count attendance and absences
            val presentCount = records.count { it.isPresent }
            val absentCount = records.size - presentCount

            // Calculate attendance rate
            val attendanceRate = if (records.isNotEmpty()) {
                (presentCount * 100) / records.size
            } else {
                0
            }

            // Convert event date to LocalDate
            val eventDate = Instant
                .ofEpochMilli(event.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            EventAttendanceStats(
                eventId = event.id,
                eventTitle = event.title,
                eventDate = eventDate,
                presentCount = presentCount,
                absentCount = absentCount,
                attendanceRate = attendanceRate
            )
        }.sortedByDescending { it.eventDate }
    }

    private fun calculateChildStats(
        attendance: List<AttendanceWithDetails>,
        totalEvents: Int
    ): List<ChildAttendanceStats> {
        // Group attendance by child
        val childAttendance = attendance.groupBy { it.childId }

        return childAttendance.map { (childId, records) ->
            val firstRecord = records.first() // Use first record to get child info

            // Count attendance and absences
            val eventsAttended = records.count { it.isPresent }
            val eventsMissed = records.size - eventsAttended

            // Calculate attendance rate
            val attendanceRate = if (records.isNotEmpty()) {
                (eventsAttended * 100) / records.size
            } else {
                0
            }

            ChildAttendanceStats(
                childId = childId,
                childName = firstRecord.childName,
                squadName = firstRecord.squadName,
                eventsAttended = eventsAttended,
                eventsMissed = eventsMissed,
                attendanceRate = attendanceRate
            )
        }.sortedByDescending { it.attendanceRate }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class AttendanceReportsUiState(
    val isLoading: Boolean = true,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val summaryStats: AttendanceSummaryStats = AttendanceSummaryStats(),
    val squadStats: List<SquadAttendanceStats> = emptyList(),
    val eventStats: List<EventAttendanceStats> = emptyList(),
    val childStats: List<ChildAttendanceStats> = emptyList(),
    val showDateRangePicker: Boolean = false,
    val message: String? = null,
    val onDateRangeClick: () -> Unit
)

data class AttendanceWithDetails(
    val eventId: Long,
    val eventTitle: String,
    val eventDate: Long,
    val childId: Long,
    val childName: String,
    val squadName: String,
    val isPresent: Boolean,
    val note: String? = null
)

data class AttendanceSummaryStats(
    val totalEvents: Int = 0,
    val totalChildren: Int = 0,
    val totalAttendance: Int = 0,
    val averageAttendanceRate: Int = 0
)

data class SquadAttendanceStats(
    val squadName: String,
    val childrenCount: Int,
    val totalAttendance: Int,
    val totalAbsences: Int,
    val attendanceRate: Int
)

data class EventAttendanceStats(
    val eventId: Long,
    val eventTitle: String,
    val eventDate: LocalDate,
    val presentCount: Int,
    val absentCount: Int,
    val attendanceRate: Int
)

data class ChildAttendanceStats(
    val childId: Long,
    val childName: String,
    val squadName: String,
    val eventsAttended: Int,
    val eventsMissed: Int,
    val attendanceRate: Int
)