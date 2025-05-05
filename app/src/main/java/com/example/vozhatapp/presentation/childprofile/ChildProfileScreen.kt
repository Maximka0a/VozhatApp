package com.example.vozhatapp.presentation.childprofile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vozhatapp.presentation.childprofile.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChildProfileScreen(
    childId: Long,
    onNavigateBack: () -> Unit,
    onEditChild: (Long) -> Unit,
    onAddAchievement: (Long) -> Unit,
    onAddNote: (Long) -> Unit,
    onNavigateToAchievementDetail: (Long) -> Unit,
    viewModel: ChildProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize child data
    LaunchedEffect(childId) {
        viewModel.loadChildData(childId)
    }

    // Tab selection state
    val pagerState = rememberPagerState { 4 } // 4 tabs
    val tabTitles = listOf(
        "Достижения", "Посещаемость", "Заметки", "Медицинская информация"
    )

    LaunchedEffect(pagerState.currentPage) {
        // Load data specific to the selected tab if needed
        when (pagerState.currentPage) {
            0 -> viewModel.loadAchievements(childId)
            1 -> viewModel.loadAttendance(childId)
            2 -> viewModel.loadNotes(childId)
            // Medical info is already part of the child data
        }
    }

    // Error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Show loading state or content
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.child == null -> {
                EmptyStateView(
                    contentType = EmptyContentType.CHILD_NOT_FOUND
                )
            }
            else -> {
                val child = uiState.child!!

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        ProfileTopAppBar(
                            childName = child.fullName,
                            scrollBehavior = scrollBehavior,
                            onNavigateBack = onNavigateBack,
                            onEditChild = { onEditChild(childId) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        ProfileActionsFab(
                            onAddAchievement = { onAddAchievement(childId) },
                            onAddNote = { onAddNote(childId) }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        // Profile header with statistics
                        ProfileHeaderStats(child = child)

                        // Tab layout
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            modifier = Modifier.fillMaxWidth(),
                            edgePadding = 0.dp
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                ProfileTabItem(
                                    title = title,
                                    index = index,
                                    isSelected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )
                            }
                        }

                        // Tab content
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                when (page) {
                                    0 -> AchievementsTab(
                                        achievements = uiState.achievements,
                                        onAchievementClick = viewModel::showAchievementDetails,
                                        onNavigateToAchievementDetail = onNavigateToAchievementDetail // Используем новый параметр
                                    )
                                    1 -> AttendanceTab(
                                        attendance = uiState.attendance,
                                        attendanceRate = uiState.attendanceRate
                                    )
                                    2 -> NotesTab(
                                        notes = uiState.notes,
                                        onNoteClick = viewModel::showNoteDetails,
                                        onDeleteNote = viewModel::deleteNote
                                    )
                                    3 -> MedicalInfoTab(
                                        medicalInfo = child.medicalNotes
                                    )
                                }
                            }
                        }
                    }
                }

                // Dialogs
                if (uiState.selectedAchievement != null) {
                    AchievementDetailsDialog(
                        achievement = uiState.selectedAchievement!!,
                        onDismiss = viewModel::dismissAchievementDetails
                    )
                }

                if (uiState.selectedNote != null) {
                    NoteDetailsDialog(
                        note = uiState.selectedNote!!,
                        onDismiss = viewModel::dismissNoteDetails,
                        onDelete = {
                            viewModel.deleteNote(uiState.selectedNote!!)
                            viewModel.dismissNoteDetails()
                        }
                    )
                }
            }
        }
    }
}