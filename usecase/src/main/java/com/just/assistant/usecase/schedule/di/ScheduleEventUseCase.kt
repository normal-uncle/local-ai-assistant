package com.just.assistant.usecase.schedule.di

import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduledItem

interface ScheduleEventUseCase {
    suspend operator fun invoke(input: ScheduleEventInput): ScheduledItem.Event?
}
