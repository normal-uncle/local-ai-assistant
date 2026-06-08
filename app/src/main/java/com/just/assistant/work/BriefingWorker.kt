package com.just.assistant.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.just.assistant.local.pref.UserPreferences
import com.just.assistant.usecase.briefing.di.BuildDailyBriefingUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BriefingWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val buildBriefing: BuildDailyBriefingUseCase,
        private val scheduler: BriefingScheduler,
        private val prefs: UserPreferences,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            val enabled = prefs.briefingEnabled.first()
            try {
                if (enabled) {
                    buildBriefing()?.let { runCatching { BriefingNotifier.show(applicationContext, it) } }
                }
            } finally {
                if (enabled) scheduler.scheduleNext()
            }
            return Result.success()
        }
    }
