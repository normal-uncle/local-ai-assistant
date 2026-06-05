package com.just.assistant.usecase.capture.impl

import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.ai.prompt.ImageClassificationPrompt
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.usecase.capture.di.ClassifyImageCaptureUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClassifyImageCaptureUseCaseImpl
    @Inject
    constructor(
        private val engine: InferenceEngine,
        private val fileStore: ModelFileStore,
        private val prefs: ModelStatusPrefs,
    ) : ClassifyImageCaptureUseCase {
        override suspend fun invoke(imageBytes: ByteArray, caption: String?): ClassificationResult? {
            val variantId = prefs.selectedVariantId.first() ?: return null
            val modelFile = fileStore.fileFor(variantId).takeIf { it.exists() } ?: return null
            return try {
                // 반드시 enableVision=true 로 로드. vision 백엔드/이미지 슬롯 미초기화 시 네이티브 무한 정지(spike 확인).
                // load()는 (config, modelPath) 동일 시 내부 dedup 하므로 매번 호출해도 저렴.
                // isReady()로만 가드하면 텍스트 분류가 vision-off로 먼저 로드한 엔진을 재사용해 정지하므로 가드하지 않는다.
                engine.load(modelFile, InferenceConfig(enableVision = true))
                val response =
                    engine.generate(
                        prompt = ImageClassificationPrompt.build(caption),
                        images = listOf(imageBytes),
                    )
                ClassificationResult.parse(response)
            } catch (t: Throwable) {
                null
            }
        }
    }
