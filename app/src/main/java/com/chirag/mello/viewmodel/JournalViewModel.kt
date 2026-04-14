package com.chirag.mello.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.chirag.mello.data.AppDatabase
import com.chirag.mello.data.JournalEntry
import com.chirag.mello.data.StreakCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).journalDao()

    val entries = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak = dao.getAllEntries()
        .map { list ->
            StreakCalculator.calculateStreak(list.map { it.timestamp })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalEntries: kotlinx.coroutines.flow.StateFlow<Int> = entries
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val moodCounts: kotlinx.coroutines.flow.StateFlow<Map<String, Int>> = entries
        .map { list -> list.groupBy { it.mood }.mapValues { it.value.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val firstEntryDate: kotlinx.coroutines.flow.StateFlow<Long?> = entries
        .map { list -> list.minByOrNull { it.timestamp }?.timestamp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val entriesByDay: kotlinx.coroutines.flow.StateFlow<Map<Long, List<JournalEntry>>> = entries
        .map { list -> list.groupBy { normalizeToMidnight(it.timestamp) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val heatmapData: kotlinx.coroutines.flow.StateFlow<Map<Long, String>> = entries
        .map { list ->
            list
                .groupBy { entry -> normalizeToMidnight(entry.timestamp) }
                .mapValues { (_, dayEntries) ->
                    dayEntries.maxByOrNull { it.timestamp }!!.mood
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private fun normalizeToMidnight(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun saveEntry(text: String, mood: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            dao.insertEntry(JournalEntry(text = text.trim(), mood = mood))
        }
    }

    fun updateEntry(entry: JournalEntry) {
        if (entry.text.isBlank()) return
        viewModelScope.launch {
            dao.updateEntry(entry.copy(text = entry.text.trim()))
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }

    fun clearAllEntries() {
        viewModelScope.launch {
            dao.deleteAllEntries()
        }
    }
}
