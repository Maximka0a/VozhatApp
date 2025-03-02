package com.example.vozhatapp.presentation.events.Detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.events.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    onEditEvent: (Long) -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load event data
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
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

    // Status color animation
    val statusColor by animateColorAsState(
        targetValue = when (uiState.event?.status) {
            0 -> Color(0xFF2196F3) // Upcoming: Blue
            1 -> Color(0xFF4CAF50) // In progress: Green
            else -> Color(0xFF9E9E9E) // Completed: Gray
        },
        label = "statusColor"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EventDetailTopAppBar(
                title = uiState.event?.title ?: "Детали события",
                onNavigateBack = onNavigateBack,
                onEditEvent = { onEditEvent(eventId) },
                onDeleteEvent = { viewModel.toggleDeleteDialog() },
                scrollBehavior = scrollBehavior,
                statusColor = statusColor,
                showActions = uiState.event != null
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingContent(padding = padding)
            }
            uiState.event == null -> {
                EventNotFoundContent(padding = padding)
            }
            else -> {
                val event = uiState.event!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status header
                    StatusHeader(event.status)

                    // Event details
                    EventDetailsContent(
                        event = event,
                        onChangeStatus = { newStatus ->
                            viewModel.updateEventStatus(newStatus)
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        DeleteEventDialog(
            eventTitle = uiState.event?.title ?: "",
            onDismiss = { viewModel.toggleDeleteDialog() },
            onConfirm = {
                viewModel.deleteEvent()
                viewModel.toggleDeleteDialog()
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EventNotFoundContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "Событие не найдено",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

