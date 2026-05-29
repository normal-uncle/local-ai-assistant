package com.just.feature.capture

import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.assistant.usecase.schedule.di.ScheduleEventUseCase
import com.just.assistant.usecase.schedule.di.ScheduleReminderUseCase
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
import java.time.Instant

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

    private class FakeScheduleEvent(private val result: ScheduledItem.Event?) : ScheduleEventUseCase {
        var lastInput: ScheduleEventInput? = null

        override suspend operator fun invoke(input: ScheduleEventInput): ScheduledItem.Event? {
            lastInput = input
            return result
        }
    }

    private class FakeScheduleReminder : ScheduleReminderUseCase {
        var lastInput: ScheduleReminderInput? = null
        var lastRequestId: Int? = null

        override suspend operator fun invoke(
            input: ScheduleReminderInput,
            requestId: Int,
        ): ScheduledItem.Reminder {
            lastInput = input
            lastRequestId = requestId
            return ScheduledItem.Reminder(
                alarmRequestId = requestId,
                title = input.title,
                whenAt = input.whenAt,
            )
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

    private fun com.just.feature.capture.capture.CapturePreview.confirmed(scheduleEnabled: Boolean = false): CapturePreviewConfirmed =
        CapturePreviewConfirmed(
            title = title,
            body = body,
            type = type,
            tags = tags,
            datetime = datetime,
            scheduleEnabled = scheduleEnabled,
        )

    @Test
    fun onPrepare_uses_AI_result_when_classifier_returns_non_null() =
        runTest {
            val vm =
                CaptureViewModel(
                    FakeSaveNote(),
                    FakeClassify(classifyResult(NoteType.EVENT, "치과", "내일 3시")),
                    FakeScheduleEvent(null),
                    FakeScheduleReminder(),
                )
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
            val vm = CaptureViewModel(FakeSaveNote(), FakeClassify(null), FakeScheduleEvent(null), FakeScheduleReminder())
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
            val vm = CaptureViewModel(FakeSaveNote(), classify, FakeScheduleEvent(null), FakeScheduleReminder())
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
            val vm = CaptureViewModel(FakeSaveNote(), classify, FakeScheduleEvent(null), FakeScheduleReminder())
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
            val vm = CaptureViewModel(FakeSaveNote(), classify, FakeScheduleEvent(null), FakeScheduleReminder())
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
            val vm =
                CaptureViewModel(
                    save,
                    FakeClassify(classifyResult(NoteType.MEMO, "원본", "")),
                    FakeScheduleEvent(null),
                    FakeScheduleReminder(),
                )
            vm.onInputChanged("원본")
            vm.onPrepare()
            advanceUntilIdle()
            val prepared = vm.state.first().preview!!
            val edited = prepared.copy(title = "수정", body = "본문")
            vm.onConfirm(edited.confirmed())
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
            val vm = CaptureViewModel(throwingSave, FakeClassify(classifyResult()), FakeScheduleEvent(null), FakeScheduleReminder())
            vm.onInputChanged("test")
            vm.onPrepare()
            advanceUntilIdle()
            vm.onConfirm(vm.state.first().preview!!.confirmed())
            advanceUntilIdle()
            val state = vm.state.first()
            assertEquals(false, state.isSaving)
            assertEquals("disk full", state.error)
        }

    @Test
    fun onCancel_clears_preview() =
        runTest {
            val vm = CaptureViewModel(FakeSaveNote(), FakeClassify(classifyResult()), FakeScheduleEvent(null), FakeScheduleReminder())
            vm.onInputChanged("test")
            vm.onPrepare()
            advanceUntilIdle()
            vm.onCancel()
            assertNull(vm.state.first().preview)
        }

    @Test
    fun onConfirm_with_event_scheduleEnabled_calls_scheduleEvent() =
        runTest {
            val fakeEvent =
                FakeScheduleEvent(
                    ScheduledItem.Event(
                        calendarEventId = 42L,
                        title = "치과",
                        whenAt = Instant.EPOCH,
                    ),
                )
            val save = FakeSaveNote()
            val vm = CaptureViewModel(save, FakeClassify(classifyResult(NoteType.EVENT, "치과", "")), fakeEvent, FakeScheduleReminder())
            vm.onInputChanged("치과")
            vm.onPrepare()
            advanceUntilIdle()
            val at = Instant.parse("2026-06-01T10:00:00Z")
            vm.onConfirm(
                CapturePreviewConfirmed(
                    title = "치과",
                    body = "",
                    type = NoteType.EVENT,
                    tags = emptyList(),
                    datetime = at,
                    scheduleEnabled = true,
                ),
            )
            advanceUntilIdle()
            assertEquals("치과", fakeEvent.lastInput?.title)
            assertEquals(at, fakeEvent.lastInput?.whenAt)
            assertEquals(42L, save.saved?.calendarEventId)
        }

    @Test
    fun onConfirm_without_scheduleEnabled_skips_scheduleEvent() =
        runTest {
            val fakeEvent = FakeScheduleEvent(null)
            val save = FakeSaveNote()
            val vm = CaptureViewModel(save, FakeClassify(classifyResult(NoteType.EVENT, "치과", "")), fakeEvent, FakeScheduleReminder())
            vm.onInputChanged("치과")
            vm.onPrepare()
            advanceUntilIdle()
            val at = Instant.parse("2026-06-01T10:00:00Z")
            vm.onConfirm(
                CapturePreviewConfirmed(
                    title = "치과",
                    body = "",
                    type = NoteType.EVENT,
                    tags = emptyList(),
                    datetime = at,
                    scheduleEnabled = false,
                ),
            )
            advanceUntilIdle()
            assertNull(fakeEvent.lastInput)
            assertNull(save.saved?.calendarEventId)
        }
}
