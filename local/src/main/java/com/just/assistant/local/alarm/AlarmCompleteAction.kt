package com.just.assistant.local.alarm

interface AlarmCompleteAction {
    suspend operator fun invoke(noteId: Long)
}
