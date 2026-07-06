package com.mdnote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mdnote.app.data.db.NoteDatabase
import com.mdnote.app.data.repository.NoteRepository
import com.mdnote.app.ui.screens.*
import com.mdnote.app.viewmodel.NoteEditViewModel
import com.mdnote.app.viewmodel.NoteListViewModel
import com.mdnote.app.viewmodel.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    val context = navController.context
    val database = NoteDatabase.getInstance(context)
    val repository = NoteRepository(database.noteDao())

    val noteListViewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModel.Factory(repository)
    )

    val noteEditViewModel: NoteEditViewModel = viewModel(
        factory = NoteEditViewModel.Factory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        // Note List
        composable(Screen.NoteList.route) {
            NoteListScreen(
                viewModel = noteListViewModel,
                onCreateNote = {
                    navController.navigate(Screen.NoteEdit.createRoute(-1))
                },
                onEditNote = { id ->
                    navController.navigate(Screen.NoteEdit.createRoute(id))
                },
                onPreviewNote = { id ->
                    navController.navigate(Screen.NotePreview.createRoute(id))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Note Edit
        composable(
            route = Screen.NoteEdit.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            NoteEditScreen(
                viewModel = noteEditViewModel,
                noteId = if (noteId == -1L) null else noteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPreview = {
                    // Navigate to preview after saving
                    val currentNoteId = noteId
                    if (currentNoteId != null && currentNoteId > 0) {
                        navController.navigate(Screen.NotePreview.createRoute(currentNoteId)) {
                            popUpTo(Screen.NoteEdit.createRoute(-1)) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Note Preview
        composable(
            route = Screen.NotePreview.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            NotePreviewScreen(
                viewModel = noteEditViewModel,
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEdit = { id ->
                    navController.navigate(Screen.NoteEdit.createRoute(id))
                },
                onDelete = { id ->
                    noteListViewModel.deleteNoteById(id)
                    navController.popBackStack(Screen.NoteList.route, inclusive = false)
                }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        // About
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}