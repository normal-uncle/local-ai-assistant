package com.just.assistant.usecase

import com.just.assistant.usecase.note.di.FindNoteByIdUseCase
import com.just.assistant.usecase.note.di.ObserveNotesUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.assistant.usecase.note.impl.FindNoteByIdUseCaseImpl
import com.just.assistant.usecase.note.impl.ObserveNotesUseCaseImpl
import com.just.assistant.usecase.note.impl.SaveNoteUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindSaveNoteUseCase(impl: SaveNoteUseCaseImpl): SaveNoteUseCase

    @Binds
    abstract fun bindObserveNotesUseCase(impl: ObserveNotesUseCaseImpl): ObserveNotesUseCase

    @Binds
    abstract fun bindFindNoteByIdUseCase(impl: FindNoteByIdUseCaseImpl): FindNoteByIdUseCase
}
