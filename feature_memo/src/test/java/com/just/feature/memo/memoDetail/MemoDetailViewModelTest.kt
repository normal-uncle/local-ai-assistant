package com.just.feature.memo.memoDetail

import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.usecase.note.di.FindNoteByIdUseCase
import com.just.assistant.usecase.schedule.di.UnscheduleNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class MemoDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeFind(private var note: Note?) : FindNoteByIdUseCase {
        var lastId: Long? = null

        override suspend fun invoke(id: Long): Note? {
            lastId = id
            return note
        }

        fun update(note: Note?) {
            this.note = note
        }
    }

    private class FakeUnschedule : UnscheduleNoteUseCase {
        var lastId: Long? = null

        override suspend fun invoke(noteId: Long) {
            lastId = noteId
        }
    }

    private fun note(
        id: Long = 1L,
        calendarEventId: Long? = null,
        alarmRequestId: Int? = null,
    ): Note =
        Note(
            id = id,
            title = "t",
            body = "b",
            type = NoteType.EVENT,
            createdAt = Instant.parse("2026-06-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-06-01T00:00:00Z"),
            calendarEventId = calendarEventId,
            alarmRequestId = alarmRequestId,
        )

    @Test
    fun init_loads_note_into_Loaded_state() =
        runTest(dispatcher) {
            val find = FakeFind(note(calendarEventId = 99L))
            val vm = MemoDetailViewModel(1L, find, FakeUnschedule())
            advanceUntilIdle()
            val state = vm.state.value
            assertTrue(state is MemoDetailState.Loaded)
            assertEquals(99L, (state as MemoDetailState.Loaded).note.calendarEventId)
        }

    @Test
    fun init_with_missing_note_yields_NotFound() =
        runTest(dispatcher) {
            val find = FakeFind(null)
            val vm = MemoDetailViewModel(1L, find, FakeUnschedule())
            advanceUntilIdle()
            assertEquals(MemoDetailState.NotFound, vm.state.value)
        }

    @Test
    fun onUnschedule_calls_use_case_and_reloads_state() =
        runTest(dispatcher) {
            val find = FakeFind(note(calendarEventId = 99L, alarmRequestId = 7))
            val unschedule = FakeUnschedule()
            val vm = MemoDetailViewModel(1L, find, unschedule)
            advanceUntilIdle()
            find.update(note(calendarEventId = null, alarmRequestId = null))

            vm.onUnschedule()
            advanceUntilIdle()

            assertEquals(1L, unschedule.lastId)
            val state = vm.state.value
            assertTrue(state is MemoDetailState.Loaded)
            assertNull((state as MemoDetailState.Loaded).note.calendarEventId)
            assertNull(state.note.alarmRequestId)
        }
}
