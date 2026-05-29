package com.just.assistant.repository.model

import java.time.Instant

data class Note(
    val id: Long = 0,
    val title: String,
    val body: String,
    val type: NoteType = NoteType.MEMO,
    val tags: List<String> = emptyList(),
    val datetime: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val calendarEventId: Long? = null,
    val alarmRequestId: Int? = null,
)
