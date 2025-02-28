package com.example.vozhatapp.presentation.home.model

import androidx.compose.ui.graphics.Color

// Вспомогательные классы
data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit
)