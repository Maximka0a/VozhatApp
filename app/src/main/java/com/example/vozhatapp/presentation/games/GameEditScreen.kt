package com.example.vozhatapp.presentation.games

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GameEditScreen(
    gameId: Long? = null,
    onNavigateBack: () -> Unit,
    onGameSaved: (Long) -> Unit,
    viewModel: GameEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Determine if we're in edit or add mode
    val isEditMode = gameId != null
    val screenTitle = if (isEditMode) "Редактирование игры" else "Добавление новой игры"

    // Load game data if in edit mode
    LaunchedEffect(gameId) {
        if (gameId != null) {
            viewModel.loadGame(gameId)
        }
    }

    // Handle success message
    LaunchedEffect(uiState.savedGameId) {
        uiState.savedGameId?.let { savedId ->
            onGameSaved(savedId)
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(screenTitle)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.saveGame()
                        },
                        enabled = uiState.isValid
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Сохранить",
                            tint = if (uiState.isValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()) // Fixed: correct function name
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.titleError != null,
                    supportingText = {
                        uiState.titleError?.let { Text(it) }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = uiState.showCategoryDropdown,
                    onExpandedChange = { viewModel.toggleCategoryDropdown() }
                ) {
                    OutlinedTextField(
                        value = uiState.category,
                        onValueChange = { viewModel.updateCategory(it) },
                        label = { Text("Категория") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.showCategoryDropdown)
                        },
                        isError = uiState.categoryError != null,
                        supportingText = {
                            uiState.categoryError?.let { Text(it) }
                        },
                        readOnly = true
                    )

                    ExposedDropdownMenu(
                        expanded = uiState.showCategoryDropdown,
                        onDismissRequest = { viewModel.toggleCategoryDropdown() }
                    ) {
                        viewModel.availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.updateCategory(category)
                                    viewModel.toggleCategoryDropdown()
                                }
                            )
                        }
                    }
                }

                // Age range
                Text(
                    text = "Возрастной диапазон",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.minAge?.toString() ?: "",
                        onValueChange = { value ->
                            viewModel.updateMinAge(value.toIntOrNull())
                        },
                        label = { Text("От") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.maxAge?.toString() ?: "",
                        onValueChange = { value ->
                            viewModel.updateMaxAge(value.toIntOrNull())
                        },
                        label = { Text("До") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                }

                // Players count
                Text(
                    text = "Количество игроков",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.minPlayers?.toString() ?: "",
                        onValueChange = { value ->
                            viewModel.updateMinPlayers(value.toIntOrNull())
                        },
                        label = { Text("От") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.maxPlayers?.toString() ?: "",
                        onValueChange = { value ->
                            viewModel.updateMaxPlayers(value.toIntOrNull())
                        },
                        label = { Text("До") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                }

                // Duration
                OutlinedTextField(
                    value = uiState.duration?.toString() ?: "",
                    onValueChange = { value ->
                        viewModel.updateDuration(value.toIntOrNull())
                    },
                    label = { Text("Длительность (в минутах)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Описание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    isError = uiState.descriptionError != null,
                    supportingText = {
                        uiState.descriptionError?.let { Text(it) }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Materials
                OutlinedTextField(
                    value = uiState.materials ?: "",
                    onValueChange = { viewModel.updateMaterials(it) },
                    label = { Text("Необходимые материалы") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    supportingText = { Text("Необязательно") }
                )

                // Save button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.saveGame()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    enabled = uiState.isValid
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (isEditMode) "Сохранить изменения" else "Добавить игру")
                }
            }
        }
    }
}