package com.just.assistant.usecase.schedule.di

import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem

interface ScheduleReminderUseCase {
    suspend operator fun invoke(
        input: ScheduleReminderInput,
        requestId: Int,
    ): ScheduledItem.Reminder
}
