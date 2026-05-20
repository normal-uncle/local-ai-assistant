package com.just.assistant.repository.di

import com.just.assistant.repository.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>

    suspend fun findById(id: Long): Note?

    suspend fun save(note: Note): Long

    suspend fun delete(id: Long): Boolean
}
