package com.just.assistant.usecase.schedule.di

interface SnoozeReminderUseCase {
    suspend operator fun invoke(
        noteId: Long,
        delayMs: Long,
    )
}
