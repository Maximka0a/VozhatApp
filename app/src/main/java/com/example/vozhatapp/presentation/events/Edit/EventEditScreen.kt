@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.vozhatapp.presentation.events.Edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.events.components.DateTimeDialogs
import com.example.vozhatapp.presentation.events.components.EventEditForm
import com.example.vozhatapp.presentation.events.components.EventEditTopAppBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EventEditScreen(
    eventId: Long? = null,
    onNavigateBack: () -> Unit,
    onEventSaved: (Long) -> Unit,
    viewModel: EventEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Determine if we're in edit or add mode
    val isEditMode = eventId != null
    val screenTitle = if (isEditMode) "Редактирование события" else "Новое событие"

    // Load event data if in edit mode
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.loadEvent(eventId)
        } else {
            viewModel.initializeNewEvent()
        }
    }

    // Handle success message
    LaunchedEffect(uiState.savedEventId) {
        uiState.savedEventId?.let { savedId ->
            onEventSaved(savedId)
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Date and time picker dialogs
    DateTimeDialogs(
        uiState = uiState,
        onStartDateSelected = viewModel::updateStartDate,
        onStartTimeSelected = viewModel::updateStartTime,
        onEndDateSelected = viewModel::updateEndDate,
        onEndTimeSelected = viewModel::updateEndTime,
        onStartDateDialogDismiss = { viewModel.toggleStartDatePicker(false) },
        onStartTimeDialogDismiss = { viewModel.toggleStartTimePicker(false) },
        onEndDateDialogDismiss = { viewModel.toggleEndDatePicker(false) },
        onEndTimeDialogDismiss = { viewModel.toggleEndTimePicker(false) }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EventEditTopAppBar(
                title = screenTitle,
                onNavigateBack = onNavigateBack,
                onSave = {
                    keyboardController?.hide()
                    viewModel.saveEvent()
                },
                isValid = uiState.isValid,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(padding)
        } else {
            EventEditForm(
                uiState = uiState,
                isEditMode = isEditMode,
                onTitleChange = viewModel::updateTitle,
                onLocationChange = viewModel::updateLocation,
                onDescriptionChange = viewModel::updateDescription,
                onStatusChange = viewModel::updateStatus,
                onSaveClick = {
                    keyboardController?.hide()
                    viewModel.saveEvent()
                },
                onStartDateClick = { viewModel.toggleStartDatePicker(true) },
                onStartTimeClick = { viewModel.toggleStartTimePicker(true) },
                onEndDateClick = { viewModel.toggleEndDatePicker(true) },
                onEndTimeClick = { viewModel.toggleEndTimePicker(true) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun LoadingIndicator(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}