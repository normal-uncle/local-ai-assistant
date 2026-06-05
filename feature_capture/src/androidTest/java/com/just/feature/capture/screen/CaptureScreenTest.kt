package com.just.feature.capture.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.feature.capture.capture.CaptureScreen
import com.just.feature.capture.capture.CaptureViewModel
import org.junit.Rule
import org.junit.Test

class CaptureScreenTest {
    @get:Rule val compose = createComposeRule()

    private class FakeSaveNote : SaveNoteUseCase {
        override suspend fun invoke(note: Note): Long = 1L
    }

    private class FakeClassify : ClassifyCaptureUseCase {
        override suspend operator fun invoke(userInput: String): ClassificationResult? = null
    }

    private class FakeScheduleEvent : com.just.assistant.usecase.schedule.di.ScheduleEventUseCase {
        override suspend operator fun invoke(input: com.just.assistant.repository.model.ScheduleEventInput) = null
    }
    private class FakeScheduleReminder : com.just.assistant.usecase.schedule.di.ScheduleReminderUseCase {
        override suspend operator fun invoke(
            input: com.just.assistant.repository.model.ScheduleReminderInput,
            requestId: Int,
            noteId: Long,
        ) = com.just.assistant.repository.model.ScheduledItem.Reminder(
            alarmRequestId = requestId, title = input.title, whenAt = input.whenAt,
        )
    }
    private class FakeClassifyImage : com.just.assistant.usecase.capture.di.ClassifyImageCaptureUseCase {
        override suspend operator fun invoke(imageBytes: ByteArray, caption: String?) = null
    }
    private class FakeImageStore : com.just.assistant.local.image.ImageStore {
        override suspend fun persist(source: android.net.Uri): android.net.Uri = source
        override suspend fun toClassifierBytes(source: android.net.Uri): ByteArray = byteArrayOf(1)
    }

    @Test
    fun input_then_prepare_shows_preview_with_title_and_body() {
        compose.setContent {
            CaptureScreen(
                onOpenMemo = {},
                viewModel = CaptureViewModel(
                    FakeSaveNote(), FakeClassify(), FakeScheduleEvent(), FakeScheduleReminder(),
                    FakeClassifyImage(), FakeImageStore(),
                ),
            )
        }

        compose.onNodeWithTag("capture_input").performTextInput("장보기\n우유")
        compose.onNodeWithTag("capture_prepare").assertIsEnabled().performClick()

        compose.onNodeWithTag("capture_save").assertIsDisplayed()
    }
}
