package com.just.assistant.local.di

import android.content.Context
import androidx.room.Room
import com.just.assistant.local.AssistantDatabase
import com.just.assistant.local.note.NoteDao
import com.just.assistant.local.pref.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext ctx: Context,
    ): AssistantDatabase = Room.databaseBuilder(ctx, AssistantDatabase::class.java, "assistant.db").build()

    @Provides
    fun provideNoteDao(db: AssistantDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext ctx: Context,
    ): UserPreferences = UserPreferences(ctx)
}
