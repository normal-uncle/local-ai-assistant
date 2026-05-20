package com.just.assistant.usecase.note.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import javax.inject.Inject

class SaveNoteUseCaseImpl
    @Inject
    constructor(
        private val repository: NoteRepository,
    ) : SaveNoteUseCase {
        override suspend fun invoke(note: Note): Long {
            require(note.title.isNotBlank()) { "title must not be blank" }
            return repository.save(note)
        }
    }
