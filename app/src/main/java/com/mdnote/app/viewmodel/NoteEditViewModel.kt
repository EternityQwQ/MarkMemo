package com.mdnote.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mdnote.app.data.model.Note
import com.mdnote.app.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteEditViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _category = MutableStateFlow("OTHER")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _exportedUri = MutableStateFlow<android.net.Uri?>(null)
    val exportedUri: StateFlow<android.net.Uri?> = _exportedUri.asStateFlow()

    private val _exportType = MutableStateFlow<String?>(null)
    val exportType: StateFlow<String?> = _exportType.asStateFlow()

    private var currentNoteId: Long? = null

    /**
     * 获取当前笔记的完整实体（用于导出）
     */
    suspend fun getCurrentNote(): Note? {
        return currentNoteId?.let { repository.getNoteById(it) }
    }

    fun setExportedUri(uri: android.net.Uri?, type: String?) {
        _exportedUri.value = uri
        _exportType.value = type
    }

    fun clearExportState() {
        _exportedUri.value = null
        _exportType.value = null
    }

    fun loadNote(noteId: Long) {
        currentNoteId = noteId
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                _title.value = note.title
                _content.value = note.content
                _category.value = note.category
            }
        }
    }

    fun createNewNote() {
        currentNoteId = null
        _title.value = ""
        _content.value = ""
        _category.value = "OTHER"
        _isSaved.value = false
    }

    fun setTitle(value: String) {
        _title.value = value
    }

    fun setContent(value: String) {
        _content.value = value
    }

    fun setCategory(value: String) {
        _category.value = value
    }

    fun saveNote() {
        viewModelScope.launch {
            val note = Note(
                id = currentNoteId ?: 0,
                title = _title.value.trim(),
                content = _content.value.trim(),
                category = _category.value,
                createdAt = if (currentNoteId != null) {
                    repository.getNoteById(currentNoteId!!)?.createdAt ?: System.currentTimeMillis()
                } else {
                    System.currentTimeMillis()
                },
                updatedAt = System.currentTimeMillis()
            )

            if (currentNoteId != null) {
                repository.updateNote(note)
            } else {
                val newId = repository.insertNote(note)
                currentNoteId = newId
            }
            _isSaved.value = true
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteEditViewModel::class.java)) {
                return NoteEditViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}