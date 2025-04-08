package com.example.vozhatapp.presentation.notes.edit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditTopAppBar(
    isEditing: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditing) "Редактировать заметку" else "Новая заметка",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            IconButton(
                onClick = onSave,
                enabled = isSaveEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Сохранить"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}