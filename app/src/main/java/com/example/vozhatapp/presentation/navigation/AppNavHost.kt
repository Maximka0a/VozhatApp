package com.example.vozhatapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.vozhatapp.presentation.analytics.AnalyticsScreen
import com.example.vozhatapp.presentation.attendance.AttendanceReportsScreen
import com.example.vozhatapp.presentation.attendance.AttendanceScreen
import com.example.vozhatapp.presentation.attendance.ChildAttendanceScreen
import com.example.vozhatapp.presentation.childprofile.ChildProfileScreen
import com.example.vozhatapp.presentation.children.ChildrenListScreen
import com.example.vozhatapp.presentation.children.edit.AddChildScreen
import com.example.vozhatapp.presentation.events.Detail.EventDetailScreen
import com.example.vozhatapp.presentation.events.Edit.EventEditScreen
import com.example.vozhatapp.presentation.events.List.EventsScreen
import com.example.vozhatapp.presentation.games.GameDetailScreen
import com.example.vozhatapp.presentation.games.GameEditScreen
import com.example.vozhatapp.presentation.games.GamesScreen
import com.example.vozhatapp.presentation.home.HomeScreen
import com.example.vozhatapp.presentation.notes.NoteDetailScreen
import com.example.vozhatapp.presentation.notes.edit.NoteEditScreen
import com.example.vozhatapp.presentation.notes.NotesScreen

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Home) {

        composable<Home> {
            HomeScreen(
                onNavigateToEvents = { navController.navigate(Events) },
                onNavigateToChildren = { navController.navigate(Children) },
                onNavigateToAttendance = { navController.navigate(Attendance) },
                onNavigateToNotes = { navController.navigate(Notes) },
                onNavigateToGames = { navController.navigate(Games) },
                onNavigateToChildDetails = { childId ->
                    navController.navigate(ChildDetail(childId))
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(EventDetail(eventId))
                },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                // Новые параметры
                onNavigateToSettings = { navController.navigate("settings") },
                onCreateNewEvent = { navController.navigate(EventEdit()) },
                onNavigateToNoteDetails = { noteId ->
                    navController.navigate(NoteDetail(noteId))
                },
            )
        }

        // ===== EVENTS NAVIGATION =====
        composable<Events> {
            EventsScreen(
                onEventClick = { eventId ->
                    navController.navigate(EventDetail(eventId))
                },
                onCreateEvent = {
                    navController.navigate(EventEdit())
                }
            )
        }

        composable<EventDetail> { backStackEntry ->
            val eventData = backStackEntry.toRoute<EventDetail>()
            EventDetailScreen(
                eventId = eventData.eventId,
                onNavigateBack = { navController.popBackStack() },
                onEditEvent = { navController.navigate(EventEdit(it)) }
            )
        }

        composable<EventEdit> { backStackEntry ->
            val eventData = backStackEntry.toRoute<EventEdit>()
            EventEditScreen(
                eventId = if (eventData.eventId == -1L) null else eventData.eventId,
                onNavigateBack = { navController.popBackStack() },
                onEventSaved = { savedEventId: Long ->
                    navController.navigate(EventDetail(savedEventId)) {
                        popUpTo(Events) {}
                    }
                }
            )
        }

        // ===== CHILDREN NAVIGATION =====
        composable<Children> {
            ChildrenListScreen(
                onAddChildClick = { navController.navigate(AddChild()) },
                onChildClick = { childId -> navController.navigate(ChildDetail(childId)) }
            )
        }

        composable<ChildDetail> { backStackEntry ->
            val childData = backStackEntry.toRoute<ChildDetail>()
            ChildProfileScreen(
                childId = childData.childId,
                onNavigateBack = { navController.popBackStack() },
                onEditChild = { childId -> navController.navigate(AddChild(childId)) },
                onAddAchievement = { childId -> navController.navigate(AddAchievement(childId)) },
                onAddNote = { childId -> navController.navigate(AddNote(childId)) }
            )
        }

        composable<AddChild> { backStackEntry ->
            val childData = backStackEntry.toRoute<AddChild>()
            AddChildScreen(
                childId = if (childData.childId == -1L) null else childData.childId,
                onNavigateBack = { navController.popBackStack() },
                onChildAdded = { childId ->
                    navController.navigate(ChildDetail(childId)) {
                        popUpTo(Children) {}
                    }
                }
            )
        }

        // ===== ATTENDANCE NAVIGATION =====
        composable<Attendance> {
            AttendanceScreen(
                onNavigateBack = { navController.popBackStack() },
                onEventSelect = { eventId ->
                    if (eventId > 0) {
                        navController.navigate(EventDetail(eventId))
                    } else {
                        navController.navigate(EventEdit())
                    }
                },
                onChildDetails = { childId ->
                    navController.navigate(ChildAttendance(childId))
                },
                onViewReports = { navController.navigate(AttendanceReports) }
            )
        }

        composable<AttendanceReports> {
            AttendanceReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ChildAttendance> { backStackEntry ->
            val childData = backStackEntry.toRoute<ChildAttendance>()
            ChildAttendanceScreen(
                childId = childData.childId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== NOTES NAVIGATION =====
        composable<Notes> {
            NotesScreen(
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(NoteDetail(noteId))
                },
                onNavigateToCreateNote = {
                    navController.navigate(NoteEdit())
                },
                onNavigateToChildDetail = { childId ->
                    navController.navigate(ChildDetail(childId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<NoteDetail> { backStackEntry ->
            val noteData = backStackEntry.toRoute<NoteDetail>()
            NoteDetailScreen(
                noteId = noteData.noteId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(NoteEdit(id)) },
                onNavigateToChild = { childId -> navController.navigate(ChildDetail(childId)) }
            )
        }

        composable<NoteEdit> { backStackEntry ->
            val noteData = backStackEntry.toRoute<NoteEdit>()
            NoteEditScreen(
                noteId = noteData.noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AddNote> { backStackEntry ->
            val noteData = backStackEntry.toRoute<AddNote>()
            // Reusing NoteEditScreen with childId parameter (assuming your NoteEditViewModel can handle this)
            NoteEditScreen(
                noteId = -1L,
                onNavigateBack = { navController.popBackStack() }
            )
        }

// ===== ANALYTICS NAVIGATION =====
        composable("analytics") {
            AnalyticsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEventDetail = { eventId ->
                    navController.navigate(EventDetail(eventId))
                },
                onNavigateToChildDetail = { childId ->
                    navController.navigate(ChildDetail(childId))
                }
            )
        }
        // ===== GAMES NAVIGATION =====
        composable<Games> {
            GamesScreen(
                onGameClick = { gameId -> navController.navigate(GameDetail(gameId)) },
                onAddGameClick = { navController.navigate(GameEdit()) }
            )
        }

        composable<GameDetail> { backStackEntry ->
            val gameData = backStackEntry.toRoute<GameDetail>()
            GameDetailScreen(
                gameId = gameData.gameId,
                onNavigateBack = { navController.popBackStack() },
                onEditGame = { gameId -> navController.navigate(GameEdit(gameId)) }
            )
        }

        composable<GameEdit> { backStackEntry ->
            val gameData = backStackEntry.toRoute<GameEdit>()
            GameEditScreen(
                gameId = if (gameData.gameId == -1L) null else gameData.gameId,
                onNavigateBack = { navController.popBackStack() },
                onGameSaved = { savedGameId ->
                    navController.navigate(GameDetail(savedGameId)) {
                        popUpTo(Games) {}
                    }
                }
            )
        }
    }
}