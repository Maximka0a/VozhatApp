package com.example.vozhatapp.presentation.events.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    onEditEvent: () -> Unit,
    onDeleteEvent: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    statusColor: Color,
    showActions: Boolean
) {
    LargeTopAppBar(
        title = {
            Text(
                text = title,
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
            if (showActions) {
                IconButton(onClick = onEditEvent) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать"
                    )
                }
                IconButton(onClick = onDeleteEvent) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = statusColor.copy(alpha = 0.15f),
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}