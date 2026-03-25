package com.chirag.mello.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val moodEmoji: String,
    val timestamp: Long = System.currentTimeMillis()
)
