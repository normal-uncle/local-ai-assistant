package com.just.assistant.usecase.schedule.impl

import com.just.assistant.local.alarm.AlarmScheduler
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class SnoozeReminderUseCaseImplTest {
    private class FakeNoteRepo(var note: Note?) : NoteRepository {
        override suspend fun save(n: Note): Long = n.id

        override suspend fun findById(id: Long): Note? = note

        override fun observeAll(): Flow<List<Note>> = flowOf(emptyList())

        override suspend fun delete(id: Long): Boolean = false
    }

    private fun reminder(alarmRequestId: Int? = 7): Note =
        Note(
            id = 1L,
            title = "콜백",
            body = "고객 A",
            type = NoteType.REMINDER,
            createdAt = Instant.parse("2026-06-04T00:00:00Z"),
            updatedAt = Instant.parse("2026-06-04T00:00:00Z"),
            alarmRequestId = alarmRequestId,
        )

    @Test
    fun snooze_schedules_alarm_with_existing_requestId_and_future_time() =
        runTest {
            val repo = FakeNoteRepo(reminder())
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val useCase = SnoozeReminderUseCaseImpl(repo, scheduler)

            val before = System.currentTimeMillis()
            useCase(1L, 5 * 60_000L)
            val after = System.currentTimeMillis()

            verify {
                scheduler.schedule(
                    requestId = 7,
                    whenEpochMs = match { it in (before + 5 * 60_000L)..(after + 5 * 60_000L) },
                    title = "콜백",
                    body = "고객 A",
                    noteId = 1L,
                )
            }
        }

    @Test
    fun snooze_when_alarmRequestId_null_is_noop() =
        runTest {
            val repo = FakeNoteRepo(reminder(alarmRequestId = null))
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val useCase = SnoozeReminderUseCaseImpl(repo, scheduler)

            useCase(1L, 5 * 60_000L)

            verify(exactly = 0) { scheduler.schedule(any(), any(), any(), any(), any()) }
            assertEquals(Unit, Unit)
        }
}
