package com.just.assistant.usecase.capture.di

import com.just.assistant.ai.prompt.ClassificationResult

interface ClassifyAudioCaptureUseCase {
    /**
     * 녹음된 음성(WAV 바이트, +선택 캡션)을 AI로 분류해 [ClassificationResult] 반환.
     * 모델 미준비/추론 실패 시 null — 호출 측은 MEMO 폴백.
     */
    suspend operator fun invoke(audioBytes: ByteArray, caption: String?): ClassificationResult?
}
