package com.just.assistant.local.alarm

interface AlarmSnoozeAction {
    suspend operator fun invoke(
        noteId: Long,
        delayMs: Long,
    )
}
