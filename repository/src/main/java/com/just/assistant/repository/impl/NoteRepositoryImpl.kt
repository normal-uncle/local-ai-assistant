package com.just.assistant.repository.impl

import com.just.assistant.local.note.NoteDao
import com.just.assistant.local.note.NoteEntity
import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.repository.model.Note
import com.just.assistant.repository.model.NoteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl
    @Inject
    constructor(
        private val dao: NoteDao,
        private val clock: () -> Instant,
    ) : NoteRepository {
        override fun observeAll(): Flow<List<Note>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

        override suspend fun findById(id: Long): Note? = dao.findById(id)?.toDomain()

        override suspend fun save(note: Note): Long {
            val now = clock()
            return if (note.id == 0L) {
                dao.insert(
                    note.toEntity().copy(
                        createdAtEpochMs = now.toEpochMilli(),
                        updatedAtEpochMs = now.toEpochMilli(),
                    ),
                )
            } else {
                dao.update(note.toEntity().copy(updatedAtEpochMs = now.toEpochMilli()))
                note.id
            }
        }

        override suspend fun delete(id: Long): Boolean = dao.deleteById(id) > 0

        private fun NoteEntity.toDomain(): Note =
            Note(
                id = id,
                title = title,
                body = body,
                type = runCatching { NoteType.valueOf(type) }.getOrDefault(NoteType.MEMO),
                tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
                datetime = datetimeIso?.let(Instant::parse),
                createdAt = Instant.ofEpochMilli(createdAtEpochMs),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMs),
                calendarEventId = calendarEventId,
                alarmRequestId = alarmRequestId,
                isCompleted = isCompleted,
            )

        private fun Note.toEntity(): NoteEntity =
            NoteEntity(
                id = id,
                title = title,
                body = body,
                type = type.name,
                tags = tags.joinToString(","),
                datetimeIso = datetime?.toString(),
                createdAtEpochMs = createdAt.toEpochMilli(),
                updatedAtEpochMs = updatedAt.toEpochMilli(),
                calendarEventId = calendarEventId,
                alarmRequestId = alarmRequestId,
                isCompleted = isCompleted,
            )
    }
