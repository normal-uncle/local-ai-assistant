package com.just.assistant.local.alarm

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmSchedulerTest {
    @Test
    fun schedule_then_cancel_does_not_throw() {
        val scheduler = AlarmScheduler(ApplicationProvider.getApplicationContext())
        val requestId = 12345
        val whenMs = System.currentTimeMillis() + 60_000L

        scheduler.schedule(requestId, whenMs, "test title", "test body", 1L)
        scheduler.cancel(requestId)
    }
}
