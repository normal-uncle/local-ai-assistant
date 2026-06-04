package com.just.assistant.local.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.just.assistant.local.R
import com.just.assistant.local.notification.NotificationActionsEntryPoint
import com.just.assistant.local.notification.NotificationChannelInitializer
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        when (intent.action) {
            ACTION_FIRE -> handleFire(context, intent, requestId, noteId)
            ACTION_DONE -> handleDone(context, requestId, noteId)
            ACTION_SNOOZE -> handleSnooze(context, requestId, noteId)
        }
    }

    private fun handleFire(
        context: Context,
        intent: Intent,
        requestId: Int,
        noteId: Long,
    ) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
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

        val doneIntent =
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_DONE
                putExtra(EXTRA_REQUEST_ID, requestId)
                putExtra(EXTRA_NOTE_ID, noteId)
            }
        val donePending =
            PendingIntent.getBroadcast(context, requestId * 3 + 1, doneIntent, pendingFlags)

        val snoozeIntent =
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_REQUEST_ID, requestId)
                putExtra(EXTRA_NOTE_ID, noteId)
            }
        val snoozePending =
            PendingIntent.getBroadcast(context, requestId * 3 + 2, snoozeIntent, pendingFlags)

        val notification =
            NotificationCompat.Builder(context, NotificationChannelInitializer.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_alarm_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentPending)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.ic_check,
                    context.getString(R.string.notification_action_done),
                    donePending,
                )
                .addAction(
                    R.drawable.ic_snooze,
                    context.getString(R.string.notification_action_snooze),
                    snoozePending,
                )
                .build()

        NotificationManagerCompat.from(context).notify(requestId, notification)
    }

    private fun handleDone(
        context: Context,
        requestId: Int,
        noteId: Long,
    ) {
        Log.i(TAG, "action DONE: requestId=$requestId noteId=$noteId")
        val entry =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                NotificationActionsEntryPoint::class.java,
            )
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            entry.completeAction().invoke(noteId)
        }
        NotificationManagerCompat.from(context).cancel(requestId)
    }

    private fun handleSnooze(
        context: Context,
        requestId: Int,
        noteId: Long,
    ) {
        Log.i(TAG, "action SNOOZE: requestId=$requestId noteId=$noteId")
        val entry =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                NotificationActionsEntryPoint::class.java,
            )
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            entry.snoozeAction().invoke(noteId, SNOOZE_DELAY_MS)
        }
        NotificationManagerCompat.from(context).cancel(requestId)
    }

    companion object {
        const val ACTION_FIRE = "com.just.assistant.local.alarm.FIRE"
        const val ACTION_DONE = "com.just.assistant.local.alarm.DONE"
        const val ACTION_SNOOZE = "com.just.assistant.local.alarm.SNOOZE"
        const val ACTION_VIEW_NOTE = "com.just.assistant.local.alarm.VIEW_NOTE"
        const val EXTRA_TITLE = "extra.title"
        const val EXTRA_BODY = "extra.body"
        const val EXTRA_REQUEST_ID = "extra.requestId"
        const val EXTRA_NOTE_ID = "extra.noteId"
        const val SNOOZE_DELAY_MS = 5 * 60 * 1000L
        private const val TAG = "AlarmReceiver"
    }
}
