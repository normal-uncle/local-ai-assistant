package com.just.assistant.usecase.chat.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RetrieveNoteContextUseCaseImplTest {
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
        body: String = "",
        type: NoteType = NoteType.MEMO,
        datetime: Instant? = null,
    ) = Note(
        id = id, title = title, body = body, type = type, datetime = datetime,
        createdAt = now, updatedAt = now,
    )

    private fun useCase(notes: List<Note>) =
        RetrieveNoteContextUseCaseImpl(FakeNoteRepository(notes), clock)

    @Test
    fun empty_repository_returns_blank() =
        runTest { assertEquals("", useCase(emptyList())("내일 일정")) }

    @Test
    fun no_match_returns_blank() =
        runTest {
            val notes = listOf(note(1, "장보기", "우유 빵"))
            assertEquals("", useCase(notes)("주식 시황 알려줘"))
        }

    @Test
    fun includes_upcoming_events_sorted() =
        runTest {
            val notes =
                listOf(
                    note(1, "치과", type = NoteType.EVENT, datetime = Instant.parse("2026-06-10T06:00:00Z")),
                    note(2, "회의", type = NoteType.EVENT, datetime = Instant.parse("2026-06-09T06:00:00Z")),
                    note(3, "지난약속", type = NoteType.EVENT, datetime = Instant.parse("2026-06-01T06:00:00Z")),
                )
            val ctx = useCase(notes)("일정 알려줘")
            assertTrue(ctx.contains("회의"))
            assertTrue(ctx.contains("치과"))
            assertFalse(ctx.contains("지난약속"))
            assertTrue(ctx.indexOf("회의") < ctx.indexOf("치과"))
        }

    @Test
    fun keyword_matches_by_title_and_body() =
        runTest {
            val notes =
                listOf(
                    note(1, "운동 계획", "헬스장 등록"),
                    note(2, "장보기", "우유"),
                )
            val ctx = useCase(notes)("헬스장 언제 가지")
            assertTrue(ctx.contains("운동 계획"))
            assertFalse(ctx.contains("장보기"))
        }

    @Test
    fun truncates_to_max_chars() =
        runTest {
            val big = (1..50).map { note(it.toLong(), "메모$it 헬스", "헬스장 ".repeat(20)) }
            val ctx = useCase(big)("헬스")
            assertTrue(ctx.length <= 1000)
        }
}
