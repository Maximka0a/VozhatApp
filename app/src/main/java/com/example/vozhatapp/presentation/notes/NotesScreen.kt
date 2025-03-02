package com.example.vozhatapp.presentation.notes

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Note
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToNoteDetail: (Long) -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToChildDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle snackbar messages
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text("Заметки и напоминания")
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
                    // Search Icon
                    IconButton(onClick = { viewModel.toggleSearchBar() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    }

                    // Filter Icon
                    IconButton(onClick = { viewModel.toggleFilterDialog() }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Фильтры"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateNote,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Создать"
                    )
                },
                text = { Text("Новая заметка") },
                expanded = !state.isScrolling
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar (collapsible)
                AnimatedVisibility(
                    visible = state.isSearchBarVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Поиск заметок...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Очистить"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NoteCategoryTabs.entries.forEachIndexed { index, category ->
                        Tab(
                            selected = state.selectedTabIndex == index,
                            onClick = { viewModel.selectTab(index) },
                            text = {
                                Text(
                                    text = category.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Reminders section (visible only in All and Reminders tabs)
                if ((state.selectedTabIndex == 0 || state.selectedTabIndex == 2) &&
                    state.todayReminders.isNotEmpty() && state.searchQuery.isEmpty()) {
                    TodayRemindersSection(
                        reminders = state.todayReminders,
                        onReminderClick = onNavigateToNoteDetail,
                        onCompleteReminder = { viewModel.completeReminder(it) }
                    )
                }

                // Main content
                NotesContent(
                    state = state,
                    onNoteClick = onNavigateToNoteDetail,
                    onDeleteNote = { viewModel.deleteNote(it) },
                    onChildClick = onNavigateToChildDetail,
                    onScroll = { isScrolling -> viewModel.setScrolling(isScrolling) }
                )
            }

            // Show loading indicator
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            // Filter dialog
            if (state.showFilterDialog) {
                NotesFilterDialog(
                    currentOrder = state.sortOrder,
                    onOrderSelected = { viewModel.updateSortOrder(it) },
                    showRemindersOnly = state.showRemindersOnly,
                    onRemindersOnlyChanged = { viewModel.toggleShowRemindersOnly() },
                    onDismiss = { viewModel.toggleFilterDialog() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesContent(
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
        when (NoteCategoryTabs.entries[state.selectedTabIndex]) {
            NoteCategoryTabs.ALL -> true
            NoteCategoryTabs.GENERAL -> note.childId == null
            NoteCategoryTabs.REMINDERS -> note.type == 1
            NoteCategoryTabs.CHILD_NOTES -> note.childId != null
        }
    }.filter {
        if (state.searchQuery.isBlank()) true
        else it.title.contains(state.searchQuery, ignoreCase = true) ||
                it.content.contains(state.searchQuery, ignoreCase = true)
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
        NotesSortOrder.DATE_DESC -> filteredNotes.groupBy {
            val date = Date(it.createdAt)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }
        NotesSortOrder.DATE_ASC -> filteredNotes.groupBy {
            val date = Date(it.createdAt)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }
        NotesSortOrder.TITLE -> mapOf("" to filteredNotes)
        NotesSortOrder.TYPE -> filteredNotes.groupBy {
            if (it.type == 0) "Заметки" else "Напоминания"
        }
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
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ) {
                        Text(
                            text = when (state.sortOrder) {
                                NotesSortOrder.DATE_DESC, NotesSortOrder.DATE_ASC -> {
                                    formatDateHeader(dateHeader)
                                }
                                NotesSortOrder.TYPE -> dateHeader
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Notes for this group
            val sortedNotes = when (state.sortOrder) {
                NotesSortOrder.DATE_DESC -> notes.sortedByDescending { it.createdAt }
                NotesSortOrder.DATE_ASC -> notes.sortedBy { it.createdAt }
                NotesSortOrder.TITLE -> notes.sortedBy { it.title }
                NotesSortOrder.TYPE -> notes // Already grouped by type
            }

            items(sortedNotes, key = { it.id }) { note ->
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
@Composable
private fun NoteItem(
    note: Note,
    childName: String?,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onChildClick: (() -> Unit)? = null
) {
    val isReminder = note.type == 1
    val formattedDate = remember(note.createdAt) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            .format(Date(note.createdAt))
    }

    val formattedReminderDate = remember(note.reminderDate) {
        note.reminderDate?.let {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(it))
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isReminder)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        // Card content remains the same
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Rest of your card content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Note type icon
                Icon(
                    imageVector = if (isReminder) Icons.Default.Alarm else Icons.Default.Notes,
                    contentDescription = if (isReminder) "Напоминание" else "Заметка",
                    tint = if (isReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Child tag if available
            if (childName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .clickable(onClick = onChildClick ?: {})
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = childName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Reminder date if applicable
            if (formattedReminderDate != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = formattedReminderDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Created date
                Text(
                    text = "Создано: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayRemindersSection(
    reminders: List<Note>,
    onReminderClick: (Long) -> Unit,
    onCompleteReminder: (Note) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Сегодняшние напоминания",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                reminders.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onCompleteReminder(reminder) }
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onReminderClick(reminder.id) }
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = reminder.content,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        // Time badge
                        val reminderTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(reminder.reminderDate ?: 0))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = reminderTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    if (reminder != reminders.last()) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun EmptyNotesPlaceholder(
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

@Composable
private fun NotesFilterDialog(
    currentOrder: NotesSortOrder,
    onOrderSelected: (NotesSortOrder) -> Unit,
    showRemindersOnly: Boolean,
    onRemindersOnlyChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Сортировка и фильтры",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Sort options
                Column {
                    Text(
                        text = "Сортировка",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NotesSortOrder.entries.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOrderSelected(order) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentOrder == order,
                                onClick = { onOrderSelected(order) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = order.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Divider()

                // Filter options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRemindersOnlyChanged() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Только напоминания",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = showRemindersOnly,
                        onCheckedChange = { onRemindersOnlyChanged() }
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}

// Helper function to format date headers
private fun formatDateHeader(dateStr: String): String {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
        Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    )

    return when (dateStr) {
        today -> "Сегодня"
        yesterday -> "Вчера"
        else -> {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date ?: Date())
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}

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