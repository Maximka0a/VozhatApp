package com.example.vozhatapp.presentation.notes.edit

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.notes.edit.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long = -1, // -1 means create new note
    onNavigateBack: () -> Unit,
    onNavigateToNoteDetail: (Long) -> Unit = {},
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val titleFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            viewModel.loadNote(noteId)
        } else {
            // Focus on title field when creating new note
            delay(300) // Small delay to ensure the UI is ready
            titleFocusRequester.requestFocus()
        }
    }

    // Handle snackbar messages
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    // Handle navigation after save
    LaunchedEffect(state.noteSaved, state.noteId) {
        if (state.noteSaved) {
            if (state.noteId > 0) {
                // Если есть ID заметки, можно открыть детали заметки
                onNavigateToNoteDetail(state.noteId)
            } else {
                // Иначе просто возвращаемся назад
                onNavigateBack()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NoteEditTopAppBar(
                isEditing = noteId > 0,
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
                onSave = {
                    viewModel.saveNote()
                },
                isSaveEnabled = state.title.isNotBlank() && state.content.isNotBlank() &&
                        (state.noteType != 1 ||
                                state.reminderDate?.let { it > System.currentTimeMillis() } == true)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                NoteEditForm(
                    state = state,
                    titleFocusRequester = titleFocusRequester,
                    onTitleChange = { viewModel.updateTitle(it) },
                    onContentChange = { viewModel.updateContent(it) },
                    onNoteTypeChange = { viewModel.toggleNoteType() },
                    onShowChildSelector = { viewModel.toggleChildSelectorDialog() },
                    onClearSelectedChild = { viewModel.clearSelectedChild() },
                    onShowDatePicker = { viewModel.toggleDatePickerDialog() },
                    onClearReminderDate = { viewModel.clearReminderDate() },
                    onSave = { viewModel.saveNote() }
                )
            }

            // Child selector dialog
            if (state.showChildSelector) {
                ChildSelectorDialog(
                    children = state.availableChildren,
                    searchQuery = state.childSearchQuery,
                    onSearchQueryChange = { viewModel.updateChildSearchQuery(it) },
                    onChildSelect = { viewModel.selectChild(it) },
                    onDismiss = { viewModel.toggleChildSelectorDialog() }
                )
            }

            // Date picker dialog
            if (state.showDatePicker) {
                DateTimePickerDialog(
                    initialDateTimeMillis = state.reminderDate
                        ?: System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Default to tomorrow
                    onDateTimeSelected = { viewModel.setReminderDate(it) },
                    onDismiss = { viewModel.toggleDatePickerDialog() }
                )
            }

            // Discard changes confirmation dialog
            if (state.showDiscardDialog) {
                DiscardChangesDialog(
                    onConfirm = {
                        viewModel.hideDiscardDialog()
                        onNavigateBack()
                    },
                    onDismiss = { viewModel.hideDiscardDialog() }
                )
            }
        }
    }

    // Handle back button logic
    BackHandler(enabled = state.hasChanges) {
        viewModel.showDiscardDialog()
    }
}

@Composable
private fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val backCallback = remember {
        object : androidx.activity.OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Update the callback's enabled state
    LaunchedEffect(enabled) {
        backCallback.isEnabled = enabled
    }

    DisposableEffect(backDispatcher) {
        backDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }
}