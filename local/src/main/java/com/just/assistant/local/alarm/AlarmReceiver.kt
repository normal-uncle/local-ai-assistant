package com.just.assistant.local.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.just.assistant.local.R
import com.just.assistant.local.notification.NotificationChannelInitializer

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        Log.i(TAG, "alarm fired: requestId=$requestId noteId=$noteId title=$title body=$body")

        val contentIntent =
            Intent().apply {
                setClassName(context, "com.just.assistant.MainActivity")
                action = ACTION_VIEW_NOTE
                putExtra(EXTRA_NOTE_ID, noteId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        val pendingFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val contentPending =
            PendingIntent.getActivity(context, requestId, contentIntent, pendingFlags)

        val notification =
            NotificationCompat.Builder(context, NotificationChannelInitializer.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_alarm_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentPending)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(requestId, notification)
    }

    companion object {
        const val ACTION_FIRE = "com.just.assistant.local.alarm.FIRE"
        const val ACTION_VIEW_NOTE = "com.just.assistant.local.alarm.VIEW_NOTE"
        const val EXTRA_TITLE = "extra.title"
        const val EXTRA_BODY = "extra.body"
        const val EXTRA_REQUEST_ID = "extra.requestId"
        const val EXTRA_NOTE_ID = "extra.noteId"
        private const val TAG = "AlarmReceiver"
    }
}
