package com.just.assistant.usecase.note.di

import com.just.assistant.repository.model.Note
import kotlinx.coroutines.flow.Flow

interface ObserveNotesUseCase {
    operator fun invoke(): Flow<List<Note>>
}
