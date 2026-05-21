package com.just.assistant.repository.di

import com.just.assistant.repository.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>

    suspend fun findById(id: Long): Note?

    suspend fun save(note: Note): Long

    /**
     * Deletes the note with [id].
     * @return true if a row was removed; false if no row matched. Throws on I/O failure.
     */
    suspend fun delete(id: Long): Boolean
}
