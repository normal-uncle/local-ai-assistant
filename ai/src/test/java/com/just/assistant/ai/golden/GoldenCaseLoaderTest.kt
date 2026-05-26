package com.just.assistant.ai.golden

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.repository.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class GoldenCaseLoaderTest {
    @Test
    fun load_returns_50_cases_balanced_across_types() {
        val loader = GoldenCaseLoader(ApplicationProvider.getApplicationContext())
        val cases = loader.load()
        assertEquals(50, cases.size)

        val byType = cases.groupingBy { it.expectedType }.eachCount()
        assertTrue("EVENT count >= 15", (byType[NoteType.EVENT] ?: 0) >= 15)
        assertTrue("REMINDER count >= 15", (byType[NoteType.REMINDER] ?: 0) >= 15)
        assertTrue("MEMO count >= 15", (byType[NoteType.MEMO] ?: 0) >= 15)
    }

    @Test
    fun load_ids_are_unique() {
        val loader = GoldenCaseLoader(ApplicationProvider.getApplicationContext())
        val ids = loader.load().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun load_event_cases_have_expectedHasDatetime_true() {
        val loader = GoldenCaseLoader(ApplicationProvider.getApplicationContext())
        val events = loader.load().filter { it.expectedType == NoteType.EVENT }
        events.forEach { assertTrue("${it.id} EVENT but expectedHasDatetime=false", it.expectedHasDatetime) }
    }
}
