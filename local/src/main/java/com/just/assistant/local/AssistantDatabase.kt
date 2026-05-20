package com.just.assistant.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.just.assistant.local.note.NoteDao
import com.just.assistant.local.note.NoteEntity

@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
abstract class AssistantDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
