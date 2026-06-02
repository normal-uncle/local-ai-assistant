package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class UnscheduleNoteUseCaseImplTest {
    private class FakeNoteRepo : NoteRepository {
        var noteById: Note? = null
        var lastSaved: Note? = null

        override suspend fun save(note: Note): Long {
            lastSaved = note
            return note.id
        }

        override suspend fun findById(id: Long): Note? = noteById

        override fun observeAll(): Flow<List<Note>> = flowOf(emptyList())

        override suspend fun delete(id: Long): Boolean = false
    }

    private class FakeSchedRepo : ScheduledItemRepository {
        var cancelEventCalls = 0
        var cancelReminderCalls = 0
        var lastCanceledEventId: Long? = null
        var lastCanceledRequestId: Int? = null

        override suspend fun scheduleEvent(input: ScheduleEventInput): ScheduledItem.Event? = null

        override suspend fun scheduleReminder(
            input: ScheduleReminderInput,
            requestId: Int,
            noteId: Long,
        ): ScheduledItem.Reminder =
            ScheduledItem.Reminder(
                alarmRequestId = requestId,
                title = input.title,
                whenAt = input.whenAt,
            )

        override suspend fun cancelEvent(calendarEventId: Long): Boolean {
            cancelEventCalls++
            lastCanceledEventId = calendarEventId
            return true
        }

        override suspend fun cancelReminder(alarmRequestId: Int) {
            cancelReminderCalls++
            lastCanceledRequestId = alarmRequestId
        }
    }

    private fun note(
        id: Long = 1L,
        calendarEventId: Long? = null,
        alarmRequestId: Int? = null,
    ): Note =
        Note(
            id = id,
            title = "t",
            body = "b",
            type = NoteType.EVENT,
            createdAt = Instant.parse("2026-06-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-06-01T00:00:00Z"),
            calendarEventId = calendarEventId,
            alarmRequestId = alarmRequestId,
        )

    @Test
    fun unschedule_EVENT_only_cancels_event_and_clears_calendarEventId() =
        runTest {
            val noteRepo = FakeNoteRepo().apply { noteById = note(calendarEventId = 42L) }
            val schedRepo = FakeSchedRepo()
            val useCase = UnscheduleNoteUseCaseImpl(noteRepo, schedRepo)

            useCase(1L)

            assertEquals(1, schedRepo.cancelEventCalls)
            assertEquals(42L, schedRepo.lastCanceledEventId)
            assertEquals(0, schedRepo.cancelReminderCalls)
            assertNull(noteRepo.lastSaved?.calendarEventId)
            assertNull(noteRepo.lastSaved?.alarmRequestId)
        }

    @Test
    fun unschedule_REMINDER_only_cancels_reminder_and_clears_alarmRequestId() =
        runTest {
            val noteRepo = FakeNoteRepo().apply { noteById = note(alarmRequestId = 7) }
            val schedRepo = FakeSchedRepo()
            val useCase = UnscheduleNoteUseCaseImpl(noteRepo, schedRepo)

            useCase(1L)

            assertEquals(0, schedRepo.cancelEventCalls)
            assertEquals(1, schedRepo.cancelReminderCalls)
            assertEquals(7, schedRepo.lastCanceledRequestId)
            assertNull(noteRepo.lastSaved?.alarmRequestId)
            assertNull(noteRepo.lastSaved?.calendarEventId)
        }

    @Test
    fun unschedule_both_cancels_both_and_clears_both() =
        runTest {
            val noteRepo =
                FakeNoteRepo().apply {
                    noteById = note(calendarEventId = 42L, alarmRequestId = 7)
                }
            val schedRepo = FakeSchedRepo()
            val useCase = UnscheduleNoteUseCaseImpl(noteRepo, schedRepo)

            useCase(1L)

            assertEquals(1, schedRepo.cancelEventCalls)
            assertEquals(1, schedRepo.cancelReminderCalls)
            assertNull(noteRepo.lastSaved?.calendarEventId)
            assertNull(noteRepo.lastSaved?.alarmRequestId)
        }

    @Test
    fun unschedule_when_both_ids_null_is_noop() =
        runTest {
            val noteRepo = FakeNoteRepo().apply { noteById = note() }
            val schedRepo = FakeSchedRepo()
            val useCase = UnscheduleNoteUseCaseImpl(noteRepo, schedRepo)

            useCase(1L)

            assertEquals(0, schedRepo.cancelEventCalls)
            assertEquals(0, schedRepo.cancelReminderCalls)
            assertTrue(noteRepo.lastSaved != null)
        }

    @Test
    fun unschedule_when_note_not_found_is_noop() =
        runTest {
            val noteRepo = FakeNoteRepo()
            val schedRepo = FakeSchedRepo()
            val useCase = UnscheduleNoteUseCaseImpl(noteRepo, schedRepo)

            useCase(999L)

            assertEquals(0, schedRepo.cancelEventCalls)
            assertEquals(0, schedRepo.cancelReminderCalls)
            assertNull(noteRepo.lastSaved)
        }
}
