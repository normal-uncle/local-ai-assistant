package com.just.feature.capture.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class CapturePreview(
    val title: String,
    val body: String,
    val type: NoteType,
    val tags: List<String> = emptyList(),
)

data class CaptureState(
    val input: String = "",
    val preview: CapturePreview? = null,
    val isPreparing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CaptureViewModel
    @Inject
    constructor(
        private val saveNote: SaveNoteUseCase,
        private val classify: ClassifyCaptureUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(CaptureState())
        val state: StateFlow<CaptureState> get() = _state.asStateFlow()

        fun onInputChanged(text: String) {
            _state.update { it.copy(input = text) }
        }

        fun onPrepare() {
            val input = _state.value.input.trim()
            if (input.isEmpty()) return
            if (_state.value.isPreparing) return

            _state.update { it.copy(isPreparing = true) }
            viewModelScope.launch {
                val aiResult = classify(input)
                val preview =
                    if (aiResult != null) {
                        CapturePreview(
                            title = aiResult.title,
                            body = aiResult.body,
                            type = aiResult.type,
                            tags = aiResult.tags,
                        )
                    } else {
                        fallbackPreview(input)
                    }
                _state.update { it.copy(preview = preview, isPreparing = false) }
            }
        }

        fun onCancel() {
            _state.update { it.copy(preview = null) }
        }

        fun onConfirm(edited: CapturePreview) {
            if (_state.value.preview == null) return
            if (_state.value.isSaving) return
            _state.update { it.copy(preview = edited, isSaving = true) }
            viewModelScope.launch {
                val now = Instant.now()
                try {
                    saveNote(
                        Note(
                            title = edited.title,
                            body = edited.body,
                            type = edited.type,
                            tags = edited.tags,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )
                    _state.update { CaptureState() }
                } catch (t: Throwable) {
                    _state.update { it.copy(isSaving = false, error = t.message ?: "저장 실패") }
                }
            }
        }

        private fun fallbackPreview(input: String): CapturePreview {
            val firstNewline = input.indexOf('\n')
            val title = if (firstNewline < 0) input else input.substring(0, firstNewline).trim()
            val body = if (firstNewline < 0) "" else input.substring(firstNewline + 1).trim()
            return CapturePreview(title = title, body = body, type = NoteType.MEMO)
        }
    }
