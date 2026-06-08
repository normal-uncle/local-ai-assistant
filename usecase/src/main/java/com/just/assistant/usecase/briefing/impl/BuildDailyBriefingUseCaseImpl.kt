package com.just.assistant.usecase.briefing.impl

import com.just.assistant.repository.di.NoteRepository
import com.just.assistant.usecase.briefing.di.BuildDailyBriefingUseCase
import com.just.assistant.usecase.briefing.di.DailyBriefing
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BuildDailyBriefingUseCaseImpl
    @Inject
    constructor(
        private val noteRepository: NoteRepository,
        private val clock: () -> Instant,
    ) : BuildDailyBriefingUseCase {
        override suspend fun invoke(): DailyBriefing? {
            val zone = ZoneId.systemDefault()
            val today = clock().atZone(zone).toLocalDate()
            val todays =
                noteRepository.observeAll().first()
                    .filter { it.datetime != null }
                    .filter { it.datetime!!.atZone(zone).toLocalDate() == today }
                    .sortedBy { it.datetime }
            if (todays.isEmpty()) return null
            val fmt = DateTimeFormatter.ofPattern("HH:mm")
            val shown = todays.take(MAX_ITEMS)
            val items = shown.map { "${fmt.format(it.datetime!!.atZone(zone))} ${it.title}" }
            return DailyBriefing(
                totalCount = todays.size,
                items = items,
                moreCount = todays.size - shown.size,
            )
        }

        companion object {
            const val MAX_ITEMS = 10
        }
    }
