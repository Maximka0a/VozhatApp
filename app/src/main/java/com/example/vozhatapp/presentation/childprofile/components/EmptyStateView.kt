package com.example.vozhatapp.presentation.childprofile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class EmptyContentType {
    CHILD_NOT_FOUND,
    NO_ACHIEVEMENTS,
    NO_ATTENDANCE,
    NO_NOTES,
    NO_MEDICAL_INFO
}

@Composable
fun EmptyStateView(
    contentType: EmptyContentType,
    modifier: Modifier = Modifier
) {
    val (icon, title, message) = when (contentType) {
        EmptyContentType.CHILD_NOT_FOUND -> Triple(
            Icons.Outlined.PersonOff,
            "Ребенок не найден",
            "Не удалось найти информацию об этом ребенке."
        )
        EmptyContentType.NO_ACHIEVEMENTS -> Triple(
            Icons.Outlined.EmojiEvents,
            "Нет достижений",
            "У ребенка пока нет достижений"
        )
        EmptyContentType.NO_ATTENDANCE -> Triple(
            Icons.Outlined.EventAvailable,
            "Нет данных о посещаемости",
            "Информация о посещаемости этого ребенка отсутствует"
        )
        EmptyContentType.NO_NOTES -> Triple(
            Icons.Outlined.Notes,
            "Нет заметок",
            "Для этого ребенка пока нет заметок"
        )
        EmptyContentType.NO_MEDICAL_INFO -> Triple(
            Icons.Outlined.HealthAndSafety,
            "Нет медицинской информации",
            "Медицинская информация об этом ребенке отсутствует"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
        )
    }
}