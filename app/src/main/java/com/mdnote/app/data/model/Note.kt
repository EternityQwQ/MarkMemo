package com.mdnote.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 笔记类别
 */
enum class NoteCategory(val displayName: String) {
    WORK("工作"),
    PERSONAL("个人"),
    STUDY("学习"),
    OTHER("其他");

    companion object {
        fun fromDisplayName(name: String): NoteCategory {
            return entries.find { it.displayName == name } ?: OTHER
        }
    }
}

/**
 * 笔记实体
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val category: String = NoteCategory.OTHER.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
) {
    val wordCount: Int
        get() = content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

    val charCount: Int
        get() = content.length

    val preview: String
        get() = content.take(100).replace("\n", " ").trim()
}