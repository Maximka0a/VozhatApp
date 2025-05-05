package com.example.vozhatapp.presentation.achievements.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.R
import com.example.vozhatapp.presentation.components.DatePickerField
import com.example.vozhatapp.ui.theme.VozhatAppTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementEditScreen(
    childId: Long,
    achievementId: Long? = null,
    onNavigateBack: () -> Unit,
    onAchievementSaved: (Long) -> Unit,
    viewModel: AchievementEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Initialize with the achievement data if editing
    LaunchedEffect(childId, achievementId) {
        viewModel.initialize(childId, achievementId)
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

    // Handle successful save
    LaunchedEffect(uiState.savedAchievementId) {
        uiState.savedAchievementId?.let { id ->
            onAchievementSaved(id)
            viewModel.clearSavedId()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (achievementId == null) "Новое достижение"
                        else "Редактирование достижения"
                    )
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
                        onClick = { viewModel.saveAchievement() },
                        enabled = !uiState.isLoading && uiState.canSave
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title field
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Название достижения") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        isError = uiState.titleError != null,
                        supportingText = {
                            uiState.titleError?.let { Text(it) }
                        }
                    )

                    // Description field
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Описание") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        maxLines = 5
                    )

                    // Points field
                    OutlinedTextField(
                        value = uiState.points,
                        onValueChange = { viewModel.updatePoints(it) },
                        label = { Text("Баллы") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        isError = uiState.pointsError != null,
                        supportingText = {
                            uiState.pointsError?.let { Text(it) }
                        }
                    )

                    // Date picker
                    DatePickerField(
                        label = "Дата достижения",
                        selectedDate = uiState.date,
                        onDateSelected = { viewModel.updateDate(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save button
                    Button(
                        onClick = { viewModel.saveAchievement() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.canSave
                    ) {
                        Text("Сохранить")
                    }

                    // Cancel button
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Отмена")
                    }
                }
            }
        }
    }
}