package com.example.vozhatapp.presentation.events.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.events.Edit.EventEditUiState
import java.time.Duration

@Composable
fun EventEditForm(
    uiState: EventEditUiState,
    isEditMode: Boolean,
    onTitleChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStatusChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title field
        EventTitleField(
            title = uiState.title,
            onTitleChange = onTitleChange,
            error = uiState.titleError
        )

        // Date and time section
        EventDateTimeSection(
            startDate = uiState.startDate,
            startTime = uiState.startTime,
            endDate = uiState.endDate,
            endTime = uiState.endTime,
            dateTimeError = uiState.dateTimeError,
            onStartDateClick = onStartDateClick,
            onStartTimeClick = onStartTimeClick,
            onEndDateClick = onEndDateClick,
            onEndTimeClick = onEndTimeClick
        )

        // Location field
        EventLocationField(
            location = uiState.location ?: "",
            onLocationChange = onLocationChange
        )

        // Description field
        EventDescriptionField(
            description = uiState.description ?: "",
            onDescriptionChange = onDescriptionChange
        )

        // Status section
        EventStatusSection(
            currentStatus = uiState.status,
            onStatusChange = onStatusChange
        )

        // Save button
        EventSaveButton(
            isEditMode = isEditMode,
            isValid = uiState.isValid,
            onSaveClick = onSaveClick
        )

        // Add some bottom space for better scrolling experience
        Spacer(modifier = Modifier.height(24.dp))
    }
}