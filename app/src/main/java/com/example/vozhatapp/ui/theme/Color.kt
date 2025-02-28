package com.example.vozhatapp.ui.theme

import androidx.compose.ui.graphics.Color

// Основная цветовая палитра
object AppColors {
    // Primary (Синий - энергичный, надежный)
    val primary = Color(0xFF3D5CA8) // #3D5CA8
    val onPrimary = Color(0xFFFFFFFF) // #FFFFFF
    val primaryContainer = Color(0xFFD8E2FF) // #D8E2FF
    val onPrimaryContainer = Color(0xFF001945) // #001945

    // Secondary (Оранжевый - теплый, игривый)
    val secondary = Color(0xFFFF8A00) // #FF8A00
    val onSecondary = Color(0xFFFFFFFF) // #FFFFFF
    val secondaryContainer = Color(0xFFFFDEA8) // #FFDEA8
    val onSecondaryContainer = Color(0xFF271900) // #271900

    // Tertiary (Зеленый - свежий, природный)
    val tertiary = Color(0xFF0B8043) // #0B8043
    val onTertiary = Color(0xFFFFFFFF) // #FFFFFF
    val tertiaryContainer = Color(0xFF92F7B6) // #92F7B6
    val onTertiaryContainer = Color(0xFF00210F) // #00210F

    // Error (Красный)
    val error = Color(0xFFBA1A1A) // #BA1A1A
    val onError = Color(0xFFFFFFFF) // #FFFFFF
    val errorContainer = Color(0xFFFFDAD6) // #FFDAD6
    val onErrorContainer = Color(0xFF410002) // #410002

    // Neutral
    val neutral = Color(0xFF5A5D68) // #5A5D68
    val onNeutral = Color(0xFFFFFFFF) // #FFFFFF
    val neutralContainer = Color(0xFFE1E2EC) // #E1E2EC
    val onNeutralContainer = Color(0xFF1B1B1F) // #1B1B1F

    // Neutral Variant
    val neutralVariant = Color(0xFF757780) // #757780
    val onNeutralVariant = Color(0xFFFFFFFF) // #FFFFFF
    val neutralVariantContainer = Color(0xFFC5C6D0) // #C5C6D0
    val onNeutralVariantContainer = Color(0xFF1B1B1F) // #1B1B1F
}

// Темная тема - более темные версии основных цветов
object DarkAppColors {
    // Primary
    val primary = Color(0xFFAEC6FF) // #AEC6FF
    val onPrimary = Color(0xFF002D6E) // #002D6E
    val primaryContainer = Color(0xFF1A438C) // #1A438C
    val onPrimaryContainer = Color(0xFFD8E2FF) // #D8E2FF

    // Secondary
    val secondary = Color(0xFFFFC166) // #FFC166
    val onSecondary = Color(0xFF422C00) // #422C00
    val secondaryContainer = Color(0xFF5F4200) // #5F4200
    val onSecondaryContainer = Color(0xFFFFDEA8) // #FFDEA8

    // Tertiary
    val tertiary = Color(0xFF76DA9C) // #76DA9C
    val onTertiary = Color(0xFF00391E) // #00391E
    val tertiaryContainer = Color(0xFF00522D) // #00522D
    val onTertiaryContainer = Color(0xFF92F7B6) // #92F7B6

    // Error
    val error = Color(0xFFFFB4AB) // #FFB4AB
    val onError = Color(0xFF690005) // #690005
    val errorContainer = Color(0xFF93000A) // #93000A
    val onErrorContainer = Color(0xFFFFDAD6) // #FFDAD6

    // Neutral
    val neutral = Color(0xFFC5C6D0) // #C5C6D0
    val onNeutral = Color(0xFF303134) // #303134
    val neutralContainer = Color(0xFF44474F) // #44474F
    val onNeutralContainer = Color(0xFFE1E2EC) // #E1E2EC

    // Neutral Variant
    val neutralVariant = Color(0xFF8F909A) // #8F909A
    val onNeutralVariant = Color(0xFF303134) // #303134
    val neutralVariantContainer = Color(0xFF44474F) // #44474F
    val onNeutralVariantContainer = Color(0xFFC5C6D0) // #C5C6D0
}

// Расширенные цвета для приложения вожатого
object ExtendedColors {
    // Статусы посещаемости
    val present = Color(0xFF81C784) // #81C784 (Зеленый)
    val absent = Color(0xFFE57373) // #E57373 (Красный)
    val onPresent = Color(0xFF002200) // #002200
    val onAbsent = Color(0xFF330000) // #330000

    // Значки достижений
    val gold = Color(0xFFFFD700) // #FFD700
    val silver = Color(0xFFC0C0C0) // #C0C0C0
    val bronze = Color(0xFFCD7F32) // #CD7F32

    // Цвета для разных отрядов
    val squad1 = Color(0xFF90CAF9) // #90CAF9 (Голубой)
    val squad2 = Color(0xFFFFF176) // #FFF176 (Желтый)
    val squad3 = Color(0xFFAED581) // #AED581 (Салатовый)
    val squad4 = Color(0xFFFF8A65) // #FF8A65 (Оранжевый)
    val squad5 = Color(0xFFF06292) // #F06292 (Розовый)

    // Дополнительные цвета
    val success = Color(0xFF4CAF50) // #4CAF50 (Зеленый)
    val warning = Color(0xFFFFC107) // #FFC107 (Желтый)
    val info = Color(0xFF2196F3) // #2196F3 (Синий)
}