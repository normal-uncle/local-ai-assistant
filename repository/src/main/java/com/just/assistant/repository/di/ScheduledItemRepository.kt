package com.just.assistant.repository.di

import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem

interface ScheduledItemRepository {
    suspend fun scheduleEvent(input: ScheduleEventInput): ScheduledItem.Event?

    suspend fun scheduleReminder(
        input: ScheduleReminderInput,
        requestId: Int,
        noteId: Long,
    ): ScheduledItem.Reminder

    suspend fun cancelEvent(calendarEventId: Long): Boolean

    suspend fun cancelReminder(alarmRequestId: Int)
}
