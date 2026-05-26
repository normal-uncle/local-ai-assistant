package com.just.feature.capture

import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.feature.capture.capture.CaptureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    private class FakeClassify(private val result: ClassificationResult?) : ClassifyCaptureUseCase {
        var calledWith: String? = null

        override suspend operator fun invoke(userInput: String): ClassificationResult? {
            calledWith = userInput
            return result
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun classifyResult(
        type: NoteType = NoteType.MEMO,
        title: String = "AI 제목",
        body: String = "AI 본문",
    ) = ClassificationResult(type = type, title = title, body = body, tags = emptyList(), datetimeIso = null)

    @Test
    fun onPrepare_uses_AI_result_when_classifier_returns_non_null() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote(), FakeClassify(classifyResult(NoteType.EVENT, "치과", "내일 3시")))
            vm.onInputChanged("내일 치과 가야 함")
            vm.onPrepare()
            advanceUntilIdle()
            val preview = vm.state.first().preview!!
            assertEquals("치과", preview.title)
            assertEquals("내일 3시", preview.body)
            assertEquals(NoteType.EVENT, preview.type)
        }

    @Test
    fun onPrepare_falls_back_to_rule_based_when_classifier_returns_null() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote(), FakeClassify(null))
            vm.onInputChanged("장보기\n우유, 빵")
            vm.onPrepare()
            advanceUntilIdle()
            val preview = vm.state.first().preview!!
            assertEquals("장보기", preview.title)
            assertEquals("우유, 빵", preview.body)
            assertEquals(NoteType.MEMO, preview.type)
        }

    @Test
    fun onPrepare_with_blank_input_does_nothing() =
        runTest {
            val classify = FakeClassify(classifyResult())
            val vm = CaptureViewModel(FakeSaveNote(), classify)
            vm.onInputChanged("   ")
            vm.onPrepare()
            advanceUntilIdle()
            assertNull(vm.state.first().preview)
            assertNull(classify.calledWith)
        }

    @Test
    fun onPrepare_sets_isPreparing_true_during_call() =
        runTest {
            val classify = FakeClassify(classifyResult())
            val vm = CaptureViewModel(FakeSaveNote(), classify)
            vm.onInputChanged("test")
            vm.onPrepare()
            assertEquals(true, vm.state.first().isPreparing)
            advanceUntilIdle()
            assertEquals(false, vm.state.first().isPreparing)
        }

    @Test
    fun onPrepare_double_call_is_guarded() =
        runTest {
            val classify = FakeClassify(classifyResult())
            val vm = CaptureViewModel(FakeSaveNote(), classify)
            vm.onInputChanged("test")
            vm.onPrepare()
            vm.onPrepare()
            advanceUntilIdle()
            assertEquals("test", classify.calledWith)
            assertNotNull(vm.state.first().preview)
        }

    @Test
    fun onConfirm_uses_edited_preview() =
        runTest {
            val save = FakeSaveNote()
            val vm = CaptureViewModel(save, FakeClassify(classifyResult(NoteType.MEMO, "원본", "")))
            vm.onInputChanged("원본")
            vm.onPrepare()
            advanceUntilIdle()
            val prepared = vm.state.first().preview!!
            val edited = prepared.copy(title = "수정", body = "본문")
            vm.onConfirm(edited)
            advanceUntilIdle()
            assertEquals("수정", save.saved?.title)
            assertEquals("본문", save.saved?.body)
        }

    @Test
    fun onConfirm_handles_save_exception() =
        runTest {
            val throwingSave =
                object : SaveNoteUseCase {
                    override suspend fun invoke(note: Note): Long = throw RuntimeException("disk full")
                }
            val vm = CaptureViewModel(throwingSave, FakeClassify(classifyResult()))
            vm.onInputChanged("test")
            vm.onPrepare()
            advanceUntilIdle()
            vm.onConfirm(vm.state.first().preview!!)
            advanceUntilIdle()
            val state = vm.state.first()
            assertEquals(false, state.isSaving)
            assertEquals("disk full", state.error)
        }

    @Test
    fun onCancel_clears_preview() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote(), FakeClassify(classifyResult()))
            vm.onInputChanged("test")
            vm.onPrepare()
            advanceUntilIdle()
            vm.onCancel()
            assertNull(vm.state.first().preview)
        }
}
