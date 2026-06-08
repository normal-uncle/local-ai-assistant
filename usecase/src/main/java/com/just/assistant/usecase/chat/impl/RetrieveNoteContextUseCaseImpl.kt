package com.just.assistant.usecase.chat.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import com.just.assistant.usecase.chat.di.RetrieveNoteContextUseCase
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RetrieveNoteContextUseCaseImpl
    @Inject
    constructor(
        private val noteRepository: NoteRepository,
        private val clock: () -> Instant,
    ) : RetrieveNoteContextUseCase {
        override suspend fun invoke(query: String): String {
            val notes = noteRepository.observeAll().first()
            if (notes.isEmpty()) return ""
            val now = clock()

            val upcoming =
                notes
                    .filter { it.datetime?.isBefore(now) == false }
                    .sortedBy { it.datetime }
                    .take(MAX_UPCOMING)

            val tokens = query.lowercase().split(Regex("\\s+")).filter { it.length >= 2 }
            val keyword =
                notes
                    .filter { it !in upcoming }
                    .map { note ->
                        val hay = (note.title + " " + note.body).lowercase()
                        note to tokens.count { hay.contains(it) }
                    }
                    .filter { it.second > 0 }
                    .sortedByDescending { it.second }
                    .take(MAX_KEYWORD)
                    .map { it.first }

            val selected = upcoming + keyword
            if (selected.isEmpty()) return ""

            val lines = selected.joinToString("\n") { formatLine(it) }
            return if (lines.length <= MAX_CHARS) {
                lines
            } else {
                lines.substring(0, MAX_CHARS).substringBeforeLast("\n")
            }
        }

        private fun formatLine(note: Note): String {
            val label =
                when (note.type) {
                    NoteType.MEMO -> "메모"
                    NoteType.EVENT -> "일정"
                    NoteType.REMINDER -> "리마인더"
                }
            val whenStr =
                note.datetime?.let {
                    " (" + FORMATTER.format(it.atZone(ZoneId.systemDefault())) + ")"
                } ?: ""
            val body =
                note.body.replace(Regex("\\s+"), " ").trim().take(BODY_MAX).let {
                    if (it.isBlank()) "" else " : $it"
                }
            return "- [$label] ${note.title}$whenStr$body"
        }

        companion object {
            const val MAX_UPCOMING = 5
            const val MAX_KEYWORD = 5
            const val MAX_CHARS = 1000
            const val BODY_MAX = 60
            private val FORMATTER: DateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        }
    }
