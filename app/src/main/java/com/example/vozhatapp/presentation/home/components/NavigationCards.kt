package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.home.model.NavigationItem
import com.example.vozhatapp.ui.theme.VozhatAppTheme

@Composable
fun NavigationCards(
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    // Определяем карточки навигации
    val navItems = listOf(
        NavigationItem(
            title = "События",
            icon = Icons.Outlined.Event,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onNavigateToEvents
        ),
        // In NavigationCards.kt, add a new card for Analytics:

        NavigationItem(
            title = "Аналитика",
            icon = Icons.Outlined.BarChart,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onNavigateToAnalytics
        ),
        NavigationItem(
            title = "Заметки",
            icon = Icons.Outlined.NoteAlt,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onNavigateToNotes
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
        items(navItems) { item ->
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