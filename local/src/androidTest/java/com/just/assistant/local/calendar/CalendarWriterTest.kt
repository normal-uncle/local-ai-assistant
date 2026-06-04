package com.just.assistant.local.calendar

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CalendarWriterTest {
    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR,
        )

    @Test
    fun insertEvent_then_deleteEvent_round_trips() {
        val writer = CalendarWriter(ApplicationProvider.getApplicationContext())
        val start = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        val end = start + TimeUnit.HOURS.toMillis(1)

        val eventId =
            writer.insertEvent(
                CalendarEventDraft(
                    title = "Assistant 테스트 이벤트",
                    description = "android instrumented test",
                    startEpochMs = start,
                    endEpochMs = end,
                ),
            )

        assumeTrue(
            "Device has no visible calendar (e.g. fresh emulator without Google account)",
            eventId != null,
        )
        assertTrue("delete should succeed for inserted event", writer.deleteEvent(eventId!!))
    }

    @Test
    fun insertEvent_then_updateEvent_then_deleteEvent_round_trips() {
        val writer = CalendarWriter(ApplicationProvider.getApplicationContext())
        val start = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        val end = start + TimeUnit.HOURS.toMillis(1)

        val eventId =
            writer.insertEvent(
                CalendarEventDraft(
                    title = "original",
                    description = "before",
                    startEpochMs = start,
                    endEpochMs = end,
                ),
            )
        assumeTrue(
            "Device has no visible calendar",
            eventId != null,
        )

        val newStart = start + TimeUnit.HOURS.toMillis(2)
        val newEnd = newStart + TimeUnit.HOURS.toMillis(1)
        val updated =
            writer.updateEvent(
                eventId!!,
                CalendarEventDraft(
                    title = "updated",
                    description = "after",
                    startEpochMs = newStart,
                    endEpochMs = newEnd,
                ),
            )
        assertTrue("update should succeed for existing event", updated)
        assertTrue("delete should succeed after update", writer.deleteEvent(eventId))
    }
}
