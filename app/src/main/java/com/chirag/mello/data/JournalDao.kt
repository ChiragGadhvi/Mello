package com.chirag.mello.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Update
    suspend fun updateEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT timestamp FROM journal_entries ORDER BY timestamp DESC")
    suspend fun getAllTimestamps(): List<Long>
}
