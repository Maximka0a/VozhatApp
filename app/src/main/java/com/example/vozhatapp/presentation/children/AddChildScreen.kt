package com.example.vozhatapp.presentation.children

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.vozhatapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
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

    // Form field focus management
    val nameFocusRequester = remember { FocusRequester() }
    val lastNameFocusRequester = remember { FocusRequester() }
    val ageFocusRequester = remember { FocusRequester() }
    val squadFocusRequester = remember { FocusRequester() }

    // Dropdown menu state
    var isSquadMenuExpanded by remember { mutableStateOf(false) }

    // Add New Squad Dialog state
    var showAddSquadDialog by remember { mutableStateOf(false) }
    var newSquadName by remember { mutableStateOf("") }
    var newSquadNameError by remember { mutableStateOf<String?>(null) }

    // Delete confirmation dialog state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        formProgress = 1f

        // Request focus with a small delay for better UX
        // Only auto-focus fields when adding a new child, not when editing
        if (childId == null) {
            kotlinx.coroutines.delay(300)
            nameFocusRequester.requestFocus()
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

    // Add New Squad Dialog
    if (showAddSquadDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSquadDialog = false
                newSquadName = ""
                newSquadNameError = null
            },
            title = {
                Text(stringResource(R.string.add_new_squad))
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSquadName,
                        onValueChange = {
                            newSquadName = it
                            newSquadNameError = null
                        },
                        label = { Text(stringResource(R.string.squad_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        isError = newSquadNameError != null,
                        supportingText = {
                            newSquadNameError?.let { Text(it) }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val squadNameRequired = stringResource(R.string.squad_name_required)
                val squadNameExists = stringResource(R.string.squad_name_exists)
                Button(
                    onClick = {
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
                    }
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddSquadDialog = false
                        newSquadName = ""
                        newSquadNameError = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.delete_child_title)) },
            text = {
                Text(stringResource(
                    R.string.delete_child_confirmation,
                    "${state.name} ${state.lastName}"
                ))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(AddChildEvent.DeleteChild)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Determine if we're in add or edit mode
    val isEditMode = state.id != null
    val titleResId = if (isEditMode) R.string.edit_child else R.string.add_child
    val saveButtonResId = if (isEditMode) R.string.update_child else R.string.save_child

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        stringResource(titleResId),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = !isFormValid,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(AddChildEvent.SaveChild)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.save_child),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .graphicsLayer {
                                alpha = formProgressAnimation.value
                                translationY = (1f - formProgressAnimation.value) * 100
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        PhotoSelectionSection(
                            photoUri = state.photoUrl,
                            onSelectPhoto = { imagePicker.launch("image/*") },
                            onClearPhoto = { viewModel.onEvent(AddChildEvent.PhotoUrlChanged(null)) }
                        )
                    }

                    // Basic info section
                    FormSection(
                        title = stringResource(R.string.basic_info),
                        icon = Icons.Outlined.Person,
                        formProgress = formProgressAnimation.value,
                        offset = 0
                    ) {
                        // Name field
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.onEvent(AddChildEvent.NameChanged(it)) },
                            label = { Text(stringResource(R.string.name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .focusRequester(nameFocusRequester),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { lastNameFocusRequester.requestFocus() }
                            ),
                            isError = !state.nameError.isNullOrEmpty(),
                            supportingText = { state.nameError?.let { Text(it) } },
                            singleLine = true
                        )

                        // Last name field
                        OutlinedTextField(
                            value = state.lastName,
                            onValueChange = { viewModel.onEvent(AddChildEvent.LastNameChanged(it)) },
                            label = { Text(stringResource(R.string.last_name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .focusRequester(lastNameFocusRequester),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { ageFocusRequester.requestFocus() }
                            ),
                            isError = !state.lastNameError.isNullOrEmpty(),
                            supportingText = { state.lastNameError?.let { Text(it) } },
                            singleLine = true
                        )

                        // Age field
                        OutlinedTextField(
                            value = if (state.age > 0) state.age.toString() else "",
                            onValueChange = { viewModel.onEvent(AddChildEvent.AgeChanged(it)) },
                            label = { Text(stringResource(R.string.age)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .focusRequester(ageFocusRequester),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Cake,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { squadFocusRequester.requestFocus() }
                            ),
                            isError = !state.ageError.isNullOrEmpty(),
                            supportingText = { state.ageError?.let { Text(it) } },
                            singleLine = true
                        )

                        // Squad dropdown
                        ExposedDropdownMenuBox(
                            expanded = isSquadMenuExpanded,
                            onExpandedChange = { isSquadMenuExpanded = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(squadFocusRequester)
                        ) {
                            OutlinedTextField(
                                value = state.squadName,
                                onValueChange = { viewModel.onEvent(AddChildEvent.SquadNameChanged(it)) },
                                label = { Text(stringResource(R.string.squad)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Groups,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isSquadMenuExpanded
                                    )
                                },
                                isError = !state.squadNameError.isNullOrEmpty(),
                                supportingText = { state.squadNameError?.let { Text(it) } },
                                singleLine = true,
                                readOnly = true,
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = isSquadMenuExpanded,
                                onDismissRequest = { isSquadMenuExpanded = false }
                            ) {
                                squadNames.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.onEvent(AddChildEvent.SquadNameChanged(option))
                                            isSquadMenuExpanded = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }

                                // Add new squad option
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.add_new_squad)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    },
                                    onClick = {
                                        isSquadMenuExpanded = false
                                        showAddSquadDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Contact info section
                    FormSection(
                        title = stringResource(R.string.contact_info),
                        icon = Icons.Outlined.ContactPhone,
                        formProgress = formProgressAnimation.value,
                        offset = 1
                    ) {
                        // Parent phone field
                        OutlinedTextField(
                            value = state.parentPhone ?: "",
                            onValueChange = { viewModel.onEvent(AddChildEvent.ParentPhoneChanged(it)) },
                            label = { Text(stringResource(R.string.parent_phone)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            isError = !state.parentPhoneError.isNullOrEmpty(),
                            supportingText = { state.parentPhoneError?.let { Text(it) } },
                            singleLine = true
                        )

                        // Parent email field
                        OutlinedTextField(
                            value = state.parentEmail ?: "",
                            onValueChange = { viewModel.onEvent(AddChildEvent.ParentEmailChanged(it)) },
                            label = { Text(stringResource(R.string.parent_email)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            isError = !state.parentEmailError.isNullOrEmpty(),
                            supportingText = { state.parentEmailError?.let { Text(it) } },
                            singleLine = true
                        )

                        // Address field
                        OutlinedTextField(
                            value = state.address ?: "",
                            onValueChange = { viewModel.onEvent(AddChildEvent.AddressChanged(it)) },
                            label = { Text(stringResource(R.string.address)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Home,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            maxLines = 3,
                        )
                    }

                    // Medical notes section
                    FormSection(
                        title = stringResource(R.string.medical_information),
                        icon = Icons.Outlined.HealthAndSafety,
                        formProgress = formProgressAnimation.value,
                        offset = 2
                    ) {
                        OutlinedTextField(
                            value = state.medicalNotes ?: "",
                            onValueChange = { viewModel.onEvent(AddChildEvent.MedicalNotesChanged(it)) },
                            label = { Text(stringResource(R.string.medical_notes)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.padding(bottom = 40.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            supportingText = { Text(stringResource(R.string.optional)) }
                        )

                        // Medical notes tips
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = stringResource(R.string.medical_notes_info),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Save/Update button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.onEvent(AddChildEvent.SaveChild)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .graphicsLayer {
                                alpha = formProgressAnimation.value
                                translationY = (1f - formProgressAnimation.value) * 100
                            }
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isFormValid
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            stringResource(saveButtonResId),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Show deletion option only in edit mode
                    if (isEditMode) {
                        OutlinedButton(
                            onClick = {
                                showDeleteConfirmDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                stringResource(R.string.delete_child),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    formProgress: Float,
    offset: Int,
    content: @Composable () -> Unit
) {
    val animationDelay = 100 * offset
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(formProgress) {
        delay(animationDelay.toLong())
        animationProgress.animateTo(
            targetValue = formProgress,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .graphicsLayer {
                alpha = animationProgress.value
                translationY = (1f - animationProgress.value) * 100
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun PhotoSelectionSection(
    photoUri: String?,
    onSelectPhoto: () -> Unit,
    onClearPhoto: () -> Unit
) {
    // Animation for photo selection button
    val buttonScale = remember { Animatable(1f) }
    val photoSelected = photoUri != null

    LaunchedEffect(photoSelected) {
        if (photoSelected) {
            buttonScale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .background(
                    color = if (photoSelected)
                        Color.Transparent
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(onClick = onSelectPhoto),
            contentAlignment = Alignment.Center
        ) {
            if (photoSelected) {
                // Display selected photo
                Image(
                    painter = rememberAsyncImagePainter(model = photoUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay for clear button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    IconButton(
                        onClick = onClearPhoto,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_photo),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                // Photo selection icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = buttonScale.value
                            scaleY = buttonScale.value
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.add_photo),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.add_photo),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !photoSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = stringResource(R.string.photo_optional),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}