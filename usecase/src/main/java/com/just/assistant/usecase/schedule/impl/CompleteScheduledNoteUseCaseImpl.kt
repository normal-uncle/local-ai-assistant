package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.usecase.schedule.di.CompleteScheduledNoteUseCase
import com.just.assistant.usecase.schedule.di.UnscheduleNoteUseCase
import javax.inject.Inject

class CompleteScheduledNoteUseCaseImpl
    @Inject
    constructor(
        private val noteRepository: NoteRepository,
        private val unschedule: UnscheduleNoteUseCase,
    ) : CompleteScheduledNoteUseCase {
        override suspend fun invoke(noteId: Long) {
            unschedule(noteId)
            val note = noteRepository.findById(noteId) ?: return
            if (note.isCompleted) return
            noteRepository.save(note.copy(isCompleted = true))
        }
    }
