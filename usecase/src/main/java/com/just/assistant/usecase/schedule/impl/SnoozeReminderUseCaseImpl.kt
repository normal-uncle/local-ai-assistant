package com.just.assistant.usecase.schedule.impl

import com.just.assistant.local.alarm.AlarmScheduler
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.usecase.schedule.di.SnoozeReminderUseCase
import javax.inject.Inject

class SnoozeReminderUseCaseImpl
    @Inject
    constructor(
        private val noteRepository: NoteRepository,
        private val alarmScheduler: AlarmScheduler,
    ) : SnoozeReminderUseCase {
        override suspend fun invoke(
            noteId: Long,
            delayMs: Long,
        ) {
            val note = noteRepository.findById(noteId) ?: return
            val requestId = note.alarmRequestId ?: return
            alarmScheduler.schedule(
                requestId = requestId,
                whenEpochMs = System.currentTimeMillis() + delayMs,
                title = note.title,
                body = note.body,
                noteId = noteId,
            )
        }
    }
