package com.just.assistant.repository.model

import java.time.Instant

sealed interface ScheduledItem {
    val title: String
    val whenAt: Instant

    data class Event(
        val calendarEventId: Long,
        override val title: String,
        override val whenAt: Instant,
        val durationMinutes: Int = 60,
    ) : ScheduledItem

    data class Reminder(
        val alarmRequestId: Int,
        override val title: String,
        override val whenAt: Instant,
    ) : ScheduledItem
}

data class ScheduleEventInput(
    val title: String,
    val body: String,
    val whenAt: Instant,
    val durationMinutes: Int = 60,
)

data class ScheduleReminderInput(
    val title: String,
    val body: String,
    val whenAt: Instant,
)
