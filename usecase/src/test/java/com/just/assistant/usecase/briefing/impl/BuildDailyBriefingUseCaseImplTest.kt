package com.just.assistant.usecase.briefing.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class BuildDailyBriefingUseCaseImplTest {
    private val now = Instant.parse("2026-06-08T00:00:00Z")
    private val clock: () -> Instant = { now }

    private class FakeNoteRepository(private val notes: List<Note>) : NoteRepository {
        override fun observeAll(): Flow<List<Note>> = flowOf(notes)

        override suspend fun findById(id: Long): Note? = notes.firstOrNull { it.id == id }

        override suspend fun save(note: Note): Long = note.id

        override suspend fun delete(id: Long): Boolean = true
    }

    private fun note(
        id: Long,
        title: String,
        datetime: Instant?,
        type: NoteType = NoteType.EVENT,
    ) = Note(id = id, title = title, body = "", type = type, datetime = datetime, createdAt = now, updatedAt = now)

    private fun useCase(notes: List<Note>) = BuildDailyBriefingUseCaseImpl(FakeNoteRepository(notes), clock)

    @Test
    fun returns_null_when_no_notes_today() =
        runTest {
            val notes =
                listOf(
                    note(1, "어제", Instant.parse("2026-06-07T05:00:00Z")),
                    note(2, "내일", Instant.parse("2026-06-09T05:00:00Z")),
                    note(3, "시각없음", null),
                )
            assertNull(useCase(notes)())
        }

    @Test
    fun includes_todays_notes_sorted_by_time() =
        runTest {
            val notes =
                listOf(
                    note(1, "치과", Instant.parse("2026-06-08T06:00:00Z")),
                    note(2, "회의", Instant.parse("2026-06-08T01:00:00Z")),
                )
            val b = useCase(notes)()!!
            assertEquals(2, b.totalCount)
            assertEquals(0, b.moreCount)
            assertEquals("회의", b.items[0].substringAfter(" "))
            assertEquals("치과", b.items[1].substringAfter(" "))
        }

    @Test
    fun caps_items_and_reports_more() =
        runTest {
            val notes =
                (1..13).map {
                    note(it.toLong(), "건$it", Instant.parse("2026-06-08T02:00:00Z").plusSeconds(it.toLong() * 60))
                }
            val b = useCase(notes)()!!
            assertEquals(13, b.totalCount)
            assertEquals(10, b.items.size)
            assertEquals(3, b.moreCount)
        }
}
