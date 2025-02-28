package com.example.vozhatapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Цветовая схема для светлой темы
private val LightColorScheme = lightColorScheme(
    primary = AppColors.primary,
    onPrimary = AppColors.onPrimary,
    primaryContainer = AppColors.primaryContainer,
    onPrimaryContainer = AppColors.onPrimaryContainer,
    secondary = AppColors.secondary,
    onSecondary = AppColors.onSecondary,
    secondaryContainer = AppColors.secondaryContainer,
    onSecondaryContainer = AppColors.onSecondaryContainer,
    tertiary = AppColors.tertiary,
    onTertiary = AppColors.onTertiary,
    tertiaryContainer = AppColors.tertiaryContainer,
    onTertiaryContainer = AppColors.onTertiaryContainer,
    error = AppColors.error,
    errorContainer = AppColors.errorContainer,
    onError = AppColors.onError,
    onErrorContainer = AppColors.onErrorContainer,
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = AppColors.neutralContainer,
    onSurfaceVariant = AppColors.onNeutralContainer,
    outline = AppColors.neutralVariant,
    outlineVariant = AppColors.neutralVariantContainer
)

// Цветовая схема для темной темы
private val DarkColorScheme = darkColorScheme(
    primary = DarkAppColors.primary,
    onPrimary = DarkAppColors.onPrimary,
    primaryContainer = DarkAppColors.primaryContainer,
    onPrimaryContainer = DarkAppColors.onPrimaryContainer,
    secondary = DarkAppColors.secondary,
    onSecondary = DarkAppColors.onSecondary,
    secondaryContainer = DarkAppColors.secondaryContainer,
    onSecondaryContainer = DarkAppColors.onSecondaryContainer,
    tertiary = DarkAppColors.tertiary,
    onTertiary = DarkAppColors.onTertiary,
    tertiaryContainer = DarkAppColors.tertiaryContainer,
    onTertiaryContainer = DarkAppColors.onTertiaryContainer,
    error = DarkAppColors.error,
    errorContainer = DarkAppColors.errorContainer,
    onError = DarkAppColors.onError,
    onErrorContainer = DarkAppColors.onErrorContainer,
    background = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE4E2E6),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE4E2E6),
    surfaceVariant = DarkAppColors.neutralContainer,
    onSurfaceVariant = DarkAppColors.onNeutralContainer,
    outline = DarkAppColors.neutralVariant,
    outlineVariant = DarkAppColors.neutralVariantContainer
)

// Дополнительные цвета для приложения вожатого
data class AppExtendedColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val present: Color,
    val onPresent: Color,
    val absent: Color,
    val onAbsent: Color,
    val badgeGold: Color,
    val badgeSilver: Color,
    val badgeBronze: Color,
    val squad1: Color,
    val squad2: Color,
    val squad3: Color,
    val squad4: Color,
    val squad5: Color
)

// Локальный объект для предоставления дополнительных цветов
val LocalAppExtendedColors = staticCompositionLocalOf {
    AppExtendedColors(
        success = Color.Unspecified,
        warning = Color.Unspecified,
        info = Color.Unspecified,
        present = Color.Unspecified,
        onPresent = Color.Unspecified,
        absent = Color.Unspecified,
        onAbsent = Color.Unspecified,
        badgeGold = Color.Unspecified,
        badgeSilver = Color.Unspecified,
        badgeBronze = Color.Unspecified,
        squad1 = Color.Unspecified,
        squad2 = Color.Unspecified,
        squad3 = Color.Unspecified,
        squad4 = Color.Unspecified,
        squad5 = Color.Unspecified
    )
}

@Composable
fun VozhatAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Динамические цвета доступны на Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Дополнительные цвета для приложения
    val appExtendedColors = AppExtendedColors(
        success = ExtendedColors.success,
        warning = ExtendedColors.warning,
        info = ExtendedColors.info,
        present = ExtendedColors.present,
        onPresent = ExtendedColors.onPresent,
        absent = ExtendedColors.absent,
        onAbsent = ExtendedColors.onAbsent,
        badgeGold = ExtendedColors.gold,
        badgeSilver = ExtendedColors.silver,
        badgeBronze = ExtendedColors.bronze,
        squad1 = ExtendedColors.squad1,
        squad2 = ExtendedColors.squad2,
        squad3 = ExtendedColors.squad3,
        squad4 = ExtendedColors.squad4,
        squad5 = ExtendedColors.squad5
    )

    // Расширенные формы
    val extendedShapes = ExtendedShapes()

    // Размеры
    val dimensions = Dimensions()

    // Настройка цвета статус-бара
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalAppExtendedColors provides appExtendedColors,
        LocalExtendedShapes provides extendedShapes,
        LocalDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

// Объект для доступа к расширенным цветам и другим элементам темы
object VozhatAppTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val extendedColors: AppExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppExtendedColors.current

    val extendedShapes: ExtendedShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedShapes.current

    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current
}