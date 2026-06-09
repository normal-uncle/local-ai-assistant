package com.just.assistant.work

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.just.assistant.local.notification.NotificationChannelInitializer
import com.just.assistant.usecase.briefing.di.DailyBriefing
import com.just.assistant.local.R as LocalR

object BriefingNotifier {
    private const val NOTIFICATION_ID = 770001

    fun show(context: Context, briefing: DailyBriefing) {
        val title = context.getString(LocalR.string.briefing_title, briefing.totalCount)
        val bodyLines =
            briefing.items.toMutableList().apply {
                if (briefing.moreCount > 0) {
                    add(context.getString(LocalR.string.briefing_more, briefing.moreCount))
                }
            }
        val body = bodyLines.joinToString("\n")

        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending =
            launch?.let {
                PendingIntent.getActivity(
                    context, 0, it,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            }

        val notification =
            NotificationCompat.Builder(context, NotificationChannelInitializer.CHANNEL_BRIEFING)
                .setSmallIcon(com.just.assistant.R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(briefing.items.firstOrNull() ?: title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .apply { if (pending != null) setContentIntent(pending) }
                .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
