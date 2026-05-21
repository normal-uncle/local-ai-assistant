package com.just.feature.capture

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import org.junit.Rule
import org.junit.Test

class CaptureSceneTest {
    @get:Rule val compose = createComposeRule()

    private class FakeSaveNote : SaveNoteUseCase {
        override suspend fun invoke(note: Note): Long = 1L
    }

    @Test
    fun input_then_prepare_shows_preview_with_title_and_body() {
        compose.setContent {
            CaptureScene(
                onOpenMemoList = {},
                viewModel = CaptureViewModel(FakeSaveNote()),
            )
        }

        compose.onNodeWithTag("capture_input").performTextInput("장보기\n우유")
        compose.onNodeWithTag("capture_prepare").assertIsEnabled().performClick()

        compose.onNodeWithText("장보기").assertIsDisplayed()
        compose.onNodeWithText("우유").assertIsDisplayed()
        compose.onNodeWithText("저장").assertIsDisplayed()
    }
}
