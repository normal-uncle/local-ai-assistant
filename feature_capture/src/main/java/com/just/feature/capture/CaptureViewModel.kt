package com.just.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
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
    val isSaving: Boolean = false,
)

@HiltViewModel
class CaptureViewModel
    @Inject
    constructor(
        private val saveNote: SaveNoteUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(CaptureState())
        val state: StateFlow<CaptureState> get() = _state.asStateFlow()

        fun onInputChanged(text: String) {
            _state.update { it.copy(input = text) }
        }

        fun onPrepare() {
            val input = _state.value.input.trim()
            if (input.isEmpty()) return
            val firstNewline = input.indexOf('\n')
            val title = if (firstNewline < 0) input else input.substring(0, firstNewline).trim()
            val body = if (firstNewline < 0) "" else input.substring(firstNewline + 1).trim()
            _state.update {
                it.copy(preview = CapturePreview(title = title, body = body, type = NoteType.MEMO))
            }
        }

        fun onCancel() {
            _state.update { it.copy(preview = null) }
        }

        fun onConfirm() {
            val preview = _state.value.preview ?: return
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                val now = Instant.now()
                saveNote(
                    Note(
                        title = preview.title,
                        body = preview.body,
                        type = preview.type,
                        tags = preview.tags,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
                _state.update { CaptureState() }
            }
        }
    }
