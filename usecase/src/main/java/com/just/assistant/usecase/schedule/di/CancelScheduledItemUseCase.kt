package com.just.assistant.usecase.schedule.di

interface CancelScheduledItemUseCase {
    suspend fun cancelEvent(calendarEventId: Long): Boolean

    suspend fun cancelReminder(alarmRequestId: Int)
}
