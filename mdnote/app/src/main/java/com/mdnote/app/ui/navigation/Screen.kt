package com.mdnote.app.ui.navigation

sealed class Screen(val route: String) {
    data object NoteList : Screen("note_list")
    data object NoteEdit : Screen("note_edit/{noteId}") {
        fun createRoute(noteId: Long = -1) = "note_edit/$noteId"
    }
    data object NotePreview : Screen("note_preview/{noteId}") {
        fun createRoute(noteId: Long) = "note_preview/$noteId"
    }
    data object Settings : Screen("settings")
}