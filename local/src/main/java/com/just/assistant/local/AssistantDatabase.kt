package com.just.assistant.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.just.assistant.local.note.NoteDao
import com.just.assistant.local.note.NoteEntity

@Database(entities = [NoteEntity::class], version = 2, exportSchema = true)
abstract class AssistantDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2: Migration =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE notes ADD COLUMN calendarEventId INTEGER")
                    db.execSQL("ALTER TABLE notes ADD COLUMN alarmRequestId INTEGER")
                }
            }
    }
}
