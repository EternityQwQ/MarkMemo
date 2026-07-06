package com.mdnote.app.data.repository

import org.junit.Assert.*
import org.junit.Test

class SortOrderTest {

    @Test
    fun `SortOrder has three values`() {
        assertEquals(3, SortOrder.entries.size)
    }

    @Test
    fun `SortOrder values are UPDATED_DESC, CREATED_DESC, TITLE_ASC`() {
        assertNotNull(SortOrder.valueOf("UPDATED_DESC"))
        assertNotNull(SortOrder.valueOf("CREATED_DESC"))
        assertNotNull(SortOrder.valueOf("TITLE_ASC"))
    }
}