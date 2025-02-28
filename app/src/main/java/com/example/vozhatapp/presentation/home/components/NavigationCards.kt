package com.example.vozhatapp.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.home.model.NavigationItem
import com.example.vozhatapp.ui.theme.VozhatAppTheme


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
