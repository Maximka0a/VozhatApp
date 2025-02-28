package com.example.vozhatapp.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// Основные формы для Material 3
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// Расширенные формы для приложения вожатого
data class ExtendedShapes(
    val card: CornerBasedShape = RoundedCornerShape(12.dp),
    val button: CornerBasedShape = RoundedCornerShape(24.dp),
    val dialog: CornerBasedShape = RoundedCornerShape(24.dp),
    val childItem: CornerBasedShape = RoundedCornerShape(12.dp),
    val profilePhoto: CornerBasedShape = RoundedCornerShape(percent = 50),
    val badge: CornerBasedShape = RoundedCornerShape(percent = 50),
    val bottomSheet: CornerBasedShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
)

val LocalExtendedShapes = staticCompositionLocalOf { ExtendedShapes() }

// Функция для доступа к расширенным формам
object AppShapes {
    val extended: ExtendedShapes
        @Composable
        get() = LocalExtendedShapes.current
}