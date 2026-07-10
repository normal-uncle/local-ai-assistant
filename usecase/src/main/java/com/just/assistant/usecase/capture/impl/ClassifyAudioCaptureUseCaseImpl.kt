package com.just.assistant.usecase.capture.impl

import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.ai.prompt.AudioClassificationPrompt
import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.usecase.capture.di.ClassifyAudioCaptureUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClassifyAudioCaptureUseCaseImpl
    @Inject
    constructor(
        private val engine: InferenceEngine,
        private val fileStore: ModelFileStore,
        private val prefs: ModelStatusPrefs,
    ) : ClassifyAudioCaptureUseCase {
        override suspend fun invoke(audioBytes: ByteArray, caption: String?): ClassificationResult? {
            val variantId = prefs.selectedVariantId.first() ?: return null
            val modelFile = fileStore.fileFor(variantId).takeIf { it.exists() } ?: return null
            return try {
                // 반드시 enableAudio=true 로 로드 (vision과 동일하게 백엔드 미초기화 시 오동작).
                // litertlm-android 0.14.0 필요 — 0.12.0은 오디오 임베딩을 조용히 무시(spike 확인).
                // preferGpu=false 강제: GPU 메인 백엔드는 init은 성공하나(load()의 CPU 폴백 미발동)
                // 오디오 추론 시점에 "Can not find OpenCL library" 예외 (SD8G1 spike 확인).
                engine.load(modelFile, InferenceConfig(enableAudio = true, preferGpu = false))
                val response =
                    engine.generate(
                        prompt = AudioClassificationPrompt.build(caption),
                        images = emptyList(),
                        audios = listOf(audioBytes),
                    )
                ClassificationResult.parse(response)
            } catch (t: Throwable) {
                null
            }
        }
    }
