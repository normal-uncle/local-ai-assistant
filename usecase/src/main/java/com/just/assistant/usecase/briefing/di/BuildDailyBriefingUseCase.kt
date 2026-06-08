package com.just.assistant.usecase.briefing.di

interface BuildDailyBriefingUseCase {
    /** 오늘(로컬 자정~다음 자정) 일정·리마인더. 없으면 null. */
    suspend operator fun invoke(): DailyBriefing?
}

/** items 예: ["09:00 회의", "15:00 치과"]. moreCount = MAX_ITEMS 초과분. */
data class DailyBriefing(
    val totalCount: Int,
    val items: List<String>,
    val moreCount: Int,
)
