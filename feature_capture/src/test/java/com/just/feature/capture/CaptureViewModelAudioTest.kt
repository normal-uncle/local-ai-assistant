package com.just.feature.capture

import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.local.audio.AudioRecorder
import com.just.assistant.local.image.ImageStore
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.repository.model.ScheduledItem
import com.just.assistant.usecase.capture.di.ClassifyAudioCaptureUseCase
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.capture.di.ClassifyImageCaptureUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.assistant.usecase.schedule.di.ScheduleEventUseCase
import com.just.assistant.usecase.schedule.di.ScheduleReminderUseCase
import com.just.feature.capture.capture.CaptureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CaptureViewModelAudioTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeSaveNote : SaveNoteUseCase {
        override suspend fun invoke(note: Note): Long = 1L
    }

    private class FakeClassify : ClassifyCaptureUseCase {
        override suspend fun invoke(userInput: String): ClassificationResult? = null
    }

    private class FakeClassifyImage : ClassifyImageCaptureUseCase {
        override suspend fun invoke(imageBytes: ByteArray, caption: String?): ClassificationResult? = null
    }

    private class FakeImageStore : ImageStore {
        override suspend fun persist(source: android.net.Uri): android.net.Uri = source

        override suspend fun toClassifierBytes(source: android.net.Uri): ByteArray = byteArrayOf(1)
    }

    private class FakeScheduleEvent : ScheduleEventUseCase {
        override suspend fun invoke(input: ScheduleEventInput): ScheduledItem.Event? = null
    }

    private class FakeScheduleReminder : ScheduleReminderUseCase {
        override suspend fun invoke(
            input: ScheduleReminderInput,
            requestId: Int,
            noteId: Long,
        ): ScheduledItem.Reminder = ScheduledItem.Reminder(requestId, input.title, input.whenAt)
    }

    private class FakeClassifyAudio(private val result: ClassificationResult? = null) : ClassifyAudioCaptureUseCase {
        var lastAudio: ByteArray? = null
        var lastCaption: String? = null

        override suspend fun invoke(audioBytes: ByteArray, caption: String?): ClassificationResult? {
            lastAudio = audioBytes
            lastCaption = caption
            return result
        }
    }

    private class FakeRecorder(
        var startResult: Boolean = true,
        var wav: ByteArray? = byteArrayOf(9, 9, 9),
    ) : AudioRecorder {
        var startCount = 0
        var stopCount = 0
        private var recording = false
        override val isRecording: Boolean get() = recording

        override fun start(): Boolean {
            startCount++
            if (startResult) recording = true
            return startResult
        }

        override fun stop(): ByteArray? {
            stopCount++
            recording = false
            return wav
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm(
        classifyAudio: FakeClassifyAudio = FakeClassifyAudio(),
        recorder: FakeRecorder = FakeRecorder(),
    ) = CaptureViewModel(
        FakeSaveNote(),
        FakeClassify(),
        FakeScheduleEvent(),
        FakeScheduleReminder(),
        FakeClassifyImage(),
        FakeImageStore(),
        classifyAudio,
        recorder,
    )

    @Test
    fun toggle_starts_recording() =
        runTest {
            val recorder = FakeRecorder()
            val vm = makeVm(recorder = recorder)
            vm.onToggleRecording("메모")
            assertEquals(true, vm.state.first().isRecording)
            assertEquals(1, recorder.startCount)
        }

    @Test
    fun toggle_start_failure_keeps_not_recording() =
        runTest {
            val recorder = FakeRecorder(startResult = false)
            val vm = makeVm(recorder = recorder)
            vm.onToggleRecording("메모")
            assertEquals(false, vm.state.first().isRecording)
        }

    @Test
    fun second_toggle_stops_and_classifies_with_ai_result() =
        runTest {
            val classifyAudio =
                FakeClassifyAudio(
                    ClassificationResult(NoteType.REMINDER, "우유 사기", "마트에서", emptyList(), null),
                )
            val recorder = FakeRecorder(wav = byteArrayOf(7, 7))
            val vm = makeVm(classifyAudio, recorder)
            vm.onToggleRecording("메모")
            vm.onToggleRecording("메모")
            advanceUntilIdle()
            val s = vm.state.first()
            assertEquals(false, s.isRecording)
            assertEquals(1, recorder.stopCount)
            assertArrayEquals(byteArrayOf(7, 7), classifyAudio.lastAudio)
            assertEquals("우유 사기", s.preview!!.title)
            assertEquals(NoteType.REMINDER, s.preview!!.type)
        }

    @Test
    fun passes_input_text_as_caption() =
        runTest {
            val classifyAudio = FakeClassifyAudio()
            val vm = makeVm(classifyAudio)
            vm.onInputChanged("장보기 관련")
            vm.onToggleRecording("메모")
            vm.onToggleRecording("메모")
            advanceUntilIdle()
            assertEquals("장보기 관련", classifyAudio.lastCaption)
        }

    @Test
    fun falls_back_to_memo_preview_when_classify_returns_null() =
        runTest {
            val vm = makeVm(FakeClassifyAudio(result = null))
            vm.onToggleRecording("음성 메모")
            vm.onToggleRecording("음성 메모")
            advanceUntilIdle()
            val preview = vm.state.first().preview!!
            assertEquals("음성 메모", preview.title)
            assertEquals(NoteType.MEMO, preview.type)
        }

    @Test
    fun auto_stops_and_classifies_at_max_duration() =
        runTest {
            val classifyAudio = FakeClassifyAudio()
            val recorder = FakeRecorder()
            val vm = makeVm(classifyAudio, recorder)
            vm.onToggleRecording("메모")
            advanceTimeBy((AudioRecorder.MAX_DURATION_SECONDS + 1) * 1000L)
            advanceUntilIdle()
            assertEquals(false, vm.state.first().isRecording)
            assertEquals(1, recorder.stopCount)
        }

    @Test
    fun elapsed_seconds_tick_while_recording() =
        runTest {
            val vm = makeVm()
            vm.onToggleRecording("메모")
            advanceTimeBy(3_500)
            assertEquals(3, vm.state.first().recordingSeconds)
        }

    @Test
    fun stop_with_null_wav_resets_state_without_preview() =
        runTest {
            val vm = makeVm(recorder = FakeRecorder(wav = null))
            vm.onToggleRecording("메모")
            vm.onToggleRecording("메모")
            advanceUntilIdle()
            val s = vm.state.first()
            assertEquals(false, s.isRecording)
            assertEquals(false, s.isPreparing)
            assertNull(s.preview)
        }
}
