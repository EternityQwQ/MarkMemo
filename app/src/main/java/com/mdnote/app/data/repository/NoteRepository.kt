package com.mdnote.app.data.repository

import com.mdnote.app.data.db.NoteDao
import com.mdnote.app.data.model.Note
import kotlinx.coroutines.flow.Flow

enum class SortOrder {
    UPDATED_DESC,
    CREATED_DESC,
    TITLE_ASC
}

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(sortOrder: SortOrder = SortOrder.UPDATED_DESC): Flow<List<Note>> {
        return when (sortOrder) {
            SortOrder.UPDATED_DESC -> noteDao.getAllNotes()
            SortOrder.CREATED_DESC -> noteDao.getAllNotesByCreatedAt()
            SortOrder.TITLE_ASC -> noteDao.getAllNotesByTitle()
        }
    }

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }

    fun getNotesByCategory(category: String): Flow<List<Note>> {
        return noteDao.getNotesByCategory(category)
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun getNoteCount(): Int {
        return noteDao.getNoteCount()
    }
}