package com.just.feature.capture

import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CaptureViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeSaveNote : SaveNoteUseCase {
        var saved: Note? = null

        override suspend fun invoke(note: Note): Long {
            saved = note
            return 1L
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_empty() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote())
            val state = vm.state.first()
            assertEquals("", state.input)
            assertNull(state.preview)
            assertFalse(state.isSaving)
        }

    @Test
    fun onInputChanged_updates_input() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote())
            vm.onInputChanged("우유 사기")
            assertEquals("우유 사기", vm.state.first().input)
        }

    @Test
    fun onPrepare_splits_first_line_as_title() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote())
            vm.onInputChanged("장보기\n우유, 빵")
            vm.onPrepare()
            val preview = vm.state.first().preview
            assertNotNull(preview)
            assertEquals("장보기", preview!!.title)
            assertEquals("우유, 빵", preview.body)
            assertEquals(NoteType.MEMO, preview.type)
        }

    @Test
    fun onPrepare_with_single_line_uses_whole_as_title() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote())
            vm.onInputChanged("회의 내일 3시")
            vm.onPrepare()
            val preview = vm.state.first().preview!!
            assertEquals("회의 내일 3시", preview.title)
            assertEquals("", preview.body)
        }

    @Test
    fun onPrepare_with_blank_input_does_nothing() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote())
            vm.onInputChanged("   ")
            vm.onPrepare()
            assertNull(vm.state.first().preview)
        }

    @Test
    fun onConfirm_saves_note_and_resets_input() =
        runTest {
            val save = FakeSaveNote()
            val vm = CaptureViewModel(save)
            vm.onInputChanged("장보기\n우유")
            vm.onPrepare()
            vm.onConfirm()
            advanceUntilIdle()

            assertEquals("장보기", save.saved?.title)
            assertEquals("우유", save.saved?.body)
            assertEquals(NoteType.MEMO, save.saved?.type)

            val state = vm.state.first()
            assertEquals("", state.input)
            assertNull(state.preview)
            assertFalse(state.isSaving)
        }

    @Test
    fun onCancel_clears_preview_but_keeps_input() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote())
            vm.onInputChanged("test")
            vm.onPrepare()
            vm.onCancel()
            val state = vm.state.first()
            assertEquals("test", state.input)
            assertNull(state.preview)
        }
}
