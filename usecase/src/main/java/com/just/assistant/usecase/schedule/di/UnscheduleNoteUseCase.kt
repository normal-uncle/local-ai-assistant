package com.just.assistant.usecase.schedule.di

interface UnscheduleNoteUseCase {
    suspend operator fun invoke(noteId: Long)
}
