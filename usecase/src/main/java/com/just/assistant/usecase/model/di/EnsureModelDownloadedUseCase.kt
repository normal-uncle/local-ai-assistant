package com.just.assistant.usecase.model.di

interface EnsureModelDownloadedUseCase {
    /**
     * 모델이 READY가 아니면 카탈로그 fetch + 추천 variant 선택 + 다운로드를 트리거.
     * 이미 READY면 no-op.
     */
    suspend operator fun invoke(catalogUrl: String)
}
