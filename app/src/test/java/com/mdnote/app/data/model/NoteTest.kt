package com.mdnote.app.data.model

import org.junit.Assert.*
import org.junit.Test

class NoteTest {

    @Test
    fun `wordCount returns correct count for simple text`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "hello world test",
            category = NoteCategory.OTHER.name
        )

        assertEquals(3, note.wordCount)
    }

    @Test
    fun `wordCount returns 0 for empty content`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "",
            category = NoteCategory.OTHER.name
        )

        assertEquals(0, note.wordCount)
    }

    @Test
    fun `wordCount returns 0 for whitespace only content`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "   \n  \t  ",
            category = NoteCategory.OTHER.name
        )

        assertEquals(0, note.wordCount)
    }

    @Test
    fun `wordCount handles multiline content`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "第一行 内容\n第二行 更多 内容",
            category = NoteCategory.OTHER.name
        )

        assertEquals(5, note.wordCount)
    }

    @Test
    fun `charCount returns correct character count`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "hello world",
            category = NoteCategory.OTHER.name
        )

        assertEquals(11, note.charCount)
    }

    @Test
    fun `charCount returns 0 for empty content`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "",
            category = NoteCategory.OTHER.name
        )

        assertEquals(0, note.charCount)
    }

    @Test
    fun `preview returns first 100 characters`() {
        val longContent = "A".repeat(150)
        val note = Note(
            id = 1,
            title = "测试",
            content = longContent,
            category = NoteCategory.OTHER.name
        )

        assertEquals(100, note.preview.length)
        assertTrue(note.preview.startsWith("A"))
    }

    @Test
    fun `preview replaces newlines with spaces`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "第一行\n第二行\n第三行",
            category = NoteCategory.OTHER.name
        )

        assertEquals("第一行 第二行 第三行", note.preview)
    }

    @Test
    fun `preview returns empty for empty content`() {
        val note = Note(
            id = 1,
            title = "测试",
            content = "",
            category = NoteCategory.OTHER.name
        )

        assertEquals("", note.preview)
    }

    @Test
    fun `note has correct default values`() {
        val note = Note()

        assertEquals(0, note.id)
        assertEquals("", note.title)
        assertEquals("", note.content)
        assertEquals(NoteCategory.OTHER.name, note.category)
        assertFalse(note.isPinned)
        assertTrue(note.createdAt > 0)
        assertTrue(note.updatedAt > 0)
    }
}