package com.example.vozhatapp.presentation.notes.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Note
import com.example.vozhatapp.presentation.notes.NoteCategoryTabs
import com.example.vozhatapp.presentation.notes.NotesUiState
import com.example.vozhatapp.presentation.notes.NotesSortOrder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesContent(
    state: NotesUiState,
    onNoteClick: (Long) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onChildClick: (Long) -> Unit,
    onScroll: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()

    // Observe scroll state to control FAB
    LaunchedEffect(listState.isScrollInProgress) {
        onScroll(listState.isScrollInProgress)
    }

    if (state.isLoading) {
        return
    }

    val filteredNotes = state.notes.filter { note ->
        // Фильтр по типу
        val categoryMatch = when (NoteCategoryTabs.entries[state.selectedTabIndex]) {
            NoteCategoryTabs.ALL -> true
            NoteCategoryTabs.GENERAL -> note.childId == null
            NoteCategoryTabs.REMINDERS -> note.type == 1
            NoteCategoryTabs.CHILD_NOTES -> note.childId != null
        }

        // Фильтр по тексту
        val searchMatch = if (state.searchQuery.isBlank()) true
        else note.title.contains(state.searchQuery, ignoreCase = true) ||
                note.content.contains(state.searchQuery, ignoreCase = true)

        // Фильтр "только напоминания"
        val reminderMatch = if (state.showRemindersOnly) note.type == 1 else true

        categoryMatch && searchMatch && reminderMatch
    }

    if (filteredNotes.isEmpty()) {
        EmptyNotesPlaceholder(
            selectedTab = NoteCategoryTabs.entries[state.selectedTabIndex],
            isSearchActive = state.searchQuery.isNotEmpty()
        )
        return
    }

    // Group notes by date if needed
    val groupedNotes = when (state.sortOrder) {
        NotesSortOrder.DATE_DESC -> filteredNotes
            .sortedByDescending { it.createdAt }
            .groupBy { formatDateForGrouping(it.createdAt) }
        NotesSortOrder.DATE_ASC -> filteredNotes
            .sortedBy { it.createdAt }
            .groupBy { formatDateForGrouping(it.createdAt) }
        NotesSortOrder.TITLE -> mapOf("" to filteredNotes.sortedBy { it.title })
        NotesSortOrder.TYPE -> filteredNotes
            .groupBy { if (it.type == 0) "Заметки" else "Напоминания" }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp) // Extra padding for FAB
    ) {
        groupedNotes.forEach { (dateHeader, notes) ->
            // Date header if required
            if (dateHeader.isNotEmpty() &&
                (state.sortOrder == NotesSortOrder.DATE_DESC ||
                        state.sortOrder == NotesSortOrder.DATE_ASC ||
                        state.sortOrder == NotesSortOrder.TYPE)) {
                stickyHeader(key = "header_$dateHeader") {
                    DateHeader(dateHeader = dateHeader)
                }
            }

            // Notes for this group
            items(
                items = notes,
                key = { it.id } // Важно: используем уникальный ключ для стабильных анимаций и правильных удалений
            ) { note ->
                NoteItem(
                    note = note,
                    childName = state.childrenMap[note.childId]?.fullName,
                    onClick = { onNoteClick(note.id) },
                    onDeleteClick = { onDeleteNote(note) },
                    onChildClick = note.childId?.let { id -> { onChildClick(id) } }
                )
            }
        }
    }
}

private fun formatDateForGrouping(timestamp: Long): String {
    return try {
        val date = java.util.Date(timestamp)
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
    } catch (e: Exception) {
        ""
    }
}