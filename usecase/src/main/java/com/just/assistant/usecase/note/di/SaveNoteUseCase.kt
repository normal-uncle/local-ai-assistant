package com.just.assistant.usecase.note.di

import com.just.assistant.repository.model.Note

interface SaveNoteUseCase {
    suspend operator fun invoke(note: Note): Long
}
