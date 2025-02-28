package com.example.vozhatapp.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Класс с размерами для единообразия дизайна
data class Dimensions(
    val paddingExtraSmall: Dp = 4.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val paddingExtraLarge: Dp = 32.dp,

    val spacingExtraSmall: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingMedium: Dp = 16.dp,
    val spacingLarge: Dp = 24.dp,

    val iconSizeExtraSmall: Dp = 12.dp,
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp,
    val iconSizeExtraLarge: Dp = 40.dp,

    val profilePhotoSizeSmall: Dp = 40.dp,
    val profilePhotoSizeMedium: Dp = 80.dp,
    val profilePhotoSizeLarge: Dp = 120.dp,

    val childListItemHeight: Dp = 72.dp,
    val badgeSize: Dp = 36.dp,

    val buttonHeight: Dp = 48.dp,
    val inputFieldHeight: Dp = 56.dp,

    val cardElevation: Dp = 4.dp,
    val dialogElevation: Dp = 8.dp,

    val bottomNavHeight: Dp = 64.dp,
    val topAppBarHeight: Dp = 56.dp,

    val dividerThickness: Dp = 1.dp
)

val LocalDimensions = compositionLocalOf { Dimensions() }

// Вспомогательная функция для доступа к размерам
object AppDimensions {
    val dimensions: Dimensions
        @Composable
        get() = LocalDimensions.current
}