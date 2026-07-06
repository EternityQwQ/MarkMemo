package com.mdnote.app.viewmodel

import com.mdnote.app.data.model.Note
import com.mdnote.app.data.model.NoteCategory
import com.mdnote.app.data.repository.NoteRepository
import com.mdnote.app.data.repository.SortOrder
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: NoteRepository
    private lateinit var viewModel: NoteListViewModel

    private val sampleNotes = listOf(
        Note(
            id = 1,
            title = "笔记A",
            content = "内容A",
            category = NoteCategory.WORK.name,
            createdAt = 1000,
            updatedAt = 2000,
            isPinned = true
        ),
        Note(
            id = 2,
            title = "笔记B",
            content = "内容B",
            category = NoteCategory.PERSONAL.name,
            createdAt = 3000,
            updatedAt = 4000,
            isPinned = false
        )
    )

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.getAllNotes(any()) } returns flowOf(sampleNotes)
        every { repository.searchNotes(any()) } returns flowOf(emptyList())
        every { repository.getNotesByCategory(any()) } returns flowOf(emptyList())
        coEvery { repository.updateNote(any()) } just Runs
        coEvery { repository.deleteNote(any()) } just Runs
        coEvery { repository.deleteNoteById(any()) } just Runs

        viewModel = NoteListViewModel(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initial state has correct defaults`() = runTest {
        assertEquals(SortOrder.UPDATED_DESC, viewModel.sortOrder.value)
        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedCategory.value)
    }

    @Test
    fun `setSortOrder updates sort order`() = runTest {
        viewModel.setSortOrder(SortOrder.CREATED_DESC)
        assertEquals(SortOrder.CREATED_DESC, viewModel.sortOrder.value)

        viewModel.setSortOrder(SortOrder.TITLE_ASC)
        assertEquals(SortOrder.TITLE_ASC, viewModel.sortOrder.value)

        viewModel.setSortOrder(SortOrder.UPDATED_DESC)
        assertEquals(SortOrder.UPDATED_DESC, viewModel.sortOrder.value)
    }

    @Test
    fun `setSearchQuery updates query`() = runTest {
        viewModel.setSearchQuery("测试")
        assertEquals("测试", viewModel.searchQuery.value)
    }

    @Test
    fun `clearSearch resets query to empty`() = runTest {
        viewModel.setSearchQuery("测试")
        viewModel.clearSearch()
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `setCategory updates selected category`() = runTest {
        viewModel.setCategory(NoteCategory.WORK.name)
        assertEquals(NoteCategory.WORK.name, viewModel.selectedCategory.value)

        viewModel.setCategory(null)
        assertNull(viewModel.selectedCategory.value)
    }

    @Test
    fun `togglePin calls repository updateNote with inverted pin`() = runTest {
        val note = sampleNotes[0].copy(isPinned = true)

        viewModel.togglePin(note)

        coVerify(exactly = 1) {
            repository.updateNote(match { it.id == note.id && !it.isPinned })
        }
    }

    @Test
    fun `togglePin unpinned note sets it to pinned`() = runTest {
        val note = sampleNotes[1].copy(isPinned = false)

        viewModel.togglePin(note)

        coVerify(exactly = 1) {
            repository.updateNote(match { it.id == note.id && it.isPinned })
        }
    }

    @Test
    fun `deleteNote calls repository deleteNote`() = runTest {
        val note = sampleNotes[0]

        viewModel.deleteNote(note)

        coVerify(exactly = 1) { repository.deleteNote(note) }
    }

    @Test
    fun `deleteNoteById calls repository deleteNoteById`() = runTest {
        viewModel.deleteNoteById(1)

        coVerify(exactly = 1) { repository.deleteNoteById(1) }
    }

    @Test
    fun `notes flow emits notes from repository`() = runTest {
        val notes = viewModel.notes.first()

        assertEquals(2, notes.size)
        assertEquals("笔记A", notes[0].title)
        assertEquals("笔记B", notes[1].title)
    }
}