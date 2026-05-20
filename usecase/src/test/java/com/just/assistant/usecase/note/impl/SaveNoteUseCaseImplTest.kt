package com.just.assistant.usecase.note.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SaveNoteUseCaseImplTest {
    private class FakeNoteRepository : NoteRepository {
        private val flow = MutableStateFlow<List<Note>>(emptyList())
        var saved: Note? = null
        var lastReturnedId: Long = 0L

        override fun observeAll(): Flow<List<Note>> = flow.asStateFlow()

        override suspend fun findById(id: Long): Note? = flow.value.firstOrNull { it.id == id }

        override suspend fun save(note: Note): Long {
            saved = note
            lastReturnedId = (flow.value.maxOfOrNull { it.id } ?: 0L) + 1
            val withId = note.copy(id = lastReturnedId)
            flow.update { it + withId }
            return lastReturnedId
        }

        override suspend fun delete(id: Long): Boolean {
            val removed = flow.value.any { it.id == id }
            flow.update { list -> list.filterNot { it.id == id } }
            return removed
        }
    }

    @Test
    fun invoke_delegates_to_repository_and_returns_id() =
        runTest {
            val repo = FakeNoteRepository()
            val useCase = SaveNoteUseCaseImpl(repo)
            val now = Instant.parse("2026-05-19T10:00:00Z")

            val id = useCase(Note(title = "장보기", body = "", createdAt = now, updatedAt = now))

            assertEquals(1L, id)
            assertEquals("장보기", repo.saved?.title)
        }

    @Test(expected = IllegalArgumentException::class)
    fun invoke_throws_when_title_is_blank() =
        runTest {
            val useCase = SaveNoteUseCaseImpl(FakeNoteRepository())
            val now = Instant.parse("2026-05-19T10:00:00Z")
            useCase(Note(title = "   ", body = "x", createdAt = now, updatedAt = now))
        }

    @Test
    fun invoke_accepts_title_with_surrounding_whitespace_if_nonblank_after() =
        runTest {
            val repo = FakeNoteRepository()
            val useCase = SaveNoteUseCaseImpl(repo)
            val now = Instant.parse("2026-05-19T10:00:00Z")
            val id = useCase(Note(title = "  메모  ", body = "", createdAt = now, updatedAt = now))
            assertTrue(id > 0)
        }
}
