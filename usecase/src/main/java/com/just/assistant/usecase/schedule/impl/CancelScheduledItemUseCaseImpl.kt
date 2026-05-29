package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.usecase.schedule.di.CancelScheduledItemUseCase
import javax.inject.Inject

class CancelScheduledItemUseCaseImpl
    @Inject
    constructor(
        private val repository: ScheduledItemRepository,
    ) : CancelScheduledItemUseCase {
        override suspend fun cancelEvent(calendarEventId: Long): Boolean = repository.cancelEvent(calendarEventId)

        override suspend fun cancelReminder(alarmRequestId: Int) {
            repository.cancelReminder(alarmRequestId)
        }
    }
