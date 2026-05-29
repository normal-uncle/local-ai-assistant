package com.just.assistant.repository

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.repository.impl.ModelRepositoryImpl
import com.just.assistant.repository.impl.NoteRepositoryImpl
import com.just.assistant.repository.impl.ScheduledItemRepositoryImpl
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

    @Singleton
    @Binds
    abstract fun bindScheduledItemRepository(impl: ScheduledItemRepositoryImpl): ScheduledItemRepository

    companion object {
        @Provides
        fun provideClock(): () -> Instant = { Instant.now() }
    }
}
