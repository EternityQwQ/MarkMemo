package com.mdnote.app.viewmodel

import com.mdnote.app.data.model.Note
import com.mdnote.app.data.model.NoteCategory
import com.mdnote.app.data.repository.NoteRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: NoteRepository
    private lateinit var viewModel: NoteEditViewModel

    private val sampleNote = Note(
        id = 1,
        title = "测试笔记",
        content = "测试内容",
        category = NoteCategory.WORK.name,
        createdAt = 1000,
        updatedAt = 2000,
        isPinned = false
    )

    @Before
    fun setUp() {
        repository = mockk()
        coEvery { repository.getNoteById(1) } returns sampleNote
        coEvery { repository.getNoteById(999) } returns null
        coEvery { repository.insertNote(any()) } returns 10
        coEvery { repository.updateNote(any()) } just Runs

        viewModel = NoteEditViewModel(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createNewNote resets all fields`() = runTest {
        viewModel.setTitle("旧标题")
        viewModel.setContent("旧内容")
        viewModel.setCategory(NoteCategory.STUDY.name)

        viewModel.createNewNote()

        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.content.value)
        assertEquals("OTHER", viewModel.category.value)
        assertFalse(viewModel.isSaved.value)
    }

    @Test
    fun `loadNote loads existing note data`() = runTest {
        viewModel.loadNote(1)

        advanceUntilIdle()

        assertEquals("测试笔记", viewModel.title.value)
        assertEquals("测试内容", viewModel.content.value)
        assertEquals(NoteCategory.WORK.name, viewModel.category.value)
        coVerify(exactly = 1) { repository.getNoteById(1) }
    }

    @Test
    fun `loadNote with non-existent id clears fields`() = runTest {
        viewModel.setTitle("旧标题")
        viewModel.loadNote(999)

        advanceUntilIdle()

        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.content.value)
        assertEquals("OTHER", viewModel.category.value)
        coVerify(exactly = 1) { repository.getNoteById(999) }
    }

    @Test
    fun `setTitle updates title state`() = runTest {
        viewModel.setTitle("新标题")
        assertEquals("新标题", viewModel.title.value)
    }

    @Test
    fun `setContent updates content state`() = runTest {
        viewModel.setContent("新内容")
        assertEquals("新内容", viewModel.content.value)
    }

    @Test
    fun `setCategory updates category state`() = runTest {
        viewModel.setCategory(NoteCategory.PERSONAL.name)
        assertEquals(NoteCategory.PERSONAL.name, viewModel.category.value)
    }

    @Test
    fun `saveNote creates new note when no current id`() = runTest {
        viewModel.createNewNote()
        viewModel.setTitle("新笔记")
        viewModel.setContent("新内容")
        viewModel.setCategory(NoteCategory.STUDY.name)

        viewModel.saveNote()

        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insertNote(match { note ->
                note.title == "新笔记" &&
                    note.content == "新内容" &&
                    note.category == NoteCategory.STUDY.name
            })
        }
        assertTrue(viewModel.isSaved.value)
    }

    @Test
    fun `saveNote updates existing note when current id exists`() = runTest {
        viewModel.loadNote(1)
        advanceUntilIdle()
        viewModel.setTitle("修改标题")
        viewModel.setContent("修改内容")

        viewModel.saveNote()

        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateNote(match { note ->
                note.id == 1L &&
                    note.title == "修改标题" &&
                    note.content == "修改内容"
            })
        }
        assertTrue(viewModel.isSaved.value)
    }

    @Test
    fun `getCurrentNote returns note from repository`() = runTest {
        viewModel.loadNote(1)
        advanceUntilIdle()

        val note = viewModel.getCurrentNote()
        assertNotNull(note)
        assertEquals(1, note?.id)
        assertEquals("测试笔记", note?.title)
    }

    @Test
    fun `getCurrentNote returns null when no note loaded`() = runTest {
        viewModel.createNewNote()

        val note = viewModel.getCurrentNote()
        assertNull(note)
    }

    @Test
    fun `setExportedUri updates export state`() = runTest {
        val uri = mockk<android.net.Uri>()
        viewModel.setExportedUri(uri, "markdown")

        assertEquals(uri, viewModel.exportedUri.value)
        assertEquals("markdown", viewModel.exportType.value)
    }

    @Test
    fun `clearExportState resets export state`() = runTest {
        val uri = mockk<android.net.Uri>()
        viewModel.setExportedUri(uri, "pdf")

        viewModel.clearExportState()

        assertNull(viewModel.exportedUri.value)
        assertNull(viewModel.exportType.value)
    }
}