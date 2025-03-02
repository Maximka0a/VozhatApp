@file:OptIn(ExperimentalFoundationApi::class)

package com.example.vozhatapp.presentation.attendance

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.R
import com.example.vozhatapp.data.local.entity.Event
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onNavigateBack: () -> Unit,
    onEventSelect: (Long) -> Unit,
    onChildDetails: (Long) -> Unit,
    onViewReports: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle errors and messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text("Посещаемость")
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
                    // Event selection dropdown
                    Box {
                        IconButton(onClick = { viewModel.toggleEventDropdown() }) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Выбрать событие"
                            )
                        }

                        // Badge showing number of present attendees if an event is selected
                        if (uiState.selectedEvent != null) {
                            val presentCount = uiState.attendanceRecords.count { it.isPresent }
                            val totalCount = uiState.attendanceRecords.size

                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text("$presentCount/$totalCount")
                            }
                        }
                    }

                    // Filter options
                    IconButton(onClick = { viewModel.toggleFilterDialog() }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Фильтры"
                        )
                    }

                    // Menu options
                    IconButton(onClick = { viewModel.toggleMenuDropdown() }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Меню"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.selectedEvent != null) {
                FloatingActionButton(
                    onClick = { viewModel.saveAttendance() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Сохранить посещаемость"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Main content based on state
            when {
                uiState.isLoading -> LoadingState()
                uiState.selectedEvent == null -> NoEventSelectedState(
                    onSelectEvent = { viewModel.toggleEventDropdown() }
                )
                uiState.attendanceRecords.isEmpty() -> EmptyAttendanceState(
                    eventName = uiState.selectedEvent!!.title,
                    onAddChildren = { viewModel.toggleAddChildrenDialog() }
                )
                else -> AttendanceContent(
                    attendanceState = uiState,
                    onChildClick = { childId -> viewModel.toggleAttendance(childId) },
                    onChildLongClick = { childId -> onChildDetails(childId) },
                    onAddNote = { childId -> viewModel.toggleNoteDialog(childId) },
                    onSquadFilter = { squad -> viewModel.updateSquadFilter(squad) }
                )
            }

            // Dropdowns and dialogs
            if (uiState.showEventDropdown) {
                EventSelectionDropdown(
                    events = uiState.availableEvents,
                    onEventSelect = { event ->
                        viewModel.selectEvent(event)
                        viewModel.toggleEventDropdown()
                    },
                    onDismiss = { viewModel.toggleEventDropdown() },
                    onCreateEvent = { onEventSelect(0) }
                )
            }

// In AttendanceScreen:
            if (uiState.showMenuDropdown) {
                MenuDropdown(
                    onSelectAll = { viewModel.markAllPresent(true) },
                    onDeselectAll = { viewModel.markAllPresent(false) },
                    onExportData = { viewModel.exportAttendanceData() },
                    onViewReports = onViewReports,
                    onDismiss = { viewModel.toggleMenuDropdown() }
                )
            }

            if (uiState.showFilterDialog) {
                FilterDialog(
                    squadList = uiState.availableSquads,
                    selectedSquad = uiState.squadFilter,
                    showAbsentOnly = uiState.showAbsentOnly,
                    onSquadSelect = { viewModel.updateSquadFilter(it) },
                    onShowAbsentToggle = { viewModel.toggleShowAbsentOnly() },
                    onDismiss = { viewModel.toggleFilterDialog() }
                )
            }

            if (uiState.showNoteDialog) {
                NoteDialog(
                    childName = uiState.selectedChildForNote?.child?.fullName ?: "",
                    initialNote = uiState.selectedChildForNote?.note ?: "",
                    onSaveNote = { note -> viewModel.saveNote(note) },
                    onDismiss = { viewModel.toggleNoteDialog(null) }
                )
            }

            if (uiState.showAddChildrenDialog) {
                AddChildrenDialog(
                    availableChildren = uiState.availableChildrenToAdd,
                    onAddChildren = { selectedChildren -> viewModel.addChildrenToEvent(selectedChildren) },
                    onDismiss = { viewModel.toggleAddChildrenDialog() }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoEventSelectedState(onSelectEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.EventBusy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Событие не выбрано",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Выберите событие для отметки посещаемости",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSelectEvent,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выбрать событие")
        }
    }
}

@Composable
private fun EmptyAttendanceState(eventName: String, onAddChildren: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Groups,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Нет данных о посещаемости",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Событие: $eventName",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Добавьте детей для отметки посещаемости",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddChildren,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Добавить детей")
        }
    }
}

@Composable
private fun AttendanceContent(
    attendanceState: AttendanceUiState,
    onChildClick: (Long) -> Unit,
    onChildLongClick: (Long) -> Unit,
    onAddNote: (Long) -> Unit,
    onSquadFilter: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Event info header
        attendanceState.selectedEvent?.let { event ->
            EventInfoHeader(event, attendanceState.attendanceRecords.size)
        }

        // Show squad filter chips if there are multiple squads
        if (attendanceState.availableSquads.size > 1) {
            SquadFilterChips(
                squads = attendanceState.availableSquads,
                selectedSquad = attendanceState.squadFilter,
                onSquadSelected = onSquadFilter
            )
        }

        // Statistics summary
        AttendanceStatsSummary(attendanceState.attendanceRecords)

        // Main attendance list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Filter the records based on selected squad and absent-only filter
            val filteredRecords = attendanceState.attendanceRecords.filter {
                (attendanceState.squadFilter == null || it.child.squadName == attendanceState.squadFilter) &&
                        (!attendanceState.showAbsentOnly || !it.isPresent)
            }

            items(filteredRecords) { record ->
                AttendanceItem(
                    childName = record.child.fullName,
                    childSquad = record.child.squadName,
                    isPresent = record.isPresent,
                    hasNote = record.note != null,
                    photoUrl = record.child.photoUrl,
                    onClick = { onChildClick(record.child.id) }, // Regular click for toggling attendance
                    onLongClick = { onChildLongClick(record.child.id) }, // Long press for child details
                    onAddNoteClick = { onAddNote(record.child.id) }
                )
            }

            // Add some bottom padding for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun EventInfoHeader(event: Event, attendeeCount: Int) {
    val startDateTime = Instant
        .ofEpochMilli(event.startTime)
        .atZone(ZoneId.systemDefault())

    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = startDateTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = startDateTime.format(timeFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "$attendeeCount участников",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (event.location != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SquadFilterChips(
    squads: List<String>,
    selectedSquad: String?,
    onSquadSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedSquad == null,
                onClick = { onSquadSelected(null) },
                label = { Text("Все") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )
        }

        // Squad chips
        items(squads) { squad ->
            FilterChip(
                selected = selectedSquad == squad,
                onClick = { onSquadSelected(squad) },
                label = { Text(squad) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Group,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )
        }
    }
}

@Composable
private fun AttendanceStatsSummary(attendanceRecords: List<AttendanceRecord>) {
    val totalCount = attendanceRecords.size
    val presentCount = attendanceRecords.count { it.isPresent }
    val absentCount = totalCount - presentCount
    val presentPercentage = if (totalCount > 0) (presentCount * 100 / totalCount) else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatisticItem(
            value = presentCount.toString(),
            label = "Присутствуют",
            color = MaterialTheme.colorScheme.primary
        )

        StatisticItem(
            value = absentCount.toString(),
            label = "Отсутствуют",
            color = MaterialTheme.colorScheme.error
        )

        StatisticItem(
            value = "$presentPercentage%",
            label = "Посещаемость",
            color = MaterialTheme.colorScheme.tertiary
        )
    }

    // Divider below stats
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun StatisticItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AttendanceItem(
    childName: String,
    childSquad: String,
    isPresent: Boolean,
    hasNote: Boolean,
    photoUrl: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAddNoteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPresent)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Child photo or placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = childName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Presence indicator
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            color = if (isPresent)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (isPresent) "Присутствует" else "Отсутствует",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Child info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = childName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = childSquad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Note indicator and button
            IconButton(onClick = onAddNoteClick) {
                Icon(
                    imageVector = if (hasNote) Icons.Filled.Comment else Icons.Outlined.Comment,
                    contentDescription = "Добавить заметку",
                    tint = if (hasNote)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EventSelectionDropdown(
    events: List<Event>,
    onEventSelect: (Event) -> Unit,
    onDismiss: () -> Unit,
    onCreateEvent: () -> Unit
) {
    // Use Box with offset to position the dropdown properly
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopEnd)
            .padding(top = 56.dp, end = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .heightIn(max = 400.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Выберите событие",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                HorizontalDivider()

                // Safe handling for empty events list
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (events.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EventBusy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Нет доступных событий",
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn {
                            items(events) { event ->
                                // Safely handle date formatting
                                val startDateTime = try {
                                    Instant
                                        .ofEpochMilli(event.startTime)
                                        .atZone(ZoneId.systemDefault())
                                } catch (e: Exception) {
                                    ZonedDateTime.now()
                                }

                                val dateFormatter = DateTimeFormatter.ofPattern("d MMM")

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEventSelect(event) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Event,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(end = 16.dp)
                                        )

                                        Column {
                                            Text(
                                                text = event.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = try {
                                                    startDateTime.format(dateFormatter)
                                                } catch (e: Exception) {
                                                    "Дата недоступна"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCreateEvent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                        )

                        Text(
                            text = "Создать новое событие",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Add cancel button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDismiss)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Отмена",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun MenuDropdown(
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onExportData: () -> Unit,
    onViewReports: () -> Unit, // Add this parameter
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Отметить всех") },
            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            onClick = {
                onSelectAll()
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = { Text("Снять отметки") },
            leadingIcon = { Icon(Icons.Default.RemoveCircle, contentDescription = null) },
            onClick = {
                onDeselectAll()
                onDismiss()
            }
        )

        HorizontalDivider()

        DropdownMenuItem(
            text = { Text("Экспорт данных") },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            onClick = {
                onExportData()
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Просмотреть отчеты") },
            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            onClick = {
                onViewReports()
                onDismiss()
            }
        )
    }
}

@Composable
private fun FilterDialog(
    squadList: List<String>,
    selectedSquad: String?,
    showAbsentOnly: Boolean,
    onSquadSelect: (String?) -> Unit,
    onShowAbsentToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Фильтры")
        },
        text = {
            Column {
                Text(
                    text = "Отряд",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Squad selection
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All" chip
                    item {
                        FilterChip(
                            selected = selectedSquad == null,
                            onClick = { onSquadSelect(null) },
                            label = { Text("Все") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }

                    // Squad chips
                    items(squadList) { squad ->
                        FilterChip(
                            selected = selectedSquad == squad,
                            onClick = { onSquadSelect(squad) },
                            label = { Text(squad) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show absent only toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Показывать только отсутствующих",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Switch(
                        checked = showAbsentOnly,
                        onCheckedChange = { onShowAbsentToggle() }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                // Reset filters to default
                onSquadSelect(null)
                if (showAbsentOnly) onShowAbsentToggle()
                onDismiss()
            }) {
                Text("Сбросить")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDialog(
    childName: String,
    initialNote: String,
    onSaveNote: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Заметка о посещаемости")
        },
        text = {
            Column {
                Text(
                    text = childName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Заметка") },
                    placeholder = { Text("Напишите причину отсутствия или комментарий...") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSaveNote(note)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun AddChildrenDialog(
    availableChildren: List<ChildSelectionItem>,
    onAddChildren: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedChildIds = remember { mutableStateListOf<Long>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить детей в событие")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search field
                var searchQuery by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск детей...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Children list with checkboxes
                val filteredChildren = availableChildren.filter {
                    searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                }

                if (filteredChildren.isEmpty()) {
                    Text(
                        text = "Нет доступных детей для добавления",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(filteredChildren) { child ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedChildIds.contains(child.id)) {
                                            selectedChildIds.remove(child.id)
                                        } else {
                                            selectedChildIds.add(child.id)
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedChildIds.contains(child.id),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedChildIds.add(child.id)
                                        } else {
                                            selectedChildIds.remove(child.id)
                                        }
                                    }
                                )

                                Column(
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = child.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = child.squad,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAddChildren(selectedChildIds)
                    onDismiss()
                },
                enabled = selectedChildIds.isNotEmpty()
            ) {
                Text("Добавить (${selectedChildIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}