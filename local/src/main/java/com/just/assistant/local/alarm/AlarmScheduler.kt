package com.just.assistant.local.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun schedule(
            requestId: Int,
            whenEpochMs: Long,
            title: String,
            body: String,
        ) {
            val pending = buildPendingIntent(requestId, title, body)
            val useExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
            if (useExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenEpochMs, pending)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenEpochMs, pending)
            }
        }

        fun cancel(requestId: Int) {
            val pending = buildPendingIntent(requestId, "", "")
            am.cancel(pending)
            pending.cancel()
        }

        private fun buildPendingIntent(
            requestId: Int,
            title: String,
            body: String,
        ): PendingIntent {
            val intent =
                Intent(context, AlarmReceiver::class.java).apply {
                    action = AlarmReceiver.ACTION_FIRE
                    putExtra(AlarmReceiver.EXTRA_REQUEST_ID, requestId)
                    putExtra(AlarmReceiver.EXTRA_TITLE, title)
                    putExtra(AlarmReceiver.EXTRA_BODY, body)
                }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getBroadcast(context, requestId, intent, flags)
        }
    }
