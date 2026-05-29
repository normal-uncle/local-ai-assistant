package com.just.assistant.usecase.schedule.impl

import com.just.assistant.repository.di.ScheduledItemRepository
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduledItem
import com.just.assistant.usecase.schedule.di.ScheduleEventUseCase
import javax.inject.Inject

class ScheduleEventUseCaseImpl
    @Inject
    constructor(
        private val repository: ScheduledItemRepository,
    ) : ScheduleEventUseCase {
        override suspend fun invoke(input: ScheduleEventInput): ScheduledItem.Event? = repository.scheduleEvent(input)
    }
