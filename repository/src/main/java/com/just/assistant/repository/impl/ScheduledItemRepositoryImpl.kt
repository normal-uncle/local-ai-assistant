package com.just.assistant.repository.impl

import com.just.assistant.local.alarm.AlarmScheduler
import com.just.assistant.local.calendar.CalendarEventDraft
import com.just.assistant.local.calendar.CalendarWriter
import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemRepositoryImpl
    @Inject
    constructor(
        private val calendarWriter: CalendarWriter,
        private val alarmScheduler: AlarmScheduler,
    ) : ScheduledItemRepository {
        override suspend fun scheduleEvent(input: ScheduleEventInput): ScheduledItem.Event? {
            val start = input.whenAt.toEpochMilli()
            val end = start + input.durationMinutes * 60_000L
            val eventId =
                calendarWriter.insertEvent(
                    CalendarEventDraft(
                        title = input.title,
                        description = input.body,
                        startEpochMs = start,
                        endEpochMs = end,
                    ),
                ) ?: return null
            return ScheduledItem.Event(
                calendarEventId = eventId,
                title = input.title,
                whenAt = input.whenAt,
                durationMinutes = input.durationMinutes,
            )
        }

        override suspend fun scheduleReminder(
            input: ScheduleReminderInput,
            requestId: Int,
        ): ScheduledItem.Reminder {
            alarmScheduler.schedule(
                requestId = requestId,
                whenEpochMs = input.whenAt.toEpochMilli(),
                title = input.title,
                body = input.body,
            )
            return ScheduledItem.Reminder(
                alarmRequestId = requestId,
                title = input.title,
                whenAt = input.whenAt,
            )
        }

        override suspend fun cancelEvent(calendarEventId: Long): Boolean = calendarWriter.deleteEvent(calendarEventId)

        override suspend fun cancelReminder(alarmRequestId: Int) {
            alarmScheduler.cancel(alarmRequestId)
        }
    }
