package com.just.assistant.usecase.capture.di

import com.just.assistant.ai.prompt.ClassificationResult

interface ClassifyCaptureUseCase {
    /**
     * 사용자 입력을 AI로 분류해 [ClassificationResult]로 반환.
     * 모델이 미준비거나 추론 실패 시 null 반환 — 호출 측은 rule-based 폴백 사용.
     */
    suspend operator fun invoke(userInput: String): ClassificationResult?
}
