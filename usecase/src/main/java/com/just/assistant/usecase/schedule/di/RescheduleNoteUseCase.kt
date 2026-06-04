package com.just.assistant.usecase.schedule.di

import java.time.Instant

interface RescheduleNoteUseCase {
    suspend operator fun invoke(
        noteId: Long,
        newDateTime: Instant,
    )
}
