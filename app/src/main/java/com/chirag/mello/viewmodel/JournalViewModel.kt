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

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).journalDao()

    val entries = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak = dao.getAllEntries()
        .map { list ->
            StreakCalculator.calculateStreak(list.map { it.timestamp })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun saveEntry(text: String, moodEmoji: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            dao.insertEntry(JournalEntry(text = text.trim(), moodEmoji = moodEmoji))
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
}
