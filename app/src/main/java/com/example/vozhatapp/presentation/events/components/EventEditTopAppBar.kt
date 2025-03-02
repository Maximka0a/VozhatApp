package com.example.vozhatapp.presentation.events.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isValid: Boolean,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        title = {
            Text(title)
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
                onClick = onSave,
                enabled = isValid
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Сохранить",
                    tint = if (isValid)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}