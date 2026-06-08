package com.just.assistant.local.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.just.assistant.local.R

object NotificationChannelInitializer {
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_BRIEFING = "briefing"

    fun init(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_REMINDERS) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    context.getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
        if (manager.getNotificationChannel(CHANNEL_BRIEFING) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_BRIEFING,
                    context.getString(R.string.notification_channel_briefing),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }
}
