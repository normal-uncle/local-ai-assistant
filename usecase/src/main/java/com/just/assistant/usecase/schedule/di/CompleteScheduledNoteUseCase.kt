package com.just.assistant.usecase.schedule.di

interface CompleteScheduledNoteUseCase {
    suspend operator fun invoke(noteId: Long)
}
