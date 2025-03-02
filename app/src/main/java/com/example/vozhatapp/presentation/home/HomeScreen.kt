package com.example.vozhatapp.presentation.home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vozhatapp.presentation.home.common.WelcomeAnimation
import com.example.vozhatapp.presentation.home.components.HomeContent
import com.example.vozhatapp.presentation.home.components.HomeTopBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToChildDetails: (Long) -> Unit,
    onNavigateToEventDetails: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeScreenTopBarState = rememberHomeScreenTopBarState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Log.d("123", uiState.todayEvents.toString())

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
                    onNavigateToAttendance = onNavigateToAttendance,
                    onNavigateToNotes = onNavigateToNotes,
                    onNavigateToGames = onNavigateToGames,
                    onNavigateToChildDetails = onNavigateToChildDetails,
                    onNavigateToEventDetails = onNavigateToEventDetails,
                    onNavigateToAnalytics = onNavigateToAnalytics

                )
            }
        }
    }
}

@Composable
fun rememberHomeScreenTopBarState(): HomeScreenTopBarState {
    return remember { HomeScreenTopBarState() }
}

class HomeScreenTopBarState {
    var isExpanded by mutableStateOf(true)
}

