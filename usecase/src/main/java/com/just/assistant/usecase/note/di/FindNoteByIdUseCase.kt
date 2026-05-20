package com.just.assistant.usecase.note.di

import com.just.assistant.repository.model.Note

interface FindNoteByIdUseCase {
    suspend operator fun invoke(id: Long): Note?
}
