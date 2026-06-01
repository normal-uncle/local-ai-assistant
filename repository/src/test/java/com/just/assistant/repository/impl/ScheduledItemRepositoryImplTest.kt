package com.just.assistant.repository.impl

import com.just.assistant.local.alarm.AlarmScheduler
import com.just.assistant.local.calendar.CalendarEventDraft
import com.just.assistant.local.calendar.CalendarWriter
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class ScheduledItemRepositoryImplTest {
    @Test
    fun scheduleEvent_returns_event_with_calendar_id_on_success() =
        runTest {
            val writer = mockk<CalendarWriter>()
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            every { writer.insertEvent(any()) } returns 42L

            val repo = ScheduledItemRepositoryImpl(writer, scheduler)
            val whenAt = Instant.parse("2026-05-30T15:00:00Z")
            val result =
                repo.scheduleEvent(
                    ScheduleEventInput(
                        title = "치과",
                        body = "정기 검진",
                        whenAt = whenAt,
                        durationMinutes = 60,
                    ),
                )

            assertNotNull(result)
            assertEquals(42L, result!!.calendarEventId)
            assertEquals("치과", result.title)
            verify {
                writer.insertEvent(
                    CalendarEventDraft(
                        title = "치과",
                        description = "정기 검진",
                        startEpochMs = whenAt.toEpochMilli(),
                        endEpochMs = whenAt.toEpochMilli() + 3_600_000L,
                        timeZoneId = TimeZone.getDefault().id,
                    ),
                )
            }
        }

    @Test
    fun scheduleEvent_returns_null_when_writer_returns_null() =
        runTest {
            val writer = mockk<CalendarWriter>()
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            every { writer.insertEvent(any()) } returns null

            val repo = ScheduledItemRepositoryImpl(writer, scheduler)
            val result =
                repo.scheduleEvent(
                    ScheduleEventInput(
                        title = "x",
                        body = "",
                        whenAt = Instant.now(),
                    ),
                )
            assertNull(result)
        }

    @Test
    fun scheduleReminder_invokes_alarm_scheduler_and_returns_reminder() =
        runTest {
            val writer = mockk<CalendarWriter>(relaxed = true)
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val repo = ScheduledItemRepositoryImpl(writer, scheduler)

            val whenAt = Instant.parse("2026-05-30T15:00:00Z")
            val result =
                repo.scheduleReminder(
                    input =
                        ScheduleReminderInput(
                            title = "콜백",
                            body = "고객 A",
                            whenAt = whenAt,
                        ),
                    requestId = 7,
                    noteId = 42L,
                )

            assertEquals(7, result.alarmRequestId)
            assertEquals(whenAt, result.whenAt)
            verify {
                scheduler.schedule(
                    requestId = 7,
                    whenEpochMs = whenAt.toEpochMilli(),
                    title = "콜백",
                    body = "고객 A",
                    noteId = 42L,
                )
            }
        }

    @Test
    fun cancelEvent_delegates_to_writer() =
        runTest {
            val writer = mockk<CalendarWriter>()
            every { writer.deleteEvent(99L) } returns true
            val repo = ScheduledItemRepositoryImpl(writer, mockk(relaxed = true))
            assertEquals(true, repo.cancelEvent(99L))
        }

    @Test
    fun cancelReminder_delegates_to_scheduler() =
        runTest {
            val scheduler = mockk<AlarmScheduler>(relaxed = true)
            val repo = ScheduledItemRepositoryImpl(mockk(relaxed = true), scheduler)
            repo.cancelReminder(7)
            verify { scheduler.cancel(7) }
        }
}
