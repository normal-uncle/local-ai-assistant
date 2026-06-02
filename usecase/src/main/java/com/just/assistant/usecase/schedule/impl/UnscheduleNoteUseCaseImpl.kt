package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.usecase.schedule.di.UnscheduleNoteUseCase
import javax.inject.Inject

class UnscheduleNoteUseCaseImpl
    @Inject
    constructor(
        private val noteRepository: NoteRepository,
        private val scheduledItemRepository: ScheduledItemRepository,
    ) : UnscheduleNoteUseCase {
        override suspend fun invoke(noteId: Long) {
            val note = noteRepository.findById(noteId) ?: return
            note.calendarEventId?.let { scheduledItemRepository.cancelEvent(it) }
            note.alarmRequestId?.let { scheduledItemRepository.cancelReminder(it) }
            noteRepository.save(
                note.copy(calendarEventId = null, alarmRequestId = null),
            )
        }
    }
