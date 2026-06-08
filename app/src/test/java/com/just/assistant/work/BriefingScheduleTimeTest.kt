package com.just.assistant.work

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class BriefingScheduleTimeTest {
    private val zone = ZoneId.of("Asia/Seoul")

    @Test
    fun before_target_same_day() {
        val now = ZonedDateTime.of(2026, 6, 8, 7, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(60 * 60 * 1000L, millisUntilHour(now, zone, 8, 0))
    }

    @Test
    fun after_target_next_day() {
        val now = ZonedDateTime.of(2026, 6, 8, 9, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(23 * 60 * 60 * 1000L, millisUntilHour(now, zone, 8, 0))
    }
}
