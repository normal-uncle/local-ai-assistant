package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.usecase.schedule.di.UnscheduleNoteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CompleteScheduledNoteUseCaseImplTest {
    private class FakeNoteRepo : NoteRepository {
        var noteById: Note? = null
        var lastSaved: Note? = null
        var saveCallCount = 0

        override suspend fun save(note: Note): Long {
            saveCallCount++
            lastSaved = note
            return note.id
        }

        override suspend fun findById(id: Long): Note? = noteById

        override fun observeAll(): Flow<List<Note>> = flowOf(emptyList())

        override suspend fun delete(id: Long): Boolean = false
    }

    private class FakeUnschedule : UnscheduleNoteUseCase {
        var lastId: Long? = null

        override suspend fun invoke(noteId: Long) {
            lastId = noteId
        }
    }

    private fun note(
        id: Long = 1L,
        isCompleted: Boolean = false,
    ): Note =
        Note(
            id = id,
            title = "t",
            body = "b",
            type = NoteType.REMINDER,
            createdAt = Instant.parse("2026-06-04T00:00:00Z"),
            updatedAt = Instant.parse("2026-06-04T00:00:00Z"),
            isCompleted = isCompleted,
        )

    @Test
    fun complete_unschedules_and_marks_isCompleted_true() =
        runTest {
            val noteRepo = FakeNoteRepo().apply { noteById = note() }
            val unschedule = FakeUnschedule()
            val useCase = CompleteScheduledNoteUseCaseImpl(noteRepo, unschedule)

            useCase(1L)

            assertEquals(1L, unschedule.lastId)
            assertEquals(true, noteRepo.lastSaved?.isCompleted)
        }

    @Test
    fun complete_is_idempotent_when_already_completed() =
        runTest {
            val noteRepo = FakeNoteRepo().apply { noteById = note(isCompleted = true) }
            val unschedule = FakeUnschedule()
            val useCase = CompleteScheduledNoteUseCaseImpl(noteRepo, unschedule)

            useCase(1L)

            assertEquals(1L, unschedule.lastId)
            assertEquals(0, noteRepo.saveCallCount)
        }

    @Test
    fun complete_when_note_not_found_is_noop_for_save() =
        runTest {
            val noteRepo = FakeNoteRepo()
            val unschedule = FakeUnschedule()
            val useCase = CompleteScheduledNoteUseCaseImpl(noteRepo, unschedule)

            useCase(999L)

            assertEquals(999L, unschedule.lastId)
            assertNull(noteRepo.lastSaved)
            assertTrue(noteRepo.saveCallCount == 0)
        }
}
