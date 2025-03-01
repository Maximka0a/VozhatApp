package com.example.vozhatapp.presentation.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: Long,
    onNavigateBack: () -> Unit,
    onEditGame: (Long) -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load game data
    LaunchedEffect(gameId) {
        viewModel.loadGame(gameId)
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

    // Determine background color based on category
    val categoryColor = uiState.game?.let { getCategoryColor(it.category) } ?: MaterialTheme.colorScheme.surface

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = uiState.game?.title ?: "Детали игры",
                        maxLines = 2
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
                    if (uiState.game != null) {
                        IconButton(onClick = { onEditGame(gameId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleDeleteDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = categoryColor.copy(alpha = 0.9f),
                    scrolledContainerColor = categoryColor.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
        } else if (uiState.game == null) {
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
                        imageVector = Icons.Outlined.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Игра не найдена",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            val game = uiState.game!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Category badge
                Surface(
                    color = categoryColor,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(game.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = game.category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Game details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Game specifications
                    GameSpecifications(game)

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Description
                    Text(
                        text = "Описание",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Materials if available
                    if (!game.materials.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        Text(
                            text = "Необходимые материалы",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = game.materials,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDeleteDialog() },
            title = { Text("Удаление игры") },
            text = { Text("Вы уверены, что хотите удалить игру \"${uiState.game?.title}\"? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGame()
                        viewModel.toggleDeleteDialog()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun GameSpecifications(game: com.example.vozhatapp.data.local.entity.Game) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Age range
            if (game.minAge != null || game.maxAge != null) {
                SpecificationItem(
                    icon = Icons.Outlined.ChildCare,
                    label = "Возраст",
                    value = when {
                        game.minAge != null && game.maxAge != null -> "${game.minAge}-${game.maxAge} лет"
                        game.minAge != null -> "от ${game.minAge} лет"
                        game.maxAge != null -> "до ${game.maxAge} лет"
                        else -> "-"
                    }
                )
            }

            // Players count
            if (game.minPlayers != null || game.maxPlayers != null) {
                SpecificationItem(
                    icon = Icons.Outlined.People,
                    label = "Игроков",
                    value = when {
                        game.minPlayers != null && game.maxPlayers != null ->
                            if (game.minPlayers == game.maxPlayers) "${game.minPlayers}"
                            else "${game.minPlayers}-${game.maxPlayers}"
                        game.minPlayers != null -> "от ${game.minPlayers}"
                        game.maxPlayers != null -> "до ${game.maxPlayers}"
                        else -> "-"
                    }
                )
            }

            // Duration
            if (game.duration != null) {
                SpecificationItem(
                    icon = Icons.Outlined.Timer,
                    label = "Длительность",
                    value = "${game.duration} минут"
                )
            }
        }
    }
}

@Composable
fun SpecificationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}