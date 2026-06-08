package com.just.assistant.usecase.chat.impl

import com.just.assistant.ai.inference.ChatSession
import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.usecase.chat.di.StartChatSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StartChatSessionUseCaseImpl
    @Inject
    constructor(
        private val engine: InferenceEngine,
        private val fileStore: ModelFileStore,
        private val prefs: ModelStatusPrefs,
    ) : StartChatSessionUseCase {
        override suspend fun invoke(): ChatSession? {
            val variantId = prefs.selectedVariantId.first() ?: return null
            val modelFile = fileStore.fileFor(variantId).takeIf { it.exists() } ?: return null
            return try {
                if (!engine.isReady()) engine.load(modelFile, InferenceConfig())
                // startChat은 non-suspend라 createConversation이 호출 스레드에서 돌 수 있으므로 IO로 오프로드.
                withContext(Dispatchers.IO) { engine.startChat() }
            } catch (t: Throwable) {
                null
            }
        }
    }
