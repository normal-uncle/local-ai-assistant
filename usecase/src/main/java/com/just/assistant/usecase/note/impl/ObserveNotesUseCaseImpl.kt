package com.just.assistant.usecase.note.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.ObserveNotesUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotesUseCaseImpl
    @Inject
    constructor(
        private val repository: NoteRepository,
    ) : ObserveNotesUseCase {
        override fun invoke(): Flow<List<Note>> = repository.observeAll()
    }
