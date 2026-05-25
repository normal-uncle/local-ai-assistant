package com.just.assistant.ai.inference

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaPipe Tasks GenAI 기반 [InferenceEngine] 구현.
 *
 * 0.10.24 SDK 노트:
 * - 엔진 수준 옵션([LlmInferenceOptions])은 `setModelPath`, `setMaxTokens`, `setMaxTopK`만 제공.
 *   `topK`/`topP`/`temperature` 같은 샘플링 파라미터는 [LlmInferenceSessionOptions]로 이동.
 * - 따라서 [generate] 호출마다 세션을 만들어 [InferenceConfig]의 샘플링 값을 적용한다.
 * - 엔진 빌드 시 `setMaxTopK(config.topK)`로 세션 topK 상한을 맞춘다.
 */
@Singleton
class MediaPipeInferenceEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : InferenceEngine {
        @Volatile
        private var inference: LlmInference? = null

        @Volatile
        private var currentConfig: InferenceConfig? = null

        override suspend fun load(
            modelFile: File,
            config: InferenceConfig,
        ) {
            withContext(Dispatchers.IO) {
                if (inference != null && currentConfig == config) return@withContext
                inference?.close()
                inference = null

                val options =
                    LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(config.maxTokens)
                        .setMaxTopK(config.topK)
                        .build()
                inference = LlmInference.createFromOptions(context, options)
                currentConfig = config
            }
        }

        override fun unload() {
            inference?.close()
            inference = null
            currentConfig = null
        }

        override fun isReady(): Boolean = inference != null

        override suspend fun generate(prompt: String): String =
            withContext(Dispatchers.IO) {
                val engine = inference ?: error("InferenceEngine not loaded; call load() first")
                val config = currentConfig ?: error("InferenceEngine not loaded; call load() first")

                val sessionOptions =
                    LlmInferenceSessionOptions.builder()
                        .setTopK(config.topK)
                        .setTopP(config.topP)
                        .setTemperature(config.temperature)
                        .build()
                LlmInferenceSession.createFromOptions(engine, sessionOptions).use { session ->
                    session.addQueryChunk(prompt)
                    session.generateResponse()
                }
            }
    }
