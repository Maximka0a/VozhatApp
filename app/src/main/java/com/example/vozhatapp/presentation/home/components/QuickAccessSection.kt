package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.ui.theme.VozhatAppTheme

@Composable
fun QuickAccessSection(
    onNavigateToEvents: () -> Unit,
    onNavigateToChildren: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Быстрый доступ",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Карточки для навигации в виде сетки
        val quickAccessItems = listOf(
            QuickAccessItem(
                icon = Icons.Outlined.Event,
                title = "События",
                color = MaterialTheme.colorScheme.primaryContainer,
                onIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onNavigateToEvents
            ),
            QuickAccessItem(
                icon = Icons.Outlined.People,
                title = "Дети",
                color = MaterialTheme.colorScheme.secondaryContainer,
                onIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onNavigateToChildren
            ),
            QuickAccessItem(
                icon = Icons.Outlined.CheckCircle,
                title = "Посещения",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                onIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onNavigateToAttendance
            ),
            QuickAccessItem(
                icon = Icons.Outlined.NoteAlt,
                title = "Заметки",
                color = MaterialTheme.colorScheme.errorContainer,
                onIconColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = onNavigateToNotes
            ),
            QuickAccessItem(
                icon = Icons.Outlined.EmojiEvents,
                title = "Игры",
                color = VozhatAppTheme.extendedColors.squad2,
                onIconColor = Color.Black.copy(alpha = 0.8f),
                onClick = onNavigateToGames
            ),
            QuickAccessItem(
                icon = Icons.Outlined.BarChart,
                title = "Аналитика",
                color = MaterialTheme.colorScheme.inversePrimary,
                onIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onNavigateToAnalytics
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(238.dp),
            userScrollEnabled = false // Отключаем скролл внутри сетки
        ) {
            items(quickAccessItems) { item ->
                QuickAccessItemCard(item = item)
            }
        }
    }
}

data class QuickAccessItem(
    val icon: ImageVector,
    val title: String,
    val color: Color,
    val onIconColor: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessItemCard(item: QuickAccessItem) {
    Card(
        onClick = item.onClick,
        colors = CardDefaults.cardColors(containerColor = item.color),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.onIconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                color = item.onIconColor
            )
        }
    }
}