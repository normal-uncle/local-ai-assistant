package com.just.assistant.ai.inference

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LiteRT-LM SDK 기반 [InferenceEngine] 구현.
 *
 * 0.12.0 SDK 노트:
 * - `.litertlm` 파일만 로드 가능. MediaPipe `.task` 포맷과 호환되지 않는다.
 * - [Engine] 은 [EngineConfig] 로 생성하고 명시적으로 [Engine.initialize] 호출이 필요.
 * - 샘플링 파라미터([SamplerConfig]: topK / topP / temperature / seed) 는 대화 단위로
 *   [ConversationConfig] 에 실어 [Engine.createConversation] 으로 전달한다.
 * - [Engine.createConversation] 으로 만든 [com.google.ai.edge.litertlm.Conversation]
 *   에 `sendMessage(String)` 호출 → [com.google.ai.edge.litertlm.Message] 반환.
 *   응답 본문은 `message.contents.contents` 내의 [Content.Text] 들을 이어붙여 추출한다.
 * - [Engine] 과 [com.google.ai.edge.litertlm.Conversation] 모두 [AutoCloseable].
 */
@Singleton
class LiteRtLmInferenceEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : InferenceEngine {
        @Volatile
        private var engine: Engine? = null

        @Volatile
        private var currentConfig: InferenceConfig? = null

        @Volatile
        private var currentModelPath: String? = null

        @Volatile
        private var _lastBackend: String? = null

        override val lastBackend: String? get() = _lastBackend

        override suspend fun load(
            modelFile: File,
            config: InferenceConfig,
        ) {
            withContext(Dispatchers.IO) {
                if (engine != null &&
                    currentConfig == config &&
                    currentModelPath == modelFile.absolutePath
                ) {
                    return@withContext
                }
                engine?.close()
                engine = null
                _lastBackend = null

                val textConfig =
                    EngineConfig(
                        modelPath = modelFile.absolutePath,
                        backend = Backend.CPU(),
                        maxNumTokens = config.maxTokens,
                        cacheDir = context.cacheDir.absolutePath,
                    )
                // vision/audio 백엔드 미초기화 상태로 해당 모달리티 입력 시 네이티브 무한 정지(spike 확인).
                var baseConfig = textConfig
                if (config.enableVision) {
                    baseConfig = baseConfig.copy(visionBackend = Backend.CPU(), maxNumImages = 1)
                }
                if (config.enableAudio) {
                    baseConfig = baseConfig.copy(audioBackend = Backend.CPU())
                }

                val (newEngine, backend) =
                    if (!config.preferGpu) {
                        val cpu = Engine(baseConfig.copy(backend = Backend.CPU()))
                        cpu.initialize()
                        cpu to "CPU"
                    } else {
                        try {
                            val gpu = Engine(baseConfig.copy(backend = Backend.GPU()))
                            gpu.initialize()
                            gpu to "GPU"
                        } catch (e: Throwable) {
                            Log.w(TAG, "GPU init failed, falling back to CPU", e)
                            val cpu = Engine(baseConfig.copy(backend = Backend.CPU()))
                            cpu.initialize()
                            cpu to "CPU(fallback)"
                        }
                    }

                engine = newEngine
                currentConfig = config
                currentModelPath = modelFile.absolutePath
                _lastBackend = backend
            }
        }

        override fun unload() {
            engine?.close()
            engine = null
            currentConfig = null
            currentModelPath = null
            _lastBackend = null
        }

        override fun isReady(): Boolean = engine != null

        override fun startChat(): ChatSession {
            val e = engine ?: error("InferenceEngine not loaded; call load() first")
            val cfg = currentConfig ?: error("InferenceEngine not loaded; call load() first")
            val samplerConfig =
                SamplerConfig(
                    topK = cfg.topK,
                    topP = cfg.topP.toDouble(),
                    temperature = cfg.temperature.toDouble(),
                    seed = 0,
                )
            val conversation =
                e.createConversation(
                    ConversationConfig(
                        systemInstruction = null,
                        initialMessages = emptyList(),
                        tools = emptyList(),
                        samplerConfig = samplerConfig,
                    ),
                )
            return LiteRtLmChatSession(conversation)
        }

        private class LiteRtLmChatSession(
            private val conversation: Conversation,
        ) : ChatSession {
            override fun send(message: String): Flow<String> =
                conversation
                    .sendMessageAsync(message)
                    .map { msg ->
                        msg.contents.contents
                            .filterIsInstance<Content.Text>()
                            .joinToString(separator = "") { it.text }
                    }
                    .flowOn(Dispatchers.IO)

            override fun close() = conversation.close()
        }

        override suspend fun generate(
            prompt: String,
            images: List<ByteArray>,
        ): String = generate(prompt, images, emptyList())

        override suspend fun generate(
            prompt: String,
            images: List<ByteArray>,
            audios: List<ByteArray>,
        ): String =
            withContext(Dispatchers.IO) {
                val e = engine ?: error("InferenceEngine not loaded; call load() first")
                val cfg = currentConfig ?: error("InferenceEngine not loaded; call load() first")

                val samplerConfig =
                    SamplerConfig(
                        topK = cfg.topK,
                        topP = cfg.topP.toDouble(),
                        temperature = cfg.temperature.toDouble(),
                        seed = 0,
                    )
                val conversationConfig =
                    ConversationConfig(
                        systemInstruction = null,
                        initialMessages = emptyList(),
                        tools = emptyList(),
                        samplerConfig = samplerConfig,
                    )

                e.createConversation(conversationConfig).use { conversation ->
                    val response =
                        if (images.isEmpty() && audios.isEmpty()) {
                            conversation.sendMessage(prompt)
                        } else {
                            val contents =
                                Contents.of(
                                    audios.map { Content.AudioBytes(it) } +
                                        images.map { Content.ImageBytes(it) } +
                                        Content.Text(prompt),
                                )
                            conversation.sendMessage(contents)
                        }
                    response.contents.contents
                        .filterIsInstance<Content.Text>()
                        .joinToString(separator = "") { it.text }
                }
            }

        companion object {
            private const val TAG = "LiteRtLmInferenceEngine"
        }
    }
