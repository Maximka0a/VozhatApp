package com.example.vozhatapp.presentation.notes.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.presentation.notes.NoteCategoryTabs

@Composable
fun EmptyNotesPlaceholder(
    selectedTab: NoteCategoryTabs,
    isSearchActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (icon, text) = when {
            isSearchActive -> Icons.Outlined.SearchOff to "По вашему запросу ничего не найдено"
            else -> when (selectedTab) {
                NoteCategoryTabs.ALL ->
                    Icons.Outlined.Notes to "У вас пока нет заметок или напоминаний"
                NoteCategoryTabs.GENERAL ->
                    Icons.Outlined.StickyNote2 to "У вас пока нет общих заметок"
                NoteCategoryTabs.REMINDERS ->
                    Icons.Outlined.Notifications to "У вас пока нет напоминаний"
                NoteCategoryTabs.CHILD_NOTES ->
                    Icons.Outlined.People to "У вас пока нет заметок о детях"
            }
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        if (!isSearchActive) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Нажмите '+' чтобы создать новую запись",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}