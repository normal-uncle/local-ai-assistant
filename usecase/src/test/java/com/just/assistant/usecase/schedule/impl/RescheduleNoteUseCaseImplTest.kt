package com.just.assistant.usecase.schedule.impl

import com.just.assistant.local.alarm.AlarmScheduler
import com.just.assistant.local.calendar.CalendarEventDraft
import com.just.assistant.local.calendar.CalendarWriter
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class RescheduleNoteUseCaseImplTest {
    private class FakeNoteRepo(var note: Note?) : NoteRepository {
        var lastSaved: Note? = null

        override suspend fun save(n: Note): Long {
            lastSaved = n
            return n.id
        }

        override suspend fun findById(id: Long): Note? = note

        override fun observeAll(): Flow<List<Note>> = flowOf(emptyList())

        override suspend fun delete(id: Long): Boolean = false
    }

    private fun note(
        type: NoteType = NoteType.EVENT,
        calendarEventId: Long? = null,
        alarmRequestId: Int? = null,
    ): Note =
        Note(
            id = 1L,
            title = "회의",
            body = "본문",
            type = type,
            createdAt = Instant.parse("2026-06-04T00:00:00Z"),
            updatedAt = Instant.parse("2026-06-04T00:00:00Z"),
            calendarEventId = calendarEventId,
            alarmRequestId = alarmRequestId,
        )

    private val newWhen = Instant.parse("2026-06-10T10:00:00Z")

    @Test
    fun reschedule_EVENT_updates_calendar_only() =
        runTest {
            val repo = FakeNoteRepo(note(NoteType.EVENT, calendarEventId = 42L))
            val writer = mockk<CalendarWriter>(relaxed = true)
            every { writer.updateEvent(any(), any()) } returns true
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val useCase = RescheduleNoteUseCaseImpl(repo, writer, scheduler)

            useCase(1L, newWhen)

            verify {
                writer.updateEvent(
                    42L,
                    CalendarEventDraft(
                        title = "회의",
                        description = "본문",
                        startEpochMs = newWhen.toEpochMilli(),
                        endEpochMs = newWhen.toEpochMilli() + 60 * 60 * 1000L,
                    ),
                )
            }
            verify(exactly = 0) { scheduler.schedule(any(), any(), any(), any(), any()) }
            assertEquals(newWhen, repo.lastSaved?.datetime)
        }

    @Test
    fun reschedule_REMINDER_reschedules_alarm_only() =
        runTest {
            val repo = FakeNoteRepo(note(NoteType.REMINDER, alarmRequestId = 7))
            val writer = mockk<CalendarWriter>(relaxed = true)
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val useCase = RescheduleNoteUseCaseImpl(repo, writer, scheduler)

            useCase(1L, newWhen)

            verify {
                scheduler.schedule(
                    requestId = 7,
                    whenEpochMs = newWhen.toEpochMilli(),
                    title = "회의",
                    body = "본문",
                    noteId = 1L,
                )
            }
            verify(exactly = 0) { writer.updateEvent(any(), any()) }
            assertEquals(newWhen, repo.lastSaved?.datetime)
        }

    @Test
    fun reschedule_with_both_updates_both() =
        runTest {
            val repo = FakeNoteRepo(note(NoteType.EVENT, calendarEventId = 42L, alarmRequestId = 7))
            val writer = mockk<CalendarWriter>(relaxed = true)
            every { writer.updateEvent(any(), any()) } returns true
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val useCase = RescheduleNoteUseCaseImpl(repo, writer, scheduler)

            useCase(1L, newWhen)

            verify { writer.updateEvent(42L, any()) }
            verify { scheduler.schedule(7, any(), any(), any(), 1L) }
        }

    @Test
    fun reschedule_with_neither_only_updates_note_datetime() =
        runTest {
            val repo = FakeNoteRepo(note(NoteType.MEMO))
            val writer = mockk<CalendarWriter>(relaxed = true)
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val useCase = RescheduleNoteUseCaseImpl(repo, writer, scheduler)

            useCase(1L, newWhen)

            verify(exactly = 0) { writer.updateEvent(any(), any()) }
            verify(exactly = 0) { scheduler.schedule(any(), any(), any(), any(), any()) }
            assertEquals(newWhen, repo.lastSaved?.datetime)
        }
}
