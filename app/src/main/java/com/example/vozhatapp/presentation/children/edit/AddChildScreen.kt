package com.example.vozhatapp.presentation.children.edit

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.R
import com.example.vozhatapp.presentation.children.edit.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildScreen(
    onNavigateBack: () -> Unit,
    onChildAdded: (Long) -> Unit,
    childId: Long? = null,
    viewModel: AddChildViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState().value

    // Load child data if in edit mode
    LaunchedEffect(childId) {
        if (childId != null) {
            viewModel.loadChild(childId)
        }
    }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Squad names from repository
    val squadNames = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        viewModel.loadSquads().collectLatest { names ->
            squadNames.clear()
            squadNames.addAll(names)
        }
    }

    // State for form animation
    var formProgress by remember { mutableFloatStateOf(0f) }
    val formProgressAnimation = animateFloatAsState(
        targetValue = formProgress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "formProgress"
    )

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(AddChildEvent.PhotoUrlChanged(it.toString()))
        }
    }

    // Form validation
    val isFormValid = !state.nameError.isNullOrEmpty() &&
            !state.lastNameError.isNullOrEmpty() &&
            !state.ageError.isNullOrEmpty() &&
            !state.squadNameError.isNullOrEmpty() &&
            (state.parentPhone.isNullOrEmpty() || state.parentEmail.isNullOrEmpty())

    // State for dialogs
    var showAddSquadDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newSquadName by remember { mutableStateOf("") }
    var newSquadNameError by remember { mutableStateOf<String?>(null) }

    // State for focus management
    val focusRequesters = rememberChildFormFocusRequesters()

    // Expanded state for squad dropdown
    var isSquadMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        formProgress = 1f

        // Request focus with a small delay for better UX
        // Only auto-focus fields when adding a new child, not when editing
        if (childId == null) {
            delay(300)
            focusRequesters.nameFocusRequester.requestFocus()
        }
    }

    // Handle successful save
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess && state.savedChildId != null) {
            onChildAdded(state.savedChildId)
        }
    }

    // Handle successful deletion
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            onNavigateBack()
        }
    }

    // Handle error messages
    LaunchedEffect(state.saveError) {
        if (!state.saveError.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(
                message = state.saveError,
                duration = SnackbarDuration.Short
            )
        }
    }
    val squadNameRequired = stringResource(R.string.squad_name_required)
    val squadNameExists = stringResource(R.string.squad_name_exists)
    // Dialogs
    if (showAddSquadDialog) {
        AddSquadDialog(
            squadName = newSquadName,
            onSquadNameChange = { newSquadName = it; newSquadNameError = null },
            onDismiss = {
                showAddSquadDialog = false
                newSquadName = ""
                newSquadNameError = null
            },
            onConfirm = {
                if (newSquadName.isBlank()) {
                    newSquadNameError = squadNameRequired
                } else if (newSquadName in squadNames) {
                    newSquadNameError = squadNameExists
                } else {
                    coroutineScope.launch {
                        viewModel.onEvent(AddChildEvent.SquadNameChanged(newSquadName))
                        showAddSquadDialog = false
                        newSquadName = ""
                    }
                }
            },
            error = newSquadNameError
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteChildConfirmDialog(
            childName = "${state.name} ${state.lastName}",
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                viewModel.onEvent(AddChildEvent.DeleteChild)
                showDeleteConfirmDialog = false
            }
        )
    }

    // Determine if we're in add or edit mode
    val isEditMode = state.id != null

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChildEditTopAppBar(
                isEditMode = isEditMode,
                isFormValid = isFormValid,
                onNavigateBack = onNavigateBack,
                onSave = { viewModel.onEvent(AddChildEvent.SaveChild) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                        .animateContentSize()
                ) {
                    // Photo selection section
                    PhotoSelectionSection(
                        photoUri = state.photoUrl,
                        onSelectPhoto = { imagePicker.launch("image/*") },
                        onClearPhoto = { viewModel.onEvent(AddChildEvent.PhotoUrlChanged(null)) },
                        formProgress = formProgressAnimation.value
                    )

                    // Basic info section
                    BasicInfoFormSection(
                        name = state.name,
                        lastName = state.lastName,
                        age = state.age,
                        squadName = state.squadName,
                        nameError = state.nameError,
                        lastNameError = state.lastNameError,
                        ageError = state.ageError,
                        squadNameError = state.squadNameError,
                        squadNames = squadNames,
                        isSquadMenuExpanded = isSquadMenuExpanded,
                        onNameChange = { viewModel.onEvent(AddChildEvent.NameChanged(it)) },
                        onLastNameChange = { viewModel.onEvent(AddChildEvent.LastNameChanged(it)) },
                        onAgeChange = { viewModel.onEvent(AddChildEvent.AgeChanged(it)) },
                        onSquadNameChange = { viewModel.onEvent(AddChildEvent.SquadNameChanged(it)) },
                        onSquadMenuExpandedChange = { isSquadMenuExpanded = it },
                        onAddNewSquad = {
                            isSquadMenuExpanded = false
                            showAddSquadDialog = true
                        },
                        focusRequesters = focusRequesters,
                        formProgress = formProgressAnimation.value,
                        formOffset = 0
                    )

                    // Contact info section
                    ContactInfoFormSection(
                        parentPhone = state.parentPhone ?: "",
                        parentEmail = state.parentEmail ?: "",
                        address = state.address ?: "",
                        parentPhoneError = state.parentPhoneError,
                        parentEmailError = state.parentEmailError,
                        onParentPhoneChange = { viewModel.onEvent(AddChildEvent.ParentPhoneChanged(it)) },
                        onParentEmailChange = { viewModel.onEvent(AddChildEvent.ParentEmailChanged(it)) },
                        onAddressChange = { viewModel.onEvent(AddChildEvent.AddressChanged(it)) },
                        formProgress = formProgressAnimation.value,
                        formOffset = 1
                    )

                    // Medical notes section
                    MedicalNotesFormSection(
                        medicalNotes = state.medicalNotes ?: "",
                        onMedicalNotesChange = { viewModel.onEvent(AddChildEvent.MedicalNotesChanged(it)) },
                        formProgress = formProgressAnimation.value,
                        formOffset = 2
                    )

                    // Form buttons
                    FormButtonsSection(
                        isEditMode = isEditMode,
                        isFormValid = !isFormValid,
                        onSave = {
                            keyboardController?.hide()
                            viewModel.onEvent(AddChildEvent.SaveChild)
                        },
                        onDelete = { showDeleteConfirmDialog = true },
                        formProgress = formProgressAnimation.value
                    )
                }
            }
        }
    }
}