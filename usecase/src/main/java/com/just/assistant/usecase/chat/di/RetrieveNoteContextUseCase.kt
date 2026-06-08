package com.just.assistant.usecase.chat.di

interface RetrieveNoteContextUseCase {
    /** [query]와 관련된 저장 노트를 짧은 텍스트 블록으로. 관련 없으면 "". */
    suspend operator fun invoke(query: String): String
}
