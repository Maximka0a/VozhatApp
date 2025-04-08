package com.example.vozhatapp.presentation.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NoteCategoryTabs(val title: String, val icon: ImageVector) {
    ALL("Все", Icons.Outlined.Notes),
    GENERAL("Общие", Icons.Outlined.Description),
    REMINDERS("Напоминания", Icons.Outlined.Alarm),
    CHILD_NOTES("Дети", Icons.Outlined.People)
}

enum class NotesSortOrder(val displayName: String) {
    DATE_DESC("Сначала новые"),
    DATE_ASC("Сначала старые"),
    TITLE("По алфавиту"),
    TYPE("По типу")
}