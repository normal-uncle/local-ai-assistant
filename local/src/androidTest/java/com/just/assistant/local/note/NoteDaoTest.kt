package com.just.assistant.local.note

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.local.AssistantDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var db: AssistantDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AssistantDatabase::class.java,
            ).allowMainThreadQueries().build()
        dao = db.noteDao()
    }

    @After fun tearDown() = db.close()

    @Test
    fun insert_then_findById_returns_saved_note() =
        runTest {
            val now = 1_700_000_000_000L
            val id =
                dao.insert(
                    NoteEntity(title = "장보기", body = "우유, 빵", createdAtEpochMs = now, updatedAtEpochMs = now),
                )
            val found = dao.findById(id)
            assertNotNull(found)
            assertEquals("장보기", found!!.title)
            assertEquals("우유, 빵", found.body)
            assertEquals("MEMO", found.type)
        }

    @Test
    fun observeAll_emits_updated_list_after_insert() =
        runTest {
            assertTrue(dao.observeAll().first().isEmpty())
            dao.insert(NoteEntity(title = "메모", body = "본문", createdAtEpochMs = 1L, updatedAtEpochMs = 1L))
            assertEquals(1, dao.observeAll().first().size)
        }

    @Test
    fun deleteById_removes_note() =
        runTest {
            val id = dao.insert(NoteEntity(title = "지울", body = "", createdAtEpochMs = 1L, updatedAtEpochMs = 1L))
            assertEquals(1, dao.deleteById(id))
            assertNull(dao.findById(id))
        }

    @Test
    fun observeAll_orders_by_updatedAt_desc() =
        runTest {
            dao.insert(NoteEntity(title = "오래된", body = "", createdAtEpochMs = 1L, updatedAtEpochMs = 100L))
            dao.insert(NoteEntity(title = "최신", body = "", createdAtEpochMs = 1L, updatedAtEpochMs = 200L))
            val list = dao.observeAll().first()
            assertEquals("최신", list[0].title)
            assertEquals("오래된", list[1].title)
        }
}
