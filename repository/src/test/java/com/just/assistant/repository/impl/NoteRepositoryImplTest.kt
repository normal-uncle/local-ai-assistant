package com.just.assistant.repository.impl

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.local.AssistantDatabase
import com.just.assistant.local.note.NoteDao
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class NoteRepositoryImplTest {
    private lateinit var db: AssistantDatabase
    private lateinit var dao: NoteDao
    private lateinit var repo: NoteRepository

    private val fixedNow = Instant.parse("2026-05-19T10:00:00Z")

    @Before
    fun setUp() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AssistantDatabase::class.java,
            ).allowMainThreadQueries().build()
        dao = db.noteDao()
        repo = NoteRepositoryImpl(dao) { fixedNow }
    }

    @After fun tearDown() = db.close()

    @Test
    fun save_new_note_then_observeAll_emits_it_as_domain_model() =
        runTest {
            val id =
                repo.save(
                    Note(
                        title = "장보기",
                        body = "우유",
                        type = NoteType.MEMO,
                        tags = listOf("home", "errand"),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                    ),
                )
            assertTrue(id > 0)
            val list = repo.observeAll().first()
            assertEquals(1, list.size)
            val saved = list[0]
            assertEquals("장보기", saved.title)
            assertEquals(NoteType.MEMO, saved.type)
            assertEquals(listOf("home", "errand"), saved.tags)
            assertEquals(fixedNow, saved.createdAt)
            assertEquals(fixedNow, saved.updatedAt)
        }

    @Test
    fun save_existing_note_preserves_createdAt_and_updates_updatedAt() =
        runTest {
            val originalCreated = Instant.parse("2026-05-01T00:00:00Z")
            val id =
                repo.save(
                    Note(title = "old", body = "", createdAt = originalCreated, updatedAt = originalCreated),
                )
            repo.save(
                Note(
                    id = id,
                    title = "updated",
                    body = "",
                    createdAt = originalCreated,
                    updatedAt = originalCreated,
                ),
            )
            val found = repo.findById(id)
            assertEquals("updated", found?.title)
            assertEquals(originalCreated, found?.createdAt)
            assertEquals(fixedNow, found?.updatedAt)
        }

    @Test
    fun findById_returns_null_when_missing() =
        runTest {
            assertNull(repo.findById(99))
        }

    @Test
    fun delete_returns_true_when_exists_and_false_when_missing() =
        runTest {
            val id =
                repo.save(
                    Note(title = "to delete", body = "", createdAt = fixedNow, updatedAt = fixedNow),
                )
            assertTrue(repo.delete(id))
            assertFalse(repo.delete(id))
        }

    @Test
    fun save_then_findById_preserves_calendarEventId_and_alarmRequestId() =
        runTest {
            val id =
                repo.save(
                    Note(
                        title = "with schedule",
                        body = "",
                        type = NoteType.EVENT,
                        createdAt = fixedNow,
                        updatedAt = fixedNow,
                        calendarEventId = 42L,
                        alarmRequestId = 7,
                    ),
                )
            val found = repo.findById(id)
            assertEquals(42L, found?.calendarEventId)
            assertEquals(7, found?.alarmRequestId)
        }

    @Test
    fun save_then_findById_preserves_isCompleted() =
        runTest {
            val id =
                repo.save(
                    Note(
                        title = "completed",
                        body = "",
                        type = NoteType.REMINDER,
                        createdAt = fixedNow,
                        updatedAt = fixedNow,
                        isCompleted = true,
                    ),
                )
            val found = repo.findById(id)
            assertEquals(true, found?.isCompleted)
        }
}
