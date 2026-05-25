package com.just.assistant.usecase.capture.impl

import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.ai.prompt.ClassificationPrompt
import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.usecase.capture.di.ClassifyCaptureUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClassifyCaptureUseCaseImpl
    @Inject
    constructor(
        private val engine: InferenceEngine,
        private val fileStore: ModelFileStore,
        private val prefs: ModelStatusPrefs,
    ) : ClassifyCaptureUseCase {
        override suspend fun invoke(userInput: String): ClassificationResult? {
            val variantId = prefs.selectedVariantId.first() ?: return null
            val modelFile = fileStore.fileFor(variantId).takeIf { it.exists() } ?: return null

            return try {
                if (!engine.isReady()) {
                    engine.load(modelFile, InferenceConfig())
                }
                val response = engine.generate(ClassificationPrompt.build(userInput))
                ClassificationResult.parse(response)
            } catch (t: Throwable) {
                null
            }
        }
    }
