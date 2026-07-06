package com.mdnote.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mdnote.app.data.model.Note
import com.mdnote.app.data.repository.NoteRepository
import com.mdnote.app.data.repository.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteListViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.UPDATED_DESC)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = combine(
        _sortOrder, _searchQuery, _selectedCategory
    ) { sortOrder, query, category ->
        Triple(sortOrder, query, category)
    }.flatMapLatest { (sortOrder, query, category) ->
        when {
            query.isNotEmpty() -> repository.searchNotes(query)
            category != null -> repository.getNotesByCategory(category)
            else -> repository.getAllNotes(sortOrder)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
                return NoteListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}