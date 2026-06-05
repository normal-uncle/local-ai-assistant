package com.just.assistant.usecase.capture.di

import com.just.assistant.ai.prompt.ClassificationResult

interface ClassifyImageCaptureUseCase {
    /**
     * 이미지(+선택 캡션)를 AI로 분류해 [ClassificationResult] 반환.
     * 모델 미준비/추론 실패 시 null — 호출 측은 MEMO 폴백.
     */
    suspend operator fun invoke(imageBytes: ByteArray, caption: String?): ClassificationResult?
}
