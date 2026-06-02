package com.just.feature.capture.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.repository.model.ScheduleEventInput
import com.just.assistant.repository.model.ScheduleReminderInput
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import com.just.assistant.usecase.note.di.SaveNoteUseCase
import com.just.assistant.usecase.schedule.di.ScheduleEventUseCase
import com.just.assistant.usecase.schedule.di.ScheduleReminderUseCase
import com.just.feature.capture.CapturePreviewConfirmed
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
    val datetime: Instant? = null,
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
        private val scheduleEvent: ScheduleEventUseCase,
        private val scheduleReminder: ScheduleReminderUseCase,
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
                            datetime = aiResult.datetimeIso?.let { runCatching { Instant.parse(it) }.getOrNull() },
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

        fun onConfirm(confirmed: CapturePreviewConfirmed) {
            if (_state.value.preview == null) return
            if (_state.value.isSaving) return
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                val now = Instant.now()
                try {
                    val initialNote =
                        Note(
                            title = confirmed.title,
                            body = confirmed.body,
                            type = confirmed.type,
                            tags = confirmed.tags,
                            datetime = confirmed.datetime,
                            createdAt = now,
                            updatedAt = now,
                            calendarEventId = null,
                            alarmRequestId = null,
                        )
                    val newId = saveNote(initialNote)

                    var calendarEventId: Long? = null
                    var alarmRequestId: Int? = null

                    if (confirmed.scheduleEnabled && confirmed.datetime != null) {
                        when (confirmed.type) {
                            NoteType.EVENT -> {
                                val event =
                                    scheduleEvent(
                                        ScheduleEventInput(
                                            title = confirmed.title,
                                            body = confirmed.body,
                                            whenAt = confirmed.datetime,
                                        ),
                                    )
                                calendarEventId = event?.calendarEventId
                            }
                            NoteType.REMINDER -> {
                                val requestId = (System.currentTimeMillis() and 0x7FFFFFFFL).toInt()
                                val reminder =
                                    scheduleReminder(
                                        ScheduleReminderInput(
                                            title = confirmed.title,
                                            body = confirmed.body,
                                            whenAt = confirmed.datetime,
                                        ),
                                        requestId = requestId,
                                        noteId = newId,
                                    )
                                alarmRequestId = reminder.alarmRequestId
                            }
                            NoteType.MEMO -> Unit
                        }
                    }

                    if (calendarEventId != null || alarmRequestId != null) {
                        saveNote(
                            initialNote.copy(
                                id = newId,
                                calendarEventId = calendarEventId,
                                alarmRequestId = alarmRequestId,
                            ),
                        )
                    }
                    _state.update { CaptureState() }
                } catch (e: Exception) {
                    _state.update { it.copy(isSaving = false, error = e.message ?: "저장 실패") }
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
