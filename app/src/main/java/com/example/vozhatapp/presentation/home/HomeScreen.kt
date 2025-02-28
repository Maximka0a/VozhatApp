package com.example.vozhatapp.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.filledTonalButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vozhatapp.R
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.ui.theme.VozhatAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChildDetails: (Long) -> Unit,
    onNavigateToEventDetails: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeScreenTopBarState = rememberHomeScreenTopBarState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Анимация появления контента
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    var showWelcomeAnimation by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = Unit) {
        // После 2 секунд скрываем приветственную анимацию
        kotlinx.coroutines.delay(2000)
        showWelcomeAnimation = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            HomeTopBar(
                scrollState = scrollState,
                topBarState = homeScreenTopBarState,
                onSearchClick = { scope.launch { snackbarHostState.showSnackbar("Поиск") } },
                onProfileClick = onNavigateToProfile
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !showWelcomeAnimation,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Создать новое событие")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Создать"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Полноэкранная приветственная анимация
            AnimatedVisibility(
                visible = showWelcomeAnimation,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                WelcomeAnimation(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Основной контент
            AnimatedVisibility(
                visible = !showWelcomeAnimation,
                enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 300)),
                exit = fadeOut()
            ) {
                HomeContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = contentAlpha.value },
                    scrollState = scrollState,
                    uiState = uiState,
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToChildren = onNavigateToChildren,
                    onNavigateToAttendance = {  },
                    onNavigateToGames = onNavigateToGames,
                    onNavigateToChildDetails = onNavigateToChildDetails,
                    onNavigateToEventDetails = onNavigateToEventDetails
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    uiState: HomeUiState,
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToChildDetails: (Long) -> Unit,
    onNavigateToEventDetails: (Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Добавляем отступ снизу для FAB
    ) {
        // Карточки для быстрой навигации
        NavigationCards(
            onNavigateToEvents = onNavigateToEvents,
            onNavigateToChildren = onNavigateToChildren,
            onNavigateToAttendance = onNavigateToAttendance,
            onNavigateToGames = onNavigateToGames
        )

        // Секция с сегодняшними событиями
        TodayEventsSection(
            events = uiState.todayEvents,
            onEventClick = onNavigateToEventDetails
        )

        // Секция с детьми
        TopChildrenSection(
            children = uiState.topChildren,
            onChildClick = onNavigateToChildDetails
        )

        // Секция с предстоящими напоминаниями
        RemindersSection(reminders = uiState.upcomingReminders)

        // Индикатор загрузки внизу экрана вместо pull-to-refresh
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    scrollState: ScrollState,
    topBarState: HomeScreenTopBarState,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val scrollProgress = if (scrollState.maxValue == 0) {
        0f
    } else {
        (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
    }

    // Анимация изменения внешнего вида топбара при скроллинге
    val appBarElevation by animateDpAsState(
        targetValue = if (scrollProgress > 0.05f) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "appBarElevation"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = appBarElevation
    ) {
        TopAppBar(
            title = {
                Column {
                    AnimatedContent(
                        targetState = topBarState.isExpanded,
                        transitionSpec = {
                            if (targetState) {
                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                        slideOutVertically { height -> height } + fadeOut()
                            } else {
                                slideInVertically { height -> height } + fadeIn() togetherWith
                                        slideOutVertically { height -> -height } + fadeOut()
                            }
                        },
                        label = "titleAnimation"
                    ) { expanded ->
                        if (expanded) {
                            Text(
                                text = "Доброе утро,\nМаксим!",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        } else {
                            Text(
                                text = "Vozhat App",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Аватар",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Индикатор статуса онлайн
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                                .align(Alignment.BottomEnd)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}


@Composable
fun NavigationCards(
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGames: () -> Unit
) {
    val animationDelay = 100 // ms

    // Определяем карточки навигации
    val navItems = listOf(
        NavigationItem(
            title = "События",
            icon = Icons.Outlined.Event,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onNavigateToEvents
        ),
        NavigationItem(
            title = "Дети",
            icon = Icons.Outlined.People,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onNavigateToChildren
        ),
        NavigationItem(
            title = "Посещения",
            icon = Icons.Outlined.CheckCircle,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onNavigateToAttendance
        ),
        NavigationItem(
            title = "Игры",
            icon = Icons.Outlined.EmojiEvents,
            containerColor = VozhatAppTheme.extendedColors.squad2,
            contentColor = Color.Black.copy(alpha = 0.8f),
            onClick = onNavigateToGames
        )
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(navItems) { index, item ->
            var visibility by remember { mutableStateOf(false) }

            // Анимация появления навигационных карточек
            LaunchedEffect(key1 = Unit) {
                kotlinx.coroutines.delay(index.toLong() * animationDelay)
                visibility = true
            }

            AnimatedVisibility(
                visible = visibility,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                        expandHorizontally(animationSpec = spring(stiffness = Spring.StiffnessLow))
            ) {
                NavigationCard(
                    title = item.title,
                    icon = item.icon,
                    containerColor = item.containerColor,
                    contentColor = item.contentColor,
                    onClick = item.onClick
                )
            }
        }
    }
}

@Composable
fun TodayEventsSection(
    events: List<Event>,
    onEventClick: (Long) -> Unit
) {
    SectionWithTitle(
        title = "Сегодняшние события",
        showSeeAllButton = events.size > 3,
        onSeeAllClick = {}
    ) {
        val cardStates = remember(events.size) {
            List(events.size) { false }
        }.toMutableStateList()

        LaunchedEffect(Unit) {
            cardStates.forEachIndexed { index, _ ->
                kotlinx.coroutines.delay(200L + index * 100)
                cardStates[index] = true
            }
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.EventBusy,
                    message = "На сегодня нет запланированных событий",
                    actionText = "Создать событие",
                    onActionClick = {}
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEachIndexed { index, event ->
                    val isVisible = cardStates.getOrElse(index) { false }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInHorizontally(
                            initialOffsetX = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TopChildrenSection(
    children: List<Child>,
    onChildClick: (Long) -> Unit
) {
    SectionWithTitle(
        title = "Топ достижений",
        showSeeAllButton = children.size > 5,
        onSeeAllClick = {}
    ) {
        if (children.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.People,
                    message = "Список детей пуст",
                    actionText = "Добавить ребенка",
                    onActionClick = {}
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(children) { index, child ->
                        val offset = remember { Animatable(200f) }
                        val alpha = remember { Animatable(0f) }

                        LaunchedEffect(key1 = Unit) {
                            kotlinx.coroutines.delay(index * 100L)
                            launch {
                                offset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                alpha.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 500)
                                )
                            }
                        }

                        ChildAchievementCard(
                            child = child,
                            rank = index + 1,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = offset.value
                                    this.alpha = alpha.value
                                },
                            onClick = { onChildClick(child.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersSection(reminders: List<ReminderItem>) {
    SectionWithTitle(
        title = "Предстоящие напоминания",
        showSeeAllButton = reminders.size > 3,
        onSeeAllClick = {}
    ) {
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.Notifications,
                    message = "Нет предстоящих напоминаний",
                    actionText = "Создать напоминание",
                    onActionClick = {}
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reminders.forEachIndexed { index, reminder ->
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(key1 = Unit) {
                        kotlinx.coroutines.delay(300 + index * 100L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + expandVertically()
                    ) {
                        ReminderCard(reminder = reminder)
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Градиентный фон
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Анимированный логотип
            Icon(
                imageVector = Icons.Default.EmojiPeople,
                contentDescription = "Логотип",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        scaleX = scale
                        scaleY = scale
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Vozhat App",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Привет, Максим!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// Вспомогательные компоненты для главного экрана
@Composable
fun NavigationCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val shadowElevation = remember { Animatable(2f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.shadowElevation = shadowElevation.value
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when {
                            event.type == PointerEventType.Press -> {
                                scope.launch {
                                    shadowElevation.animateTo(8f)
                                    scale.animateTo(
                                        targetValue = 0.95f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }

                            event.type == PointerEventType.Release -> {
                                scope.launch {
                                    shadowElevation.animateTo(2f)
                                    scale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                onClick()
                            }
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun SectionWithTitle(
    title: String,
    showSeeAllButton: Boolean = true,
    onSeeAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            if (showSeeAllButton) {
                TextButton(onClick = onSeeAllClick) {
                    Text(
                        text = "Все",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTime = remember(event.startTime) { dateFormatter.format(event.startTime) }
    val endTime = remember(event.endTime) { dateFormatter.format(event.endTime) }
    val currentTime = remember { System.currentTimeMillis() }
    val isActive = currentTime in event.startTime.time..event.endTime.time

    val progress = if (currentTime < event.startTime.time) {
        0f
    } else if (currentTime > event.endTime.time) {
        1f
    } else {
        val total = event.endTime.time - event.startTime.time
        val elapsed = currentTime - event.startTime.time
        (elapsed.toFloat() / total).coerceIn(0f, 1f)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Progress indicator
            if (isActive) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isActive) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(text = "Сейчас")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = "$startTime - $endTime",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Место",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.location ?: "Не указано",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChildAchievementCard(
    child: Child,
    rank: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val badgeColor = when (rank) {
        1 -> VozhatAppTheme.extendedColors.badgeGold
        2 -> VozhatAppTheme.extendedColors.badgeSilver
        3 -> VozhatAppTheme.extendedColors.badgeBronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeContentColor = when (rank) {
        1, 2, 3 -> Color.Black.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Бейдж с рангом
            Surface(
                shape = CircleShape,
                color = badgeColor,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeContentColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Фото ребенка
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (child.photoUrl != null) {
                        AsyncImage(
                            model = child.photoUrl,
                            contentDescription = "Фото ${child.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Фото ${child.name}",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${child.name} ${child.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = child.squadName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Количество баллов (заглушка)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Баллы",
                        modifier = Modifier.size(16.dp),
                        tint = when (rank) {
                            1 -> VozhatAppTheme.extendedColors.badgeGold
                            2 -> VozhatAppTheme.extendedColors.badgeSilver
                            3 -> VozhatAppTheme.extendedColors.badgeBronze
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${150 - (rank - 1) * 15} баллов", // Заглушка для демонстрации
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: ReminderItem) {
    val calendar = Calendar.getInstance()
    calendar.time = reminder.date
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val timeFormatted = String.format("%02d:%02d", hour, minute)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка времени
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Alarm,
                    contentDescription = "Время",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Время напоминания
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyStateItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onActionClick,
                colors = filledTonalButtonColors()
            ) {
                Text(text = actionText)
            }
        }
    }
}

// Вспомогательные классы
data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun rememberHomeScreenTopBarState(): HomeScreenTopBarState {
    return remember { HomeScreenTopBarState() }
}

class HomeScreenTopBarState {
    var isExpanded by mutableStateOf(true)
}

