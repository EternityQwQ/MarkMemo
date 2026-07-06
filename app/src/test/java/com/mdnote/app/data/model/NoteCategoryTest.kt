package com.mdnote.app.data.model

import org.junit.Assert.*
import org.junit.Test

class NoteCategoryTest {

    @Test
    fun `fromDisplayName returns correct category`() {
        assertEquals(NoteCategory.WORK, NoteCategory.fromDisplayName("工作"))
        assertEquals(NoteCategory.PERSONAL, NoteCategory.fromDisplayName("个人"))
        assertEquals(NoteCategory.STUDY, NoteCategory.fromDisplayName("学习"))
        assertEquals(NoteCategory.OTHER, NoteCategory.fromDisplayName("其他"))
    }

    @Test
    fun `fromDisplayName returns OTHER for unknown name`() {
        assertEquals(NoteCategory.OTHER, NoteCategory.fromDisplayName("未知"))
        assertEquals(NoteCategory.OTHER, NoteCategory.fromDisplayName(""))
    }

    @Test
    fun `all categories have display names`() {
        NoteCategory.entries.forEach { category ->
            assertNotNull(category.displayName)
            assertTrue(category.displayName.isNotEmpty())
        }
    }

    @Test
    fun `category names are unique`() {
        val names = NoteCategory.entries.map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `display names are unique`() {
        val displayNames = NoteCategory.entries.map { it.displayName }
        assertEquals(displayNames.size, displayNames.distinct().size)
    }
}