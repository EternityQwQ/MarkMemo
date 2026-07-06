package com.mdnote.app.data.repository

import com.mdnote.app.data.db.NoteDao
import com.mdnote.app.data.model.Note
import com.mdnote.app.data.model.NoteCategory
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NoteRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noteDao: NoteDao
    private lateinit var repository: NoteRepository

    private val sampleNotes = listOf(
        Note(
            id = 1,
            title = "测试笔记1",
            content = "内容1",
            category = NoteCategory.WORK.name,
            createdAt = 1000,
            updatedAt = 2000,
            isPinned = true
        ),
        Note(
            id = 2,
            title = "测试笔记2",
            content = "内容2",
            category = NoteCategory.PERSONAL.name,
            createdAt = 3000,
            updatedAt = 4000,
            isPinned = false
        ),
        Note(
            id = 3,
            title = "另一个笔记",
            content = "测试内容",
            category = NoteCategory.STUDY.name,
            createdAt = 5000,
            updatedAt = 6000,
            isPinned = false
        )
    )

    @Before
    fun setUp() {
        noteDao = mockk()
        repository = NoteRepository(noteDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getAllNotes with UPDATED_DESC returns notes from DAO`() = runTest {
        val flow = flowOf(sampleNotes)
        every { noteDao.getAllNotes() } returns flow

        val result = repository.getAllNotes(SortOrder.UPDATED_DESC).first()

        assertEquals(3, result.size)
        verify(exactly = 1) { noteDao.getAllNotes() }
    }

    @Test
    fun `getAllNotes with CREATED_DESC returns notes from DAO`() = runTest {
        val flow = flowOf(sampleNotes)
        every { noteDao.getAllNotesByCreatedAt() } returns flow

        val result = repository.getAllNotes(SortOrder.CREATED_DESC).first()

        assertEquals(3, result.size)
        verify(exactly = 1) { noteDao.getAllNotesByCreatedAt() }
    }

    @Test
    fun `getAllNotes with TITLE_ASC returns notes from DAO`() = runTest {
        val flow = flowOf(sampleNotes)
        every { noteDao.getAllNotesByTitle() } returns flow

        val result = repository.getAllNotes(SortOrder.TITLE_ASC).first()

        assertEquals(3, result.size)
        verify(exactly = 1) { noteDao.getAllNotesByTitle() }
    }

    @Test
    fun `searchNotes returns filtered results`() = runTest {
        val flow = flowOf(listOf(sampleNotes[0]))
        every { noteDao.searchNotes("测试") } returns flow

        val result = repository.searchNotes("测试").first()

        assertEquals(1, result.size)
        assertEquals("测试笔记1", result[0].title)
        verify(exactly = 1) { noteDao.searchNotes("测试") }
    }

    @Test
    fun `searchNotes with empty query returns all notes`() = runTest {
        val flow = flowOf(sampleNotes)
        every { noteDao.searchNotes("") } returns flow

        val result = repository.searchNotes("").first()

        assertEquals(3, result.size)
        verify(exactly = 1) { noteDao.searchNotes("") }
    }

    @Test
    fun `getNotesByCategory returns filtered results`() = runTest {
        val flow = flowOf(listOf(sampleNotes[0]))
        every { noteDao.getNotesByCategory(NoteCategory.WORK.name) } returns flow

        val result = repository.getNotesByCategory(NoteCategory.WORK.name).first()

        assertEquals(1, result.size)
        assertEquals(NoteCategory.WORK.name, result[0].category)
        verify(exactly = 1) { noteDao.getNotesByCategory(NoteCategory.WORK.name) }
    }

    @Test
    fun `getNoteById returns correct note`() = runTest {
        coEvery { noteDao.getNoteById(1) } returns sampleNotes[0]

        val result = repository.getNoteById(1)

        assertNotNull(result)
        assertEquals(1, result?.id)
        assertEquals("测试笔记1", result?.title)
        coVerify(exactly = 1) { noteDao.getNoteById(1) }
    }

    @Test
    fun `getNoteById returns null for non-existent note`() = runTest {
        coEvery { noteDao.getNoteById(999) } returns null

        val result = repository.getNoteById(999)

        assertNull(result)
        coVerify(exactly = 1) { noteDao.getNoteById(999) }
    }

    @Test
    fun `insertNote delegates to DAO and returns id`() = runTest {
        val newNote = Note(
            title = "新笔记",
            content = "新内容",
            category = NoteCategory.OTHER.name
        )
        coEvery { noteDao.insertNote(newNote) } returns 10

        val result = repository.insertNote(newNote)

        assertEquals(10, result)
        coVerify(exactly = 1) { noteDao.insertNote(newNote) }
    }

    @Test
    fun `updateNote updates timestamp and delegates to DAO`() = runTest {
        val note = sampleNotes[0]
        coEvery { noteDao.updateNote(any()) } just Runs

        repository.updateNote(note)

        coVerify(exactly = 1) {
            noteDao.updateNote(match { it.id == note.id && it.updatedAt != note.updatedAt })
        }
    }

    @Test
    fun `deleteNote delegates to DAO`() = runTest {
        val note = sampleNotes[0]
        coEvery { noteDao.deleteNote(note) } just Runs

        repository.deleteNote(note)

        coVerify(exactly = 1) { noteDao.deleteNote(note) }
    }

    @Test
    fun `deleteNoteById delegates to DAO`() = runTest {
        coEvery { noteDao.deleteNoteById(1) } just Runs

        repository.deleteNoteById(1)

        coVerify(exactly = 1) { noteDao.deleteNoteById(1) }
    }

    @Test
    fun `getNoteCount returns count from DAO`() = runTest {
        coEvery { noteDao.getNoteCount() } returns 3

        val result = repository.getNoteCount()

        assertEquals(3, result)
        coVerify(exactly = 1) { noteDao.getNoteCount() }
    }
}