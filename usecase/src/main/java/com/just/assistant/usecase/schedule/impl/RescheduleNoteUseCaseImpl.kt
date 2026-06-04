package com.just.assistant.usecase.schedule.impl

import com.just.assistant.local.alarm.AlarmScheduler
import com.just.assistant.local.calendar.CalendarEventDraft
import com.just.assistant.local.calendar.CalendarWriter
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.usecase.schedule.di.RescheduleNoteUseCase
import java.time.Instant
import javax.inject.Inject

class RescheduleNoteUseCaseImpl
    @Inject
    constructor(
        private val noteRepository: NoteRepository,
        private val calendarWriter: CalendarWriter,
        private val alarmScheduler: AlarmScheduler,
    ) : RescheduleNoteUseCase {
        override suspend fun invoke(
            noteId: Long,
            newDateTime: Instant,
        ) {
            val note = noteRepository.findById(noteId) ?: return
            val start = newDateTime.toEpochMilli()
            note.calendarEventId?.let { eventId ->
                calendarWriter.updateEvent(
                    eventId,
                    CalendarEventDraft(
                        title = note.title,
                        description = note.body,
                        startEpochMs = start,
                        endEpochMs = start + 60 * 60 * 1000L,
                    ),
                )
            }
            note.alarmRequestId?.let { requestId ->
                alarmScheduler.schedule(
                    requestId = requestId,
                    whenEpochMs = start,
                    title = note.title,
                    body = note.body,
                    noteId = noteId,
                )
            }
            noteRepository.save(
                note.copy(datetime = newDateTime, updatedAt = Instant.now()),
            )
        }
    }
