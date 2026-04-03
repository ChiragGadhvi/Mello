package com.chirag.mello.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [JournalEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rename moodEmoji to mood by recreating the table
                db.execSQL("CREATE TABLE journal_entries_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, text TEXT NOT NULL, mood TEXT NOT NULL, timestamp INTEGER NOT NULL)")
                db.execSQL("INSERT INTO journal_entries_new (id, text, mood, timestamp) SELECT id, text, moodEmoji, timestamp FROM journal_entries")
                db.execSQL("DROP TABLE journal_entries")
                db.execSQL("ALTER TABLE journal_entries_new RENAME TO journal_entries")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mello_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
