package com.just.assistant.usecase.note.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.FindNoteByIdUseCase
import javax.inject.Inject

class FindNoteByIdUseCaseImpl
    @Inject
    constructor(
        private val repository: NoteRepository,
    ) : FindNoteByIdUseCase {
        override suspend fun invoke(id: Long): Note? = repository.findById(id)
    }
