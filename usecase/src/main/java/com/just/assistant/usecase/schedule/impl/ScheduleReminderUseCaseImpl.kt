package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem
import com.just.assistant.usecase.schedule.di.ScheduleReminderUseCase
import javax.inject.Inject

class ScheduleReminderUseCaseImpl
    @Inject
    constructor(
        private val repository: ScheduledItemRepository,
    ) : ScheduleReminderUseCase {
        override suspend fun invoke(
            input: ScheduleReminderInput,
            requestId: Int,
            noteId: Long,
        ): ScheduledItem.Reminder = repository.scheduleReminder(input, requestId, noteId)
    }
