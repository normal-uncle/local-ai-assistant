package com.just.assistant.local.notification

import com.just.assistant.local.alarm.AlarmCompleteAction
import com.just.assistant.local.alarm.AlarmSnoozeAction
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationActionsEntryPoint {
    fun completeAction(): AlarmCompleteAction

    fun snoozeAction(): AlarmSnoozeAction
}
