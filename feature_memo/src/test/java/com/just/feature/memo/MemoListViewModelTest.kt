package com.just.feature.memo

import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.ObserveNotesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class MemoListViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeObserveNotes(initial: List<Note>) : ObserveNotesUseCase {
        val flow = MutableStateFlow(initial)

        override fun invoke(): Flow<List<Note>> = flow
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun state_starts_Loading() =
        runTest {
            val vm = MemoListViewModel(FakeObserveNotes(emptyList()))
            assertEquals(MemoListState.Loading, vm.state.value)
        }

    @Test
    fun state_emits_Loaded_with_notes_from_use_case() =
        runTest {
            val now = Instant.parse("2026-05-19T10:00:00Z")
            val notes =
                listOf(
                    Note(id = 1, title = "a", body = "", createdAt = now, updatedAt = now),
                    Note(id = 2, title = "b", body = "", createdAt = now, updatedAt = now),
                )
            val vm = MemoListViewModel(FakeObserveNotes(notes))
            advanceUntilIdle()
            val s = vm.state.first()
            assertTrue(s is MemoListState.Loaded)
            assertEquals(2, (s as MemoListState.Loaded).notes.size)
        }
}
