package com.just.assistant.local.note

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val type: String = "MEMO",
    val tags: String = "",
    val datetimeIso: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val calendarEventId: Long? = null,
    val alarmRequestId: Int? = null,
    val isCompleted: Boolean = false,
)
