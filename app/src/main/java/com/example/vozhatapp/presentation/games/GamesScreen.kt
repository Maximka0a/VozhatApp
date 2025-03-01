package com.example.vozhatapp.presentation.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GamesScreen(
    onGameClick: (Long) -> Unit,
    onAddGameClick: () -> Unit,
    viewModel: GamesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Animation for fab
    val fabAnimationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fabAnimationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
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
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Игры и активности",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilterDialog() }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Фильтры"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onAddGameClick,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = fabAnimationProgress.value
                            scaleY = fabAnimationProgress.value
                            alpha = fabAnimationProgress.value
                        },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить игру")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                focusRequester = searchFocusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category chips
            CategoryChips(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            )

            // Games display
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.filteredGames.isEmpty() -> {
                        EmptyGamesPlaceholder(
                            isSearching = uiState.searchQuery.isNotEmpty(),
                            onClearSearch = { viewModel.setSearchQuery("") }
                        )
                    }
                    else -> {
                        GamesGrid(
                            games = uiState.filteredGames,
                            onGameClick = onGameClick
                        )
                    }
                }
            }
        }

        // Filter dialog
        if (uiState.showFilterDialog) {
            FilterDialog(
                onDismiss = { viewModel.toggleFilterDialog() },
                minAge = uiState.minAgeFilter,
                maxAge = uiState.maxAgeFilter,
                minPlayers = uiState.minPlayersFilter,
                maxPlayers = uiState.maxPlayersFilter,
                maxDuration = uiState.maxDurationFilter,
                onMinAgeChange = viewModel::setMinAgeFilter,
                onMaxAgeChange = viewModel::setMaxAgeFilter,
                onMinPlayersChange = viewModel::setMinPlayersFilter,
                onMaxPlayersChange = viewModel::setMaxPlayersFilter,
                onMaxDurationChange = viewModel::setMaxDurationFilter,
                onResetFilters = viewModel::resetFilters,
                onApplyFilters = {
                    viewModel.applyFilters()
                    viewModel.toggleFilterDialog()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { Text("Поиск игр...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поиск"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Очистить"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
//        colors = OutlinedTextFieldDefaults(
//            focusedBorderColor = MaterialTheme.colorScheme.primary,
//            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
//            cursorColor = MaterialTheme.colorScheme.primary
//        )
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("Все") },
            leadingIcon = if (selectedCategory == null) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )

        // Category chips
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = if (selectedCategory == category) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun GamesGrid(
    games: List<com.example.vozhatapp.data.local.entity.Game>,
    onGameClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(games) { game ->
            GameCard(
                game = game,
                onClick = { onGameClick(game.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCard(
    game: com.example.vozhatapp.data.local.entity.Game,
    onClick: () -> Unit
) {
    var cardElevation by remember { mutableStateOf(1.dp) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                shadowElevation = cardElevation.toPx()
            }
            .pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation,
            pressedElevation = 4.dp,
            focusedElevation = 3.dp,
            hoveredElevation = 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Category indicator at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(getCategoryColor(game.category))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title and category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getCategoryIcon(game.category),
                        contentDescription = null,
                        tint = getCategoryColor(game.category),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = game.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Description
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Game info chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Age range chip
                    if (game.minAge != null || game.maxAge != null) {
                        val ageText = when {
                            game.minAge != null && game.maxAge != null -> "${game.minAge}-${game.maxAge} лет"
                            game.minAge != null -> "от ${game.minAge} лет"
                            game.maxAge != null -> "до ${game.maxAge} лет"
                            else -> ""
                        }

                        InfoChip(
                            icon = Icons.Outlined.ChildCare,
                            text = ageText
                        )
                    }

                    // Players chip
                    if (game.minPlayers != null || game.maxPlayers != null) {
                        val playersText = when {
                            game.minPlayers != null && game.maxPlayers != null ->
                                if (game.minPlayers == game.maxPlayers) "${game.minPlayers} игроков"
                                else "${game.minPlayers}-${game.maxPlayers} игроков"
                            game.minPlayers != null -> "от ${game.minPlayers} игроков"
                            game.maxPlayers != null -> "до ${game.maxPlayers} игроков"
                            else -> ""
                        }

                        InfoChip(
                            icon = Icons.Outlined.People,
                            text = playersText
                        )
                    }

                    // Duration chip
                    if (game.duration != null) {
                        InfoChip(
                            icon = Icons.Outlined.Timer,
                            text = "${game.duration} мин"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EmptyGamesPlaceholder(
    isSearching: Boolean,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Outlined.SearchOff else Icons.Outlined.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        Text(
            text = if (isSearching) "Ничего не найдено" else "Пока нет игр",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = if (isSearching)
                "По вашему запросу не найдено игр. Попробуйте изменить критерии поиска."
            else
                "Добавьте игры и активности для работы с детьми",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
        )

        if (isSearching) {
            Button(
                onClick = onClearSearch,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Очистить поиск")
            }
        }
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    minAge: Int?,
    maxAge: Int?,
    minPlayers: Int?,
    maxPlayers: Int?,
    maxDuration: Int?,
    onMinAgeChange: (Int?) -> Unit,
    onMaxAgeChange: (Int?) -> Unit,
    onMinPlayersChange: (Int?) -> Unit,
    onMaxPlayersChange: (Int?) -> Unit,
    onMaxDurationChange: (Int?) -> Unit,
    onResetFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Фильтры игр")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Age range filter
                Text(
                    text = "Возраст",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTextField(
                        value = minAge?.toString() ?: "",
                        onValueChange = { value ->
                            onMinAgeChange(value.toIntOrNull())
                        },
                        label = "От",
                        modifier = Modifier.weight(1f)
                    )
                    FilterTextField(
                        value = maxAge?.toString() ?: "",
                        onValueChange = { value ->
                            onMaxAgeChange(value.toIntOrNull())
                        },
                        label = "До",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Players filter
                Text(
                    text = "Количество игроков",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTextField(
                        value = minPlayers?.toString() ?: "",
                        onValueChange = { value ->
                            onMinPlayersChange(value.toIntOrNull())
                        },
                        label = "От",
                        modifier = Modifier.weight(1f)
                    )
                    FilterTextField(
                        value = maxPlayers?.toString() ?: "",
                        onValueChange = { value ->
                            onMaxPlayersChange(value.toIntOrNull())
                        },
                        label = "До",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Duration filter
                Text(
                    text = "Длительность",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "До",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterTextField(
                        value = maxDuration?.toString() ?: "",
                        onValueChange = { value ->
                            onMaxDurationChange(value.toIntOrNull())
                        },
                        label = "Минут",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApplyFilters
            ) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onResetFilters
            ) {
                Text("Сбросить")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow digits
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = modifier
    )
}

@Composable
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "активные" -> Icons.Outlined.DirectionsRun
        "настольные" -> Icons.Outlined.Casino
        "творческие" -> Icons.Outlined.Palette
        "интеллектуальные" -> Icons.Outlined.Psychology
        "командные" -> Icons.Outlined.Groups
        "музыкальные" -> Icons.Outlined.MusicNote
        "вечерние" -> Icons.Outlined.Nightlight
        else -> Icons.Outlined.SportsEsports
    }
}

@Composable
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "активные" -> MaterialTheme.colorScheme.primary
        "настольные" -> MaterialTheme.colorScheme.secondary
        "творческие" -> MaterialTheme.colorScheme.tertiary
        "интеллектуальные" -> Color(0xFF6750A4) // Purple
        "командные" -> Color(0xFF1976D2) // Blue
        "музыкальные" -> Color(0xFFE91E63) // Pink
        "вечерние" -> Color(0xFF4A148C) // Deep Purple
        else -> MaterialTheme.colorScheme.primary
    }
}