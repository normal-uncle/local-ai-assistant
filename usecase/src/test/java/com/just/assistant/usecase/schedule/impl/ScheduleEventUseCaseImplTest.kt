package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ScheduleEventUseCaseImplTest {
    private class FakeRepo(
        private val eventResult: ScheduledItem.Event?,
    ) : ScheduledItemRepository {
        var lastEventInput: ScheduleEventInput? = null

        override suspend fun scheduleEvent(input: ScheduleEventInput): ScheduledItem.Event? {
            lastEventInput = input
            return eventResult
        }

        override suspend fun scheduleReminder(
            input: ScheduleReminderInput,
            requestId: Int,
            noteId: Long,
        ): ScheduledItem.Reminder = error("not used")

        override suspend fun cancelEvent(calendarEventId: Long): Boolean = false

        override suspend fun cancelReminder(alarmRequestId: Int) {}
    }

    @Test
    fun invoke_delegates_to_repository_and_returns_result() =
        runTest {
            val expected =
                ScheduledItem.Event(
                    calendarEventId = 42L,
                    title = "치과",
                    whenAt = Instant.parse("2026-05-30T15:00:00Z"),
                    durationMinutes = 60,
                )
            val repo = FakeRepo(eventResult = expected)
            val useCase = ScheduleEventUseCaseImpl(repo)
            val input =
                ScheduleEventInput(
                    title = "치과",
                    body = "정기",
                    whenAt = Instant.parse("2026-05-30T15:00:00Z"),
                )
            val result = useCase(input)
            assertEquals(expected, result)
            assertEquals(input, repo.lastEventInput)
        }
}
