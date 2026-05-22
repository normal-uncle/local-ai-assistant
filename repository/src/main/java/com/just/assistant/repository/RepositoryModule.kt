package com.just.assistant.repository

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.impl.ModelRepositoryImpl
import com.just.assistant.repository.impl.NoteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Singleton
    @Binds
    abstract fun bindModelRepository(impl: ModelRepositoryImpl): ModelRepository

    companion object {
        @Provides
        fun provideClock(): () -> Instant = { Instant.now() }
    }
}
