package com.just.assistant.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.just.assistant.usecase.briefing.di.BriefingController
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BriefingScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clock: () -> Instant,
    ) : BriefingController {
        override fun enable() = scheduleNext()

        fun scheduleNext() {
            val delay = millisUntilHour(clock().toEpochMilli(), ZoneId.systemDefault(), HOUR, MINUTE)
            val req =
                OneTimeWorkRequestBuilder<BriefingWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, req)
        }

        override fun disable() {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        companion object {
            const val WORK_NAME = "daily_briefing"
            const val HOUR = 8
            const val MINUTE = 0
        }
    }

/** [nowMillis] 기준 [zone]의 다음 [hour]:[minute]까지 남은 ms. 이미 지났으면 다음 날. */
fun millisUntilHour(nowMillis: Long, zone: ZoneId, hour: Int, minute: Int): Long {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
    var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (!target.isAfter(now)) target = target.plusDays(1)
    return Duration.between(now, target).toMillis()
}
